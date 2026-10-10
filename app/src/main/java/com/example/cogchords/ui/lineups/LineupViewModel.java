package com.example.cogchords.ui.lineups;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.cogchords.data.entity.Lineup;
import com.example.cogchords.data.entity.LineupSong;
import com.example.cogchords.data.model.LineupSongDetail;
import com.example.cogchords.data.repository.LineupRepository;

import java.util.List;

public class LineupViewModel extends AndroidViewModel {

    private final LineupRepository repository;
    private final LiveData<List<Lineup>> allLineups;

    public LineupViewModel(@NonNull Application application) {
        super(application);
        repository = new LineupRepository(application);
        allLineups = repository.getAllLineups();
    }

    public LiveData<List<Lineup>> getAllLineups() {
        return allLineups;
    }

    public LiveData<Lineup> getLineupById(long id) {
        return repository.getLineupById(id);
    }

    public LiveData<List<LineupSongDetail>> getSongsForLineup(long lineupId) {
        return repository.getSongsForLineup(lineupId);
    }

    public void insertLineup(Lineup lineup, List<LineupSong> songs, LineupRepository.OnLineupSavedCallback callback) {
        repository.insertLineupWithSongs(lineup, songs, callback);
    }

    public void updateLineup(Lineup lineup, List<LineupSong> songs, LineupRepository.OnCompleteCallback callback) {
        repository.updateLineupWithSongs(lineup, songs, callback);
    }

    public void deleteLineup(Lineup lineup, LineupRepository.OnCompleteCallback callback) {
        repository.delete(lineup, callback);
    }
}
