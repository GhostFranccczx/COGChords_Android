package com.example.cogchords.data.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "lineup_songs",
        foreignKeys = {
                @ForeignKey(
                        entity = Lineup.class,
                        parentColumns = "id",
                        childColumns = "lineupId",
                        onDelete = ForeignKey.CASCADE
                ),
                @ForeignKey(
                        entity = Song.class,
                        parentColumns = "id",
                        childColumns = "songId",
                        onDelete = ForeignKey.CASCADE
                )
        },
        indices = {
                @Index("lineupId"),
                @Index("songId")
        }
)
public class LineupSong {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private long lineupId;
    private long songId;
    private int position;

    public LineupSong() {
    }

    @Ignore
    public LineupSong(long lineupId, long songId, int position) {
        this.lineupId = lineupId;
        this.songId = songId;
        this.position = position;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getLineupId() { return lineupId; }
    public void setLineupId(long lineupId) { this.lineupId = lineupId; }

    public long getSongId() { return songId; }
    public void setSongId(long songId) { this.songId = songId; }

    public int getPosition() { return position; }
    public void setPosition(int position) { this.position = position; }
}
