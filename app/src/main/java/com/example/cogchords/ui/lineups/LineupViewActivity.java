package com.example.cogchords.ui.lineups;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cogchords.R;
import com.example.cogchords.data.model.LineupSongDetail;
import com.example.cogchords.ui.songs.SongDetailActivity;

public class LineupViewActivity extends AppCompatActivity implements LineupSongViewAdapter.OnSongOpenListener {

    public static final String EXTRA_LINEUP_ID = "extra_lineup_id";

    private TextView tvLineupName;
    private TextView tvLineupDate;
    private TextView tvSongCount;
    private ImageView btnBack;
    private ImageView btnEdit;
    private RecyclerView rvSongsView;

    private LineupViewModel lineupViewModel;
    private LineupSongViewAdapter songViewAdapter;
    private long lineupId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        com.example.cogchords.util.ThemeManager.applyTheme(this);
        super.onCreate(savedInstanceState);
        com.example.cogchords.util.ThemeManager.enableEdgeToEdge(this);
        setContentView(R.layout.activity_lineup_view);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();

        lineupId = getIntent().getLongExtra(EXTRA_LINEUP_ID, -1);
        if (lineupId == -1) {
            finish();
            return;
        }

        setupRecyclerView();
        setupViewModel();
        setupListeners();
    }

    private void initViews() {
        tvLineupName = findViewById(R.id.tv_lineup_name);
        tvLineupDate = findViewById(R.id.tv_lineup_date);
        tvSongCount = findViewById(R.id.tv_song_count);
        btnBack = findViewById(R.id.btn_back);
        btnEdit = findViewById(R.id.btn_edit_lineup);
        rvSongsView = findViewById(R.id.rv_lineup_songs_view);
    }

    private void setupRecyclerView() {
        songViewAdapter = new LineupSongViewAdapter(this);
        rvSongsView.setLayoutManager(new LinearLayoutManager(this));
        rvSongsView.setAdapter(songViewAdapter);
    }

    private void setupViewModel() {
        lineupViewModel = new ViewModelProvider(this).get(LineupViewModel.class);

        lineupViewModel.getLineupById(lineupId).observe(this, lineup -> {
            if (lineup != null) {
                tvLineupName.setText(lineup.getName());
                tvLineupDate.setText(lineup.getDate() != null ? lineup.getDate() : "");
            }
        });

        lineupViewModel.getSongsForLineup(lineupId).observe(this, songs -> {
            currentSongsList = songs != null ? songs : new java.util.ArrayList<>();
            songViewAdapter.setSongs(currentSongsList);
            int count = currentSongsList.size();
            tvSongCount.setText("SETLIST (" + count + " song" + (count != 1 ? "s" : "") + ")");
        });
    }

    private java.util.List<LineupSongDetail> currentSongsList = new java.util.ArrayList<>();

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddEditLineupActivity.class);
            intent.putExtra(AddEditLineupActivity.EXTRA_LINEUP_ID, lineupId);
            startActivity(intent);
        });
    }

    @Override
    public void onSongOpen(LineupSongDetail song) {
        Intent intent = new Intent(this, SongDetailActivity.class);
        intent.putExtra(SongDetailActivity.EXTRA_SONG_ID, song.getSongId());

        if (currentSongsList != null && !currentSongsList.isEmpty()) {
            long[] songIds = new long[currentSongsList.size()];
            int currentIndex = 0;
            for (int i = 0; i < currentSongsList.size(); i++) {
                songIds[i] = currentSongsList.get(i).getSongId();
                if (currentSongsList.get(i).getSongId() == song.getSongId()) {
                    currentIndex = i;
                }
            }
            intent.putExtra(SongDetailActivity.EXTRA_SONG_IDS, songIds);
            intent.putExtra(SongDetailActivity.EXTRA_CURRENT_INDEX, currentIndex);
            intent.putExtra(SongDetailActivity.EXTRA_LINEUP_NAME, tvLineupName.getText().toString());
        }

        startActivity(intent);
    }
}
