package com.example.cogchords.ui.songs;

import android.content.Intent;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Choreographer;
import android.view.MotionEvent;
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
import com.example.cogchords.data.repository.SongAnnotationRepository;
import com.example.cogchords.util.DoodleCodec;
import com.example.cogchords.util.ChordTransposer;
import com.example.cogchords.util.SongFormatter;
import com.example.cogchords.util.SongPdfExporter;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Collections;

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
    private ImageView btnExportPdf;

    // Collapsible controls drawer (transpose / text size / auto scroll)
    private static final String STATE_CONTROLS_OPEN = "controls_open";
    private ImageView btnToggleControls;
    private View layoutTransposeControls;
    private View transposeDivider;
    private boolean controlsOpen = false;

    // Doodle (annotation) controls
    private DrawingView drawingView;
    private View layoutDoodleToolbar;
    private ImageView btnDoodle;
    private MaterialButtonToggleGroup toggleDoodleTool;
    private MaterialButton btnDoodleUndo;
    private MaterialButton btnDoodleClear;
    private MaterialButton btnDoodleDone;
    private View[] colorDots;

    private static final int[] DOODLE_COLORS =
            {0xFFE53935, 0xFF1E88E5, 0xFF43A047, 0xFFFDD835, 0xFFFB8C00};
    private int selectedColorIndex = 0;
    private boolean doodleMode = false;
    private boolean annotationsDirty = false;
    private long annotationSongId = -1; // the song the strokes in drawingView belong to
    private SongAnnotationRepository annotationRepository;

    // Auto scroll controls
    private MaterialButton btnScrollToggle;
    private MaterialButton btnScrollSlower;
    private MaterialButton btnScrollFaster;
    private TextView tvScrollSpeed;

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

    // Auto scroll state
    private static final int MIN_SCROLL_SPEED = 1;
    private static final int MAX_SCROLL_SPEED = 10;
    private static final float DP_PER_SECOND_PER_LEVEL = 5f;
    private boolean autoScrolling = false;
    private int scrollSpeedLevel = 3;
    private long lastFrameTimeNanos = 0;
    private float scrollRemainderPx = 0f;

    private final Choreographer.FrameCallback autoScrollCallback = new Choreographer.FrameCallback() {
        @Override
        public void doFrame(long frameTimeNanos) {
            if (!autoScrolling) return;

            if (lastFrameTimeNanos == 0) lastFrameTimeNanos = frameTimeNanos;
            float deltaSeconds = (frameTimeNanos - lastFrameTimeNanos) / 1_000_000_000f;
            lastFrameTimeNanos = frameTimeNanos;

            float pxPerSecond = scrollSpeedLevel * DP_PER_SECOND_PER_LEVEL
                    * getResources().getDisplayMetrics().density;
            scrollRemainderPx += pxPerSecond * deltaSeconds;

            int dy = (int) scrollRemainderPx;
            if (dy > 0) {
                scrollRemainderPx -= dy;
                scrollSongContent.scrollBy(0, dy);
            }

            // Stop automatically once the end of the song is reached
            if (!scrollSongContent.canScrollVertically(1)) {
                stopAutoScroll();
                return;
            }
            Choreographer.getInstance().postFrameCallback(this);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        com.example.cogchords.util.ThemeManager.applyTheme(this);
        super.onCreate(savedInstanceState);
        com.example.cogchords.util.ThemeManager.enableEdgeToEdge(this);
        setContentView(R.layout.activity_song_detail);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Keep screen awake while musician is reading chord charts
        getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        initViews();
        // Controls drawer starts closed (more room for chords) unless we are restoring state
        setControlsOpen(savedInstanceState != null
                && savedInstanceState.getBoolean(STATE_CONTROLS_OPEN, false), false);

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
        setupDoodle();
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
        btnExportPdf = findViewById(R.id.btn_export_pdf);
        btnToggleControls = findViewById(R.id.btn_toggle_controls);
        layoutTransposeControls = findViewById(R.id.layout_transpose_controls);
        transposeDivider = findViewById(R.id.transpose_divider);

        drawingView = findViewById(R.id.drawing_view);
        layoutDoodleToolbar = findViewById(R.id.layout_doodle_toolbar);
        btnDoodle = findViewById(R.id.btn_doodle);
        toggleDoodleTool = findViewById(R.id.toggle_doodle_tool);
        btnDoodleUndo = findViewById(R.id.btn_doodle_undo);
        btnDoodleClear = findViewById(R.id.btn_doodle_clear);
        btnDoodleDone = findViewById(R.id.btn_doodle_done);
        colorDots = new View[]{
                findViewById(R.id.dot_color_0), findViewById(R.id.dot_color_1),
                findViewById(R.id.dot_color_2), findViewById(R.id.dot_color_3),
                findViewById(R.id.dot_color_4)};

        btnScrollToggle = findViewById(R.id.btn_scroll_toggle);
        btnScrollSlower = findViewById(R.id.btn_scroll_slower);
        btnScrollFaster = findViewById(R.id.btn_scroll_faster);
        tvScrollSpeed = findViewById(R.id.tv_scroll_speed);

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
                loadAnnotationsIfSongChanged(song.getId());
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
                    stopAutoScroll();
                    exitDoodleMode();
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
                    stopAutoScroll();
                    exitDoodleMode();
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

    /** Opens or closes the controls drawer, optionally with a short slide animation. */
    private void setControlsOpen(boolean open, boolean animate) {
        controlsOpen = open;

        if (animate) {
            android.transition.AutoTransition transition = new android.transition.AutoTransition();
            transition.setDuration(200);
            android.transition.TransitionManager.beginDelayedTransition(
                    (android.view.ViewGroup) findViewById(R.id.main), transition);
        }

        int visibility = open ? View.VISIBLE : View.GONE;
        layoutTransposeControls.setVisibility(visibility);
        transposeDivider.setVisibility(visibility);

        // Highlight the toggle button while the drawer is open
        int tint = open
                ? com.example.cogchords.util.ThemeManager.getThemedColor(this,
                        androidx.appcompat.R.attr.colorPrimary,
                        com.example.cogchords.util.ThemeManager.getOnSurfaceColor(this))
                : com.example.cogchords.util.ThemeManager.getOnSurfaceColor(this);
        androidx.core.widget.ImageViewCompat.setImageTintList(btnToggleControls,
                android.content.res.ColorStateList.valueOf(tint));
        btnToggleControls.setContentDescription(open ? "Hide controls" : "Show controls");
    }

    @Override
    protected void onSaveInstanceState(@androidx.annotation.NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_CONTROLS_OPEN, controlsOpen);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnToggleControls.setOnClickListener(v -> setControlsOpen(!controlsOpen, true));

        btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddEditSongActivity.class);
            intent.putExtra(AddEditSongActivity.EXTRA_SONG_ID, songId);
            startActivity(intent);
        });

        // Export the song (in the currently selected key) as a printable PDF
        btnExportPdf.setOnClickListener(v ->
                SongPdfExporter.exportAndShare(this, currentSong, semitoneOffset));

        btnScrollToggle.setOnClickListener(v -> {
            if (autoScrolling) {
                stopAutoScroll();
            } else {
                startAutoScroll();
            }
        });

        btnScrollSlower.setOnClickListener(v -> {
            if (scrollSpeedLevel > MIN_SCROLL_SPEED) {
                scrollSpeedLevel--;
                updateAutoScrollUi();
            }
        });

        btnScrollFaster.setOnClickListener(v -> {
            if (scrollSpeedLevel < MAX_SCROLL_SPEED) {
                scrollSpeedLevel++;
                updateAutoScrollUi();
            }
        });
        updateAutoScrollUi();

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
                drawingView.setFontSizeSp(currentFontSizeSp);
            }
        });

        btnFontDecrease.setOnClickListener(v -> {
            if (currentFontSizeSp > 11f) {
                currentFontSizeSp -= 1.5f;
                tvChordContent.setTextSize(TypedValue.COMPLEX_UNIT_SP, currentFontSizeSp);
                drawingView.setFontSizeSp(currentFontSizeSp);
            }
        });
    }

    // ---------------------------------------------------------------------
    // Doodle (annotations)
    // ---------------------------------------------------------------------

    private void setupDoodle() {
        annotationRepository = new SongAnnotationRepository(this);
        drawingView.setOnStrokesChangedListener(() -> annotationsDirty = true);
        drawingView.setFontSizeSp(currentFontSizeSp);

        btnDoodle.setOnClickListener(v -> {
            if (doodleMode) {
                exitDoodleMode();
            } else {
                enterDoodleMode();
            }
        });

        toggleDoodleTool.check(R.id.btn_tool_pen);
        toggleDoodleTool.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;
            if (checkedId == R.id.btn_tool_marker) {
                drawingView.setTool(DrawingView.Tool.MARKER);
            } else if (checkedId == R.id.btn_tool_eraser) {
                drawingView.setTool(DrawingView.Tool.ERASER);
            } else {
                drawingView.setTool(DrawingView.Tool.PEN);
            }
        });

        for (int i = 0; i < colorDots.length; i++) {
            final int index = i;
            colorDots[i].setOnClickListener(v -> {
                selectedColorIndex = index;
                drawingView.setColor(DOODLE_COLORS[index]);
                updateColorDots();
                // Picking a colour while erasing goes back to the pen
                if (toggleDoodleTool.getCheckedButtonId() == R.id.btn_tool_eraser) {
                    toggleDoodleTool.check(R.id.btn_tool_pen);
                }
            });
        }
        drawingView.setColor(DOODLE_COLORS[selectedColorIndex]);
        updateColorDots();

        btnDoodleUndo.setOnClickListener(v -> drawingView.undo());

        btnDoodleClear.setOnClickListener(v -> {
            if (!drawingView.hasStrokes()) return;
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Clear doodles?")
                    .setMessage("This removes everything you drew on this song.")
                    .setPositiveButton("Clear", (d, w) -> drawingView.clearAll())
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        btnDoodleDone.setOnClickListener(v -> exitDoodleMode());
    }

    private void updateColorDots() {
        float density = getResources().getDisplayMetrics().density;
        int selectedStroke = com.example.cogchords.util.ThemeManager.getTextColor(this);
        int normalStroke = com.example.cogchords.util.ThemeManager.getOutlineColor(this);

        for (int i = 0; i < colorDots.length; i++) {
            boolean selected = i == selectedColorIndex;
            GradientDrawable dot = new GradientDrawable();
            dot.setShape(GradientDrawable.OVAL);
            dot.setColor(DOODLE_COLORS[i]);
            dot.setStroke((int) ((selected ? 3f : 1f) * density), selected ? selectedStroke : normalStroke);
            colorDots[i].setBackground(dot);
        }
    }

    private void enterDoodleMode() {
        stopAutoScroll();
        doodleMode = true;
        layoutDoodleToolbar.setVisibility(View.VISIBLE);
        drawingView.setFontSizeSp(currentFontSizeSp);
        drawingView.setDrawingEnabled(true);
    }

    private void exitDoodleMode() {
        if (doodleMode) {
            doodleMode = false;
            layoutDoodleToolbar.setVisibility(View.GONE);
            drawingView.setDrawingEnabled(false);
        }
        saveAnnotationsIfDirty();
    }

    private void saveAnnotationsIfDirty() {
        if (!annotationsDirty || annotationSongId == -1) return;
        annotationsDirty = false;
        annotationRepository.save(annotationSongId, DoodleCodec.encode(drawingView.getStrokes()));
    }

    private void loadAnnotationsIfSongChanged(long newSongId) {
        if (newSongId == annotationSongId) return;

        saveAnnotationsIfDirty(); // belongs to the previous song, annotationSongId is still the old id
        annotationSongId = newSongId;
        annotationsDirty = false;
        drawingView.setStrokes(Collections.emptyList());

        final long loadingId = newSongId;
        annotationRepository.load(loadingId, data -> runOnUiThread(() -> {
            // Ignore a late result if the user already moved on or started drawing
            if (loadingId == annotationSongId && !annotationsDirty) {
                drawingView.setStrokes(DoodleCodec.decode(data));
            }
        }));
    }

    private void startAutoScroll() {
        if (currentSong == null) return;
        if (doodleMode) exitDoodleMode();

        // If the song was already scrolled to the end, start over from the top
        if (!scrollSongContent.canScrollVertically(1) && scrollSongContent.getScrollY() > 0) {
            scrollSongContent.scrollTo(0, 0);
        }

        autoScrolling = true;
        lastFrameTimeNanos = 0;
        scrollRemainderPx = 0f;
        updateAutoScrollUi();
        Choreographer.getInstance().postFrameCallback(autoScrollCallback);
    }

    private void stopAutoScroll() {
        autoScrolling = false;
        Choreographer.getInstance().removeFrameCallback(autoScrollCallback);
        updateAutoScrollUi();
    }

    private void updateAutoScrollUi() {
        btnScrollToggle.setText(autoScrolling ? "❚❚ PAUSE" : "▶ PLAY");
        tvScrollSpeed.setText(String.valueOf(scrollSpeedLevel));
        btnScrollSlower.setAlpha(scrollSpeedLevel > MIN_SCROLL_SPEED ? 1.0f : 0.35f);
        btnScrollFaster.setAlpha(scrollSpeedLevel < MAX_SCROLL_SPEED ? 1.0f : 0.35f);
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopAutoScroll();
        saveAnnotationsIfDirty();
    }

    /** Touching the lyrics while auto scroll is running pauses it so the musician can read freely. */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (autoScrolling && ev.getActionMasked() == MotionEvent.ACTION_DOWN) {
            Rect bounds = new Rect();
            scrollSongContent.getGlobalVisibleRect(bounds);
            if (bounds.contains((int) ev.getRawX(), (int) ev.getRawY())) {
                stopAutoScroll();
            }
        }
        return super.dispatchTouchEvent(ev);
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
        drawingView.setFontSizeSp(currentFontSizeSp);
    }
}
