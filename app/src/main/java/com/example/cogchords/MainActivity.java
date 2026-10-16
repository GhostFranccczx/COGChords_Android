package com.example.cogchords;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.cogchords.ui.chords.ChordReferenceFragment;
import com.example.cogchords.ui.lineups.LineupsFragment;
import com.example.cogchords.ui.songs.SongsFragment;
import com.example.cogchords.util.SongJsonHelper;
import com.example.cogchords.util.ThemeManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private String currentTheme;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.applyTheme(this);
        currentTheme = ThemeManager.getTheme(this);
        super.onCreate(savedInstanceState);
        com.example.cogchords.util.ThemeManager.enableEdgeToEdge(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_songs) {
                switchFragment(SongsFragment.newInstance(), "TAG_SONGS");
                return true;
            } else if (itemId == R.id.nav_lineups) {
                switchFragment(LineupsFragment.newInstance(), "TAG_LINEUPS");
                return true;
            } else if (itemId == R.id.nav_chords) {
                switchFragment(ChordReferenceFragment.newInstance(), "TAG_CHORDS");
                return true;
            }
            return false;
        });

        if (savedInstanceState == null) {
            // Default to Songs tab on first launch
            switchFragment(SongsFragment.newInstance(), "TAG_SONGS");
        }

        handleIncomingIntent(getIntent());
    }

    @Override
    protected void onResume() {
        super.onResume();
        String activeTheme = ThemeManager.getTheme(this);
        if (currentTheme != null && !currentTheme.equals(activeTheme)) {
            currentTheme = activeTheme;
            recreate();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIncomingIntent(intent);
    }

    private void handleIncomingIntent(Intent intent) {
        if (intent != null && Intent.ACTION_VIEW.equals(intent.getAction())) {
            Uri uri = intent.getData();
            if (uri != null) {
                SongJsonHelper.importSongsFromUri(this, uri, result -> {
                    if (result.success) {
                        new MaterialAlertDialogBuilder(this)
                                .setTitle("Import Complete")
                                .setMessage("Successfully imported " + result.total + " songs from file:\n\n" +
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
        }
    }

    private void switchFragment(Fragment fragment, String tag) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment_container, fragment, tag)
                .commit();
    }
}