package com.example.cogchords.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A chord described as piano keys (pure Java, no Android dependencies).
 * Everything is computed from the chord name, so any chord such as "F#m7" or "D/F#" works
 * without hardcoding a list of key positions.
 *
 * Key positions are semitone numbers counted from a C (C = 0, C# = 1 ... B = 11, next C = 12).
 */
public class PianoChord {

    private static final String[] SHARP_NAMES = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};
    private static final String[] FLAT_NAMES = {"C", "Db", "D", "Eb", "E", "F", "Gb", "G", "Ab", "A", "Bb", "B"};
    private static final boolean[] IS_WHITE = {true, false, true, false, true, true, false, true, false, true, false, true};

    /** Chord qualities shown in the reference: {suffix, label, intervals in semitones}. */
    private static final String[][] QUALITIES = {
            {"", "Major", "0,4,7"},
            {"m", "Minor", "0,3,7"},
            {"7", "Dominant 7th", "0,4,7,10"},
            {"maj7", "Major 7th", "0,4,7,11"},
            {"m7", "Minor 7th", "0,3,7,10"},
            {"sus2", "Suspended 2nd", "0,2,7"},
            {"sus4", "Suspended 4th", "0,5,7"},
            {"dim", "Diminished", "0,3,6"},
            {"aug", "Augmented", "0,4,8"},
            {"add9", "Add 9", "0,4,7,14"},
            {"6", "Major 6th", "0,4,7,9"},
            {"m6", "Minor 6th", "0,3,7,9"},
            {"9", "Dominant 9th", "0,4,7,10,14"},
    };

    /** Extra spellings understood when parsing chord names (not listed in the reference grid). */
    private static final String[][] EXTRA_QUALITIES = {
            {"5", "Power chord", "0,7"},
            {"dim7", "Diminished 7th", "0,3,6,9"},
            {"m7b5", "Half-diminished", "0,3,6,10"},
            {"7sus4", "Dominant 7th sus4", "0,5,7,10"},
            {"maj9", "Major 9th", "0,4,7,11,14"},
            {"m9", "Minor 9th", "0,3,7,10,14"},
            {"madd9", "Minor add 9", "0,3,7,14"},
    };

    private static final String[][] ALIASES = {
            {"min", "m"}, {"-", "m"}, {"M7", "maj7"}, {"Maj7", "maj7"}, {"sus", "sus4"},
            {"+", "aug"}, {"2", "add9"}, {"M", ""}, {"maj", ""},
    };

    private static final Map<String, String[]> BY_SUFFIX = new HashMap<>();
    private static final Pattern CHORD_NAME = Pattern.compile("^([A-G][#b]?)([^/]*)(?:/([A-G][#b]?))?$");

    static {
        for (String[] q : QUALITIES) BY_SUFFIX.put(q[0], q);
        for (String[] q : EXTRA_QUALITIES) BY_SUFFIX.put(q[0], q);
        for (String[] alias : ALIASES) BY_SUFFIX.put(alias[0], BY_SUFFIX.get(alias[1]));
    }

    private static List<PianoChord> allChords;

    private final String name;
    private final String root;
    private final String suffix;
    private final String quality;
    private final int rootPc;
    private final int bassPc; // -1 if no slash bass
    private final int[] intervals;
    private final boolean preferFlats;

    private PianoChord(String name, String root, String suffix, String quality,
                       int rootPc, int bassPc, int[] intervals, boolean preferFlats) {
        this.name = name;
        this.root = root;
        this.suffix = suffix;
        this.quality = quality;
        this.rootPc = rootPc;
        this.bassPc = bassPc;
        this.intervals = intervals;
        this.preferFlats = preferFlats;
    }

    /** Parses a chord name like "C", "F#m7", "Bb", "D/F#". Returns null if it is not understood. */
    public static PianoChord parse(String chordName) {
        if (chordName == null) return null;
        String trimmed = chordName.trim();
        Matcher m = CHORD_NAME.matcher(trimmed);
        if (!m.matches()) return null;

        String root = m.group(1);
        String suffix = m.group(2) == null ? "" : m.group(2);
        String bass = m.group(3);

        String[] q = BY_SUFFIX.get(suffix);
        if (q == null) return null;

        int rootPc = pitchClass(root);
        int bassPc = bass == null ? -1 : pitchClass(bass);
        boolean flats = root.endsWith("b") || root.equals("F")
                || (bass != null && bass.endsWith("b"));

        return new PianoChord(trimmed, root, suffix, q[1], rootPc, bassPc, parseIntervals(q[2]), flats);
    }

    public String getName() { return name; }
    public String getRoot() { return root; }
    public String getQuality() { return quality; }
    public int getRootPitchClass() { return rootPc; }
    public boolean hasBass() { return bassPc >= 0; }

    /** Same chord spelled with flats (e.g. "A#m" -> "Bbm"), used for searching. */
    public String getAltName() {
        if (suffix == null) return name;
        return FLAT_NAMES[rootPc] + suffix + (bassPc >= 0 ? "/" + FLAT_NAMES[bassPc] : "");
    }

    /** Note names of the chord in order (root, third, fifth, ...), e.g. {"C", "E", "G"}. */
    public String[] getNoteNames() {
        String[] names = preferFlats ? FLAT_NAMES : SHARP_NAMES;
        List<String> result = new ArrayList<>();
        if (bassPc >= 0 && !containsPc(bassPc)) result.add(names[bassPc]);
        for (int interval : intervals) {
            String n = names[(rootPc + interval) % 12];
            if (!result.contains(n)) result.add(n);
        }
        return result.toArray(new String[0]);
    }

    /** Note names as one display string, e.g. "C E G" (slash bass is shown first). */
    public String getNotesString() {
        StringBuilder sb = new StringBuilder();
        for (String n : getNoteNames()) {
            if (sb.length() > 0) sb.append(' ');
            sb.append(n);
        }
        return sb.toString();
    }

    /**
     * Sorted, distinct key positions to highlight. Chord notes are folded into the octave above
     * the root so the diagram stays compact; a slash bass note is placed in the first octave.
     */
    public int[] getKeyPositions() {
        List<Integer> positions = new ArrayList<>();
        for (int interval : intervals) {
            int pos = rootPc + (interval % 12);
            if (!positions.contains(pos)) positions.add(pos);
        }
        if (bassPc >= 0 && !positions.contains(bassPc)) positions.add(bassPc);
        Collections.sort(positions);
        int[] result = new int[positions.size()];
        for (int i = 0; i < result.length; i++) result[i] = positions.get(i);
        return result;
    }

    /** True if the key at this position is the root of the chord. */
    public boolean isRootKey(int position) {
        return position % 12 == rootPc && position >= rootPc && position < rootPc + 12;
    }

    /**
     * The range of keys to draw as {startSemitone, endSemitone}, both inclusive and both white keys,
     * covering all highlighted notes and at least {@code minWhiteKeys} white keys.
     */
    public int[] getKeyWindow(int minWhiteKeys) {
        int[] positions = getKeyPositions();
        int start = positions[0];
        int end = positions[positions.length - 1];
        if (!isWhite(start)) start--;
        if (!isWhite(end)) end++;
        while (countWhiteKeys(start, end) < minWhiteKeys) {
            end++;
            if (!isWhite(end)) end++;
        }
        return new int[]{start, end};
    }

    public static boolean isWhite(int position) {
        return IS_WHITE[((position % 12) + 12) % 12];
    }

    public static int countWhiteKeys(int start, int end) {
        int count = 0;
        for (int p = start; p <= end; p++) {
            if (isWhite(p)) count++;
        }
        return count;
    }

    /** Every chord shown in the piano reference (12 roots x common qualities), sorted by root. */
    public static synchronized List<PianoChord> getAll() {
        if (allChords == null) {
            List<PianoChord> list = new ArrayList<>();
            for (String root : SHARP_NAMES) {
                for (String[] q : QUALITIES) {
                    PianoChord chord = parse(root + q[0]);
                    if (chord != null) list.add(chord);
                }
            }
            allChords = Collections.unmodifiableList(list);
        }
        return allChords;
    }

    /** Filters the reference by root letter chip ("ALL", "C", "D" ...) and a search query. */
    public static List<PianoChord> filter(String rootLetter, String query) {
        List<PianoChord> result = new ArrayList<>();
        String q = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        boolean filterByRoot = rootLetter != null && !rootLetter.isEmpty() && !rootLetter.equalsIgnoreCase("ALL");

        for (PianoChord chord : getAll()) {
            boolean matchesRoot = !filterByRoot || chord.root.substring(0, 1).equalsIgnoreCase(rootLetter);
            boolean matchesQuery = q.isEmpty()
                    || chord.name.toLowerCase(Locale.ROOT).contains(q)
                    || chord.getAltName().toLowerCase(Locale.ROOT).contains(q)
                    || chord.quality.toLowerCase(Locale.ROOT).contains(q);
            if (matchesRoot && matchesQuery) result.add(chord);
        }
        return result;
    }

    private boolean containsPc(int pc) {
        for (int interval : intervals) {
            if ((rootPc + interval) % 12 == pc) return true;
        }
        return false;
    }

    private static int[] parseIntervals(String csv) {
        String[] parts = csv.split(",");
        int[] result = new int[parts.length];
        for (int i = 0; i < parts.length; i++) result[i] = Integer.parseInt(parts[i].trim());
        return result;
    }

    private static int pitchClass(String note) {
        int base;
        switch (note.charAt(0)) {
            case 'C': base = 0; break;
            case 'D': base = 2; break;
            case 'E': base = 4; break;
            case 'F': base = 5; break;
            case 'G': base = 7; break;
            case 'A': base = 9; break;
            default:  base = 11; break; // B
        }
        if (note.length() > 1) {
            base += note.charAt(1) == '#' ? 1 : -1;
        }
        return ((base % 12) + 12) % 12;
    }

    @Override
    public String toString() {
        return name + Arrays.toString(getKeyPositions());
    }
}
