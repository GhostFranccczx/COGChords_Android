package com.example.cogchords.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Converts song text into print-ready lines (pure Java, no Android dependencies).
 * Inline bracket chords like "[G]Amazing [C]grace" are converted into the classic
 * "chords above lyrics" layout so the PDF reads the same for everyone.
 */
public class SongPrintFormatter {

    public enum Type { SECTION, CHORDS, LYRICS, BLANK }

    public static class PrintLine {
        public final Type type;
        public final String text;
        /** True if this line should not be separated from the next line by a page break. */
        public final boolean keepWithNext;

        PrintLine(Type type, String text, boolean keepWithNext) {
            this.type = type;
            this.text = text;
            this.keepWithNext = keepWithNext;
        }
    }

    private static final Pattern BRACKETED_CHORD = Pattern.compile("\\[([A-G][#b]?[^\\]]*)\\]");

    public static List<PrintLine> format(String content) {
        List<PrintLine> result = new ArrayList<>();
        if (content == null) return result;

        String[] lines = content.split("\\r?\\n", -1);
        for (String rawLine : lines) {
            String line = rightTrim(rawLine.replace("\t", "    "));

            if (line.isEmpty()) {
                result.add(new PrintLine(Type.BLANK, "", false));
            } else if (ChordTransposer.isSectionHeader(line)) {
                result.add(new PrintLine(Type.SECTION, line.trim(), true));
            } else if (ChordTransposer.hasBracketedChords(line)) {
                addBracketedLine(result, line);
            } else if (ChordTransposer.isChordLine(line)) {
                result.add(new PrintLine(Type.CHORDS, line, true));
            } else {
                result.add(new PrintLine(Type.LYRICS, line, false));
            }
        }
        return result;
    }

    /** "[G]Amazing [C]grace" -> chord line "G       C" above lyric line "Amazing grace". */
    static void addBracketedLine(List<PrintLine> out, String line) {
        StringBuilder chordLine = new StringBuilder();
        StringBuilder lyricLine = new StringBuilder();

        Matcher m = BRACKETED_CHORD.matcher(line);
        int last = 0;
        while (m.find()) {
            lyricLine.append(line, last, m.start());

            int pos = lyricLine.length();
            // Keep at least one space between neighbouring chords
            if (chordLine.length() > 0 && pos < chordLine.length() + 1) {
                pos = chordLine.length() + 1;
                pad(lyricLine, pos);
            }
            pad(chordLine, pos);
            chordLine.append(m.group(1));

            last = m.end();
        }
        lyricLine.append(line.substring(last));

        String chords = rightTrim(chordLine.toString());
        String lyrics = rightTrim(lyricLine.toString());

        if (!chords.isEmpty()) {
            out.add(new PrintLine(Type.CHORDS, chords, !lyrics.trim().isEmpty()));
        }
        if (!lyrics.trim().isEmpty()) {
            out.add(new PrintLine(Type.LYRICS, lyrics, false));
        }
    }

    private static void pad(StringBuilder sb, int length) {
        while (sb.length() < length) sb.append(' ');
    }

    private static String rightTrim(String s) {
        int end = s.length();
        while (end > 0 && Character.isWhitespace(s.charAt(end - 1))) end--;
        return s.substring(0, end);
    }
}
