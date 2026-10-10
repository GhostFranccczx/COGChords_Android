package com.example.cogchords.util;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChordTransposer {

    // Chromatic scales
    private static final List<String> SHARP_SCALE = Arrays.asList(
            "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"
    );

    private static final List<String> FLAT_SCALE = Arrays.asList(
            "C", "Db", "D", "Eb", "E", "F", "Gb", "G", "Ab", "A", "Bb", "B"
    );

    // Regex for single chord: Root [A-G][#b]?, optional suffix, optional bass note /[A-G][#b]?
    // Examples: C, Am, G7, Dm7, F#m, Bbmaj7, C/E, D/F#
    private static final Pattern CHORD_PATTERN = Pattern.compile(
            "([A-G][#b]?)(m|maj|min|dim|aug|sus|add|[0-9]|b5|#5|b9|#9|#11|b13)*(/([A-G][#b]?))?"
    );

    // Regex for bracketed chord: [C], [Am7], [C/E]
    private static final Pattern BRACKETED_CHORD_PATTERN = Pattern.compile(
            "\\[([A-G][#b]?[^\\]]*)\\]"
    );

    // Regex to detect section headers: [Intro], [Verse 1], [Chorus], [Bridge], etc.
    private static final Pattern SECTION_PATTERN = Pattern.compile(
            "^\\[(intro|verse|chorus|bridge|pre-chorus|ending|outro|refrain|tag|interlude|instrumental|v[0-9]|c[0-9]).*\\]$",
            Pattern.CASE_INSENSITIVE
    );

    /**
     * Transposes a full song text by a given semitone offset.
     * Preserves sections, lyrics, and formatting.
     */
    public static String transposeContent(String content, int semitoneOffset) {
        if (content == null || content.isEmpty() || semitoneOffset % 12 == 0) {
            return content;
        }

        StringBuilder result = new StringBuilder();
        String[] lines = content.split("\n", -1);

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];

            if (isSectionHeader(line.trim())) {
                result.append(line);
            } else if (hasBracketedChords(line)) {
                result.append(transposeBracketedLine(line, semitoneOffset));
            } else if (isChordLine(line)) {
                result.append(transposeChordLine(line, semitoneOffset));
            } else {
                result.append(line);
            }

            if (i < lines.length - 1) {
                result.append("\n");
            }
        }

        return result.toString();
    }

    /**
     * Transposes a key representation (e.g. "G", "Am", "F#m", "Bb")
     */
    public static String transposeKey(String key, int semitoneOffset) {
        if (key == null || key.trim().isEmpty() || semitoneOffset % 12 == 0) {
            return key;
        }
        return transposeChordToken(key.trim(), semitoneOffset);
    }

    /**
     * Checks if a line is a section header like [Verse 1], [Chorus], etc.
     */
    public static boolean isSectionHeader(String line) {
        if (line == null) return false;
        String trimmed = line.trim();
        return SECTION_PATTERN.matcher(trimmed).matches();
    }

    /**
     * Checks if line contains chords in brackets [C], [Am]
     */
    public static boolean hasBracketedChords(String line) {
        if (line == null) return false;
        return BRACKETED_CHORD_PATTERN.matcher(line).find();
    }

    /**
     * Transposes all [Chord] occurrences in a bracketed line
     */
    private static String transposeBracketedLine(String line, int semitoneOffset) {
        Matcher matcher = BRACKETED_CHORD_PATTERN.matcher(line);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String chordText = matcher.group(1);
            if (isChord(chordText)) {
                String transposed = transposeChordToken(chordText, semitoneOffset);
                matcher.appendReplacement(sb, "[" + Matcher.quoteReplacement(transposed) + "]");
            } else {
                matcher.appendReplacement(sb, "[" + Matcher.quoteReplacement(chordText) + "]");
            }
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    /**
     * Determines whether an entire line is composed of chord symbols and whitespace.
     */
    public static boolean isChordLine(String line) {
        if (line == null || line.trim().isEmpty()) {
            return false;
        }

        String trimmed = line.trim();
        if (isSectionHeader(trimmed)) {
            return false;
        }

        String[] tokens = trimmed.split("\\s+");
        if (tokens.length == 0) {
            return false;
        }

        int chordCount = 0;
        for (String token : tokens) {
            if (isChord(token)) {
                chordCount++;
            }
        }

        // If at least 70% of tokens on line are valid chords, consider it a chord line
        return (double) chordCount / tokens.length >= 0.7;
    }

    /**
     * Transposes a line containing spaced chords, preserving column positions where possible.
     */
    private static String transposeChordLine(String line, int semitoneOffset) {
        StringBuilder result = new StringBuilder();
        int index = 0;
        int length = line.length();

        while (index < length) {
            if (Character.isWhitespace(line.charAt(index))) {
                result.append(line.charAt(index));
                index++;
            } else {
                // Collect word token
                int start = index;
                while (index < length && !Character.isWhitespace(line.charAt(index))) {
                    index++;
                }
                String token = line.substring(start, index);
                if (isChord(token)) {
                    result.append(transposeChordToken(token, semitoneOffset));
                } else {
                    result.append(token);
                }
            }
        }

        return result.toString();
    }

    /**
     * Checks if a single token represents a valid musical chord.
     */
    public static boolean isChord(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }
        return CHORD_PATTERN.matcher(token).matches();
    }

    /**
     * Transposes an individual chord token (e.g. "C", "Am", "Dm7", "C/E", "F#m7b5").
     */
    public static String transposeChordToken(String chord, int semitoneOffset) {
        if (chord == null || chord.isEmpty() || semitoneOffset % 12 == 0) {
            return chord;
        }

        // Handle slash chords: Root/Bass
        String rootPart = chord;
        String bassPart = null;
        int slashIndex = chord.indexOf('/');
        if (slashIndex != -1) {
            rootPart = chord.substring(0, slashIndex);
            bassPart = chord.substring(slashIndex + 1);
        }

        // Extract root note and suffix from rootPart
        String rootNote = extractRootNote(rootPart);
        if (rootNote == null) {
            return chord; // Unable to parse root note
        }
        String suffix = rootPart.substring(rootNote.length());

        boolean preferFlats = shouldUseFlats(rootNote, semitoneOffset);

        String transposedRoot = transposeRoot(rootNote, semitoneOffset, preferFlats);
        if (transposedRoot == null) {
            return chord;
        }

        StringBuilder transposedChord = new StringBuilder(transposedRoot).append(suffix);

        if (bassPart != null && !bassPart.isEmpty()) {
            String bassRoot = extractRootNote(bassPart);
            if (bassRoot != null) {
                String bassSuffix = bassPart.substring(bassRoot.length());
                String transposedBass = transposeRoot(bassRoot, semitoneOffset, preferFlats);
                transposedChord.append("/").append(transposedBass).append(bassSuffix);
            } else {
                transposedChord.append("/").append(bassPart);
            }
        }

        return transposedChord.toString();
    }

    private static String extractRootNote(String chord) {
        if (chord == null || chord.isEmpty()) return null;
        char firstChar = chord.charAt(0);
        if (firstChar < 'A' || firstChar > 'G') {
            return null;
        }
        if (chord.length() > 1) {
            char secondChar = chord.charAt(1);
            if (secondChar == '#' || secondChar == 'b') {
                return chord.substring(0, 2);
            }
        }
        return chord.substring(0, 1);
    }

    private static String transposeRoot(String root, int semitones, boolean preferFlats) {
        int index = getNoteIndex(root);
        if (index == -1) return null;

        int newIndex = (index + semitones) % 12;
        if (newIndex < 0) {
            newIndex += 12;
        }

        return preferFlats ? FLAT_SCALE.get(newIndex) : SHARP_SCALE.get(newIndex);
    }

    private static int getNoteIndex(String note) {
        for (int i = 0; i < 12; i++) {
            if (SHARP_SCALE.get(i).equalsIgnoreCase(note) || FLAT_SCALE.get(i).equalsIgnoreCase(note)) {
                return i;
            }
        }
        return -1;
    }

    private static boolean shouldUseFlats(String root, int semitoneOffset) {
        if (root.endsWith("b") || root.equalsIgnoreCase("F")) {
            return true;
        }
        return false;
    }
}
