package com.example.cogchords;

import static org.junit.Assert.*;

import com.example.cogchords.util.PianoChord;

import org.junit.Test;

public class PianoChordTest {

    @Test
    public void cMajorHasThreeNotes() {
        PianoChord c = PianoChord.parse("C");
        assertArrayEquals(new int[]{0, 4, 7}, c.getKeyPositions());
        assertEquals("C E G", c.getNotesString());
        assertEquals("Major", c.getQuality());
    }

    @Test
    public void sharpMinorSeventh() {
        PianoChord chord = PianoChord.parse("F#m7");
        assertArrayEquals(new int[]{6, 9, 13, 16}, chord.getKeyPositions());
        assertEquals("F# A C# E", chord.getNotesString());
    }

    @Test
    public void flatRootsUseFlatNames() {
        PianoChord chord = PianoChord.parse("Bb");
        assertEquals("Bb D F", chord.getNotesString());
        assertArrayEquals(new int[]{10, 14, 17}, chord.getKeyPositions());
    }

    @Test
    public void slashBassInsideChordAddsNoExtraNote() {
        PianoChord chord = PianoChord.parse("D/F#");
        assertArrayEquals(new int[]{2, 6, 9}, chord.getKeyPositions());
        assertEquals("D F# A", chord.getNotesString());
    }

    @Test
    public void slashBassOutsideChordIsAdded() {
        PianoChord chord = PianoChord.parse("C/D");
        assertArrayEquals(new int[]{0, 2, 4, 7}, chord.getKeyPositions());
        assertEquals("D C E G", chord.getNotesString());
        assertTrue(chord.hasBass());
    }

    @Test
    public void extensionsFoldIntoOneOctave() {
        PianoChord chord = PianoChord.parse("Cadd9");
        assertArrayEquals(new int[]{0, 2, 4, 7}, chord.getKeyPositions());
    }

    @Test
    public void aliasesAreUnderstood() {
        assertArrayEquals(PianoChord.parse("Cm").getKeyPositions(), PianoChord.parse("Cmin").getKeyPositions());
        assertArrayEquals(PianoChord.parse("Cadd9").getKeyPositions(), PianoChord.parse("C2").getKeyPositions());
        assertArrayEquals(PianoChord.parse("Cmaj7").getKeyPositions(), PianoChord.parse("CM7").getKeyPositions());
    }

    @Test
    public void unknownChordsReturnNull() {
        assertNull(PianoChord.parse("H"));
        assertNull(PianoChord.parse("Cxyz"));
        assertNull(PianoChord.parse(""));
        assertNull(PianoChord.parse(null));
    }

    @Test
    public void rootKeyIsMarked() {
        PianoChord chord = PianoChord.parse("Em");
        assertTrue(chord.isRootKey(4));
        assertFalse(chord.isRootKey(7));
    }

    @Test
    public void keyWindowStartsAndEndsOnWhiteKeysAndIsWideEnough() {
        String[] names = {"C", "C#", "D", "Eb", "F#m7", "B", "Bm9", "G/D", "Ab"};
        for (String name : names) {
            PianoChord chord = PianoChord.parse(name);
            int[] window = chord.getKeyWindow(8);
            assertTrue(name, PianoChord.isWhite(window[0]));
            assertTrue(name, PianoChord.isWhite(window[1]));
            assertTrue(name, PianoChord.countWhiteKeys(window[0], window[1]) >= 8);
            for (int pos : chord.getKeyPositions()) {
                assertTrue(name, pos >= window[0] && pos <= window[1]);
            }
        }
    }

    @Test
    public void referenceListHasEveryRootAndQuality() {
        assertEquals(12 * 13, PianoChord.getAll().size());
        // The "C" chip lists C and C# chords, since there are no separate chips for sharps
        assertEquals(26, PianoChord.filter("C", "").size());
        for (PianoChord chord : PianoChord.filter("C", "")) {
            assertTrue(chord.getName().startsWith("C"));
        }
    }

    @Test
    public void searchFindsFlatSpellings() {
        boolean found = false;
        for (PianoChord chord : PianoChord.filter("ALL", "bbm")) {
            if (chord.getName().equals("A#m")) found = true;
        }
        assertTrue(found);
    }

    @Test
    public void searchByQuality() {
        assertEquals(12, PianoChord.filter("ALL", "diminished").size());
    }
}
