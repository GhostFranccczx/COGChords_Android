package com.example.cogchords.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Turns text from imported files (Word, PDF, plain text) into song text (pure Java).
 */
public class SongTextImporter {

    private static final Pattern DOCX_TOKEN = Pattern.compile(
            "<w:t(?:\\s[^>]*)?>([^<]*)</w:t>|<w:tab\\s*/>|<w:br\\s*/>|<w:cr\\s*/>|</w:p>");

    private static final Pattern PLAIN_SECTION = Pattern.compile(
            "^(intro|verse|pre[- ]?chorus|chorus|bridge|tag|outro|interlude|instrumental|ending|refrain|hook|turnaround)"
                    + "\\s*(\\d+)?\\s*:?$", Pattern.CASE_INSENSITIVE);

    private static final Pattern FIRST_CHORD = Pattern.compile("^([A-G][#b]?)(m(?!aj))?");
    private static final Pattern BRACKET_CHORD = Pattern.compile("\\[([A-G][#b]?[^\\]]*)\\]");

    /** Reads the text out of a .docx file (a zip containing word/document.xml). */
    public static String extractDocxText(InputStream in) throws IOException {
        try (ZipInputStream zip = new ZipInputStream(in)) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                if (entry.getName().equals("word/document.xml")) {
                    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                    byte[] buffer = new byte[8192];
                    int read;
                    while ((read = zip.read(buffer)) != -1) bytes.write(buffer, 0, read);
                    return docxXmlToText(new String(bytes.toByteArray(), StandardCharsets.UTF_8));
                }
            }
        }
        throw new IOException("This does not look like a Word (.docx) file");
    }

    /** Converts Word's document.xml into plain text, one line per paragraph. */
    public static String docxXmlToText(String xml) {
        StringBuilder sb = new StringBuilder();
        Matcher m = DOCX_TOKEN.matcher(xml);
        while (m.find()) {
            String token = m.group();
            if (m.group(1) != null) {
                sb.append(unescapeXml(m.group(1)));
            } else if (token.startsWith("<w:tab")) {
                sb.append("    ");
            } else {
                sb.append('\n'); // </w:p>, <w:br/>, <w:cr/>
            }
        }
        return sb.toString();
    }

    /**
     * Cleans imported text: normalises line endings and spaces, turns plain headings such as
     * "Verse 1" or "Chorus:" into [Verse 1] / [Chorus], and limits blank lines to one in a row.
     */
    public static String normalize(String text) {
        if (text == null) return "";
        String cleaned = text.replace("\r\n", "\n").replace('\r', '\n')
                .replace('\u00A0', ' ').replace("\f", "\n").replace("\t", "    ");

        List<String> out = new ArrayList<>();
        for (String raw : cleaned.split("\n", -1)) {
            String line = rtrim(raw);
            String header = asSectionHeader(line);
            if (header != null) {
                if (!out.isEmpty() && !out.get(out.size() - 1).isEmpty()) out.add("");
                out.add(header);
            } else if (line.isEmpty() && (out.isEmpty() || out.get(out.size() - 1).isEmpty())) {
                // skip leading blank lines and repeated blank lines
            } else {
                out.add(line);
            }
        }
        while (!out.isEmpty() && out.get(out.size() - 1).isEmpty()) out.remove(out.size() - 1);
        return String.join("\n", out);
    }

    /** Guesses the key from the first chord in the song, e.g. "Dm7" -> "Dm". Empty if none found. */
    public static String guessKey(String text) {
        if (text == null) return "";
        for (String line : text.split("\n")) {
            String token = null;
            if (ChordTransposer.hasBracketedChords(line)) {
                Matcher b = BRACKET_CHORD.matcher(line);
                if (b.find()) token = b.group(1);
            } else if (ChordTransposer.isChordLine(line)) {
                token = line.trim().split("\\s+")[0];
            }
            if (token != null && ChordTransposer.isChord(token)) {
                Matcher m = FIRST_CHORD.matcher(token);
                if (m.find()) return m.group(1) + (m.group(2) != null ? "m" : "");
            }
        }
        return "";
    }

    /** "my_song-name.pdf" -> "my song name". */
    public static String titleFromFileName(String fileName) {
        if (fileName == null) return "";
        String name = fileName.trim();
        int dot = name.lastIndexOf('.');
        if (dot > 0) name = name.substring(0, dot);
        return name.replace('_', ' ').replaceAll("\\s+", " ").trim();
    }

    private static String asSectionHeader(String line) {
        Matcher m = PLAIN_SECTION.matcher(line.trim());
        if (!m.matches()) return null;
        String word = m.group(1);
        String label = Character.toUpperCase(word.charAt(0)) + word.substring(1).toLowerCase();
        if (m.group(2) != null) label += " " + m.group(2);
        return "[" + label + "]";
    }

    private static String unescapeXml(String s) {
        if (s.indexOf('&') < 0) return s;
        Matcher m = Pattern.compile("&(#x[0-9a-fA-F]+|#[0-9]+|amp|lt|gt|quot|apos);").matcher(s);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String e = m.group(1);
            String replacement;
            switch (e) {
                case "amp":  replacement = "&"; break;
                case "lt":   replacement = "<"; break;
                case "gt":   replacement = ">"; break;
                case "quot": replacement = "\""; break;
                case "apos": replacement = "'"; break;
                default:
                    int code = e.startsWith("#x")
                            ? Integer.parseInt(e.substring(2), 16)
                            : Integer.parseInt(e.substring(1));
                    replacement = new String(Character.toChars(code));
            }
            m.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    private static String rtrim(String s) {
        int end = s.length();
        while (end > 0 && Character.isWhitespace(s.charAt(end - 1))) end--;
        return s.substring(0, end);
    }
}
