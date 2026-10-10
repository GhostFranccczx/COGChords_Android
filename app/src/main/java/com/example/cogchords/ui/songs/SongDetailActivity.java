package com.example.cogchords.ui.songs;

import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.widget.NestedScrollView;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;

import com.example.cogchords.R;
import com.example.cogchords.data.entity.Song;
import com.example.cogchords.util.ChordTransposer;
import com.example.cogchords.util.SongFormatter;
import com.google.android.material.button.MaterialButton;

public class SongDetailActivity extends AppCompatActivity {

    public static final String EXTRA_SONG_ID = "extra_song_id";
    public static final String EXTRA_SONG_IDS = "extra_song_ids";
    public static final String EXTRA_CURRENT_INDEX = "extra_current_index";
    public static final String EXTRA_LINEUP_NAME = "extra_lineup_name";

    private TextView tvDetailTitle;
    private TextView tvDetailArtist;
    private TextView tvCurrentKey;
    private TextView tvOriginalKey;
    private TextView tvOffsetBadge;
    private TextView tvChordContent;
    private MaterialButton btnTransposeUp;
    private MaterialButton btnTransposeDown;
    private MaterialButton btnTransposeReset;
    private MaterialButton btnFontIncrease;
    private MaterialButton btnFontDecrease;
    private ImageView btnBack;
    private ImageView btnEdit;

    // Lineup Navigation controls
    private View layoutLineupNav;
    private MaterialButton btnPrevSong;
    private MaterialButton btnNextSong;
    private TextView tvLineupSongPosition;
    private NestedScrollView scrollSongContent;

    private long[] lineupSongIds = null;
    private int currentLineupIndex = -1;
    private String lineupName = null;

    private SongViewModel songViewModel;
    private long songId = -1;
    private Song currentSong;
    private LiveData<Song> currentSongLiveData;

    // Transposition and reading state (in-memory, non-destructive)
    private int semitoneOffset = 0;
    private float currentFontSizeSp = 15f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        com.example.cogchords.util.ThemeManager.applyTheme(this);
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_song_detail);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Keep screen awake while musician is reading chord charts
        getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        initViews();

        songId = getIntent().getLongExtra(EXTRA_SONG_ID, -1);
        if (songId == -1) {
            finish();
            return;
        }

        if (getIntent().hasExtra(EXTRA_SONG_IDS)) {
            lineupSongIds = getIntent().getLongArrayExtra(EXTRA_SONG_IDS);
            currentLineupIndex = getIntent().getIntExtra(EXTRA_CURRENT_INDEX, 0);
            lineupName = getIntent().getStringExtra(EXTRA_LINEUP_NAME);
        }

        setupViewModel();
        setupListeners();
        setupLineupNavigation();
        loadSong(songId);
    }

    private void initViews() {
        tvDetailTitle = findViewById(R.id.tv_detail_title);
        tvDetailArtist = findViewById(R.id.tv_detail_artist);
        tvCurrentKey = findViewById(R.id.tv_current_key);
        tvOriginalKey = findViewById(R.id.tv_original_key);
        tvOffsetBadge = findViewById(R.id.tv_offset_badge);
        tvChordContent = findViewById(R.id.tv_chord_content);

        btnTransposeUp = findViewById(R.id.btn_transpose_up);
        btnTransposeDown = findViewById(R.id.btn_transpose_down);
        btnTransposeReset = findViewById(R.id.btn_transpose_reset);

        btnFontIncrease = findViewById(R.id.btn_font_increase);
        btnFontDecrease = findViewById(R.id.btn_font_decrease);

        btnBack = findViewById(R.id.btn_back);
        btnEdit = findViewById(R.id.btn_edit_song);

        layoutLineupNav = findViewById(R.id.layout_lineup_nav);
        btnPrevSong = findViewById(R.id.btn_prev_song);
        btnNextSong = findViewById(R.id.btn_next_song);
        tvLineupSongPosition = findViewById(R.id.tv_lineup_song_position);
        scrollSongContent = findViewById(R.id.scroll_song_content);
    }

    private void setupViewModel() {
        songViewModel = new ViewModelProvider(this).get(SongViewModel.class);
    }

    private void loadSong(long id) {
        if (currentSongLiveData != null) {
            currentSongLiveData.removeObservers(this);
        }
        currentSongLiveData = songViewModel.getSongById(id);
        currentSongLiveData.observe(this, song -> {
            if (song != null) {
                currentSong = song;
                renderSong();
            }
        });
    }

    private void setupLineupNavigation() {
        if (lineupSongIds != null && lineupSongIds.length > 1) {
            layoutLineupNav.setVisibility(View.VISIBLE);
            updateLineupNavControls();

            btnNextSong.setOnClickListener(v -> {
                if (lineupSongIds != null && currentLineupIndex < lineupSongIds.length - 1) {
                    currentLineupIndex++;
                    songId = lineupSongIds[currentLineupIndex];
                    semitoneOffset = 0;
                    updateLineupNavControls();
                    loadSong(songId);
                    if (scrollSongContent != null) scrollSongContent.smoothScrollTo(0, 0);
                }
            });

            btnPrevSong.setOnClickListener(v -> {
                if (lineupSongIds != null && currentLineupIndex > 0) {
                    currentLineupIndex--;
                    songId = lineupSongIds[currentLineupIndex];
                    semitoneOffset = 0;
                    updateLineupNavControls();
                    loadSong(songId);
                    if (scrollSongContent != null) scrollSongContent.smoothScrollTo(0, 0);
                }
            });
        } else {
            layoutLineupNav.setVisibility(View.GONE);
        }
    }

    private void updateLineupNavControls() {
        if (lineupSongIds == null) return;
        tvLineupSongPosition.setText("Song " + (currentLineupIndex + 1) + " of " + lineupSongIds.length);

        btnPrevSong.setEnabled(currentLineupIndex > 0);
        btnPrevSong.setAlpha(currentLineupIndex > 0 ? 1.0f : 0.35f);

        btnNextSong.setEnabled(currentLineupIndex < lineupSongIds.length - 1);
        btnNextSong.setAlpha(currentLineupIndex < lineupSongIds.length - 1 ? 1.0f : 0.35f);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddEditSongActivity.class);
            intent.putExtra(AddEditSongActivity.EXTRA_SONG_ID, songId);
            startActivity(intent);
        });

        btnTransposeUp.setOnClickListener(v -> {
            semitoneOffset++;
            renderSong();
        });

        btnTransposeDown.setOnClickListener(v -> {
            semitoneOffset--;
            renderSong();
        });

        btnTransposeReset.setOnClickListener(v -> {
            semitoneOffset = 0;
            renderSong();
        });

        btnFontIncrease.setOnClickListener(v -> {
            if (currentFontSizeSp < 28f) {
                currentFontSizeSp += 1.5f;
                tvChordContent.setTextSize(TypedValue.COMPLEX_UNIT_SP, currentFontSizeSp);
            }
        });

        btnFontDecrease.setOnClickListener(v -> {
            if (currentFontSizeSp > 11f) {
                currentFontSizeSp -= 1.5f;
                tvChordContent.setTextSize(TypedValue.COMPLEX_UNIT_SP, currentFontSizeSp);
            }
        });
    }

    private void renderSong() {
        if (currentSong == null) return;

        tvDetailTitle.setText(currentSong.getTitle());

        if (currentSong.getArtist() != null && !currentSong.getArtist().trim().isEmpty()) {
            tvDetailArtist.setText(currentSong.getArtist());
            tvDetailArtist.setVisibility(View.VISIBLE);
        } else {
            tvDetailArtist.setVisibility(View.GONE);
        }

        String origKey = currentSong.getOriginalKey();
        if (origKey == null || origKey.trim().isEmpty()) {
            origKey = "-";
        }

        tvOriginalKey.setText("Original: " + origKey);

        String currentKey = origKey;
        if (!origKey.equals("-")) {
            currentKey = ChordTransposer.transposeKey(origKey, semitoneOffset);
        }
        tvCurrentKey.setText("KEY: " + currentKey);

        if (semitoneOffset > 0) {
            tvOffsetBadge.setText("+" + semitoneOffset);
            tvOffsetBadge.setVisibility(View.VISIBLE);
        } else if (semitoneOffset < 0) {
            tvOffsetBadge.setText(String.valueOf(semitoneOffset));
            tvOffsetBadge.setVisibility(View.VISIBLE);
        } else {
            tvOffsetBadge.setText("0");
            tvOffsetBadge.setVisibility(View.VISIBLE);
        }

        String rawContent = currentSong.getContent();
        if (rawContent == null) rawContent = "";

        String transposedContent = ChordTransposer.transposeContent(rawContent, semitoneOffset);
        CharSequence formattedText = SongFormatter.formatSong(this, transposedContent);

        tvChordContent.setText(formattedText);
        tvChordContent.setTextSize(TypedValue.COMPLEX_UNIT_SP, currentFontSizeSp);
    }
}
