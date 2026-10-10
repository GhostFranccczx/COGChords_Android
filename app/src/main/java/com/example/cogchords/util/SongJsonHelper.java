package com.example.cogchords.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import androidx.core.content.FileProvider;

import com.example.cogchords.data.AppDatabase;
import com.example.cogchords.data.dao.SongDao;
import com.example.cogchords.data.entity.Song;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class SongJsonHelper {

    public static class ImportResult {
        public int added = 0;
        public int updated = 0;
        public int total = 0;
        public boolean success = false;
        public String errorMessage = null;
    }

    public interface OnImportFinishedListener {
        void onFinished(ImportResult result);
    }

    public static List<Song> parseJsonToSongs(String jsonString) throws Exception {
        List<Song> songs = new ArrayList<>();
        String trimmed = jsonString.trim();

        if (trimmed.startsWith("[")) {
            JSONArray array = new JSONArray(trimmed);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                Song song = parseSingleSong(obj);
                if (song != null) {
                    songs.add(song);
                }
            }
        } else if (trimmed.startsWith("{")) {
            JSONObject obj = new JSONObject(trimmed);
            Song song = parseSingleSong(obj);
            if (song != null) {
                songs.add(song);
            }
        }

        return songs;
    }

    private static Song parseSingleSong(JSONObject obj) {
        String title = obj.optString("title", "").trim();
        if (title.isEmpty()) return null;

        String artist = obj.optString("artist", "").trim();
        String originalKey = obj.optString("originalKey", "").trim();
        if (originalKey.isEmpty()) {
            originalKey = obj.optString("key", "").trim();
        }
        String category = obj.optString("category", "").trim();
        String content = obj.optString("content", "");
        if (content.isEmpty()) {
            content = obj.optString("chords", "");
        }

        return new Song(title, artist, originalKey, category, content);
    }

    public static void importSongsFromUri(Context context, Uri uri, OnImportFinishedListener listener) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            ImportResult result = new ImportResult();
            try {
                InputStream is = context.getContentResolver().openInputStream(uri);
                if (is == null) {
                    result.errorMessage = "Could not open selected file";
                    postResult(result, listener);
                    return;
                }

                BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
                reader.close();
                is.close();

                List<Song> parsedSongs = parseJsonToSongs(sb.toString());
                result.total = parsedSongs.size();

                SongDao dao = AppDatabase.getDatabase(context).songDao();
                for (Song song : parsedSongs) {
                    Song existing = dao.getSongByTitleSync(song.getTitle());
                    if (existing != null) {
                        existing.setArtist(song.getArtist());
                        existing.setOriginalKey(song.getOriginalKey());
                        existing.setCategory(song.getCategory());
                        existing.setContent(song.getContent());
                        existing.setUpdatedAt(System.currentTimeMillis());
                        dao.update(existing);
                        result.updated++;
                    } else {
                        dao.insert(song);
                        result.added++;
                    }
                }

                result.success = true;
            } catch (Exception e) {
                result.success = false;
                result.errorMessage = e.getMessage() != null ? e.getMessage() : "Invalid JSON format";
            }
            postResult(result, listener);
        });
    }

    private static void postResult(ImportResult result, OnImportFinishedListener listener) {
        if (listener != null) {
            new Handler(Looper.getMainLooper()).post(() -> listener.onFinished(result));
        }
    }

    public static String songsToJson(List<Song> songs) {
        JSONArray array = new JSONArray();
        for (Song song : songs) {
            try {
                JSONObject obj = new JSONObject();
                obj.put("title", song.getTitle());
                obj.put("artist", song.getArtist() != null ? song.getArtist() : "");
                obj.put("originalKey", song.getOriginalKey() != null ? song.getOriginalKey() : "");
                obj.put("category", song.getCategory() != null ? song.getCategory() : "");
                obj.put("content", song.getContent() != null ? song.getContent() : "");
                array.put(obj);
            } catch (Exception ignored) {}
        }
        try {
            return array.toString(2);
        } catch (Exception e) {
            return array.toString();
        }
    }

    public static void exportSongs(Context context) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                SongDao dao = AppDatabase.getDatabase(context).songDao();
                List<Song> songs = dao.getAllSongsSync();
                String json = songsToJson(songs);

                File cachePath = new File(context.getCacheDir(), "exports");
                cachePath.mkdirs();
                File file = new File(cachePath, "cogchords_songs.json");
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(json.getBytes(StandardCharsets.UTF_8));
                fos.close();

                Uri contentUri = FileProvider.getUriForFile(
                        context,
                        context.getPackageName() + ".fileprovider",
                        file
                );

                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("application/json");
                shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "COGChords Song Library Export");
                shareIntent.putExtra(Intent.EXTRA_TEXT, "Here is the COGChords song library JSON file containing " + songs.size() + " songs.");
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                Intent chooser = Intent.createChooser(shareIntent, "Share / Export Songs JSON");
                chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(chooser);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
