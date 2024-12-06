package com.example.gotoesig.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.gotoesig.MainActivity;
import com.example.gotoesig.R;

public class SignUpActivity extends AppCompatActivity {

    private EditText usernameEditText, emailEditText, passwordEditText;
    private Button signUpButton;

    private static final String TAG = "SignUpActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // Initialize UI elements
        usernameEditText = findViewById(R.id.signupUsername);
        emailEditText = findViewById(R.id.signupEmail);
        passwordEditText = findViewById(R.id.signupPassword);
        signUpButton = findViewById(R.id.signUpButton);

        // Set up button click listener
        signUpButton.setOnClickListener(v -> {
            // Log the values to check what is being entered
            String username = usernameEditText.getText().toString();
            String email = emailEditText.getText().toString();
            String password = passwordEditText.getText().toString();

            Log.d(TAG, "Entered username: " + username);
            Log.d(TAG, "Entered email: " + email);
            Log.d(TAG, "Entered password: " + password);

            // Check if fields are empty
            if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Log.e(TAG, "All fields are required.");
                Toast.makeText(SignUpActivity.this, "All fields are required", Toast.LENGTH_SHORT).show();
            } else {
                try {
                    // Example: Register the user (this should be replaced with Firebase authentication or other logic)
                    Log.d(TAG, "Attempting to register user...");

                    // Simulate successful registration
                    Toast.makeText(SignUpActivity.this, "Registration successful", Toast.LENGTH_SHORT).show();

                    // Redirect to MainActivity after successful registration
                    Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    finish();
                } catch (Exception e) {
                    // Log the exception if something goes wrong
                    Log.e(TAG, "Registration failed: " + e.getMessage());
                    Toast.makeText(SignUpActivity.this, "Registration failed. Please try again.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
