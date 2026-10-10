package com.example.cogchords;

import static org.junit.Assert.assertEquals;

import com.example.cogchords.util.ThemeManager;

import org.junit.Test;

public class ThemeManagerTest {

    @Test
    public void testThemeDisplayNames() {
        assertEquals("Dark Stage", ThemeManager.getThemeDisplayName(ThemeManager.THEME_DARK_STAGE));
        assertEquals("Clean Paper", ThemeManager.getThemeDisplayName(ThemeManager.THEME_CLEAN_PAPER));
        assertEquals("Vibrant & Playful", ThemeManager.getThemeDisplayName(ThemeManager.THEME_VIBRANT));
        assertEquals("Rainbow Spectrum", ThemeManager.getThemeDisplayName(ThemeManager.THEME_RAINBOW));
        assertEquals("Dark Stage", ThemeManager.getThemeDisplayName("unknown"));
    }

    @Test
    public void testThemeDescriptions() {
        assertEquals("Great for gigs and late-night practice", ThemeManager.getThemeDescription(ThemeManager.THEME_DARK_STAGE));
        assertEquals("Feels like a songbook", ThemeManager.getThemeDescription(ThemeManager.THEME_CLEAN_PAPER));
        assertEquals("Good for beginners or kids", ThemeManager.getThemeDescription(ThemeManager.THEME_VIBRANT));
        assertEquals("if you are unique", ThemeManager.getThemeDescription(ThemeManager.THEME_RAINBOW));
    }

    @Test
    public void testThemeIndices() {
        assertEquals(0, ThemeManager.getThemeIndex(ThemeManager.THEME_DARK_STAGE));
        assertEquals(1, ThemeManager.getThemeIndex(ThemeManager.THEME_CLEAN_PAPER));
        assertEquals(2, ThemeManager.getThemeIndex(ThemeManager.THEME_VIBRANT));
        assertEquals(3, ThemeManager.getThemeIndex(ThemeManager.THEME_RAINBOW));

        assertEquals(ThemeManager.THEME_DARK_STAGE, ThemeManager.getThemeFromIndex(0));
        assertEquals(ThemeManager.THEME_CLEAN_PAPER, ThemeManager.getThemeFromIndex(1));
        assertEquals(ThemeManager.THEME_VIBRANT, ThemeManager.getThemeFromIndex(2));
        assertEquals(ThemeManager.THEME_RAINBOW, ThemeManager.getThemeFromIndex(3));
    }
}
