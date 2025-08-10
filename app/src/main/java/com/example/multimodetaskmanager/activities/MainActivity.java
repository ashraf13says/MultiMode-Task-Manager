package com.example.multimodetaskmanager.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;

import com.example.multimodetaskmanager.R;
import com.example.multimodetaskmanager.fragments.TaskListFragment;

public class MainActivity extends AppCompatActivity {

    private static final String PREF_THEME_MODE = "theme_mode";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Apply the saved theme preference FIRST, before setting content view
        SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
        int savedThemeMode = prefs.getInt(PREF_THEME_MODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        AppCompatDelegate.setDefaultNightMode(savedThemeMode);

        // Set content view ONLY ONCE
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("My Tasks");
        }

        // Only add the fragment if it's the first creation of the activity
        // (i.e., not a recreation after a theme change or orientation change)
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container_list, new TaskListFragment())
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        int newThemeMode = -1;

        // Handle sort options
        if (id == R.id.menu_sort_priority) {
            Toast.makeText(this, "Sort by Priority selected", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.id_sort_due_date) {
            Toast.makeText(this, "Sort by Due Date selected", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.menu_sort_name) {
            Toast.makeText(this, "Sort by Name selected", Toast.LENGTH_SHORT).show();
            return true;
        }
        // Handle theme options
        else if (id == R.id.menu_theme_light) {
            newThemeMode = AppCompatDelegate.MODE_NIGHT_NO; // Light theme
            Toast.makeText(this, "Light Theme selected", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.menu_theme_dark) {
            newThemeMode = AppCompatDelegate.MODE_NIGHT_YES; // Dark theme
            Toast.makeText(this, "Dark Theme selected", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.menu_theme_high_contrast) { // System Default
            newThemeMode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM; // Follow system theme
            Toast.makeText(this, "System Default Theme selected", Toast.LENGTH_SHORT).show();
        }

        if (newThemeMode != -1) {
            // Apply the new theme mode
            AppCompatDelegate.setDefaultNightMode(newThemeMode);
            // Save the preference
            editor.putInt(PREF_THEME_MODE, newThemeMode);
            editor.apply();
            // Recreate the activity for the theme to take effect immediately
            recreate();
            return true;
        }

        // Handle Logout
        else if (id == R.id.menu_logout) {
            Toast.makeText(this, "Logout selected", Toast.LENGTH_SHORT).show();
            SharedPreferences loginPrefs = getSharedPreferences(LoginActivity.PREFS_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor loginEditor = loginPrefs.edit();
            loginEditor.putBoolean(LoginActivity.KEY_LOGGED_IN, false);
            loginEditor.apply();

            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}