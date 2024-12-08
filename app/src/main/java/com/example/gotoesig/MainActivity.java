package com.example.gotoesig;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.gotoesig.ui.*;
import com.example.gotoesig.ui.login.LoginActivity;

public class MainActivity extends AppCompatActivity {

    private ImageView profileIcon, addTrajetIcon, trajetsIcon, searchIcon, statisticsIcon, logoutIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Carga el layout

        // Initialize icons
        profileIcon = findViewById(R.id.profileIcon);
        addTrajetIcon = findViewById(R.id.addTrajetIcon);
        trajetsIcon = findViewById(R.id.myTrajectsIcon);
        searchIcon = findViewById(R.id.searchIcon);
        statisticsIcon = findViewById(R.id.statsIcon);
        logoutIcon = findViewById(R.id.logoutIcon);  // Nuevo icono de logout

        // Set OnClickListeners
        profileIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, ProfileActivity.class));
            }
        });

        addTrajetIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, AddTrajetActivity.class));
            }
        });

        trajetsIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, TrajetsActivity.class));
            }
        });

        searchIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, SearchActivity.class));
            }
        });

        statisticsIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, StatisticsActivity.class));
            }
        });

        // Set OnClickListener for logout
        logoutIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Clear user session (for example, by clearing SharedPreferences)
                SharedPreferences preferences = getSharedPreferences("user_session", MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.clear();  // Remove all stored data (or use editor.remove("key") for specific data)
                editor.apply();

                // Navigate to LoginActivity
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();  // Close the current activity
            }
        });
    }
}
