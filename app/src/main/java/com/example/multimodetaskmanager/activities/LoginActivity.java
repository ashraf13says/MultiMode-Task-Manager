package com.example.multimodetaskmanager.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.multimodetaskmanager.R;
import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {

    public static final String PREFS_NAME = "LoginPrefs";
    public static final String KEY_LOGGED_IN = "isLoggedIn";
    // ADD THESE TWO NEW CONSTANTS:
    public static final String KEY_EMAIL = "email";
    public static final String KEY_PASSWORD = "password";


    private TextInputEditText emailEditText;
    private TextInputEditText passwordEditText;
    private Button loginButton;
    private Button goToSignupButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Check if already logged in
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean isLoggedIn = prefs.getBoolean(KEY_LOGGED_IN, false);
        if (isLoggedIn) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish(); // Close LoginActivity
            return; // Prevent further execution of onCreate
        }

        // Initialize views by finding them by their IDs
        emailEditText = findViewById(R.id.edit_text_email_login);
        passwordEditText = findViewById(R.id.edit_text_password_login);
        loginButton = findViewById(R.id.button_login);
        goToSignupButton = findViewById(R.id.button_go_to_signup);

        // Set OnClickListener for the Login button
        if (loginButton != null) {
            loginButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String email = emailEditText.getText().toString().trim();
                    String password = passwordEditText.getText().toString().trim();

                    // Retrieve stored email and password for validation
                    String storedEmail = prefs.getString(KEY_EMAIL, null);
                    String storedPassword = prefs.getString(KEY_PASSWORD, null);


                    if (email.isEmpty() || password.isEmpty()) {
                        Toast.makeText(LoginActivity.this, "Please enter email and password", Toast.LENGTH_SHORT).show();
                    } else if (storedEmail != null && storedPassword != null &&
                            email.equals(storedEmail) && password.equals(storedPassword)) {
                        // Login successful - save login state
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putBoolean(KEY_LOGGED_IN, true);
                        editor.apply();

                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish(); // Close LoginActivity
                    } else {
                        Toast.makeText(LoginActivity.this, "Invalid credentials or account not registered.", Toast.LENGTH_LONG).show();
                    }
                }
            });
        } else {
            Log.e("LoginActivity", "Login button (R.id.button_login) not found in layout!");
            Toast.makeText(this, "Internal error: Login button not found!", Toast.LENGTH_LONG).show();
        }


        // Set OnClickListener for the Go to Sign Up button
        if (goToSignupButton != null) {
            goToSignupButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
                    startActivity(intent);
                }
            });
        } else {
            Log.e("LoginActivity", "Go to Sign Up button (R.id.button_go_to_signup) not found in layout!");
            Toast.makeText(this, "Internal error: Sign Up button not found!", Toast.LENGTH_LONG).show();
        }
    }
}