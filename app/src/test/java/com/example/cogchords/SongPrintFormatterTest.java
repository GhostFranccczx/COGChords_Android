package com.example.cogchords;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.example.cogchords.util.SongPrintFormatter;
import com.example.cogchords.util.SongPrintFormatter.PrintLine;
import com.example.cogchords.util.SongPrintFormatter.Type;

import java.util.List;

import org.junit.Test;

public class SongPrintFormatterTest {

    @Test
    public void sectionHeaderIsDetected() {
        List<PrintLine> lines = SongPrintFormatter.format("[Chorus]");
        assertEquals(1, lines.size());
        assertEquals(Type.SECTION, lines.get(0).type);
        assertTrue(lines.get(0).keepWithNext);
    }

    @Test
    public void chordLineAboveLyricsIsKeptTogether() {
        List<PrintLine> lines = SongPrintFormatter.format("G   C\nAmazing grace");
        assertEquals(2, lines.size());
        assertEquals(Type.CHORDS, lines.get(0).type);
        assertTrue(lines.get(0).keepWithNext);
        assertEquals(Type.LYRICS, lines.get(1).type);
    }

    @Test
    public void bracketedChordsBecomeChordsAboveLyrics() {
        List<PrintLine> lines = SongPrintFormatter.format("[G]Amazing [C]grace");
        assertEquals(2, lines.size());
        assertEquals("G       C", lines.get(0).text);
        assertEquals("Amazing grace", lines.get(1).text);
    }

    @Test
    public void adjacentBracketedChordsKeepASpaceBetweenThem() {
        List<PrintLine> lines = SongPrintFormatter.format("[C][G] [D]");
        assertEquals(1, lines.size());
        assertEquals(Type.CHORDS, lines.get(0).type);
        assertEquals("C G D", lines.get(0).text);
    }

    @Test
    public void blankAndNullInputAreHandled() {
        assertTrue(SongPrintFormatter.format(null).isEmpty());
        assertEquals(Type.BLANK, SongPrintFormatter.format("").get(0).type);
    }
}
