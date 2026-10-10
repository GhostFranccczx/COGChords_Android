package com.example.cogchords;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.example.cogchords.data.model.ChordModel;
import com.example.cogchords.util.ChordDictionary;

import org.junit.Test;

import java.util.List;

public class ChordDictionaryTest {

    @Test
    public void testAllChordsHaveValidStrings() {
        List<ChordModel> chords = ChordDictionary.getAllChords();
        assertFalse(chords.isEmpty());

        for (ChordModel chord : chords) {
            assertNotNull(chord.getName());
            assertNotNull(chord.getRoot());
            assertNotNull(chord.getQuality());
            assertEquals(6, chord.getFrets().length);
            assertEquals(6, chord.getFingers().length);
            assertTrue(chord.getBaseFret() >= 1);
            assertNotNull(chord.getFretString());
        }
    }

    @Test
    public void testFilteringByRootNote() {
        List<ChordModel> cChords = ChordDictionary.filterChords("C", "");
        assertFalse(cChords.isEmpty());
        for (ChordModel chord : cChords) {
            assertEquals("C", chord.getRoot());
        }

        List<ChordModel> gChords = ChordDictionary.filterChords("G", "");
        assertFalse(gChords.isEmpty());
        for (ChordModel chord : gChords) {
            assertEquals("G", chord.getRoot());
        }
    }

    @Test
    public void testSearchQuery() {
        List<ChordModel> m7Chords = ChordDictionary.filterChords("ALL", "m7");
        assertFalse(m7Chords.isEmpty());
        for (ChordModel chord : m7Chords) {
            assertTrue(chord.getName().toLowerCase().contains("m7") ||
                    chord.getQuality().toLowerCase().contains("m7") ||
                    chord.getQuality().toLowerCase().contains("minor 7th"));
        }
    }

    @Test
    public void testCMajorFretString() {
        List<ChordModel> cList = ChordDictionary.filterChords("C", "C");
        ChordModel cMajor = null;
        for (ChordModel c : cList) {
            if (c.getName().equals("C")) {
                cMajor = c;
                break;
            }
        }
        assertNotNull(cMajor);
        assertEquals("X 3 2 0 1 0", cMajor.getFretString());
    }
}
