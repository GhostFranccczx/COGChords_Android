package com.example.cogchords;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.example.cogchords.data.entity.Song;
import com.example.cogchords.util.SongJsonHelper;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class SongJsonHelperTest {

    @Test
    public void testParseJsonArray() throws Exception {
        String json = "[\n" +
                "  {\n" +
                "    \"title\": \"Hosanna\",\n" +
                "    \"artist\": \"Brooke Fraser\",\n" +
                "    \"originalKey\": \"E\",\n" +
                "    \"category\": \"Praise\",\n" +
                "    \"content\": \"[Verse 1]\\nE             C#m\\nI see the King of glory\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"title\": \"Mighty To Save\",\n" +
                "    \"artist\": \"Reuben Morgan\",\n" +
                "    \"key\": \"A\",\n" +
                "    \"content\": \"[Chorus]\\nA          E\\nSavior, He can move the mountains\"\n" +
                "  }\n" +
                "]";

        List<Song> songs = SongJsonHelper.parseJsonToSongs(json);
        assertNotNull(songs);
        assertEquals(2, songs.size());

        Song s1 = songs.get(0);
        assertEquals("Hosanna", s1.getTitle());
        assertEquals("Brooke Fraser", s1.getArtist());
        assertEquals("E", s1.getOriginalKey());
        assertEquals("Praise", s1.getCategory());

        Song s2 = songs.get(1);
        assertEquals("Mighty To Save", s2.getTitle());
        assertEquals("A", s2.getOriginalKey());
    }

    @Test
    public void testParseSingleJsonObject() throws Exception {
        String json = "{\n" +
                "  \"title\": \"Great Are You Lord\",\n" +
                "  \"artist\": \"All Sons & Daughters\",\n" +
                "  \"originalKey\": \"G\",\n" +
                "  \"content\": \"[Verse]\\nC        Em        D\\nYou give life, You are love\"\n" +
                "}";

        List<Song> songs = SongJsonHelper.parseJsonToSongs(json);
        assertNotNull(songs);
        assertEquals(1, songs.size());
        assertEquals("Great Are You Lord", songs.get(0).getTitle());
    }

    @Test
    public void testSongsToJsonConversion() throws Exception {
        List<Song> original = new ArrayList<>();
        original.add(new Song("Song 1", "Artist 1", "C", "Hymn", "Chords 1"));
        original.add(new Song("Song 2", "Artist 2", "D", "Worship", "Chords 2"));

        String json = SongJsonHelper.songsToJson(original);
        assertNotNull(json);
        assertTrue(json.contains("Song 1"));
        assertTrue(json.contains("Song 2"));

        List<Song> parsedBack = SongJsonHelper.parseJsonToSongs(json);
        assertEquals(2, parsedBack.size());
        assertEquals("Song 1", parsedBack.get(0).getTitle());
        assertEquals("Song 2", parsedBack.get(1).getTitle());
    }
}
