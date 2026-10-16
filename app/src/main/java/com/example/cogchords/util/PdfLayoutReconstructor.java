package com.example.cogchords.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Rebuilds "chords above lyrics" text from positioned PDF text (pure Java).
 *
 * PDF text extraction normally collapses the spacing between chords, which destroys chord
 * placement. Here every character keeps its x position from the page, so each chord is placed
 * over the lyric character that sits underneath it on the page. Vertical gaps between lines
 * are used to restore blank lines between sections.
 */
public class PdfLayoutReconstructor {

    /** One line of text where each character remembers its position on the page. */
    public static class Line {
        private final StringBuilder text = new StringBuilder();
        private final List<Float> xs = new ArrayList<>();
        private final List<Float> widths = new ArrayList<>();
        private float y = Float.NaN;

        public void append(char c, float x, float width) {
            text.append(c);
            xs.add(x);
            widths.add(width);
        }

        public void setY(float y) {
            if (Float.isNaN(this.y)) this.y = y;
        }

        public float getY() { return y; }
        public int length() { return text.length(); }
        public String getText() { return text.toString(); }

        float endX() {
            if (xs.isEmpty()) return 0f;
            int last = xs.size() - 1;
            return xs.get(last) + widths.get(last);
        }

        /** Average width of the visible characters on this line (fallback if unknown). */
        float averageCharWidth() {
            float sum = 0f;
            int count = 0;
            for (int i = 0; i < text.length(); i++) {
                if (text.charAt(i) != ' ' && widths.get(i) > 0f) {
                    sum += widths.get(i);
                    count++;
                }
            }
            return count == 0 ? 5f : sum / count;
        }
    }

    public static String reconstruct(List<Line> lines) {
        List<Float> gaps = new ArrayList<>();
        for (int i = 1; i < lines.size(); i++) {
            float gap = lines.get(i).getY() - lines.get(i - 1).getY();
            if (!Float.isNaN(gap) && gap > 0f) gaps.add(gap);
        }
        float medianGap = median(gaps);

        StringBuilder out = new StringBuilder();
        for (int i = 0; i < lines.size(); i++) {
            Line line = lines.get(i);
            String text = line.getText();
            if (text.trim().isEmpty()) continue;

            if (i > 0 && out.length() > 0 && startsNewBlock(lines.get(i - 1), line, medianGap)) {
                out.append('\n');
            }

            if (ChordTransposer.isChordLine(text)) {
                Line lyric = lyricBelow(lines, i);
                out.append(lyric != null ? alignOver(line, lyric) : spaceByGaps(line));
            } else {
                out.append(rtrim(text));
            }
            out.append('\n');
        }
        return out.toString();
    }

    /** A blank line is inserted at a page change or when the vertical gap is clearly larger than normal. */
    private static boolean startsNewBlock(Line previous, Line current, float medianGap) {
        float gap = current.getY() - previous.getY();
        if (Float.isNaN(gap)) return false;
        if (gap <= 0f) return true; // new page or column
        return medianGap > 0f && gap > medianGap * 1.9f;
    }

    private static Line lyricBelow(List<Line> lines, int chordIndex) {
        if (chordIndex + 1 >= lines.size()) return null;
        Line next = lines.get(chordIndex + 1);
        String t = next.getText();
        if (t.trim().isEmpty()) return null;
        if (ChordTransposer.isChordLine(t) || ChordTransposer.isSectionHeader(t)
                || ChordTransposer.hasBracketedChords(t)) {
            return null;
        }
        return next;
    }

    /** Places each chord over the lyric character found at the same horizontal position. */
    static String alignOver(Line chords, Line lyric) {
        String lyricText = lyric.getText();
        int n = lyricText.length();
        float lyricEnd = lyric.endX();
        float avgWidth = lyric.averageCharWidth();
        String chordText = chords.getText();

        StringBuilder sb = new StringBuilder();
        int i = 0;
        while (i < chordText.length()) {
            if (chordText.charAt(i) == ' ') { i++; continue; }
            int start = i;
            while (i < chordText.length() && chordText.charAt(i) != ' ') i++;
            String token = chordText.substring(start, i);
            float x = chords.xs.get(start);

            int col;
            if (n == 0 || x <= lyric.xs.get(0)) {
                col = 0;
            } else if (x >= lyricEnd) {
                col = n + Math.round((x - lyricEnd) / avgWidth);
            } else {
                int j = 0;
                while (j + 1 < n && lyric.xs.get(j + 1) <= x + 0.5f) j++;
                col = j;
            }

            if (sb.length() > 0 && col <= sb.length()) col = sb.length() + 1;
            while (sb.length() < col) sb.append(' ');
            sb.append(token);
        }
        return sb.toString();
    }

    /** Chord-only line (no lyrics under it): keep the relative gaps between chords. */
    static String spaceByGaps(Line chords) {
        String chordText = chords.getText();
        float avgWidth = chords.averageCharWidth();

        StringBuilder sb = new StringBuilder();
        float prevEnd = 0f;
        int i = 0;
        while (i < chordText.length()) {
            if (chordText.charAt(i) == ' ') { i++; continue; }
            int start = i;
            while (i < chordText.length() && chordText.charAt(i) != ' ') i++;
            String token = chordText.substring(start, i);

            if (sb.length() > 0) {
                int spaces = Math.round((chords.xs.get(start) - prevEnd) / avgWidth);
                spaces = Math.max(1, Math.min(spaces, 12));
                for (int s = 0; s < spaces; s++) sb.append(' ');
            }
            sb.append(token);
            prevEnd = chords.xs.get(i - 1) + chords.widths.get(i - 1);
        }
        return sb.toString();
    }

    private static float median(List<Float> values) {
        if (values.isEmpty()) return 0f;
        List<Float> sorted = new ArrayList<>(values);
        Collections.sort(sorted);
        return sorted.get(sorted.size() / 2);
    }

    private static String rtrim(String s) {
        int end = s.length();
        while (end > 0 && Character.isWhitespace(s.charAt(end - 1))) end--;
        return s.substring(0, end);
    }
}
