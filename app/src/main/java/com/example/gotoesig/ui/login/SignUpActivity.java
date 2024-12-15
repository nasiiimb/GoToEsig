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

        // Initialiser Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialiser les éléments de l'interface utilisateur
        usernameEditText = findViewById(R.id.signupPrenom);
        emailEditText = findViewById(R.id.signupEmail);
        passwordEditText = findViewById(R.id.signupPassword);
        phoneEditText = findViewById(R.id.signupPhone);
        cityEditText = findViewById(R.id.signupCity);
        signUpButton = findViewById(R.id.signUpButton);

        // Configurer le listener du bouton d'inscription
        signUpButton.setOnClickListener(v -> {
            String firstName = usernameEditText.getText().toString().trim();
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();
            String phone = phoneEditText.getText().toString().trim();
            String city = cityEditText.getText().toString().trim();

            // Valider les champs
            if (firstName.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(SignUpActivity.this, "Tous les champs sont obligatoires", Toast.LENGTH_SHORT).show();
                return;
            }

            // Créer l'utilisateur avec Firebase Authentication
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            // Obtenir le UID de l'utilisateur
                            String uid = mAuth.getCurrentUser().getUid();

                            // Créer les données du profil utilisateur
                            Map<String, Object> userProfile = new HashMap<>();
                            userProfile.put("uid", uid); // Stocker explicitement le UID
                            userProfile.put("firstName", firstName);
                            userProfile.put("email", email);
                            userProfile.put("phone", phone);
                            userProfile.put("city", city);
                            userProfile.put("photo", ""); // Ajouter le champ photo avec une URI vide initialement

                            // Enregistrer le profil dans Firestore
                            db.collection("users").document(uid).set(userProfile)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(SignUpActivity.this, "Inscription réussie", Toast.LENGTH_SHORT).show();
                                        // Rediriger vers MainActivity
                                        Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                                        startActivity(intent);
                                        finish();
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e(TAG, "Erreur lors de l'enregistrement du profil : " + e.getMessage());
                                        Toast.makeText(SignUpActivity.this, "Échec de l'enregistrement des données du profil", Toast.LENGTH_SHORT).show();
                                    });
                        } else {
                            Log.e(TAG, "Échec de l'inscription : " + task.getException().getMessage());
                            Toast.makeText(SignUpActivity.this, "Échec de l'inscription : " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }
}
