package com.example.gotoesig.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.gotoesig.MainActivity;
import com.example.gotoesig.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {

    private static final String TAG = "SignUpActivity";

    private EditText usernameEditText, emailEditText, passwordEditText, phoneEditText, cityEditText;
    private Button signUpButton;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize UI elements
        usernameEditText = findViewById(R.id.signupPrenom);
        emailEditText = findViewById(R.id.signupEmail);
        passwordEditText = findViewById(R.id.signupPassword);
        phoneEditText = findViewById(R.id.signupPhone);
        cityEditText = findViewById(R.id.signupCity);
        signUpButton = findViewById(R.id.signUpButton);

        // Set up sign-up button click listener
        signUpButton.setOnClickListener(v -> {
            String prenom = usernameEditText.getText().toString().trim();
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();
            String phone = phoneEditText.getText().toString().trim();
            String city = cityEditText.getText().toString().trim();

            // Validate inputs
            if (prenom.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(SignUpActivity.this, "All fields are required", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create user with Firebase Authentication
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            // Get user UID
                            String uid = mAuth.getCurrentUser().getUid();

                            // Create user profile data
                            Map<String, Object> userProfile = new HashMap<>();
                            userProfile.put("Prenom", prenom);
                            userProfile.put("email", email);
                            userProfile.put("phone", phone);
                            userProfile.put("city", city);

                            // Save profile to Firestore
                            db.collection("users").document(uid).set(userProfile)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(SignUpActivity.this, "Registration successful", Toast.LENGTH_SHORT).show();
                                        // Redirect to MainActivity
                                        Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                                        startActivity(intent);
                                        finish();
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e(TAG, "Error saving profile: " + e.getMessage());
                                        Toast.makeText(SignUpActivity.this, "Failed to save profile data", Toast.LENGTH_SHORT).show();
                                    });
                        } else {
                            Log.e(TAG, "Registration failed: " + task.getException().getMessage());
                            Toast.makeText(SignUpActivity.this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }
}
