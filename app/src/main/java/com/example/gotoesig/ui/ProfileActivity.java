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

        // Verificar permisos de almacenamiento
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    1);
        }

        // Inicializar Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();

        // Referencias de UI
        prenomEditText = findViewById(R.id.profile_name);
        surnameEditText = findViewById(R.id.profile_surname);
        phoneEditText = findViewById(R.id.profile_phone);
        cityEditText = findViewById(R.id.profile_city);
        profileImageView = findViewById(R.id.profile_image);
        saveButton = findViewById(R.id.save_button);
        backButton = findViewById(R.id.back_button);

        // Cargar datos del usuario si están disponibles
        loadUserData();

        // Configurar el clic del perfil para abrir el selector de imagen
        profileImageView.setOnClickListener(v -> openImagePicker());

        // Configurar el clic del botón guardar para guardar los datos del perfil
        saveButton.setOnClickListener(v -> saveProfileData());

        // Configurar el clic del botón de atrás para navegar hacia MainActivity
        backButton.setOnClickListener(v -> goBack());
    }

    // Cargar los datos del usuario desde Firestore
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

                    // Log de los datos obtenidos para depuración
                    Log.d(TAG, "Data retrieved: " + documentSnapshot.getData());

                    // Establecer los valores en los EditText
                    prenomEditText.setText(prenom != null ? prenom : "");
                    surnameEditText.setText(surname != null ? surname : "");
                    phoneEditText.setText(phone != null ? phone : "");
                    cityEditText.setText(city != null ? city : "");

                    // Establecer la imagen de perfil si está disponible
                    if (profileImage != null && !profileImage.isEmpty()) {
                        Glide.with(this).load(profileImage).into(profileImageView);
                    }
                }
            }).addOnFailureListener(e -> {
                // Manejar el error al obtener los datos
                Log.e(TAG, "Error getting user data", e);
                Toast.makeText(ProfileActivity.this, "Failed to load user data", Toast.LENGTH_SHORT).show();
            });
        }
    }

    // Abrir el selector de imagen para elegir una nueva imagen de perfil
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("image/*");  // Solo permitir imágenes
        startActivityForResult(intent, 1);  // Código de solicitud 1
    }

    // Manejar la imagen seleccionada por el usuario
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            Log.d(TAG, "Image selected: " + selectedImageUri);
            uploadProfileImage(selectedImageUri);
        }
    }

    // Convertir la imagen a Base64
    private String convertImageToBase64(Uri imageUri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            return Base64.encodeToString(byteArray, Base64.DEFAULT);
        } catch (IOException e) {
            Log.e(TAG, "Error converting image to Base64", e);
            return null;
        }
    }

    // Subir la imagen de perfil a Firebase Storage y obtener su representación Base64
    private void uploadProfileImage(Uri imageUri) {
        if (currentUser != null && imageUri != null) {
            String base64Image = convertImageToBase64(imageUri);
            if (base64Image != null) {
                // Actualizar la imagen de perfil en Firestore con Base64
                updateProfileImageBase64(base64Image);
                Glide.with(ProfileActivity.this).load(imageUri).into(profileImageView);
                Toast.makeText(ProfileActivity.this, "Profile image updated", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(ProfileActivity.this, "Error converting image", Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.e(TAG, "No user or image selected");
            Toast.makeText(ProfileActivity.this, "No image selected", Toast.LENGTH_SHORT).show();
        }
    }

    // Actualizar la imagen de perfil Base64 en Firestore
    private void updateProfileImageBase64(String base64Image) {
        if (currentUser != null) {
            DocumentReference userDocRef = db.collection("users").document(currentUser.getUid());
            userDocRef.update("profileImageBase64", base64Image)
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Profile image Base64 updated successfully"))
                    .addOnFailureListener(e -> Log.e(TAG, "Error updating profile image Base64", e));
        }
    }

    // Guardar los datos del perfil actualizados en Firestore
    private void saveProfileData() {
        String prenom = prenomEditText.getText().toString().trim();
        String surname = surnameEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();
        String city = cityEditText.getText().toString().trim();

        // Inicializar un mapa para almacenar los campos actualizados
        Map<String, Object> updatedData = new HashMap<>();

        // Agregar los campos no vacíos al mapa
        if (!prenom.isEmpty()) updatedData.put("prenom", prenom);
        if (!surname.isEmpty()) updatedData.put("surname", surname);
        if (!phone.isEmpty()) updatedData.put("phone", phone);
        if (!city.isEmpty()) updatedData.put("city", city);

        if (selectedImageUri != null) {
            // Convertir la imagen seleccionada a Base64
            String base64Image = convertImageToBase64(selectedImageUri);
            if (base64Image != null) {
                updatedData.put("profileImageBase64", base64Image);
                saveToFirestore(updatedData);
            }
        } else {
            saveToFirestore(updatedData);
        }
    }

    // Guardar los datos actualizados del usuario en Firestore
    private void saveToFirestore(Map<String, Object> updatedData) {
        if (currentUser != null && !updatedData.isEmpty()) {
            DocumentReference userDocRef = db.collection("users").document(currentUser.getUid());
            userDocRef.update(updatedData)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Profile updated successfully: " + updatedData);
                        Toast.makeText(ProfileActivity.this, "Profile updated", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error updating profile data", e);
                        Toast.makeText(ProfileActivity.this, "Failed to update profile", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Log.e(TAG, "No data to update");
            Toast.makeText(ProfileActivity.this, "No data to update", Toast.LENGTH_SHORT).show();
        }
    }

    // Navegar de vuelta a MainActivity
    private void goBack() {
        Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
        startActivity(intent);
        finish();  // Asegúrate de que el usuario no pueda volver a esta actividad al presionar el botón de atrás
    }

    // Manejar la respuesta de la solicitud de permisos
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permisos concedidos, puedes acceder al almacenamiento
                Toast.makeText(this, "Permiso concedido", Toast.LENGTH_SHORT).show();
            } else {
                // Permisos no concedidos, muestra un mensaje al usuario
                Toast.makeText(this, "Permisos necesarios para seleccionar una imagen", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
