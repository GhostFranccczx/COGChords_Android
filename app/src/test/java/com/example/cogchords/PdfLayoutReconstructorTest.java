package com.example.cogchords;

import static org.junit.Assert.*;

import com.example.cogchords.util.PdfLayoutReconstructor;
import com.example.cogchords.util.PdfLayoutReconstructor.Line;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class PdfLayoutReconstructorTest {

    private static final float W = 6f;

    /** A line where every character is 6 wide, starting at x0 (like a monospace PDF line). */
    private static Line mono(String text, float x0, float y) {
        Line line = new Line();
        line.setY(y);
        for (int i = 0; i < text.length(); i++) {
            line.append(text.charAt(i), x0 + i * W, text.charAt(i) == ' ' ? 0f : W);
        }
        return line;
    }

    /** A chord line as PDF extraction gives it: chords separated by a single space, but real x positions. */
    private static Line chords(float y, Object... tokensAndX) {
        Line line = new Line();
        line.setY(y);
        float lastEnd = 0;
        for (int i = 0; i < tokensAndX.length; i += 2) {
            String token = (String) tokensAndX[i];
            float x = ((Number) tokensAndX[i + 1]).floatValue();
            if (i > 0) line.append(' ', lastEnd, 0f);
            for (int k = 0; k < token.length(); k++) line.append(token.charAt(k), x + k * W, W);
            lastEnd = x + token.length() * W;
        }
        return line;
    }

    @Test
    public void chordsAreAlignedOverTheLyricUnderThem() {
        List<Line> lines = new ArrayList<>();
        // G above "A" (index 0), C above the "g" of grace (index 8)
        lines.add(chords(100f, "G", 50f, "C", 98f));
        lines.add(mono("Amazing grace", 50f, 114f));

        assertEquals("G       C\nAmazing grace\n", PdfLayoutReconstructor.reconstruct(lines));
    }

    @Test
    public void chordBeforeTheLyricStartsAtColumnZero() {
        List<Line> lines = new ArrayList<>();
        lines.add(chords(100f, "D", 30f));
        lines.add(mono("Hello", 50f, 114f));
        assertEquals("D\nHello\n", PdfLayoutReconstructor.reconstruct(lines));
    }

    @Test
    public void chordAfterTheEndOfTheLyricIsPlacedBeyondIt() {
        List<Line> lines = new ArrayList<>();
        lines.add(chords(100f, "C", 80f));
        lines.add(mono("Hi", 50f, 114f)); // ends at x = 62
        assertEquals("     C\nHi\n", PdfLayoutReconstructor.reconstruct(lines));
    }

    @Test
    public void closeChordsKeepAtLeastOneSpace() {
        List<Line> lines = new ArrayList<>();
        lines.add(chords(100f, "Am", 50f, "G", 54f)); // both over the same lyric area
        lines.add(mono("Hello there", 50f, 114f));
        String result = PdfLayoutReconstructor.reconstruct(lines);
        assertEquals("Am G\nHello there\n", result);
    }

    @Test
    public void chordOnlyLineKeepsRelativeGaps() {
        List<Line> lines = new ArrayList<>();
        lines.add(chords(100f, "C", 50f, "G", 110f)); // (110 - 56) / 6 = 9 spaces
        assertEquals("C         G\n", PdfLayoutReconstructor.reconstruct(lines));
    }

    @Test
    public void plainLinesAreKept() {
        List<Line> lines = new ArrayList<>();
        lines.add(mono("[Verse 1]", 50f, 100f));
        lines.add(mono("Just words   ", 50f, 114f));
        assertEquals("[Verse 1]\nJust words\n", PdfLayoutReconstructor.reconstruct(lines));
    }

    @Test
    public void largeVerticalGapBecomesABlankLine() {
        List<Line> lines = new ArrayList<>();
        lines.add(mono("[Verse 1]", 50f, 100f));
        lines.add(chords(114f, "G", 50f));
        lines.add(mono("line one", 50f, 128f));
        lines.add(chords(142f, "C", 50f));
        lines.add(mono("line two", 50f, 156f));
        lines.add(mono("[Chorus]", 50f, 198f)); // 42pt gap, normal is 14
        lines.add(chords(212f, "D", 50f));
        lines.add(mono("line three", 50f, 226f));

        String result = PdfLayoutReconstructor.reconstruct(lines);
        assertEquals("[Verse 1]\nG\nline one\nC\nline two\n\n[Chorus]\nD\nline three\n", result);
    }

    @Test
    public void newPageStartsANewBlock() {
        List<Line> lines = new ArrayList<>();
        lines.add(mono("first", 50f, 700f));
        lines.add(mono("second", 50f, 40f)); // y goes back up = next page
        assertEquals("first\n\nsecond\n", PdfLayoutReconstructor.reconstruct(lines));
    }

    @Test
    public void blankLinesAndEmptyInputAreHandled() {
        assertEquals("", PdfLayoutReconstructor.reconstruct(new ArrayList<>()));
        List<Line> lines = new ArrayList<>();
        lines.add(mono("   ", 50f, 100f));
        assertEquals("", PdfLayoutReconstructor.reconstruct(lines));
    }
}
