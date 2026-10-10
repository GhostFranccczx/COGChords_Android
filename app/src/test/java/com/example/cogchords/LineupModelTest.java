package com.example.cogchords;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.example.cogchords.data.entity.Lineup;
import com.example.cogchords.data.entity.LineupSong;
import com.example.cogchords.data.model.LineupSongDetail;

import org.junit.Test;

public class LineupModelTest {

    @Test
    public void testLineupCreationAndProperties() {
        Lineup lineup = new Lineup("Sunday Worship", "September 20, 2026");
        assertEquals("Sunday Worship", lineup.getName());
        assertEquals("September 20, 2026", lineup.getDate());
        assertTrue(lineup.getCreatedAt() > 0);
        assertTrue(lineup.getUpdatedAt() >= lineup.getCreatedAt());

        lineup.setName("Youth Fellowship");
        lineup.setDate("October 5, 2026");
        assertEquals("Youth Fellowship", lineup.getName());
        assertEquals("October 5, 2026", lineup.getDate());
    }

    @Test
    public void testLineupSongOrdering() {
        LineupSong song1 = new LineupSong(1, 101, 1);
        LineupSong song2 = new LineupSong(1, 102, 2);

        assertEquals(1, song1.getPosition());
        assertEquals(2, song2.getPosition());

        // Test reordering
        song1.setPosition(2);
        song2.setPosition(1);
        assertEquals(2, song1.getPosition());
        assertEquals(1, song2.getPosition());
    }

    @Test
    public void testLineupSongDetailJoinedProperties() {
        LineupSongDetail detail = new LineupSongDetail();
        detail.setLineupId(1);
        detail.setSongId(101);
        detail.setPosition(1);
        detail.setTitle("10,000 Reasons");
        detail.setOriginalKey("G");
        detail.setArtist("Matt Redman");

        assertEquals("10,000 Reasons", detail.getTitle());
        assertEquals("G", detail.getOriginalKey());
        assertEquals(1, detail.getPosition());
    }
}
