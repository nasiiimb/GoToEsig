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

        // Initialize views
        recyclerView = findViewById(R.id.recycler_view_trips);
        etStartPoint = findViewById(R.id.et_start_point);
        etDate = findViewById(R.id.et_date);
        btnSearch = findViewById(R.id.btn_search);

        // Initialize trip list and adapter
        tripList = new ArrayList<>();
        trajetAdapter = new TrajetAdapter(this, tripList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(trajetAdapter);

        // Configura el botón de regreso
        ImageView backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Al hacer clic en la flecha, regresa a MainActivity
                Intent intent = new Intent(SearchActivity.this, MainActivity.class);
                startActivity(intent);
                finish(); // Termina SearchActivity
            }
        });



        // Set up item click listener for RecyclerView
        trajetAdapter.setOnItemClickListener(new TrajetAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Trip trip) throws IOException {
                // Create Intent to go to MapActivity
                Intent intent = new Intent(SearchActivity.this, MapActivity.class);

                // Pass the selected trip to MapActivity
                intent.putExtra("trip", trip);



                // Start MapActivity
                startActivity(intent);
            }
        });

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Set up search button click listener
        btnSearch.setOnClickListener(v -> fetchTrips());
    }

    private void fetchTrips() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid(); // Get logged-in user's ID
        String startPoint = etStartPoint.getText().toString().trim();  // Get the start point from the EditText
        String date = etDate.getText().toString().trim();  // Get the date from the EditText

        if (startPoint.isEmpty() || date.isEmpty()) {
            Toast.makeText(this, "Please fill in both the start point and date.", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("trips")
                .addSnapshotListener((querySnapshot, e) -> {
                    if (e != null) {
                        Toast.makeText(this, "Error loading trips", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (querySnapshot != null) {
                        tripList.clear();

                        for (var document : querySnapshot.getDocuments()) {
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

                            // Filter by start point and date
                            if (tripStartPoint != null && tripStartPoint.toLowerCase().contains(startPoint.toLowerCase())
                                    && tripDate != null && tripDate.equals(date)) {
                                Trip trip = new Trip(
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

                                // Check if the current user is involved (creator or participant)
                                boolean isCreator = creatorId != null && creatorId.equals(currentUserId);
                                boolean isParticipant = participants != null && participants.contains(currentUserId);

                                // Only add the trip if the user is involved
                                if (!isCreator &&    !isParticipant) {
                                    tripList.add(trip);
                                }
                            }
                        }

                        trajetAdapter.notifyDataSetChanged(); // Refresh the RecyclerView with the updated list
                    }
                });
    }
}