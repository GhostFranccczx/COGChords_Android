package com.example.cogchords.util;

import com.example.cogchords.data.model.ChordModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChordDictionary {

    private static final List<ChordModel> CHORDS = new ArrayList<>();

    static {
        // C Chords
        CHORDS.add(new ChordModel("C", "C", "Major", 1,
                new int[]{-1, 3, 2, 0, 1, 0}, new int[]{0, 3, 2, 0, 1, 0}));
        CHORDS.add(new ChordModel("C7", "C", "Dominant 7th", 1,
                new int[]{-1, 3, 2, 3, 1, 0}, new int[]{0, 3, 2, 4, 1, 0}));
        CHORDS.add(new ChordModel("Cmaj7", "C", "Major 7th", 1,
                new int[]{-1, 3, 2, 0, 0, 0}, new int[]{0, 3, 2, 0, 0, 0}));
        CHORDS.add(new ChordModel("Cadd9", "C", "Add 9", 1,
                new int[]{-1, 3, 2, 0, 3, 3}, new int[]{0, 2, 1, 0, 3, 4}));

        // D Chords
        CHORDS.add(new ChordModel("D", "D", "Major", 1,
                new int[]{-1, -1, 0, 2, 3, 2}, new int[]{0, 0, 0, 1, 3, 2}));
        CHORDS.add(new ChordModel("Dm", "D", "Minor", 1,
                new int[]{-1, -1, 0, 2, 3, 1}, new int[]{0, 0, 0, 2, 3, 1}));
        CHORDS.add(new ChordModel("D7", "D", "Dominant 7th", 1,
                new int[]{-1, -1, 0, 2, 1, 2}, new int[]{0, 0, 0, 2, 1, 3}));
        CHORDS.add(new ChordModel("Dm7", "D", "Minor 7th", 1,
                new int[]{-1, -1, 0, 2, 1, 1}, new int[]{0, 0, 0, 2, 1, 1}));
        CHORDS.add(new ChordModel("Dsus4", "D", "Suspended 4th", 1,
                new int[]{-1, -1, 0, 2, 3, 3}, new int[]{0, 0, 0, 1, 2, 3}));
        CHORDS.add(new ChordModel("Dsus2", "D", "Suspended 2nd", 1,
                new int[]{-1, -1, 0, 2, 3, 0}, new int[]{0, 0, 0, 1, 2, 0}));

        // E Chords
        CHORDS.add(new ChordModel("E", "E", "Major", 1,
                new int[]{0, 2, 2, 1, 0, 0}, new int[]{0, 2, 3, 1, 0, 0}));
        CHORDS.add(new ChordModel("Em", "E", "Minor", 1,
                new int[]{0, 2, 2, 0, 0, 0}, new int[]{0, 2, 3, 0, 0, 0}));
        CHORDS.add(new ChordModel("E7", "E", "Dominant 7th", 1,
                new int[]{0, 2, 0, 1, 0, 0}, new int[]{0, 2, 0, 1, 0, 0}));
        CHORDS.add(new ChordModel("Em7", "E", "Minor 7th", 1,
                new int[]{0, 2, 0, 0, 0, 0}, new int[]{0, 2, 0, 0, 0, 0}));
        CHORDS.add(new ChordModel("Esus4", "E", "Suspended 4th", 1,
                new int[]{0, 2, 2, 2, 0, 0}, new int[]{0, 2, 3, 4, 0, 0}));

        // F Chords
        CHORDS.add(new ChordModel("F", "F", "Major", 1,
                new int[]{1, 3, 3, 2, 1, 1}, new int[]{1, 3, 4, 2, 1, 1}));
        CHORDS.add(new ChordModel("Fm", "F", "Minor", 1,
                new int[]{1, 3, 3, 1, 1, 1}, new int[]{1, 3, 4, 1, 1, 1}));
        CHORDS.add(new ChordModel("Fmaj7", "F", "Major 7th", 1,
                new int[]{-1, -1, 3, 2, 1, 0}, new int[]{0, 0, 3, 2, 1, 0}));

        // G Chords
        CHORDS.add(new ChordModel("G", "G", "Major", 1,
                new int[]{3, 2, 0, 0, 0, 3}, new int[]{2, 1, 0, 0, 0, 3}));
        CHORDS.add(new ChordModel("G7", "G", "Dominant 7th", 1,
                new int[]{3, 2, 0, 0, 0, 1}, new int[]{3, 2, 0, 0, 0, 1}));
        CHORDS.add(new ChordModel("Gsus4", "G", "Suspended 4th", 1,
                new int[]{3, 3, 0, 0, 1, 3}, new int[]{3, 4, 0, 0, 1, 2}));

        // A Chords
        CHORDS.add(new ChordModel("A", "A", "Major", 1,
                new int[]{-1, 0, 2, 2, 2, 0}, new int[]{0, 0, 1, 2, 3, 0}));
        CHORDS.add(new ChordModel("Am", "A", "Minor", 1,
                new int[]{-1, 0, 2, 2, 1, 0}, new int[]{0, 0, 2, 3, 1, 0}));
        CHORDS.add(new ChordModel("A7", "A", "Dominant 7th", 1,
                new int[]{-1, 0, 2, 0, 2, 0}, new int[]{0, 0, 2, 0, 3, 0}));
        CHORDS.add(new ChordModel("Am7", "A", "Minor 7th", 1,
                new int[]{-1, 0, 2, 0, 1, 0}, new int[]{0, 0, 2, 0, 1, 0}));
        CHORDS.add(new ChordModel("Asus4", "A", "Suspended 4th", 1,
                new int[]{-1, 0, 2, 2, 3, 0}, new int[]{0, 0, 1, 2, 3, 0}));
        CHORDS.add(new ChordModel("Asus2", "A", "Suspended 2nd", 1,
                new int[]{-1, 0, 2, 2, 0, 0}, new int[]{0, 0, 1, 2, 0, 0}));
        CHORDS.add(new ChordModel("Amaj7", "A", "Major 7th", 1,
                new int[]{-1, 0, 2, 1, 2, 0}, new int[]{0, 0, 2, 1, 3, 0}));

        // B Chords
        CHORDS.add(new ChordModel("B7", "B", "Dominant 7th", 1,
                new int[]{-1, 2, 1, 2, 0, 2}, new int[]{0, 2, 1, 3, 0, 4}));
        CHORDS.add(new ChordModel("Bm", "B", "Minor", 2,
                new int[]{-1, 2, 4, 4, 3, 2}, new int[]{0, 1, 3, 4, 2, 1}));
        CHORDS.add(new ChordModel("Bm7", "B", "Minor 7th", 2,
                new int[]{-1, 2, 4, 2, 3, 2}, new int[]{0, 1, 3, 1, 2, 1}));

        // F# Chords
        CHORDS.add(new ChordModel("F#m", "F#", "Minor", 2,
                new int[]{2, 4, 4, 2, 2, 2}, new int[]{1, 3, 4, 1, 1, 1}));
    }

    public static List<ChordModel> getAllChords() {
        return Collections.unmodifiableList(CHORDS);
    }

    public static List<ChordModel> filterChords(String rootNote, String query) {
        List<ChordModel> filtered = new ArrayList<>();
        String queryLower = query != null ? query.trim().toLowerCase() : "";
        boolean filterByRoot = rootNote != null && !rootNote.equalsIgnoreCase("ALL") && !rootNote.isEmpty();

        for (ChordModel chord : CHORDS) {
            boolean matchesRoot = !filterByRoot || chord.getRoot().equalsIgnoreCase(rootNote);
            boolean matchesQuery = queryLower.isEmpty()
                    || chord.getName().toLowerCase().contains(queryLower)
                    || chord.getQuality().toLowerCase().contains(queryLower);

            if (matchesRoot && matchesQuery) {
                filtered.add(chord);
            }
        }

        return filtered;
    }
}
