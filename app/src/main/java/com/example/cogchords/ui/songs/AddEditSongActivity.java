package com.example.cogchords.ui.songs;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.cogchords.R;
import com.example.cogchords.data.entity.Song;
import com.google.android.material.button.MaterialButton;

public class AddEditSongActivity extends AppCompatActivity {

    public static final String EXTRA_SONG_ID = "extra_song_id";

    private EditText etTitle;
    private EditText etArtist;
    private EditText etKey;
    private EditText etCategory;
    private EditText etContent;
    private TextView tvScreenTitle;
    private MaterialButton btnSave;
    private ImageView btnBack;

    private SongViewModel songViewModel;
    private long songId = -1;
    private Song existingSong = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        com.example.cogchords.util.ThemeManager.applyTheme(this);
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_edit_song);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();

        songViewModel = new ViewModelProvider(this).get(SongViewModel.class);

        if (getIntent().hasExtra(EXTRA_SONG_ID)) {
            songId = getIntent().getLongExtra(EXTRA_SONG_ID, -1);
        }

        if (songId != -1) {
            tvScreenTitle.setText("EDIT SONG");
            loadSongData();
        } else {
            tvScreenTitle.setText("NEW SONG");
        }

        btnBack.setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> saveSong());
    }

    private void initViews() {
        etTitle = findViewById(R.id.et_song_title);
        etArtist = findViewById(R.id.et_song_artist);
        etKey = findViewById(R.id.et_song_key);
        etCategory = findViewById(R.id.et_song_category);
        etContent = findViewById(R.id.et_song_content);
        tvScreenTitle = findViewById(R.id.tv_screen_title);
        btnSave = findViewById(R.id.btn_save);
        btnBack = findViewById(R.id.btn_back);
    }

    private void loadSongData() {
        songViewModel.getSongById(songId).observe(this, song -> {
            if (song != null && existingSong == null) {
                existingSong = song;
                etTitle.setText(song.getTitle());
                etArtist.setText(song.getArtist());
                etKey.setText(song.getOriginalKey());
                etCategory.setText(song.getCategory());
                etContent.setText(song.getContent());
            }
        });
    }

    private void saveSong() {
        String title = etTitle.getText().toString().trim();
        String artist = etArtist.getText().toString().trim();
        String key = etKey.getText().toString().trim();
        String category = etCategory.getText().toString().trim();
        String content = etContent.getText().toString().trim();

        if (title.isEmpty()) {
            etTitle.setError("Title is required");
            etTitle.requestFocus();
            return;
        }

        if (songId == -1) {
            // New Song
            Song newSong = new Song(title, artist, key, category, content);
            songViewModel.insert(newSong, newId -> {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Song saved", Toast.LENGTH_SHORT).show();
                    finish();
                });
            });
        } else {
            // Edit existing song
            if (existingSong != null) {
                existingSong.setTitle(title);
                existingSong.setArtist(artist);
                existingSong.setOriginalKey(key);
                existingSong.setCategory(category);
                existingSong.setContent(content);
                existingSong.setUpdatedAt(System.currentTimeMillis());

                songViewModel.update(existingSong, () -> {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Song updated", Toast.LENGTH_SHORT).show();
                        finish();
                    });
                });
            }
        }
    }
}
