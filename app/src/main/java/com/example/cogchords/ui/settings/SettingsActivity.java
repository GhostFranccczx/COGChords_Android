package com.example.cogchords.ui.settings;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.cogchords.R;
import com.example.cogchords.util.SongJsonHelper;
import com.example.cogchords.util.ThemeManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class SettingsActivity extends AppCompatActivity {

    private TextView tvCurrentTheme;
    private ActivityResultLauncher<String[]> importJsonLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.applyTheme(this);
        super.onCreate(savedInstanceState);
        com.example.cogchords.util.ThemeManager.enableEdgeToEdge(this);
        setContentView(R.layout.activity_settings);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        setupFilePicker();
        setupListeners();
    }

    private void initViews() {
        ImageView btnBack = findViewById(R.id.btn_back);
        tvCurrentTheme = findViewById(R.id.tv_current_theme);

        btnBack.setOnClickListener(v -> finish());
        updateThemeDisplay();
    }

    private void updateThemeDisplay() {
        String currentTheme = ThemeManager.getTheme(this);
        if (ThemeManager.THEME_RAINBOW.equals(currentTheme)) {
            tvCurrentTheme.setText(createRainbowThemeTitle());
        } else {
            String text = ThemeManager.getThemeDisplayName(currentTheme) + ": " + ThemeManager.getThemeDescription(currentTheme);
            tvCurrentTheme.setText(text);
        }
    }

    private void setupFilePicker() {
        importJsonLauncher = registerForActivityResult(
                new ActivityResultContracts.OpenDocument(),
                uri -> {
                    if (uri != null) {
                        importSongsFromUri(uri);
                    }
                }
        );
    }

    private void setupListeners() {
        // Theme Selection
        findViewById(R.id.card_theme).setOnClickListener(v -> showThemePickerDialog());

        // Import Songs from JSON
        findViewById(R.id.card_import_json).setOnClickListener(v -> {
            importJsonLauncher.launch(new String[]{"application/json", "text/*", "*/*"});
        });

        // Export Library JSON
        findViewById(R.id.card_export_json).setOnClickListener(v -> {
            SongJsonHelper.exportSongs(this);
        });

        // About Card
        findViewById(R.id.card_about).setOnClickListener(v -> showAboutDialog());
    }

    private void showThemePickerDialog() {
        CharSequence[] themeOptions = new CharSequence[]{
                "Dark Stage\n(great for gigs & late-night practice)",
                "Clean Paper\n(feels like a songbook)",
                "Vibrant & Playful\n(good for beginners or kids)",
                createRainbowThemeTitle()
        };
        String currentTheme = ThemeManager.getTheme(this);
        int selectedIndex = ThemeManager.getThemeIndex(currentTheme);

        new MaterialAlertDialogBuilder(this)
                .setTitle("Select Theme")
                .setSingleChoiceItems(themeOptions, selectedIndex, (dialog, which) -> {
                    String selectedTheme = ThemeManager.getThemeFromIndex(which);
                    if (!selectedTheme.equals(currentTheme)) {
                        ThemeManager.setTheme(this, selectedTheme);
                        dialog.dismiss();
                        recreate();
                    } else {
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private CharSequence createRainbowThemeTitle() {
        String title = "Rainbow Spectrum";
        android.text.SpannableStringBuilder ssb = new android.text.SpannableStringBuilder();
        ssb.append(title);

        // Colorize each letter of "Rainbow Spectrum" in vivid rainbow sequence
        int[] rainbowColors = {
                androidx.core.content.ContextCompat.getColor(this, R.color.rainbow_red),
                androidx.core.content.ContextCompat.getColor(this, R.color.rainbow_orange),
                androidx.core.content.ContextCompat.getColor(this, R.color.rainbow_yellow),
                androidx.core.content.ContextCompat.getColor(this, R.color.rainbow_green),
                androidx.core.content.ContextCompat.getColor(this, R.color.rainbow_cyan),
                androidx.core.content.ContextCompat.getColor(this, R.color.rainbow_blue),
                androidx.core.content.ContextCompat.getColor(this, R.color.rainbow_indigo),
                androidx.core.content.ContextCompat.getColor(this, R.color.rainbow_violet),
                androidx.core.content.ContextCompat.getColor(this, R.color.rainbow_pink)
        };

        for (int i = 0; i < title.length(); i++) {
            if (title.charAt(i) != ' ') {
                int color = rainbowColors[i % rainbowColors.length];
                ssb.setSpan(new android.text.style.ForegroundColorSpan(color), i, i + 1, android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                ssb.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD), i, i + 1, android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }

        // Subtitle: (if you are unique) in subtle secondary color
        int subStart = title.length();
        int subEnd = ssb.length();
        ssb.setSpan(new android.text.style.ForegroundColorSpan(
                androidx.core.content.ContextCompat.getColor(this, R.color.rainbow_cyan)),
                subStart, subEnd, android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        return ssb;
    }

    private void importSongsFromUri(Uri uri) {
        SongJsonHelper.importSongsFromUri(this, uri, result -> {
            if (isFinishing() || isDestroyed()) return;
            if (result.success) {
                new MaterialAlertDialogBuilder(this)
                        .setTitle("Import Complete")
                        .setMessage("Processed " + result.total + " songs from JSON file:\n\n" +
                                "• " + result.added + " new songs added\n" +
                                "• " + result.updated + " existing songs updated")
                        .setPositiveButton("OK", null)
                        .show();
            } else {
                new MaterialAlertDialogBuilder(this)
                        .setTitle("Import Failed")
                        .setMessage("Failed to import JSON file:\n\n" + result.errorMessage)
                        .setPositiveButton("OK", null)
                        .show();
            }
        });
    }

    private void showAboutDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("COGChords")
                .setMessage("Personal Music Chord & Lineup App\nVersion 1.0\n\nDeveloped by COGChords Team.\n\n\"We Exalt You, Jesus!\"\nChurch of God")
                .setPositiveButton("Close", null)
                .show();
    }
}
