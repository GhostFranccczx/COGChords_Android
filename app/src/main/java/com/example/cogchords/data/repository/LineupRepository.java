package com.example.cogchords.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.cogchords.data.AppDatabase;
import com.example.cogchords.data.dao.LineupDao;
import com.example.cogchords.data.entity.Lineup;
import com.example.cogchords.data.entity.LineupSong;
import com.example.cogchords.data.model.LineupSongDetail;

import java.util.List;

public class LineupRepository {

    private final LineupDao lineupDao;

    public interface OnCompleteCallback {
        void onComplete();
    }

    public interface OnLineupSavedCallback {
        void onSaved(long lineupId);
    }

    public LineupRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        lineupDao = db.lineupDao();
    }

    public LiveData<List<Lineup>> getAllLineups() {
        return lineupDao.getAllLineups();
    }

    public LiveData<Lineup> getLineupById(long id) {
        return lineupDao.getLineupById(id);
    }

    public LiveData<List<LineupSongDetail>> getSongsForLineup(long lineupId) {
        return lineupDao.getSongsForLineup(lineupId);
    }

    public LiveData<Integer> getSongCountForLineup(long lineupId) {
        return lineupDao.getSongCountForLineup(lineupId);
    }

    public void insertLineupWithSongs(Lineup lineup, List<LineupSong> songs, OnLineupSavedCallback callback) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            long newId = lineupDao.insertLineupWithSongs(lineup, songs);
            if (callback != null) {
                callback.onSaved(newId);
            }
        });
    }

    public void updateLineupWithSongs(Lineup lineup, List<LineupSong> songs, OnCompleteCallback callback) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            lineupDao.updateLineupWithSongs(lineup, songs);
            if (callback != null) {
                callback.onComplete();
            }
        });
    }

    public void delete(Lineup lineup, OnCompleteCallback callback) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            lineupDao.delete(lineup);
            if (callback != null) {
                callback.onComplete();
            }
        });
    }

    public void deleteById(long id, OnCompleteCallback callback) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            Lineup lineup = lineupDao.getLineupByIdSync(id);
            if (lineup != null) {
                lineupDao.delete(lineup);
            }
            if (callback != null) {
                callback.onComplete();
            }
        });
    }
}
