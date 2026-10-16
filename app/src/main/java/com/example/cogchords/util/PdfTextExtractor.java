package com.example.cogchords.util;

import android.content.Context;

import com.tom_roush.pdfbox.android.PDFBoxResourceLoader;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.pdmodel.PDPage;
import com.tom_roush.pdfbox.text.PDFTextStripper;
import com.tom_roush.pdfbox.text.TextPosition;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads the text of a PDF with PDFBox and rebuilds chord alignment from where each character
 * sits on the page (see {@link PdfLayoutReconstructor}). Works for PDFs that contain real text;
 * scanned pages or photos have no text and would need OCR.
 */
public class PdfTextExtractor {

    private static boolean initialized = false;

    public static synchronized String extract(Context context, InputStream in) throws IOException {
        if (!initialized) {
            PDFBoxResourceLoader.init(context.getApplicationContext());
            initialized = true;
        }

        PDDocument document = null;
        try {
            document = PDDocument.load(in);
            CollectingStripper stripper = new CollectingStripper();
            stripper.setSortByPosition(true);
            stripper.getText(document);
            stripper.finish();
            return PdfLayoutReconstructor.reconstruct(stripper.getLines());
        } finally {
            if (document != null) {
                document.close();
            }
        }
    }

    /** Collects every character with its position instead of building one flat string. */
    private static class CollectingStripper extends PDFTextStripper {

        private final List<PdfLayoutReconstructor.Line> lines = new ArrayList<>();
        private PdfLayoutReconstructor.Line current = new PdfLayoutReconstructor.Line();
        private float lastEndX = 0f;

        CollectingStripper() throws IOException {
            super();
        }

        @Override
        protected void writeString(String text, List<TextPosition> textPositions) throws IOException {
            for (TextPosition tp : textPositions) {
                String unicode = tp.getUnicode();
                if (unicode == null || unicode.isEmpty()) continue;

                float x = tp.getXDirAdj();
                float width = tp.getWidthDirAdj();
                current.setY(tp.getYDirAdj());

                float perChar = width / unicode.length();
                for (int i = 0; i < unicode.length(); i++) {
                    current.append(unicode.charAt(i), x + i * perChar, perChar);
                }
                lastEndX = x + width;
            }
        }

        @Override
        protected void writeWordSeparator() throws IOException {
            current.append(' ', lastEndX, 0f);
        }

        @Override
        protected void writeLineSeparator() throws IOException {
            flushLine();
        }

        @Override
        protected void writeParagraphSeparator() throws IOException {
            flushLine();
        }

        @Override
        protected void endPage(PDPage page) throws IOException {
            flushLine();
            super.endPage(page);
        }

        void finish() {
            flushLine();
        }

        List<PdfLayoutReconstructor.Line> getLines() {
            return lines;
        }

        private void flushLine() {
            if (current.length() > 0) {
                lines.add(current);
            }
            current = new PdfLayoutReconstructor.Line();
        }
    }
}
