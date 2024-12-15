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

        // Initialiser Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialiser les éléments UI
        emailEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.password);
        loginButton = findViewById(R.id.loginButton);
        signUpButton = findViewById(R.id.signupText);

        // Configurer l'écouteur du clic sur le bouton de connexion
        loginButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Tous les champs sont requis", Toast.LENGTH_SHORT).show();
            } else {
                // Enregistrer l'email pour le débogage
                Log.d(TAG, "Tentative de connexion avec l'email : " + email);

                // Se connecter avec Firebase Authentication
                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this, task -> {
                            if (task.isSuccessful()) {
                                // Connexion réussie
                                Log.d(TAG, "signInWithEmailAndPassword:success");
                                FirebaseUser user = mAuth.getCurrentUser();

                                if (user != null) {
                                    Log.d(TAG, "Utilisateur authentifié : UID = " + user.getUid());

                                    // Récupérer des données supplémentaires de l'utilisateur depuis Firestore
                                    db.collection("users")
                                            .document(user.getUid())
                                            .get()
                                            .addOnSuccessListener(documentSnapshot -> {
                                                if (documentSnapshot.exists()) {
                                                    // Récupérer les données de l'utilisateur
                                                    String prenom = documentSnapshot.getString("prenom");
                                                    Log.d(TAG, "Données de l'utilisateur récupérées avec succès : Prénom = " + prenom);
                                                    Toast.makeText(LoginActivity.this, "Bienvenue, " + prenom, Toast.LENGTH_SHORT).show();
                                                } else {
                                                    Log.w(TAG, "Document de l'utilisateur non trouvé dans Firestore.");
                                                }

                                                // Passer à MainActivity
                                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                                startActivity(intent);
                                                finish(); // Fermer LoginActivity
                                            })
                                            .addOnFailureListener(e -> {
                                                Log.e(TAG, "Erreur lors de la récupération des données de l'utilisateur : ", e);
                                                Toast.makeText(LoginActivity.this, "Erreur lors de la récupération des données de l'utilisateur", Toast.LENGTH_SHORT).show();
                                            });
                                } else {
                                    Log.e(TAG, "signInWithEmailAndPassword : l'utilisateur est nul après la connexion.");
                                    Toast.makeText(LoginActivity.this, "Une erreur inattendue est survenue. Veuillez réessayer.", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Log.e(TAG, "signInWithEmailAndPassword:failure", task.getException());
                                Toast.makeText(LoginActivity.this, "Échec de l'authentification : " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        // Configurer le lien d'inscription (TextView) pour naviguer vers SignUpActivity
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
                                Log.d(TAG, "E-mail de réinitialisation de mot de passe envoyé à : " + email);
                            } else {
                                Log.e(TAG, "Échec de l'envoi de l'e-mail de réinitialisation du mot de passe : ", task.getException());
                                Toast.makeText(LoginActivity.this, "Échec de l'envoi de l'e-mail. Veuillez vérifier l'adresse saisie", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

    }
}
