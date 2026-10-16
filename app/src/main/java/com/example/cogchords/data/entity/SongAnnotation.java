package com.example.cogchords.data.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

/**
 * The doodles (pen / marker strokes) a user drew over one song. One row per song;
 * deleted automatically when the song is deleted.
 */
@Entity(
        tableName = "song_annotations",
        foreignKeys = @ForeignKey(
                entity = Song.class,
                parentColumns = "id",
                childColumns = "songId",
                onDelete = ForeignKey.CASCADE
        )
)
public class SongAnnotation {

    @PrimaryKey
    private long songId;

    @NonNull
    private String strokesData = "";

    private long updatedAt;

    public SongAnnotation() {
    }

    @Ignore
    public SongAnnotation(long songId, @NonNull String strokesData) {
        this.songId = songId;
        this.strokesData = strokesData;
        this.updatedAt = System.currentTimeMillis();
    }

    public long getSongId() { return songId; }
    public void setSongId(long songId) { this.songId = songId; }

    @NonNull
    public String getStrokesData() { return strokesData; }
    public void setStrokesData(@NonNull String strokesData) { this.strokesData = strokesData; }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
}
