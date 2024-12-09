package com.example.gotoesig.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.gotoesig.MainActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

import com.example.gotoesig.R;

public class ProfileActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private EditText prenomEditText, surnameEditText;
    private ImageView profileImageView, backButton;
    private Uri selectedImageUri;
    private Button saveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();

        // UI references
        prenomEditText = findViewById(R.id.profile_name);
        surnameEditText = findViewById(R.id.profile_surname);
        profileImageView = findViewById(R.id.profile_image);
        saveButton = findViewById(R.id.save_button);
        backButton = findViewById(R.id.back_button);

        // Load user data
        loadUserData();

        // Set up profile image click listener to open image picker
        profileImageView.setOnClickListener(v -> openImagePicker());

        // Set up save button listener
        saveButton.setOnClickListener(v -> saveProfileData());

        // Set up back button listener to navigate back to MainActivity
        backButton.setOnClickListener(v -> goBack());
    }

    // Load user data from Firestore
    private void loadUserData() {
        if (currentUser != null) {
            DocumentReference userDocRef = db.collection("users").document(currentUser.getUid());
            userDocRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String prenom = documentSnapshot.getString("prenom");
                    String surname = documentSnapshot.getString("surname");
                    String profileImage = documentSnapshot.getString("profileImage");

                    // Log data retrieved
                    Log.d("ProfileActivity", "Retrieved data - Prenom: " + prenom + ", Surname: " + surname + ", Profile Image: " + profileImage);

                    // Set values in EditTexts
                    prenomEditText.setText(prenom != null && !prenom.isEmpty() ? prenom : "Enter your name");
                    surnameEditText.setText(surname != null && !surname.isEmpty() ? surname : "Enter your surname");

                    // Set profile image if available
                    if (profileImage != null && !profileImage.isEmpty()) {
                        Glide.with(this).load(profileImage).into(profileImageView);
                    }
                }
            }).addOnFailureListener(e -> {
                // Handle error fetching user data
                Log.e("ProfileActivity", "Error getting user data", e);
            });
        }
    }

    // Open image picker to choose a new profile image
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("image/*");  // Allow only images
        intent.addCategory(Intent.CATEGORY_OPENABLE);  // Make sure the picker is openable
        startActivityForResult(intent, 1);  // Request code 1
    }

    // Handle the image selected by the user
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            profileImageView.setImageURI(selectedImageUri);  // Display selected image
        }
    }

    // Save the updated profile data to Firestore
    private void saveProfileData() {
        String prenom = prenomEditText.getText().toString();
        String surname = surnameEditText.getText().toString();

        // Guardamos el valor de la URL de la imagen de perfil en una variable final
        final String[] profileImageUrl = {null}; // Usamos un arreglo para hacerlo final

        Log.d("ProfileActivity", "Saving data - Prenom: " + prenom + ", Surname: " + surname);

        if (selectedImageUri != null) {
            // Subir la imagen del perfil a Firebase Storage
            StorageReference storageRef = FirebaseStorage.getInstance().getReference("profile_images")
                    .child(currentUser.getUid() + ".jpg");
            storageRef.putFile(selectedImageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            profileImageUrl[0] = uri.toString();  // Asignamos el valor de la URL
                            saveToFirestore(prenom, surname, profileImageUrl[0]);  // Llamamos a la función con la URL
                        });
                    })
                    .addOnFailureListener(e -> {
                        // Si la carga falla, guardamos sin la URL de la imagen
                        Log.e("ProfileActivity", "Error uploading profile image", e);
                        saveToFirestore(prenom, surname, profileImageUrl[0]);
                    });
        } else {
            // Si no se ha seleccionado ninguna imagen, solo guardamos los datos
            saveToFirestore(prenom, surname, profileImageUrl[0]);
        }
    }

    // Save the updated user data to Firestore
    private void saveToFirestore(String prenom, String surname, String profileImageUrl) {
        if (currentUser != null) {
            DocumentReference userDocRef = db.collection("users").document(currentUser.getUid());
            Map<String, Object> userData = new HashMap<>();
            userData.put("prenom", prenom);
            userData.put("surname", surname);
            if (profileImageUrl != null) {
                userData.put("profileImage", profileImageUrl);
            }

            userDocRef.update(userData)
                    .addOnSuccessListener(aVoid -> {
                        // Successfully updated the data
                        Log.d("ProfileActivity", "Profile updated successfully");
                        Toast.makeText(ProfileActivity.this, "Profile updated", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        // Handle error updating profile data
                        Log.e("ProfileActivity", "Error updating profile data", e);
                        Toast.makeText(ProfileActivity.this, "Failed to update profile", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    // Navigate back to MainActivity
    private void goBack() {
        Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
        startActivity(intent);
        finish();  // Ensure the user can't go back to this activity when pressing the back button
    }
}
