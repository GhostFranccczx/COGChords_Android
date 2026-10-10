package com.example.cogchords.ui.songs;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cogchords.R;
import com.example.cogchords.data.entity.Song;
import com.example.cogchords.ui.settings.SettingsActivity;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class SongsFragment extends Fragment implements SongAdapter.OnSongClickListener {

    private SongViewModel songViewModel;
    private SongAdapter songAdapter;
    private RecyclerView rvSongs;
    private View layoutEmptyState;
    private EditText etSearchSongs;

    public SongsFragment() {
        // Required empty public constructor
    }

    public static SongsFragment newInstance() {
        return new SongsFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_songs, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvSongs = view.findViewById(R.id.rv_songs);
        layoutEmptyState = view.findViewById(R.id.layout_empty_state);
        etSearchSongs = view.findViewById(R.id.et_search_songs);
        FloatingActionButton fabAddSong = view.findViewById(R.id.fab_add_song);
        ImageView btnSettings = view.findViewById(R.id.btn_settings);

        setupRecyclerView();
        setupViewModel();
        setupSearch();

        if (fabAddSong != null) {
            fabAddSong.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), AddEditSongActivity.class);
                startActivity(intent);
            });
        }

        View ivHeaderLogo = view.findViewById(R.id.iv_header_logo);
        if (ivHeaderLogo != null) {
            ivHeaderLogo.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), SettingsActivity.class);
                startActivity(intent);
            });
        }

        if (btnSettings != null) {
            btnSettings.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), SettingsActivity.class);
                startActivity(intent);
            });
        }
    }

    private void setupRecyclerView() {
        songAdapter = new SongAdapter(this);
        rvSongs.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvSongs.setAdapter(songAdapter);
    }

    private void setupViewModel() {
        songViewModel = new ViewModelProvider(this).get(SongViewModel.class);
        songViewModel.getSongs().observe(getViewLifecycleOwner(), songs -> {
            if (songs == null || songs.isEmpty()) {
                layoutEmptyState.setVisibility(View.VISIBLE);
                rvSongs.setVisibility(View.GONE);
            } else {
                layoutEmptyState.setVisibility(View.GONE);
                rvSongs.setVisibility(View.VISIBLE);
            }
            songAdapter.setSongs(songs);
        });
    }

    private void setupSearch() {
        if (etSearchSongs != null) {
            etSearchSongs.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    songViewModel.setSearchQuery(s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }
    }

    @Override
    public void onSongClick(Song song) {
        Intent intent = new Intent(requireContext(), SongDetailActivity.class);
        intent.putExtra(SongDetailActivity.EXTRA_SONG_ID, song.getId());
        startActivity(intent);
    }

    @Override
    public void onSongEdit(Song song) {
        Intent intent = new Intent(requireContext(), AddEditSongActivity.class);
        intent.putExtra(AddEditSongActivity.EXTRA_SONG_ID, song.getId());
        startActivity(intent);
    }

    @Override
    public void onSongDelete(Song song) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Delete Song")
                .setMessage("Are you sure you want to delete \"" + song.getTitle() + "\"? This cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    songViewModel.delete(song, () -> {
                        if (isAdded()) {
                            requireActivity().runOnUiThread(() ->
                                    Toast.makeText(requireContext(), "Song deleted", Toast.LENGTH_SHORT).show()
                            );
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
