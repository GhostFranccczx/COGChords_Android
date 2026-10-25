package com.example.cogchords.ui.lineups;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cogchords.R;
import com.example.cogchords.data.entity.Lineup;
import com.example.cogchords.data.entity.LineupSong;
import com.example.cogchords.data.entity.Song;
import com.example.cogchords.data.model.LineupSongDetail;
import com.example.cogchords.ui.songs.SongViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.Locale;

public class AddEditLineupActivity extends AppCompatActivity implements LineupSongEditAdapter.OnSongOrderChangeListener {

    public static final String EXTRA_LINEUP_ID = "extra_lineup_id";

    private EditText etLineupName;
    private TextView tvLineupDate;
    private TextView tvScreenTitle;
    private TextView tvSongCountHeader;
    private MaterialButton btnSave;
    private MaterialButton btnAddSongsFromLibrary;
    private ImageView btnBack;
    private RecyclerView rvLineupSongsEdit;
    private View layoutNoSongsInLineup;

    private LineupViewModel lineupViewModel;
    private SongViewModel songViewModel;
    private LineupSongEditAdapter editAdapter;

    private long lineupId = -1;
    private Lineup existingLineup = null;
    private String selectedDate = "";
    private boolean isEditDataLoaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        com.example.cogchords.util.ThemeManager.applyTheme(this);
        super.onCreate(savedInstanceState);
        com.example.cogchords.util.ThemeManager.enableEdgeToEdge(this);
        setContentView(R.layout.activity_add_edit_lineup);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        setupRecyclerView();
        setupViewModels();

        if (getIntent().hasExtra(EXTRA_LINEUP_ID)) {
            lineupId = getIntent().getLongExtra(EXTRA_LINEUP_ID, -1);
        }

        if (lineupId != -1) {
            tvScreenTitle.setText("EDIT LINEUP");
            loadLineupData();
        } else {
            tvScreenTitle.setText("NEW LINEUP");
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());
            selectedDate = sdf.format(calendar.getTime());
            tvLineupDate.setText(selectedDate);
            updateEmptyState();
        }

        setupListeners();
    }

    private void initViews() {
        etLineupName = findViewById(R.id.et_lineup_name);
        tvLineupDate = findViewById(R.id.tv_lineup_date);
        tvScreenTitle = findViewById(R.id.tv_screen_title);
        tvSongCountHeader = findViewById(R.id.tv_song_count_header);
        btnSave = findViewById(R.id.btn_save);
        btnAddSongsFromLibrary = findViewById(R.id.btn_add_songs_from_library);
        btnBack = findViewById(R.id.btn_back);
        rvLineupSongsEdit = findViewById(R.id.rv_lineup_songs_edit);
        layoutNoSongsInLineup = findViewById(R.id.layout_no_songs_in_lineup);
    }

    private void setupRecyclerView() {
        editAdapter = new LineupSongEditAdapter(this);
        rvLineupSongsEdit.setLayoutManager(new LinearLayoutManager(this));
        rvLineupSongsEdit.setAdapter(editAdapter);
    }

    private void setupViewModels() {
        lineupViewModel = new ViewModelProvider(this).get(LineupViewModel.class);
        songViewModel = new ViewModelProvider(this).get(SongViewModel.class);
    }

    private void loadLineupData() {
        lineupViewModel.getLineupById(lineupId).observe(this, lineup -> {
            if (lineup != null && existingLineup == null) {
                existingLineup = lineup;
                etLineupName.setText(lineup.getName());
                selectedDate = lineup.getDate() != null ? lineup.getDate() : "";
                tvLineupDate.setText(selectedDate);
            }
        });

        lineupViewModel.getSongsForLineup(lineupId).observe(this, songs -> {
            if (songs != null && !isEditDataLoaded) {
                isEditDataLoaded = true;
                editAdapter.setSongs(songs);
                updateSongCountHeader();
                updateEmptyState();
            }
        });
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> saveLineup());

        findViewById(R.id.layout_date_picker).setOnClickListener(v -> showDatePicker());

        btnAddSongsFromLibrary.setOnClickListener(v -> showSongPickerDialog());
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            Calendar selected = Calendar.getInstance();
            selected.set(year, month, dayOfMonth);
            SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());
            selectedDate = sdf.format(selected.getTime());
            tvLineupDate.setText(selectedDate);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    private void showSongPickerDialog() {
        songViewModel.getSongs().observe(this, songs -> {
            if (songs == null || songs.isEmpty()) {
                Toast.makeText(this, "No songs in library. Add songs first.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Remove observer after first emission to prevent repeated dialogs
            songViewModel.getSongs().removeObservers(this);

            View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_song_picker, null);
            EditText etSearch = dialogView.findViewById(R.id.et_picker_search);
            ImageView btnClear = dialogView.findViewById(R.id.btn_picker_clear);
            ScrollView scroll = dialogView.findViewById(R.id.picker_scroll);
            LinearLayout songListContainer = dialogView.findViewById(R.id.song_list_container);
            TextView tvEmpty = dialogView.findViewById(R.id.tv_picker_empty);

            // Songs already in this lineup: shown greyed out and cannot be picked again
            Set<Long> alreadyAdded = new HashSet<>();
            for (LineupSongDetail added : editAdapter.getSongs()) {
                alreadyAdded.add(added.getSongId());
            }

            // Ticked songs are tracked by id, so ticks survive while the list is filtered.
            // Insertion order = the order the songs were ticked.
            Map<Long, Song> selectedSongs = new LinkedHashMap<>();

            AlertDialog[] dialogRef = new AlertDialog[1];

            Runnable updateAddButton = () -> {
                if (dialogRef[0] == null) return;
                Button add = dialogRef[0].getButton(AlertDialog.BUTTON_POSITIVE);
                if (add != null) {
                    add.setText(selectedSongs.isEmpty()
                            ? "Add Selected"
                            : "Add Selected (" + selectedSongs.size() + ")");
                }
            };

            Runnable renderList = () -> {
                String rawQuery = etSearch.getText().toString().trim();
                String query = rawQuery.toLowerCase(Locale.getDefault());

                songListContainer.removeAllViews();
                int shown = 0;

                for (Song song : songs) {
                    String title = song.getTitle() != null ? song.getTitle() : "";
                    String artist = song.getArtist() != null ? song.getArtist() : "";

                    // Search matches title or artist (same as the Songs tab)
                    if (!query.isEmpty()
                            && !title.toLowerCase(Locale.getDefault()).contains(query)
                            && !artist.toLowerCase(Locale.getDefault()).contains(query)) {
                        continue;
                    }
                    shown++;

                    View itemView = LayoutInflater.from(this).inflate(R.layout.item_song_picker, songListContainer, false);
                    TextView tvTitle = itemView.findViewById(R.id.tv_picker_title);
                    TextView tvArtistKey = itemView.findViewById(R.id.tv_picker_artist_key);
                    CheckBox cbSelect = itemView.findViewById(R.id.cb_song_select);

                    tvTitle.setText(song.getTitle());
                    String subtitle = "";
                    if (!artist.isEmpty()) {
                        subtitle = artist;
                    }
                    if (song.getOriginalKey() != null && !song.getOriginalKey().isEmpty()) {
                        subtitle += (subtitle.isEmpty() ? "" : " • ") + "Key: " + song.getOriginalKey();
                    }

                    if (alreadyAdded.contains(song.getId())) {
                        // Already in the lineup: greyed out, ticked, not clickable
                        tvArtistKey.setText(subtitle.isEmpty() ? "Already added" : "Already added • " + subtitle);
                        cbSelect.setChecked(true);
                        cbSelect.setEnabled(false);
                        itemView.setClickable(false);
                        itemView.setFocusable(false);
                        itemView.setAlpha(0.45f);
                    } else {
                        tvArtistKey.setText(subtitle);
                        cbSelect.setChecked(selectedSongs.containsKey(song.getId()));
                        itemView.setOnClickListener(iv -> {
                            if (selectedSongs.containsKey(song.getId())) {
                                selectedSongs.remove(song.getId());
                                cbSelect.setChecked(false);
                            } else {
                                selectedSongs.put(song.getId(), song);
                                cbSelect.setChecked(true);
                            }
                            updateAddButton.run();
                        });
                    }

                    songListContainer.addView(itemView);
                }

                if (shown == 0) {
                    tvEmpty.setText(rawQuery.isEmpty() ? "No songs" : "No songs match \"" + rawQuery + "\"");
                    tvEmpty.setVisibility(View.VISIBLE);
                } else {
                    tvEmpty.setVisibility(View.GONE);
                }
                scroll.scrollTo(0, 0);
            };

            etSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) { }

                @Override
                public void afterTextChanged(Editable s) {
                    btnClear.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
                    renderList.run();
                }
            });
            btnClear.setOnClickListener(v -> etSearch.setText(""));
            etSearch.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    if (imm != null) imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    return true;
                }
                return false;
            });

            renderList.run();

            dialogRef[0] = new MaterialAlertDialogBuilder(this)
                    .setTitle("Select Songs")
                    .setView(dialogView)
                    .setPositiveButton("Add Selected", (dialog, which) -> {
                        for (Song song : selectedSongs.values()) {
                            LineupSongDetail detail = new LineupSongDetail();
                            detail.setSongId(song.getId());
                            detail.setTitle(song.getTitle());
                            detail.setArtist(song.getArtist());
                            detail.setOriginalKey(song.getOriginalKey());
                            detail.setCategory(song.getCategory());
                            detail.setContent(song.getContent());
                            editAdapter.addSong(detail);
                        }
                        updateSongCountHeader();
                        updateEmptyState();
                    })
                    .setNegativeButton("Cancel", null)
                    .create();

            if (dialogRef[0].getWindow() != null) {
                dialogRef[0].getWindow().setSoftInputMode(
                        android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
            }
            dialogRef[0].show();
            updateAddButton.run();
        });
    }

    private void saveLineup() {
        String name = etLineupName.getText().toString().trim();

        if (name.isEmpty()) {
            etLineupName.setError("Name is required");
            etLineupName.requestFocus();
            return;
        }

        if (selectedDate.isEmpty()) {
            Toast.makeText(this, "Please select a date", Toast.LENGTH_SHORT).show();
            return;
        }

        List<LineupSongDetail> currentSongs = editAdapter.getSongs();
        List<LineupSong> lineupSongs = new ArrayList<>();
        for (int i = 0; i < currentSongs.size(); i++) {
            LineupSongDetail detail = currentSongs.get(i);
            lineupSongs.add(new LineupSong(0, detail.getSongId(), i + 1));
        }

        if (lineupId == -1) {
            Lineup newLineup = new Lineup(name, selectedDate);
            lineupViewModel.insertLineup(newLineup, lineupSongs, newId -> {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Lineup saved", Toast.LENGTH_SHORT).show();
                    finish();
                });
            });
        } else {
            if (existingLineup != null) {
                existingLineup.setName(name);
                existingLineup.setDate(selectedDate);
                existingLineup.setUpdatedAt(System.currentTimeMillis());
                lineupViewModel.updateLineup(existingLineup, lineupSongs, () -> {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Lineup updated", Toast.LENGTH_SHORT).show();
                        finish();
                    });
                });
            }
        }
    }

    private void updateSongCountHeader() {
        int count = editAdapter.getItemCount();
        tvSongCountHeader.setText("SONGS IN LINEUP (" + count + ")");
    }

    private void updateEmptyState() {
        if (editAdapter.getItemCount() == 0) {
            layoutNoSongsInLineup.setVisibility(View.VISIBLE);
            rvLineupSongsEdit.setVisibility(View.GONE);
        } else {
            layoutNoSongsInLineup.setVisibility(View.GONE);
            rvLineupSongsEdit.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onSongRemoved(int position) {
        updateSongCountHeader();
        updateEmptyState();
    }
}
