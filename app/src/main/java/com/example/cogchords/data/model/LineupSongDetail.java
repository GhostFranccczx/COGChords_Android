package com.example.cogchords.data.model;

public class LineupSongDetail {

    private long lineupSongId;
    private long lineupId;
    private long songId;
    private int position;

    // Joined song properties
    private String title;
    private String artist;
    private String originalKey;
    private String category;
    private String content;

    public LineupSongDetail() {
    }

    public long getLineupSongId() { return lineupSongId; }
    public void setLineupSongId(long lineupSongId) { this.lineupSongId = lineupSongId; }

    public long getLineupId() { return lineupId; }
    public void setLineupId(long lineupId) { this.lineupId = lineupId; }

    public long getSongId() { return songId; }
    public void setSongId(long songId) { this.songId = songId; }

    public int getPosition() { return position; }
    public void setPosition(int position) { this.position = position; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getArtist() { return artist; }
    public void setArtist(String artist) { this.artist = artist; }

    public String getOriginalKey() { return originalKey; }
    public void setOriginalKey(String originalKey) { this.originalKey = originalKey; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}
