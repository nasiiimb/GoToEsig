    package com.example.gotoesig.ui;

    import android.content.Intent;
    import android.os.Bundle;
    import android.util.Log;
    import android.widget.ImageView;
    import android.widget.Toast;

    import androidx.appcompat.app.AppCompatActivity;
    import androidx.recyclerview.widget.LinearLayoutManager;
    import androidx.recyclerview.widget.RecyclerView;

    import com.example.gotoesig.MainActivity;
    import com.example.gotoesig.R;
    import com.example.gotoesig.model.Trip;
    import com.example.gotoesig.model.TrajetAdapter;
    import com.google.firebase.auth.FirebaseAuth;
    import com.google.firebase.firestore.FirebaseFirestore;

    import java.text.SimpleDateFormat;
    import java.util.ArrayList;
    import java.util.Date;
    import java.util.List;

    public class TrajetsActivity extends AppCompatActivity {

        private RecyclerView inProgressRecyclerView, completedRecyclerView;
        private TrajetAdapter inProgressAdapter, completedAdapter;
        private List<Trip> inProgressTrips = new ArrayList<>();
        private List<Trip> completedTrips = new ArrayList<>();
        private FirebaseFirestore db = FirebaseFirestore.getInstance();

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_trajets);

            inProgressRecyclerView = findViewById(R.id.recycler_view_in_progress);
            completedRecyclerView = findViewById(R.id.recycler_view_completed);
            ImageView backButton = findViewById(R.id.back_button);

            inProgressAdapter = new TrajetAdapter(this, inProgressTrips);
            completedAdapter = new TrajetAdapter(this, completedTrips);

            inProgressRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            completedRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            inProgressRecyclerView.setAdapter(inProgressAdapter);
            completedRecyclerView.setAdapter(completedAdapter);

            // Listener para volver al MainActivity
            backButton.setOnClickListener(v -> {
                Intent intent = new Intent(TrajetsActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            });

            fetchTrips();
        }

        private void fetchTrips() {
            String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid(); // Obtener ID del usuario logueado

            db.collection("trips")
                    .addSnapshotListener((querySnapshot, e) -> {
                        if (e != null) {
                            Toast.makeText(this, "Error loading trips", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (querySnapshot != null) {
                            inProgressTrips.clear();
                            completedTrips.clear();

                            for (var document : querySnapshot.getDocuments()) {
                                // Obtener los campos manualmente con los nombres correctos
                                String contributionAmount = document.getString("contribution_amount");
                                String creatorId = document.getString("creator_id");
                                String date = document.getString("date");
                                String delayTolerance = document.getString("delay_tolerance");
                                Double distance = document.getDouble("distance");
                                Double duration = document.getDouble("duration");
                                List<String> participants = (List<String>) document.get("participants");
                                int seatsAvailable = ((Number) document.get("seats_available")).intValue();;

                                String startPoint = document.getString("start_point");
                                String time = document.getString("time");
                                String transportType = document.getString("transport_type");

                                // Crear el objeto Trip manualmente
                                Trip trip = new Trip(
                                        contributionAmount,
                                        creatorId,
                                        date,
                                        delayTolerance,
                                        distance != null ? distance : 0,
                                        duration != null ? duration : 0,
                                        participants != null ? participants : new ArrayList<>(),
                                        seatsAvailable,
                                        startPoint,
                                        time,
                                        transportType
                                );
                                Log.d("TrajetsActivity", trip.toString());

                                // Verificar si el usuario está involucrado (creador o participante)
                                boolean isCreator = creatorId != null && creatorId.equals(currentUserId);
                                boolean isParticipant = participants != null && participants.contains(currentUserId);

                                if (isCreator || isParticipant) {
                                    if (isTripCompleted(trip)) {
                                        completedTrips.add(trip);
                                    } else {
                                        inProgressTrips.add(trip);
                                    }
                                }
                            }
                            inProgressAdapter.notifyDataSetChanged();
                            completedAdapter.notifyDataSetChanged();
                        }
                    });
        }



        private boolean isTripCompleted(Trip trip) {
            try {
                SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                Date tripEndDate = format.parse(trip.getDate() + " " + trip.getTime());
                return new Date().after(tripEndDate);
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
    }
