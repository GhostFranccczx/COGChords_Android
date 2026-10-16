package com.example.cogchords.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.cogchords.data.entity.SongAnnotation;

@Dao
public interface SongAnnotationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsert(SongAnnotation annotation);

    @Query("SELECT * FROM song_annotations WHERE songId = :songId LIMIT 1")
    SongAnnotation getForSongSync(long songId);

    @Query("DELETE FROM song_annotations WHERE songId = :songId")
    void deleteForSong(long songId);
}
