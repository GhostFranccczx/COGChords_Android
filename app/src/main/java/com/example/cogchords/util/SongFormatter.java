package com.example.cogchords.util;

import android.content.Context;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.TypedValue;

import androidx.core.content.ContextCompat;

import com.example.cogchords.R;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SongFormatter {

    private static final Pattern BRACKETED_CHORD = Pattern.compile("\\[([A-G][#b]?[^\\]]*)\\]");

    /**
     * Formats raw lyrics & chords into an aesthetically styled Spannable text
     * with high-contrast sections and distinct chords for live performance reading.
     */
    public static CharSequence formatSong(Context context, String rawText) {
        if (rawText == null || rawText.isEmpty()) {
            return "";
        }

        SpannableStringBuilder ssb = new SpannableStringBuilder();
        String[] lines = rawText.split("\n", -1);

        // Resolve active theme colors
        int colorChord = ThemeManager.getChordColor(context);
        int colorLyrics = ThemeManager.getTextColor(context);
        int colorHeader = ThemeManager.getAccentColor(context);

        boolean isRainbow = ThemeManager.isRainbowTheme(context);
        int chordCounter = 0;
        int sectionCounter = 0;

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            int startPos = ssb.length();

            if (ChordTransposer.isSectionHeader(line)) {
                // Section Header (e.g. [Verse 1], [Chorus])
                ssb.append(line.trim());
                int endPos = ssb.length();

                int headerColor = isRainbow ? ThemeManager.getRainbowColorForPosition(context, sectionCounter++) : colorHeader;
                ssb.setSpan(new StyleSpan(Typeface.BOLD), startPos, endPos, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                ssb.setSpan(new RelativeSizeSpan(1.15f), startPos, endPos, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                ssb.setSpan(new ForegroundColorSpan(headerColor), startPos, endPos, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else if (ChordTransposer.hasBracketedChords(line)) {
                // Bracketed line: format brackets/chords prominently
                Matcher matcher = BRACKETED_CHORD.matcher(line);
                int lastIdx = 0;
                while (matcher.find()) {
                    // Lyric text before bracket
                    String beforeText = line.substring(lastIdx, matcher.start());
                    int beforeStart = ssb.length();
                    ssb.append(beforeText);
                    int beforeEnd = ssb.length();
                    ssb.setSpan(new ForegroundColorSpan(colorLyrics), beforeStart, beforeEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                    // Chord token
                    String chord = matcher.group(1);
                    int chordStart = ssb.length();
                    ssb.append(" ").append(chord).append(" ");
                    int chordEnd = ssb.length();

                    int activeChordColor = isRainbow ? ThemeManager.getRainbowColorForPosition(context, chordCounter++) : colorChord;
                    ssb.setSpan(new StyleSpan(Typeface.BOLD), chordStart, chordEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    ssb.setSpan(new ForegroundColorSpan(activeChordColor), chordStart, chordEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                    lastIdx = matcher.end();
                }
                if (lastIdx < line.length()) {
                    int remainingStart = ssb.length();
                    ssb.append(line.substring(lastIdx));
                    int remainingEnd = ssb.length();
                    ssb.setSpan(new ForegroundColorSpan(colorLyrics), remainingStart, remainingEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            } else if (ChordTransposer.isChordLine(line)) {
                // Line composed purely of chords
                ssb.append(line);
                int endPos = ssb.length();

                int activeChordColor = isRainbow ? ThemeManager.getRainbowColorForPosition(context, chordCounter++) : colorChord;
                ssb.setSpan(new StyleSpan(Typeface.BOLD), startPos, endPos, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                ssb.setSpan(new ForegroundColorSpan(activeChordColor), startPos, endPos, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else {
                // Standard lyric line
                ssb.append(line);
                int endPos = ssb.length();
                ssb.setSpan(new ForegroundColorSpan(colorLyrics), startPos, endPos, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            if (i < lines.length - 1) {
                ssb.append("\n");
            }
        }

        return ssb;
    }
}
