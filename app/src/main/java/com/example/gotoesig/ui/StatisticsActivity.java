package com.example.gotoesig.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.gotoesig.MainActivity;
import com.example.gotoesig.R;
import com.example.gotoesig.model.Trip;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

public class StatisticsActivity extends AppCompatActivity {

    private TextView tvNumberOfTrips, tvTotalAmount;
    private FirebaseFirestore db;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        // Initialisation des vues
        tvNumberOfTrips = findViewById(R.id.tv_number_of_trips);
        tvTotalAmount = findViewById(R.id.tv_total_amount);

        // Récupérer l'ID de l'utilisateur actuel
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Initialiser Firestore
        db = FirebaseFirestore.getInstance();

        // Récupérer les statistiques
        fetchStatistics();

        // Configurer le bouton de retour
        ImageView backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> {
            // Revenir à MainActivity
            Intent intent = new Intent(StatisticsActivity.this, MainActivity.class);
            startActivity(intent);
            finish(); // Fermer StatisticsActivity
        });
    }

    private void fetchStatistics() {
        db.collection("trips")
                .whereEqualTo("creator_id", currentUserId) // Seulement les trajets créés par l'utilisateur actuel
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        int tripCount = 0;
                        int totalAmount = 0;

                        QuerySnapshot result = task.getResult();
                        if (result != null) {
                            for (QueryDocumentSnapshot document : result) {
                                // Récupérer le nombre de participants et le montant de la contribution
                                List<String> participants = (List<String>) document.get("participants");
                                String contributionAmountString = document.getString("contribution_amount");

                                if (contributionAmountString != null) {
                                    try {
                                        int contributionAmount = Integer.parseInt(contributionAmountString);

                                        if (participants != null && !participants.isEmpty()) {
                                            // Calculer le montant total en fonction du nombre de participants
                                            totalAmount += contributionAmount * participants.size();
                                        }
                                    } catch (NumberFormatException e) {
                                        Log.e("StatisticsActivity", "Erreur de conversion du montant de la contribution", e);
                                    }
                                }
                                tripCount++; // Incrémenter le nombre de trajets
                            }

                            // Afficher les résultats
                            tvNumberOfTrips.setText("Nombre de trajets proposés : " + tripCount);
                            tvTotalAmount.setText("Montant total récolté : " + totalAmount + "€");
                        }
                    } else {
                        Toast.makeText(StatisticsActivity.this, "Erreur de récupération des statistiques", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
