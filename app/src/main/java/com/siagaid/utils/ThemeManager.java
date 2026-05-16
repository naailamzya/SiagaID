package com.siagaid.utils;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatDelegate;

/**
 * Manager untuk Dark Mode / Light Mode.
 * Menyimpan preferensi tema ke SharedPreferences.
 */
public class ThemeManager {

    private static final String PREF_NAME  = "siagaid_prefs";
    private static final String KEY_THEME  = "theme_mode";

    // Nilai tema
    public static final int THEME_LIGHT  = AppCompatDelegate.MODE_NIGHT_NO;
    public static final int THEME_DARK   = AppCompatDelegate.MODE_NIGHT_YES;
    public static final int THEME_SYSTEM = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;

    // ========================
    // SIMPAN TEMA
    // ========================
    public static void saveTheme(Context context, int themeMode) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME,
                Context.MODE_PRIVATE);
        prefs.edit().putInt(KEY_THEME, themeMode).apply();
    }

    // ========================
    // AMBIL TEMA TERSIMPAN
    // ========================
    public static int getSavedTheme(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME,
                Context.MODE_PRIVATE);
        return prefs.getInt(KEY_THEME, THEME_SYSTEM); // default: ikut sistem
    }

    // ========================
    // TERAPKAN TEMA
    // ========================
    public static void applyTheme(int themeMode) {
        AppCompatDelegate.setDefaultNightMode(themeMode);
    }

    // ========================
    // TERAPKAN TEMA TERSIMPAN (dipanggil saat app start)
    // ========================
    public static void applySavedTheme(Context context) {
        int savedTheme = getSavedTheme(context);
        applyTheme(savedTheme);
    }

    // ========================
    // TOGGLE TEMA (Light ↔ Dark)
    // ========================
    public static void toggleTheme(Context context) {
        int current = getSavedTheme(context);
        int newTheme;

        if (current == THEME_DARK) {
            newTheme = THEME_LIGHT;
        } else {
            newTheme = THEME_DARK;
        }

        saveTheme(context, newTheme);
        applyTheme(newTheme);
    }

    // ========================
    // CEK APAKAH DARK MODE AKTIF
    // ========================
    public static boolean isDarkMode(Context context) {
        return getSavedTheme(context) == THEME_DARK;
    }
}