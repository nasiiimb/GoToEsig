package com.example.gotoesig.ui;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.gotoesig.MainActivity;
import com.example.gotoesig.R;
import com.example.gotoesig.data.api.OpenRouteService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AddTrajetActivity extends AppCompatActivity {

    private Spinner transportSpinner;
    private ImageView backButton;
    private EditText departPoint, date, time, toleranceTime, availableSeats, contribution;
    private Button addTripButton;
    private FirebaseFirestore firestore;
    private FirebaseAuth firebaseAuth;

    private double distance = 0.0;
    private double duration = 0.0;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());

    // Coordenadas de ESIGELEC
    private static final double[] ESIGELEC_COORDS = {1.10879, 49.387083};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_trajet);

        firestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        transportSpinner = findViewById(R.id.transport_spinner);
        departPoint = findViewById(R.id.depart_point);
        date = findViewById(R.id.date);
        time = findViewById(R.id.time);
        toleranceTime = findViewById(R.id.tolerance_time);
        availableSeats = findViewById(R.id.available_seats);
        contribution = findViewById(R.id.contribution);
        addTripButton = findViewById(R.id.add_trip_button);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.transport_modes, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        transportSpinner.setAdapter(adapter);

        // Inicializa el botón de retroceso
        backButton = findViewById(R.id.back_button);
        // Listener para volver al MainActivity
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(AddTrajetActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        transportSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if (transportSpinner.getSelectedItem().toString().equals("Véhicule")) {
                    contribution.setVisibility(View.VISIBLE);
                } else {
                    contribution.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parentView) {
                // No hacer nada si no hay selección
            }
        });

        addTripButton.setOnClickListener(v -> addTrip());

        date.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    AddTrajetActivity.this,
                    (view, year, month, dayOfMonth) -> date.setText(dayOfMonth + "/" + (month + 1) + "/" + year),
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.show();
        });

        time.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            TimePickerDialog timePickerDialog = new TimePickerDialog(
                    AddTrajetActivity.this,
                    (view, hourOfDay, minute) -> time.setText(hourOfDay + ":" + minute),
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true
            );
            timePickerDialog.show();
        });
    }

    private void addTrip() {
        // Obtener los valores de los EditText y Spinner antes de iniciar el proceso en el executor
        String transportType = transportSpinner.getSelectedItem().toString();
        String startPoint = departPoint.getText().toString();
        String tripDate = date.getText().toString();
        String tripTime = time.getText().toString();
        String delayTolerance = toleranceTime.getText().toString();
        String seatsAvailableString = availableSeats.getText().toString();
        String contributionAmount = contribution.getVisibility() == View.VISIBLE ? contribution.getText().toString() : "0";

        // Validar que los campos no estén vacíos
        if (startPoint.isEmpty() || tripDate.isEmpty() || tripTime.isEmpty() || delayTolerance.isEmpty() || seatsAvailableString.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            return;
        }

        // Convertir availableSeats a Integer
        int seatsAvailable;
        try {
            seatsAvailable = Integer.parseInt(seatsAvailableString);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Le nombre de sièges disponibles doit être un nombre valide", Toast.LENGTH_SHORT).show();
            return;
        }

        executor.execute(() -> {
            try {
                // Obtener las coordenadas del punto de inicio
                double[] startCoords = OpenRouteService.getCoordinatesFromAddress(startPoint);

                // Calcular distancia y duración
                double[] result = OpenRouteService.getDistanceAndDuration(startCoords, ESIGELEC_COORDS, transportType);

                double calculatedDistance = result[0];
                double calculatedDuration = result[1];

                handler.post(() -> {
                    distance = calculatedDistance;
                    duration = calculatedDuration;

                    // Crear el mensaje para mostrar en el AlertDialog
                    String message = String.format("Départ: %s\nDestination: ESIGELEC\nHeure: %s\nDistance(km): %.2f km\nTemps estimé(min): %.2f min\nPrix: %s",
                            startPoint, tripTime, calculatedDistance, calculatedDuration, contributionAmount);

                    new AlertDialog.Builder(this)
                            .setTitle("Confirmer l'ajout")
                            .setMessage(message)
                            .setPositiveButton("Oui", (dialog, which) -> {
                                Map<String, Object> trip = new HashMap<>();
                                trip.put("transport_type", transportType);
                                trip.put("start_point", startPoint);
                                trip.put("date", tripDate);
                                trip.put("time", tripTime);
                                trip.put("delay_tolerance", delayTolerance);
                                trip.put("seats_available", seatsAvailable); // Aquí es un Integer
                                trip.put("contribution_amount", contributionAmount);
                                trip.put("distance", distance);
                                trip.put("duration", duration);
                                trip.put("creator_id", firebaseAuth.getCurrentUser().getUid());
                                trip.put("participants", new ArrayList<>()); // Lista vacía de participantes

                                // Guardar en Firestore
                                firestore.collection("trips").add(trip)
                                        .addOnSuccessListener(documentReference -> {
                                            Toast.makeText(AddTrajetActivity.this, "Trajet ajouté avec succès", Toast.LENGTH_SHORT).show();
                                            finish();
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(AddTrajetActivity.this, "Erreur d'ajout du trajet", Toast.LENGTH_SHORT).show();
                                        });
                            })
                            .setNegativeButton("Non", (dialog, which) -> dialog.dismiss())
                            .show();
                });
            } catch (IOException | JSONException e) {
                e.printStackTrace();
                handler.post(() -> Toast.makeText(this, "Erreur lors du calcul de la distance.", Toast.LENGTH_SHORT).show());
            }
        });
    }





}
