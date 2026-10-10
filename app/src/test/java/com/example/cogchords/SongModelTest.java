package com.example.cogchords;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.example.cogchords.data.entity.Song;

import org.junit.Test;

public class SongModelTest {

    @Test
    public void testSongCreationAndProperties() {
        Song song = new Song("Amazing Grace", "John Newton", "G", "Hymn", "[G] Amazing grace");
        assertEquals("Amazing Grace", song.getTitle());
        assertEquals("John Newton", song.getArtist());
        assertEquals("G", song.getOriginalKey());
        assertEquals("Hymn", song.getCategory());
        assertEquals("[G] Amazing grace", song.getContent());
        assertTrue(song.getCreatedAt() > 0);
        assertTrue(song.getUpdatedAt() >= song.getCreatedAt());

        song.setTitle("How Great Thou Art");
        song.setOriginalKey("A");
        assertEquals("How Great Thou Art", song.getTitle());
        assertEquals("A", song.getOriginalKey());
    }
}
