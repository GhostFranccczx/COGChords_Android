package com.example.cogchords.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.cogchords.data.entity.Song;

import java.util.List;

@Dao
public interface SongDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Song song);

    @Update
    void update(Song song);

    @Delete
    void delete(Song song);

    @Query("DELETE FROM songs WHERE id = :id")
    void deleteById(long id);

    @Query("SELECT * FROM songs ORDER BY title COLLATE NOCASE ASC")
    LiveData<List<Song>> getAllSongs();

    @Query("SELECT * FROM songs WHERE title LIKE '%' || :query || '%' OR artist LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%' ORDER BY title COLLATE NOCASE ASC")
    LiveData<List<Song>> searchSongs(String query);

    @Query("SELECT * FROM songs WHERE id = :id LIMIT 1")
    LiveData<Song> getSongById(long id);

    @Query("SELECT * FROM songs WHERE id = :id LIMIT 1")
    Song getSongByIdSync(long id);

    @Query("SELECT COUNT(*) FROM songs")
    int getSongCountSync();

    @Query("SELECT * FROM songs WHERE LOWER(title) = LOWER(:title) LIMIT 1")
    Song getSongByTitleSync(String title);

    @Query("SELECT * FROM songs ORDER BY title COLLATE NOCASE ASC")
    List<Song> getAllSongsSync();
}
