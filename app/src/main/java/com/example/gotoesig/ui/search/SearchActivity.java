package com.example.gotoesig.ui.search;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gotoesig.MainActivity;
import com.example.gotoesig.R;
import com.example.gotoesig.data.api.OpenRouteService;
import com.example.gotoesig.model.TrajetAdapter;
import com.example.gotoesig.model.Trip;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private TrajetAdapter trajetAdapter;
    private List<Trip> tripList;
    private FirebaseFirestore db;
    private EditText etStartPoint, etDate;
    private Button btnSearch;
    private static final double[] ESIGELEC_COORDS = {1.10879, 49.387083};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // Initialiser les vues
        recyclerView = findViewById(R.id.recycler_view_trips);
        etStartPoint = findViewById(R.id.et_start_point);
        etDate = findViewById(R.id.et_date);
        btnSearch = findViewById(R.id.btn_search);

        // Initialiser la liste des trajets et l'adaptateur
        tripList = new ArrayList<>();
        trajetAdapter = new TrajetAdapter(this, tripList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(trajetAdapter);

        // Configurer le bouton de retour
        ImageView backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // En cliquant sur la flèche, revenir à MainActivity
                Intent intent = new Intent(SearchActivity.this, MainActivity.class);
                startActivity(intent);
                finish(); // Terminer SearchActivity
            }
        });

        // Configurer le listener de clic sur les éléments du RecyclerView
        trajetAdapter.setOnItemClickListener(new TrajetAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Trip trip) throws IOException {
                // Créer l'Intent pour aller à MapActivity
                Intent intent = new Intent(SearchActivity.this, MapActivity.class);

                // Passer le trajet sélectionné à MapActivity
                intent.putExtra("trip", trip);

                // Démarrer MapActivity
                startActivity(intent);
            }
        });

        // Initialiser Firestore
        db = FirebaseFirestore.getInstance();

        // Configurer le listener du bouton de recherche
        btnSearch.setOnClickListener(v -> fetchTrips());
    }

    private void fetchTrips() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid(); // Obtenir l'ID de l'utilisateur connecté
        String startPoint = etStartPoint.getText().toString().trim();  // Obtenir le point de départ depuis l'EditText
        String date = etDate.getText().toString().trim();  // Obtenir la date depuis l'EditText

        if (startPoint.isEmpty() || date.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir à la fois le point de départ et la date.", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("trips")
                .addSnapshotListener((querySnapshot, e) -> {
                    if (e != null) {
                        Toast.makeText(this, "Erreur lors du chargement des trajets", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (querySnapshot != null) {
                        tripList.clear();

                        for (var document : querySnapshot.getDocuments()) {
                            String id=document.getId();
                            String contributionAmount = document.getString("contribution_amount");
                            String creatorId = document.getString("creator_id");
                            String tripStartPoint = document.getString("start_point");
                            String tripDate = document.getString("date");
                            String delayTolerance = document.getString("delay_tolerance");
                            Double distance = document.getDouble("distance");
                            Double duration = document.getDouble("duration");
                            List<String> participants = (List<String>) document.get("participants");
                            int seatsAvailable = ((Number) document.get("seats_available")).intValue();;
                            String time = document.getString("time");
                            String transportType = document.getString("transport_type");

                            // Filtrer par point de départ et date
                            if (tripStartPoint != null && tripStartPoint.toLowerCase().contains(startPoint.toLowerCase())
                                    && tripDate != null && tripDate.equals(date)) {
                                Trip trip = new Trip(
                                        id,
                                        contributionAmount,
                                        creatorId,
                                        tripDate,
                                        delayTolerance,
                                        distance != null ? distance : 0,
                                        duration != null ? duration : 0,
                                        participants != null ? participants : new ArrayList<>(),
                                        seatsAvailable,
                                        tripStartPoint,
                                        time,
                                        transportType
                                );

                                // Vérifier si l'utilisateur actuel est impliqué (créateur ou participant)
                                boolean isCreator = creatorId != null && creatorId.equals(currentUserId);
                                boolean isParticipant = participants != null && participants.contains(currentUserId);

                                // Ajouter le trajet uniquement si l'utilisateur est impliqué
                                if (!isCreator && !isParticipant && seatsAvailable > 0) {
                                    tripList.add(trip);
                                }
                            }
                        }

                        trajetAdapter.notifyDataSetChanged(); // Rafraîchir le RecyclerView avec la liste mise à jour
                    }
                });
    }
}
