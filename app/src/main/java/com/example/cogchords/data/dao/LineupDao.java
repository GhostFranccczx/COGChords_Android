package com.example.cogchords.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.example.cogchords.data.entity.Lineup;
import com.example.cogchords.data.entity.LineupSong;
import com.example.cogchords.data.model.LineupSongDetail;

import java.util.List;

@Dao
public interface LineupDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Lineup lineup);

    @Update
    void update(Lineup lineup);

    @Delete
    void delete(Lineup lineup);

    @Query("SELECT * FROM lineups ORDER BY createdAt DESC")
    LiveData<List<Lineup>> getAllLineups();

    @Query("SELECT * FROM lineups WHERE id = :id LIMIT 1")
    LiveData<Lineup> getLineupById(long id);

    @Query("SELECT * FROM lineups WHERE id = :id LIMIT 1")
    Lineup getLineupByIdSync(long id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertLineupSongs(List<LineupSong> lineupSongs);

    @Query("DELETE FROM lineup_songs WHERE lineupId = :lineupId")
    void deleteSongsForLineup(long lineupId);

    @Query("SELECT ls.id AS lineupSongId, ls.lineupId, ls.songId, ls.position, " +
            "s.title, s.artist, s.originalKey, s.category, s.content " +
            "FROM lineup_songs ls " +
            "INNER JOIN songs s ON ls.songId = s.id " +
            "WHERE ls.lineupId = :lineupId " +
            "ORDER BY ls.position ASC")
    LiveData<List<LineupSongDetail>> getSongsForLineup(long lineupId);

    @Query("SELECT COUNT(*) FROM lineup_songs WHERE lineupId = :lineupId")
    LiveData<Integer> getSongCountForLineup(long lineupId);

    @Transaction
    default void updateLineupWithSongs(Lineup lineup, List<LineupSong> songs) {
        update(lineup);
        deleteSongsForLineup(lineup.getId());
        if (songs != null && !songs.isEmpty()) {
            for (int i = 0; i < songs.size(); i++) {
                songs.get(i).setPosition(i + 1);
                songs.get(i).setLineupId(lineup.getId());
            }
            insertLineupSongs(songs);
        }
    }

    @Transaction
    default long insertLineupWithSongs(Lineup lineup, List<LineupSong> songs) {
        long newId = insert(lineup);
        if (songs != null && !songs.isEmpty()) {
            for (int i = 0; i < songs.size(); i++) {
                songs.get(i).setPosition(i + 1);
                songs.get(i).setLineupId(newId);
            }
            insertLineupSongs(songs);
        }
        return newId;
    }
}
