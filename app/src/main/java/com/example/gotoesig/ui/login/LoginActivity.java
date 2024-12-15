package com.example.gotoesig.ui.login;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.gotoesig.MainActivity;
import com.example.gotoesig.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private Button loginButton;
    private TextView signUpButton;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize UI elements
        emailEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.password);
        loginButton = findViewById(R.id.loginButton);
        signUpButton = findViewById(R.id.signupText);

        // Set up login button click listener
        loginButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(LoginActivity.this, "All fields are required", Toast.LENGTH_SHORT).show();
            } else {
                // Log input for debugging
                Log.d(TAG, "Attempting to log in with Email: " + email);

                // Sign in with Firebase Authentication
                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this, task -> {
                            if (task.isSuccessful()) {
                                // Successfully logged in
                                Log.d(TAG, "signInWithEmailAndPassword:success");
                                FirebaseUser user = mAuth.getCurrentUser();

                                if (user != null) {
                                    Log.d(TAG, "User authenticated: UID = " + user.getUid());

                                    // Fetch additional user data from Firestore
                                    db.collection("users")
                                            .document(user.getUid())
                                            .get()
                                            .addOnSuccessListener(documentSnapshot -> {
                                                if (documentSnapshot.exists()) {
                                                    // Retrieve user data
                                                    String prenom = documentSnapshot.getString("prenom");
                                                    Log.d(TAG, "User data fetched successfully: Prenom = " + prenom);
                                                    Toast.makeText(LoginActivity.this, "Welcome, " + prenom, Toast.LENGTH_SHORT).show();
                                                } else {
                                                    Log.w(TAG, "User document not found in Firestore.");
                                                }

                                                // Proceed to MainActivity
                                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                                startActivity(intent);
                                                finish(); // Close LoginActivity
                                            })
                                            .addOnFailureListener(e -> {
                                                Log.e(TAG, "Error fetching user data: ", e);
                                                Toast.makeText(LoginActivity.this, "Error fetching user data", Toast.LENGTH_SHORT).show();
                                            });
                                } else {
                                    Log.e(TAG, "signInWithEmailAndPassword: user is null after login.");
                                    Toast.makeText(LoginActivity.this, "Unexpected error occurred. Please try again.", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Log.e(TAG, "signInWithEmailAndPassword:failure", task.getException());
                                Toast.makeText(LoginActivity.this, "Authentication failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        // Set up sign-up link (TextView) to navigate to SignUpActivity
        signUpButton.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
            startActivity(intent);
        });

        TextView forgotPasswordText = findViewById(R.id.forgotPasswordText);
        forgotPasswordText.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();

            if (email.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Veuillez entrer votre adresse e-mail", Toast.LENGTH_SHORT).show();
            } else {
                mAuth.sendPasswordResetEmail(email)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(LoginActivity.this, "Un e-mail pour réinitialiser votre mot de passe a été envoyé", Toast.LENGTH_SHORT).show();
                                Log.d(TAG, "Password reset email sent to: " + email);
                            } else {
                                Log.e(TAG, "Failed to send password reset email: ", task.getException());
                                Toast.makeText(LoginActivity.this, "Échec de l'envoi de l'e-mail. Veuillez vérifier l'adresse saisie", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    
    }
}
