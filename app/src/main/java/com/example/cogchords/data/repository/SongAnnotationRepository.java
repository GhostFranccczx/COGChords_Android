package com.example.cogchords.data.repository;

import android.content.Context;

import com.example.cogchords.data.AppDatabase;
import com.example.cogchords.data.dao.SongAnnotationDao;
import com.example.cogchords.data.entity.SongAnnotation;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SongAnnotationRepository {

    public interface Callback<T> {
        void onResult(T value);
    }

    // One thread, so a save is always finished before the next load of the same song
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();

    private final SongAnnotationDao dao;

    public SongAnnotationRepository(Context context) {
        dao = AppDatabase.getDatabase(context).songAnnotationDao();
    }

    /** Loads the saved strokes of a song ("" if there are none). The callback runs on a background thread. */
    public void load(long songId, Callback<String> callback) {
        EXECUTOR.execute(() -> {
            String data = "";
            try {
                SongAnnotation annotation = dao.getForSongSync(songId);
                if (annotation != null) data = annotation.getStrokesData();
            } catch (RuntimeException ignored) {
                // show the song without doodles rather than crash
            }
            callback.onResult(data);
        });
    }

    /** Saves the strokes of a song; empty data removes the saved doodles. */
    public void save(long songId, String data) {
        EXECUTOR.execute(() -> {
            try {
                if (data == null || data.isEmpty()) {
                    dao.deleteForSong(songId);
                } else {
                    dao.upsert(new SongAnnotation(songId, data));
                }
            } catch (RuntimeException ignored) {
                // e.g. the song was deleted while the screen was open
            }
        });
    }
}
