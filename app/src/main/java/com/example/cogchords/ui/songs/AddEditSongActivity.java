package com.example.cogchords.ui.songs;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.cogchords.R;
import com.example.cogchords.data.AppDatabase;
import com.example.cogchords.data.entity.Song;
import com.example.cogchords.util.PdfTextExtractor;
import com.example.cogchords.util.SongTextImporter;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

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
    private MaterialButton btnImportFile;

    private static final String MIME_PDF = "application/pdf";
    private static final String MIME_DOCX =
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document";

    private final ActivityResultLauncher<String[]> importFileLauncher =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(), this::onImportFilePicked);

    private SongViewModel songViewModel;
    private long songId = -1;
    private Song existingSong = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        com.example.cogchords.util.ThemeManager.applyTheme(this);
        super.onCreate(savedInstanceState);
        com.example.cogchords.util.ThemeManager.enableEdgeToEdge(this);
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
        btnImportFile.setOnClickListener(v ->
                importFileLauncher.launch(new String[]{MIME_PDF, MIME_DOCX, "text/plain"}));
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
        btnImportFile = findViewById(R.id.btn_import_file);
    }

    // ---------------------------------------------------------------------
    // Import from PDF / Word / text file
    // ---------------------------------------------------------------------

    private void onImportFilePicked(Uri uri) {
        if (uri == null) return; // user cancelled

        if (!etContent.getText().toString().trim().isEmpty()) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Replace current text?")
                    .setMessage("Importing will replace the chords and lyrics you have typed so far.")
                    .setPositiveButton("Replace", (d, w) -> startImport(uri))
                    .setNegativeButton("Cancel", null)
                    .show();
        } else {
            startImport(uri);
        }
    }

    private void startImport(Uri uri) {
        btnImportFile.setEnabled(false);
        btnImportFile.setText("IMPORTING...");

        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                String fileName = queryFileName(uri);
                String lowerName = fileName == null ? "" : fileName.toLowerCase(Locale.ROOT);
                String mime = getContentResolver().getType(uri);

                String raw;
                try (InputStream in = getContentResolver().openInputStream(uri)) {
                    if (in == null) throw new IOException("Could not open the file");

                    if (MIME_PDF.equals(mime) || lowerName.endsWith(".pdf")) {
                        raw = PdfTextExtractor.extract(getApplicationContext(), in);
                    } else if (MIME_DOCX.equals(mime) || lowerName.endsWith(".docx")) {
                        raw = SongTextImporter.extractDocxText(in);
                    } else if ("text/plain".equals(mime) || lowerName.endsWith(".txt")) {
                        raw = readAll(in);
                    } else {
                        throw new IOException("Unsupported file type. Please choose a PDF, a Word (.docx) or a text file.");
                    }
                }

                String text = SongTextImporter.normalize(raw);
                if (text.isEmpty()) {
                    throw new IOException("No text was found in this file. Scanned pages and photos "
                            + "have no text, and would need OCR, which is not supported yet.");
                }

                String title = SongTextImporter.titleFromFileName(fileName);
                String key = SongTextImporter.guessKey(text);
                runOnUiThread(() -> onImportSucceeded(text, title, key));
            } catch (Exception | OutOfMemoryError e) {
                String message = e.getMessage() != null ? e.getMessage() : "Unknown error";
                runOnUiThread(() -> onImportFailed(message));
            }
        });
    }

    private void onImportSucceeded(String text, String title, String key) {
        if (isFinishing() || isDestroyed()) return;
        resetImportButton();

        etContent.setText(text);
        if (etTitle.getText().toString().trim().isEmpty() && !title.isEmpty()) {
            etTitle.setText(title);
        }
        if (etKey.getText().toString().trim().isEmpty() && !key.isEmpty()) {
            etKey.setText(key);
        }
        Toast.makeText(this, "Imported. Please check the chords before saving.", Toast.LENGTH_LONG).show();
    }

    private void onImportFailed(String message) {
        if (isFinishing() || isDestroyed()) return;
        resetImportButton();
        new MaterialAlertDialogBuilder(this)
                .setTitle("Import failed")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }

    private void resetImportButton() {
        btnImportFile.setEnabled(true);
        btnImportFile.setText("IMPORT FROM PDF / WORD");
    }

    private String queryFileName(Uri uri) {
        try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (index >= 0) return cursor.getString(index);
            }
        } catch (RuntimeException ignored) {
            // fall through to the path-based name
        }
        return uri.getLastPathSegment();
    }

    private static String readAll(InputStream in) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int read;
        while ((read = in.read(buffer)) != -1) bytes.write(buffer, 0, read);
        return new String(bytes.toByteArray(), StandardCharsets.UTF_8);
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
