package com.example.cogchords.util;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.example.cogchords.data.AppDatabase;
import com.example.cogchords.data.entity.Song;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

/**
 * Exports a song (lyrics + chords) to a printable A4 PDF and opens the share sheet.
 * Uses Android's built-in PdfDocument, so no extra libraries are needed.
 * The PDF is always black-on-white regardless of the app theme so it prints well.
 */
public class SongPdfExporter {

    // A4 at 72 dpi
    private static final int PAGE_WIDTH = 595;
    private static final int PAGE_HEIGHT = 842;
    private static final int MARGIN_X = 40;
    private static final int MARGIN_TOP = 50;
    private static final int MARGIN_BOTTOM = 55;

    private static final float MAX_FONT_SIZE = 11f;
    private static final float MIN_FONT_SIZE = 7f;

    /**
     * @param semitoneOffset current transposition, so the PDF matches what is on screen
     */
    public static void exportAndShare(Activity activity, Song song, int semitoneOffset) {
        if (song == null) return;

        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                File file = createPdf(activity, song, semitoneOffset);
                Uri uri = FileProvider.getUriForFile(
                        activity, activity.getPackageName() + ".fileprovider", file);

                Intent share = new Intent(Intent.ACTION_SEND);
                share.setType("application/pdf");
                share.putExtra(Intent.EXTRA_STREAM, uri);
                share.putExtra(Intent.EXTRA_SUBJECT, song.getTitle());
                share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                activity.runOnUiThread(() ->
                        activity.startActivity(Intent.createChooser(share, "Share song PDF")));
            } catch (Exception e) {
                e.printStackTrace();
                activity.runOnUiThread(() ->
                        Toast.makeText(activity, "Could not create PDF", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private static File createPdf(Activity activity, Song song, int semitoneOffset) throws Exception {
        String raw = song.getContent() == null ? "" : song.getContent();
        String content = ChordTransposer.transposeContent(raw, semitoneOffset);
        List<SongPrintFormatter.PrintLine> lines = SongPrintFormatter.format(content);

        // ---- Paints ----
        Paint titlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        titlePaint.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD));
        titlePaint.setColor(Color.BLACK);

        Paint subPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        subPaint.setTypeface(Typeface.SANS_SERIF);
        subPaint.setColor(Color.DKGRAY);
        subPaint.setTextSize(12f);

        Paint lyricPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        lyricPaint.setTypeface(Typeface.MONOSPACE);
        lyricPaint.setColor(Color.rgb(30, 30, 30));

        Paint chordPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        chordPaint.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.BOLD));
        chordPaint.setColor(Color.BLACK);

        Paint sectionPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        sectionPaint.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.BOLD));
        sectionPaint.setColor(Color.BLACK);

        Paint footerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        footerPaint.setTypeface(Typeface.SANS_SERIF);
        footerPaint.setColor(Color.GRAY);
        footerPaint.setTextSize(9f);

        Paint rulePaint = new Paint();
        rulePaint.setColor(Color.LTGRAY);
        rulePaint.setStrokeWidth(1f);

        // ---- Pick a font size so the longest line fits the page width ----
        float usableWidth = PAGE_WIDTH - 2f * MARGIN_X;
        int longest = 0;
        for (SongPrintFormatter.PrintLine l : lines) {
            longest = Math.max(longest, l.text.length());
        }
        float fontSize = MAX_FONT_SIZE;
        lyricPaint.setTextSize(MAX_FONT_SIZE);
        float charWidthAtMax = lyricPaint.measureText("M");
        if (longest * charWidthAtMax > usableWidth) {
            fontSize = Math.max(MIN_FONT_SIZE, MAX_FONT_SIZE * usableWidth / (longest * charWidthAtMax));
        }
        lyricPaint.setTextSize(fontSize);
        chordPaint.setTextSize(fontSize);
        sectionPaint.setTextSize(fontSize * 1.1f);
        float charWidth = lyricPaint.measureText("M");
        int maxChars = Math.max(10, (int) (usableWidth / charWidth));

        float lineHeight = fontSize * 1.4f;
        float sectionGap = lineHeight * 0.6f;
        float contentBottom = PAGE_HEIGHT - MARGIN_BOTTOM;

        PdfDocument doc = new PdfDocument();
        try {
            int pageNumber = 1;
            PdfDocument.Page page = doc.startPage(
                    new PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create());
            Canvas canvas = page.getCanvas();
            float y = MARGIN_TOP;

            // ---- Header (first page only) ----
            String title = song.getTitle() == null ? "Untitled" : song.getTitle();
            float titleSize = 22f;
            titlePaint.setTextSize(titleSize);
            while (titlePaint.measureText(title) > usableWidth && titleSize > 12f) {
                titleSize -= 1f;
                titlePaint.setTextSize(titleSize);
            }
            y += titleSize;
            canvas.drawText(title, MARGIN_X, y, titlePaint);

            String artist = song.getArtist();
            if (artist != null && !artist.trim().isEmpty()) {
                y += 18f;
                canvas.drawText(artist.trim(), MARGIN_X, y, subPaint);
            }

            String origKey = song.getOriginalKey();
            if (origKey != null && !origKey.trim().isEmpty()) {
                String keyText = "Key: " + ChordTransposer.transposeKey(origKey.trim(), semitoneOffset);
                if (semitoneOffset != 0) {
                    keyText += "   (original: " + origKey.trim() + ")";
                }
                y += 16f;
                canvas.drawText(keyText, MARGIN_X, y, subPaint);
            }

            y += 12f;
            canvas.drawLine(MARGIN_X, y, PAGE_WIDTH - MARGIN_X, y, rulePaint);
            y += 22f;
            float contentTop = y;

            // ---- Body ----
            for (int i = 0; i < lines.size(); i++) {
                SongPrintFormatter.PrintLine line = lines.get(i);

                // Don't start a page with blank lines
                if (line.type == SongPrintFormatter.Type.BLANK && y <= contentTop) continue;

                // Space needed to keep this line together with the lines that must follow it
                float required = heightOf(line, lineHeight, sectionGap, y <= contentTop);
                int j = i;
                while (lines.get(j).keepWithNext && j + 1 < lines.size() && j - i < 3) {
                    j++;
                    required += heightOf(lines.get(j), lineHeight, sectionGap, false);
                }

                if (y + required > contentBottom && y > contentTop) {
                    drawFooter(canvas, footerPaint, pageNumber);
                    doc.finishPage(page);
                    pageNumber++;
                    page = doc.startPage(
                            new PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create());
                    canvas = page.getCanvas();
                    y = MARGIN_TOP;
                    contentTop = y;
                    if (line.type == SongPrintFormatter.Type.BLANK) continue;
                }

                if (line.type == SongPrintFormatter.Type.BLANK) {
                    y += lineHeight * 0.8f;
                    continue;
                }

                if (line.type == SongPrintFormatter.Type.SECTION && y > contentTop) {
                    y += sectionGap;
                }

                Paint paint;
                switch (line.type) {
                    case SECTION: paint = sectionPaint; break;
                    case CHORDS:  paint = chordPaint;   break;
                    default:      paint = lyricPaint;   break;
                }

                // Very long lines (rare) are hard-wrapped instead of running off the page
                String text = line.text;
                int start = 0;
                do {
                    int end = Math.min(text.length(), start + maxChars);
                    y += lineHeight;
                    canvas.drawText(text.substring(start, end), MARGIN_X, y, paint);
                    start = end;
                } while (start < text.length());
            }

            drawFooter(canvas, footerPaint, pageNumber);
            doc.finishPage(page);

            File dir = new File(activity.getCacheDir(), "exports");
            dir.mkdirs();
            File file = new File(dir, safeFileName(title) + ".pdf");
            try (FileOutputStream fos = new FileOutputStream(file)) {
                doc.writeTo(fos);
            }
            return file;
        } finally {
            doc.close();
        }
    }

    private static float heightOf(SongPrintFormatter.PrintLine line, float lineHeight,
                                  float sectionGap, boolean atTop) {
        switch (line.type) {
            case SECTION: return lineHeight + (atTop ? 0f : sectionGap);
            case BLANK:   return lineHeight * 0.8f;
            default:      return lineHeight;
        }
    }

    private static void drawFooter(Canvas canvas, Paint paint, int pageNumber) {
        float y = PAGE_HEIGHT - 28f;
        canvas.drawText("COGChords", MARGIN_X, y, paint);
        String label = "Page " + pageNumber;
        canvas.drawText(label, PAGE_WIDTH - MARGIN_X - paint.measureText(label), y, paint);
    }

    private static String safeFileName(String title) {
        String name = title.replaceAll("[^a-zA-Z0-9 _-]", "").trim().replaceAll("\\s+", "_");
        return name.isEmpty() ? "song" : name;
    }
}
