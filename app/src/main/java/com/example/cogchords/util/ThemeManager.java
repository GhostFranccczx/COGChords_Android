package com.example.cogchords.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.example.cogchords.R;

public class ThemeManager {

    private static final String PREFS_NAME = "cogchords_theme_prefs";
    private static final String KEY_THEME = "selected_theme_v2";

    public static final String THEME_DARK_STAGE = "dark_stage";
    public static final String THEME_CLEAN_PAPER = "clean_paper";
    public static final String THEME_VIBRANT = "vibrant";
    public static final String THEME_RAINBOW = "rainbow";

    public static String getTheme(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_THEME, THEME_DARK_STAGE);
    }

    public static void setTheme(Context context, String themeMode) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_THEME, themeMode).apply();
    }

    public static int getThemeResId(Context context) {
        String themeMode = getTheme(context);
        return getThemeResIdByName(themeMode);
    }

    public static int getThemeResIdByName(String themeMode) {
        switch (themeMode) {
            case THEME_CLEAN_PAPER:
                return R.style.Theme_COGChords_CleanPaper;
            case THEME_VIBRANT:
                return R.style.Theme_COGChords_Vibrant;
            case THEME_RAINBOW:
                return R.style.Theme_COGChords_Rainbow;
            case THEME_DARK_STAGE:
            default:
                return R.style.Theme_COGChords_DarkStage;
        }
    }

    public static void applyTheme(Activity activity) {
        activity.setTheme(getThemeResId(activity));
    }

    public static String getThemeDisplayName(String themeMode) {
        switch (themeMode) {
            case THEME_CLEAN_PAPER:
                return "Clean Paper";
            case THEME_VIBRANT:
                return "Vibrant & Playful";
            case THEME_RAINBOW:
                return "Rainbow Spectrum";
            case THEME_DARK_STAGE:
            default:
                return "Dark Stage";
        }
    }

    public static String getThemeDescription(String themeMode) {
        switch (themeMode) {
            case THEME_CLEAN_PAPER:
                return "Feels like a songbook";
            case THEME_VIBRANT:
                return "Good for beginners or kids";
            case THEME_RAINBOW:
                return "if you are unique";
            case THEME_DARK_STAGE:
            default:
                return "Great for gigs and late-night practice";
        }
    }

    public static int getThemeIndex(String themeMode) {
        switch (themeMode) {
            case THEME_CLEAN_PAPER:
                return 1;
            case THEME_VIBRANT:
                return 2;
            case THEME_RAINBOW:
                return 3;
            case THEME_DARK_STAGE:
            default:
                return 0;
        }
    }

    public static String getThemeFromIndex(int index) {
        switch (index) {
            case 1:
                return THEME_CLEAN_PAPER;
            case 2:
                return THEME_VIBRANT;
            case 3:
                return THEME_RAINBOW;
            case 0:
            default:
                return THEME_DARK_STAGE;
        }
    }

    public static int getChordColor(Context context) {
        String theme = getTheme(context);
        switch (theme) {
            case THEME_CLEAN_PAPER:
                return androidx.core.content.ContextCompat.getColor(context, R.color.clean_paper_chord_text);
            case THEME_VIBRANT:
                return androidx.core.content.ContextCompat.getColor(context, R.color.vibrant_chord_text);
            case THEME_RAINBOW:
                return androidx.core.content.ContextCompat.getColor(context, R.color.rainbow_yellow);
            case THEME_DARK_STAGE:
            default:
                return androidx.core.content.ContextCompat.getColor(context, R.color.dark_stage_accent);
        }
    }

    public static int getTextColor(Context context) {
        String theme = getTheme(context);
        switch (theme) {
            case THEME_CLEAN_PAPER:
                return androidx.core.content.ContextCompat.getColor(context, R.color.clean_paper_text);
            case THEME_VIBRANT:
                return androidx.core.content.ContextCompat.getColor(context, R.color.vibrant_text);
            case THEME_RAINBOW:
                return androidx.core.content.ContextCompat.getColor(context, R.color.rainbow_text);
            case THEME_DARK_STAGE:
            default:
                return androidx.core.content.ContextCompat.getColor(context, R.color.dark_stage_text);
        }
    }

    public static int getAccentColor(Context context) {
        String theme = getTheme(context);
        switch (theme) {
            case THEME_CLEAN_PAPER:
                return androidx.core.content.ContextCompat.getColor(context, R.color.clean_paper_accent);
            case THEME_VIBRANT:
                return androidx.core.content.ContextCompat.getColor(context, R.color.vibrant_accent_pink);
            case THEME_RAINBOW:
                return androidx.core.content.ContextCompat.getColor(context, R.color.rainbow_orange);
            case THEME_DARK_STAGE:
            default:
                return androidx.core.content.ContextCompat.getColor(context, R.color.dark_stage_accent);
        }
    }

    public static int getOnAccentColor(Context context) {
        String theme = getTheme(context);
        switch (theme) {
            case THEME_CLEAN_PAPER:
                return androidx.core.content.ContextCompat.getColor(context, R.color.white);
            case THEME_VIBRANT:
                return androidx.core.content.ContextCompat.getColor(context, R.color.white);
            case THEME_RAINBOW:
                return androidx.core.content.ContextCompat.getColor(context, R.color.white);
            case THEME_DARK_STAGE:
            default:
                return androidx.core.content.ContextCompat.getColor(context, R.color.dark_stage_bg);
        }
    }

    public static int getOutlineColor(Context context) {
        String theme = getTheme(context);
        switch (theme) {
            case THEME_CLEAN_PAPER:
                return androidx.core.content.ContextCompat.getColor(context, R.color.clean_paper_divider);
            case THEME_VIBRANT:
                return androidx.core.content.ContextCompat.getColor(context, R.color.vibrant_divider);
            case THEME_RAINBOW:
                return androidx.core.content.ContextCompat.getColor(context, R.color.rainbow_divider);
            case THEME_DARK_STAGE:
            default:
                return androidx.core.content.ContextCompat.getColor(context, R.color.dark_stage_divider);
        }
    }

    // ------------------------------------------------------------------
    // Theme-aware colors: always resolve from the ACTIVE theme so text is
    // readable on every theme (instead of fixed Dark Stage colors).
    // ------------------------------------------------------------------
    public static int getThemedColor(Context context, int attr, int fallback) {
        android.util.TypedValue tv = new android.util.TypedValue();
        if (context.getTheme().resolveAttribute(attr, tv, true)
                && tv.type >= android.util.TypedValue.TYPE_FIRST_COLOR_INT
                && tv.type <= android.util.TypedValue.TYPE_LAST_COLOR_INT) {
            return tv.data;
        }
        return fallback;
    }

    public static int getOnSurfaceColor(Context context) {
        return getThemedColor(context, com.google.android.material.R.attr.colorOnSurface, getTextColor(context));
    }

    public static int getOnSurfaceVariantColor(Context context) {
        return getThemedColor(context, com.google.android.material.R.attr.colorOnSurfaceVariant, getTextColor(context));
    }

    public static boolean isLightTheme(Context context) {
        return THEME_CLEAN_PAPER.equals(getTheme(context));
    }

    /**
     * Enables edge-to-edge and sets the status/navigation bar ICON colors from the
     * selected app theme (dark icons on Clean Paper, light icons on dark themes),
     * instead of following the phone's system dark mode.
     */
    public static void enableEdgeToEdge(androidx.activity.ComponentActivity activity) {
        if (isLightTheme(activity)) {
            androidx.activity.EdgeToEdge.enable(activity,
                    androidx.activity.SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT),
                    androidx.activity.SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT));
        } else {
            androidx.activity.EdgeToEdge.enable(activity,
                    androidx.activity.SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
                    androidx.activity.SystemBarStyle.dark(android.graphics.Color.TRANSPARENT));
        }
    }

    public static boolean isRainbowTheme(Context context) {
        return THEME_RAINBOW.equals(getTheme(context));
    }

    public static int getRainbowColorForPosition(Context context, int position) {
        int[] rainbowColors = {
                androidx.core.content.ContextCompat.getColor(context, R.color.rainbow_red),
                androidx.core.content.ContextCompat.getColor(context, R.color.rainbow_orange),
                androidx.core.content.ContextCompat.getColor(context, R.color.rainbow_yellow),
                androidx.core.content.ContextCompat.getColor(context, R.color.rainbow_green),
                androidx.core.content.ContextCompat.getColor(context, R.color.rainbow_cyan),
                androidx.core.content.ContextCompat.getColor(context, R.color.rainbow_blue),
                androidx.core.content.ContextCompat.getColor(context, R.color.rainbow_indigo),
                androidx.core.content.ContextCompat.getColor(context, R.color.rainbow_violet),
                androidx.core.content.ContextCompat.getColor(context, R.color.rainbow_pink)
        };
        int index = Math.abs(position) % rainbowColors.length;
        return rainbowColors[index];
    }

    public static int getRainbowChipBgForPosition(Context context, int position) {
        int[] chipColors = {
                androidx.core.content.ContextCompat.getColor(context, R.color.rainbow_chip_red),
                androidx.core.content.ContextCompat.getColor(context, R.color.rainbow_chip_orange),
                androidx.core.content.ContextCompat.getColor(context, R.color.rainbow_chip_yellow),
                androidx.core.content.ContextCompat.getColor(context, R.color.rainbow_chip_green),
                androidx.core.content.ContextCompat.getColor(context, R.color.rainbow_chip_cyan),
                androidx.core.content.ContextCompat.getColor(context, R.color.rainbow_chip_blue),
                androidx.core.content.ContextCompat.getColor(context, R.color.rainbow_chip_indigo),
                androidx.core.content.ContextCompat.getColor(context, R.color.rainbow_chip_violet)
        };
        int index = Math.abs(position) % chipColors.length;
        return chipColors[index];
    }
}
