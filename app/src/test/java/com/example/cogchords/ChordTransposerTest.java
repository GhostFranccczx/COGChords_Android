package com.example.cogchords;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.example.cogchords.util.ChordTransposer;

import org.junit.Test;

public class ChordTransposerTest {

    @Test
    public void testBasicChordTranspositionUpTwoSemitones() {
        // C -> D
        assertEquals("D", ChordTransposer.transposeChordToken("C", 2));
        // G -> A
        assertEquals("A", ChordTransposer.transposeChordToken("G", 2));
        // Am -> Bm
        assertEquals("Bm", ChordTransposer.transposeChordToken("Am", 2));
        // F -> G
        assertEquals("G", ChordTransposer.transposeChordToken("F", 2));
    }

    @Test
    public void testChordQualitiesPreserved() {
        // Dm7 + 2 -> Em7
        assertEquals("Em7", ChordTransposer.transposeChordToken("Dm7", 2));
        // G7 + 2 -> A7
        assertEquals("A7", ChordTransposer.transposeChordToken("G7", 2));
        // Cmaj7 + 2 -> Dmaj7
        assertEquals("Dmaj7", ChordTransposer.transposeChordToken("Cmaj7", 2));
        // Asus4 + 2 -> Bsus4
        assertEquals("Bsus4", ChordTransposer.transposeChordToken("Asus4", 2));
    }

    @Test
    public void testSlashChordsTransposition() {
        // C/E + 2 -> D/F#
        assertEquals("D/F#", ChordTransposer.transposeChordToken("C/E", 2));
        // G/B + 2 -> A/C#
        assertEquals("A/C#", ChordTransposer.transposeChordToken("G/B", 2));
    }

    @Test
    public void testNegativeSemitonesAndOctaveWrap() {
        // C - 2 -> A# or Bb
        String transposed = ChordTransposer.transposeChordToken("C", -2);
        assertTrue(transposed.equals("A#") || transposed.equals("Bb"));

        // C + 12 -> C (octave reset)
        assertEquals("C", ChordTransposer.transposeChordToken("C", 12));
    }

    @Test
    public void testBracketedLyricsTransposition() {
        String input = "[G]Amazing grace how [C]sweet the [G]sound";
        String expected = "[A]Amazing grace how [D]sweet the [A]sound";
        assertEquals(expected, ChordTransposer.transposeContent(input, 2));
    }

    @Test
    public void testSpacedChordLineTransposition() {
        String input = "[Intro]\nG   C   D\n\n[Verse 1]\nG             C\nAmazing grace how sweet";
        String output = ChordTransposer.transposeContent(input, 2);
        assertTrue(output.contains("[Intro]"));
        assertTrue(output.contains("A   D   E"));
        assertTrue(output.contains("A             D"));
        assertTrue(output.contains("Amazing grace how sweet"));
    }
}
