package com.example.cogchords.ui.songs;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.cogchords.data.entity.Song;
import com.example.cogchords.data.repository.SongRepository;

import java.util.List;

public class SongViewModel extends AndroidViewModel {

    private final SongRepository repository;
    private final MutableLiveData<String> searchQuery = new MutableLiveData<>("");
    private final LiveData<List<Song>> allSongs;

    public SongViewModel(@NonNull Application application) {
        super(application);
        repository = new SongRepository(application);
        allSongs = Transformations.switchMap(searchQuery, query -> {
            if (query == null || query.trim().isEmpty()) {
                return repository.getAllSongs();
            } else {
                return repository.searchSongs(query);
            }
        });
    }

    public LiveData<List<Song>> getSongs() {
        return allSongs;
    }

    public LiveData<Song> getSongById(long id) {
        return repository.getSongById(id);
    }

    public void setSearchQuery(String query) {
        searchQuery.setValue(query);
    }

    public void insert(Song song, SongRepository.OnSongInsertedCallback callback) {
        repository.insert(song, callback);
    }

    public void update(Song song, SongRepository.OnCompleteCallback callback) {
        repository.update(song, callback);
    }

    public void delete(Song song, SongRepository.OnCompleteCallback callback) {
        repository.delete(song, callback);
    }

    public void deleteById(long id, SongRepository.OnCompleteCallback callback) {
        repository.deleteById(id, callback);
    }
}
