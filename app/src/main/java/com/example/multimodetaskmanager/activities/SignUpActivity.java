package com.example.multimodetaskmanager.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.multimodetaskmanager.R;
import com.google.android.material.textfield.TextInputEditText;

import java.util.regex.Matcher;
import java.util.regex.Pattern; // Import Pattern and Matcher for regex validation

public class SignUpActivity extends AppCompatActivity {

    private TextInputEditText emailEditText;
    private TextInputEditText passwordEditText;
    private TextInputEditText confirmPasswordEditText;
    private Button signupButton;
    private Button goToLoginButton;

    // Regex for email validation
    private static final String EMAIL_REGEX = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

    // Regex for password validation:
    // ^                 # start of line
    // (?=.*[0-9])       # must contain at least one digit
    // (?=.*[a-z])       # must contain at least one lowercase letter
    // (?=.*[A-Z])       # must contain at least one uppercase letter
    // (?=.*[!@#$%^&+=]) # must contain at least one special character
    // (?=\S+$)          # no whitespace allowed in the entire string
    // .{8,}             # at least 8 characters long
    // $                 # end of line
    private static final String PASSWORD_REGEX = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&+=])(?=\\S+$).{8,}$";
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(PASSWORD_REGEX);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Initialize UI components
        emailEditText = findViewById(R.id.edit_text_email_signup);
        passwordEditText = findViewById(R.id.edit_text_password_signup);
        confirmPasswordEditText = findViewById(R.id.edit_text_confirm_password_signup);
        signupButton = findViewById(R.id.button_signup);
        goToLoginButton = findViewById(R.id.button_go_to_login);

        // Set up Sign Up button click listener
        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptSignUp();
            }
        });

        // Set up "Go to Login" button click listener
        goToLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                startActivity(intent);
                finish(); // Close SignUpActivity
            }
        });
    }

    private void attemptSignUp() {
        // Reset errors
        emailEditText.setError(null);
        passwordEditText.setError(null);
        confirmPasswordEditText.setError(null);

        // Get user input
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();

        boolean cancel = false;
        View focusView = null;

        // --- NEW VALIDATION LOGIC ---

        // 1. Validate Email Format
        if (TextUtils.isEmpty(email)) {
            emailEditText.setError("Email is required");
            focusView = emailEditText;
            cancel = true;
        } else if (!EMAIL_PATTERN.matcher(email).matches()) {
            emailEditText.setError("Enter a valid email address (e.g., user@example.com)");
            focusView = emailEditText;
            cancel = true;
        }

        // 2. Validate Password Complexity
        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError("Password is required");
            focusView = passwordEditText;
            cancel = true;
        } else if (!PASSWORD_PATTERN.matcher(password).matches()) {
            passwordEditText.setError("Password must be at least 8 characters, with 1 uppercase, 1 lowercase, 1 digit, and 1 special character.");
            focusView = passwordEditText;
            cancel = true;
        }

        // 3. Check for password confirmation (after complexity check)
        if (!password.equals(confirmPassword)) {
            confirmPasswordEditText.setError("Passwords do not match");
            focusView = confirmPasswordEditText;
            cancel = true;
        }


        // --- END NEW VALIDATION LOGIC ---

        if (cancel) {
            // There was an error; don't attempt signup and focus the first form field with an error.
            if (focusView != null) {
                focusView.requestFocus();
            }
        } else {
            // Attempt to save credentials locally
            SharedPreferences prefs = getSharedPreferences(LoginActivity.PREFS_NAME, Context.MODE_PRIVATE);

            // In a real app, you'd check if email already exists before saving.
            // For this local example, we'll just overwrite/save.
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(LoginActivity.KEY_EMAIL, email);
            editor.putString(LoginActivity.KEY_PASSWORD, password);
            editor.putBoolean(LoginActivity.KEY_LOGGED_IN, true); // Log in automatically after signup
            editor.apply();

            Toast.makeText(SignUpActivity.this, "Account created successfully!", Toast.LENGTH_SHORT).show();

            // Navigate to MainActivity after successful signup
            Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
            startActivity(intent);
            finish(); // Close SignUpActivity
        }
    }
}