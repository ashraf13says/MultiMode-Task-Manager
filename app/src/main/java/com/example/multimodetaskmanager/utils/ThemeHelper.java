package com.example.multimodetaskmanager.utils;

import android.content.Context; // Not directly used in applyTheme, but good to keep if other methods might use it
import androidx.appcompat.app.AppCompatDelegate;

public class ThemeHelper {
    /**
     * Applies the specified theme to the application.
     *
     * @param themePref A string indicating the desired theme ("light", "dark", or any other string for system default).
     */
    public static void applyTheme(String themePref) {
        switch (themePref) {
            case "light":
                // Set the app to always use light mode
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case "dark":
                // Set the app to always use dark mode
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            default:
                // For any other value, follow the system's night mode setting
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }
    }
}