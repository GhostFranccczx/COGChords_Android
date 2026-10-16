package com.example.cogchords;

import static org.junit.Assert.*;

import com.example.cogchords.util.SongTextImporter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.junit.Test;

public class SongTextImporterTest {

    @Test
    public void docxParagraphsTabsAndEntitiesBecomeText() {
        String xml = "<w:body><w:p><w:pPr><w:tabs><w:tab w:val=\"left\" w:pos=\"720\"/></w:tabs></w:pPr>"
                + "<w:r><w:t>G   C</w:t></w:r></w:p>"
                + "<w:p><w:r><w:t xml:space=\"preserve\">Amazing &amp; grace</w:t></w:r>"
                + "<w:r><w:tab/></w:r><w:r><w:t>x</w:t></w:r><w:r><w:br/></w:r><w:r><w:t>&#8217;s</w:t></w:r></w:p></w:body>";
        assertEquals("G   C\nAmazing & grace    x\n\u2019s\n", SongTextImporter.docxXmlToText(xml));
    }

    @Test
    public void docxFileIsReadFromTheZip() throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(bytes)) {
            zip.putNextEntry(new ZipEntry("[Content_Types].xml"));
            zip.write("<x/>".getBytes(StandardCharsets.UTF_8));
            zip.closeEntry();
            zip.putNextEntry(new ZipEntry("word/document.xml"));
            zip.write("<w:p><w:r><w:t>Hello</w:t></w:r></w:p>".getBytes(StandardCharsets.UTF_8));
            zip.closeEntry();
        }
        String text = SongTextImporter.extractDocxText(new ByteArrayInputStream(bytes.toByteArray()));
        assertEquals("Hello\n", text);
    }

    @Test
    public void notADocxThrows() {
        try {
            SongTextImporter.extractDocxText(new ByteArrayInputStream("not a zip".getBytes(StandardCharsets.UTF_8)));
            fail("expected an exception");
        } catch (java.io.IOException expected) {
            // ok
        }
    }

    @Test
    public void normalizeCleansLayoutAndConvertsHeadings() {
        String raw = "\n\nVerse 1\r\nG C   \nlyric line\n\n\n\nChorus:\nC\t\u00A0D\n\n";
        assertEquals("[Verse 1]\nG C\nlyric line\n\n[Chorus]\nC     D", SongTextImporter.normalize(raw));
    }

    @Test
    public void normalizeKeepsExistingBracketHeaders() {
        assertEquals("[Bridge]\nG", SongTextImporter.normalize("[Bridge]\nG"));
    }

    @Test
    public void headingWordsInsideLyricsAreNotChanged() {
        assertEquals("The chorus of angels", SongTextImporter.normalize("The chorus of angels"));
    }

    @Test
    public void keyIsGuessedFromFirstChord() {
        assertEquals("Dm", SongTextImporter.guessKey("[Verse]\n   Dm7  G\nlyrics"));
        assertEquals("G", SongTextImporter.guessKey("[G]Amazing [C]grace"));
        assertEquals("C", SongTextImporter.guessKey("Cmaj7 F\nlyrics"));
        assertEquals("Bb", SongTextImporter.guessKey("Bb  F\nlyrics"));
        assertEquals("", SongTextImporter.guessKey("just some words"));
        assertEquals("", SongTextImporter.guessKey(null));
    }

    @Test
    public void titleComesFromFileName() {
        assertEquals("my song-name", SongTextImporter.titleFromFileName("my_song-name.pdf"));
        assertEquals("Amazing Grace", SongTextImporter.titleFromFileName("Amazing Grace.docx"));
        assertEquals("", SongTextImporter.titleFromFileName(null));
    }
}
