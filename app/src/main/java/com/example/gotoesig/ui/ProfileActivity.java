package com.example.gotoesig.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.gotoesig.MainActivity;
import com.example.gotoesig.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private EditText prenomEditText, surnameEditText, phoneEditText, cityEditText;
    private ImageView profileImageView, backButton;
    private Uri selectedImageUri;
    private Button saveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Vérification des autorisations de stockage
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    1);
        }

        // Initialiser Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();

        // Références UI
        prenomEditText = findViewById(R.id.profile_name);
        surnameEditText = findViewById(R.id.profile_surname);
        phoneEditText = findViewById(R.id.profile_phone);
        cityEditText = findViewById(R.id.profile_city);
        profileImageView = findViewById(R.id.profile_image);
        saveButton = findViewById(R.id.save_button);
        backButton = findViewById(R.id.back_button);

        // Charger les données de l'utilisateur si disponibles
        loadUserData();

        // Définir un écouteur de clic pour l'image du profil afin d'ouvrir le sélecteur d'images
        profileImageView.setOnClickListener(v -> openImagePicker());

        // Définir un écouteur de clic pour le bouton de sauvegarde pour enregistrer les données du profil
        saveButton.setOnClickListener(v -> saveProfileData());

        // Définir un écouteur de clic pour le bouton de retour pour revenir à MainActivity
        backButton.setOnClickListener(v -> goBack());
    }

    // Charger les données de l'utilisateur depuis Firestore
    private void loadUserData() {
        if (currentUser != null) {
            DocumentReference userDocRef = db.collection("users").document(currentUser.getUid());
            userDocRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String prenom = documentSnapshot.getString("prenom");
                    String surname = documentSnapshot.getString("surname");
                    String phone = documentSnapshot.getString("phone");
                    String city = documentSnapshot.getString("city");
                    String profileImage = documentSnapshot.getString("profileImage");

                    // Enregistrer les données récupérées pour le débogage
                    Log.d(TAG, "Données récupérées : " + documentSnapshot.getData());

                    // Définir les valeurs dans les EditTexts
                    prenomEditText.setText(prenom != null ? prenom : "");
                    surnameEditText.setText(surname != null ? surname : "");
                    phoneEditText.setText(phone != null ? phone : "");
                    cityEditText.setText(city != null ? city : "");

                    // Définir l'image de profil si disponible
                    if (profileImage != null && !profileImage.isEmpty()) {
                        Glide.with(this).load(profileImage).into(profileImageView);
                    }
                }
            }).addOnFailureListener(e -> {
                // Gérer les erreurs lors de la récupération des données
                Log.e(TAG, "Erreur lors de la récupération des données utilisateur", e);
                Toast.makeText(ProfileActivity.this, "Échec du chargement des données utilisateur", Toast.LENGTH_SHORT).show();
            });
        }
    }

    // Ouvrir le sélecteur d'images pour choisir une nouvelle image de profil
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("image/*");  // Autoriser uniquement les images
        startActivityForResult(intent, 1);  // Code de demande 1
    }

    // Gérer l'image sélectionnée par l'utilisateur
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            Log.d(TAG, "Image sélectionnée : " + selectedImageUri);
            uploadProfileImage(selectedImageUri);
        }
    }

    // Convertir l'image en Base64
    private String convertImageToBase64(Uri imageUri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            return Base64.encodeToString(byteArray, Base64.DEFAULT);
        } catch (IOException e) {
            Log.e(TAG, "Erreur lors de la conversion de l'image en Base64", e);
            return null;
        }
    }

    // Télécharger l'image de profil vers Firebase Storage et obtenir sa représentation Base64
    private void uploadProfileImage(Uri imageUri) {
        if (currentUser != null && imageUri != null) {
            String base64Image = convertImageToBase64(imageUri);
            if (base64Image != null) {
                // Mettre à jour l'image de profil dans Firestore avec Base64
                updateProfileImageBase64(base64Image);
                Glide.with(ProfileActivity.this).load(imageUri).into(profileImageView);
                Toast.makeText(ProfileActivity.this, "Image de profil mise à jour", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(ProfileActivity.this, "Erreur lors de la conversion de l'image", Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.e(TAG, "Aucun utilisateur ou image sélectionnée");
            Toast.makeText(ProfileActivity.this, "Aucune image sélectionnée", Toast.LENGTH_SHORT).show();
        }
    }

    // Mettre à jour l'image de profil Base64 dans Firestore
    private void updateProfileImageBase64(String base64Image) {
        if (currentUser != null) {
            DocumentReference userDocRef = db.collection("users").document(currentUser.getUid());
            userDocRef.update("profileImageBase64", base64Image)
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Image de profil Base64 mise à jour avec succès"))
                    .addOnFailureListener(e -> Log.e(TAG, "Erreur lors de la mise à jour de l'image de profil Base64", e));
        }
    }

    // Enregistrer les données du profil mises à jour dans Firestore
    private void saveProfileData() {
        String prenom = prenomEditText.getText().toString().trim();
        String surname = surnameEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();
        String city = cityEditText.getText().toString().trim();

        // Initialiser une carte pour stocker les champs mis à jour
        Map<String, Object> updatedData = new HashMap<>();

        // Ajouter les champs non vides à la carte
        if (!prenom.isEmpty()) updatedData.put("prenom", prenom);
        if (!surname.isEmpty()) updatedData.put("surname", surname);
        if (!phone.isEmpty()) updatedData.put("phone", phone);
        if (!city.isEmpty()) updatedData.put("city", city);

        if (selectedImageUri != null) {
            // Convertir l'image sélectionnée en Base64
            String base64Image = convertImageToBase64(selectedImageUri);
            if (base64Image != null) {
                updatedData.put("profileImageBase64", base64Image);
                saveToFirestore(updatedData);
            }
        } else {
            saveToFirestore(updatedData);
        }
    }

    // Sauvegarder les données mises à jour de l'utilisateur dans Firestore
    private void saveToFirestore(Map<String, Object> updatedData) {
        if (currentUser != null && !updatedData.isEmpty()) {
            DocumentReference userDocRef = db.collection("users").document(currentUser.getUid());
            userDocRef.update(updatedData)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Profil mis à jour avec succès : " + updatedData);
                        Toast.makeText(ProfileActivity.this, "Profil mis à jour", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Erreur lors de la mise à jour des données du profil", e);
                        Toast.makeText(ProfileActivity.this, "Échec de la mise à jour du profil", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Log.e(TAG, "Aucune donnée à mettre à jour");
            Toast.makeText(ProfileActivity.this, "Aucune donnée à mettre à jour", Toast.LENGTH_SHORT).show();
        }
    }

    // Revenir à MainActivity
    private void goBack() {
        Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
        startActivity(intent);
        finish();  // Assurez-vous que l'utilisateur ne peut pas revenir à cette activité en appuyant sur le bouton retour
    }

    // Gérer la réponse à la demande d'autorisation
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permissions accordées, vous pouvez accéder au stockage
                Toast.makeText(this, "Permission accordée", Toast.LENGTH_SHORT).show();
            } else {
                // Permissions non accordées, afficher un message à l'utilisateur
                Toast.makeText(this, "Les autorisations sont nécessaires pour sélectionner une image", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
