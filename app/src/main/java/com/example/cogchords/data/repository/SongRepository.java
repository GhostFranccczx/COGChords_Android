package com.example.cogchords.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.cogchords.data.AppDatabase;
import com.example.cogchords.data.dao.SongDao;
import com.example.cogchords.data.entity.Song;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class SongRepository {

    private final SongDao songDao;

    public interface OnCompleteCallback {
        void onComplete();
    }

    public interface OnSongInsertedCallback {
        void onInserted(long newSongId);
    }

    public SongRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        songDao = db.songDao();

        // Ensure starter songs exist if library is empty
        AppDatabase.databaseWriteExecutor.execute(() -> {
            if (songDao.getSongCountSync() == 0) {
                loadSongsFromAssets(application);
            }
        });
    }

    private void loadSongsFromAssets(Application application) {
        try {
            InputStream is = application.getAssets().open("songs.json");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            reader.close();

            JSONArray jsonArray = new JSONArray(sb.toString());
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                String title = obj.optString("title", "").trim();
                String artist = obj.optString("artist", "").trim();
                String originalKey = obj.optString("originalKey", "").trim();
                String category = obj.optString("category", "").trim();
                String content = obj.optString("content", "");

                if (!title.isEmpty()) {
                    songDao.insert(new Song(title, artist, originalKey, category, content));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Fallback to hardcoded basic songs if asset fails
            songDao.insert(new Song(
                    "Amazing Grace",
                    "John Newton",
                    "G",
                    "Hymn",
                    "[Verse 1]\nG             C         G\nAmazing grace how sweet the sound\n               D\nThat saved a wretch like me\n   G          C          G\nI once was lost, but now I'm found\n     Em       D      G\nWas blind, but now I see"
            ));
        }
    }

    public LiveData<List<Song>> getAllSongs() {
        return songDao.getAllSongs();
    }

    public LiveData<List<Song>> searchSongs(String query) {
        if (query == null || query.trim().isEmpty()) {
            return songDao.getAllSongs();
        }
        return songDao.searchSongs(query.trim());
    }

    public LiveData<Song> getSongById(long id) {
        return songDao.getSongById(id);
    }

    public void insert(Song song, OnSongInsertedCallback callback) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            long newId = songDao.insert(song);
            if (callback != null) {
                callback.onInserted(newId);
            }
        });
    }

    public void update(Song song, OnCompleteCallback callback) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            songDao.update(song);
            if (callback != null) {
                callback.onComplete();
            }
        });
    }

    public void delete(Song song, OnCompleteCallback callback) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            songDao.delete(song);
            if (callback != null) {
                callback.onComplete();
            }
        });
    }

    public void deleteById(long id, OnCompleteCallback callback) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            songDao.deleteById(id);
            if (callback != null) {
                callback.onComplete();
            }
        });
    }
}
