package com.example.gotoesig.ui.login;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
    private TextView signUpButton;  // Cambiado a TextView
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
        emailEditText = findViewById(R.id.username);  // Aquí se usa "username" ya que el campo de usuario está con ese ID
        passwordEditText = findViewById(R.id.password);  // Aquí se usa "password" como está en el XML
        loginButton = findViewById(R.id.loginButton);
        signUpButton = findViewById(R.id.signupText);  // Aquí se usa el TextView para redirigir al SignUp

        // Set up login button click listener
        loginButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString();
            String password = passwordEditText.getText().toString();

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

                                    // Fetch additional user data from Firestore (optional)
                                    db.collection("users")
                                            .document(user.getUid())
                                            .get()
                                            .addOnSuccessListener(documentSnapshot -> {
                                                if (documentSnapshot.exists()) {
                                                    // You can access the user's data here
                                                    String username = documentSnapshot.getString("username");
                                                    Log.d(TAG, "User data fetched successfully: Username = " + username);
                                                    Toast.makeText(LoginActivity.this, "Welcome, " + username, Toast.LENGTH_SHORT).show();
                                                } else {
                                                    Log.w(TAG, "User document not found in Firestore.");
                                                }

                                                // Proceed to MainActivity
                                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                                startActivity(intent);
                                                finish();  // Close LoginActivity
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
    }
}
