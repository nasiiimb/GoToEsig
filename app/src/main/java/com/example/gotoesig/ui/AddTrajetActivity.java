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
        String seatsAvailable = availableSeats.getText().toString();
        String contributionAmount = contribution.getVisibility() == View.VISIBLE ? contribution.getText().toString() : "0";

        if (startPoint.isEmpty() || tripDate.isEmpty() || tripTime.isEmpty() || delayTolerance.isEmpty() || seatsAvailable.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            return;
        }

        executor.execute(() -> {
            try {
                // Obtener los valores dentro del executor para asegurarse de que están actualizados
                String transportTypeInsideExecutor = transportSpinner.getSelectedItem().toString();
                String startPointInsideExecutor = departPoint.getText().toString();
                String tripDateInsideExecutor = date.getText().toString();
                String tripTimeInsideExecutor = time.getText().toString();
                String delayToleranceInsideExecutor = toleranceTime.getText().toString();
                String seatsAvailableInsideExecutor = availableSeats.getText().toString();
                String contributionAmountInsideExecutor = contribution.getVisibility() == View.VISIBLE ? contribution.getText().toString() : "0";

                Log.d("AddTrajetActivity", "Iniciando obtención de coordenadas para el punto de partida...");
                double[] startCoords = OpenRouteService.getCoordinatesFromAddress(startPointInsideExecutor);

                // Agregar mensaje de depuración para verificar las coordenadas obtenidas
                Log.d("AddTrajetActivity", "Coordenadas obtenidas: Lat: " + startCoords[0] + ", Lon: " + startCoords[1]);

                Log.d("AddTrajetActivity", "Iniciando cálculo de distancia y duración...");
                // Usar las coordenadas de ESIGELEC como destino
                double[] result = OpenRouteService.getDistanceAndDuration(startCoords, ESIGELEC_COORDS, transportTypeInsideExecutor);

                // Agregar mensaje de depuración para los resultados del cálculo
                Log.d("AddTrajetActivity", "Resultado distancia y duración: " + result[0] + " km, " + result[1] + " minutos");

                double calculatedDistance = result[0];
                double calculatedDuration = result[1];
                Log.d("AddTrajetActivity", "Distancia calculada: " + calculatedDistance + " km, Duración calculada: " + calculatedDuration + " minutos");

                handler.post(() -> {
                    // Actualizar las variables de distancia y duración
                    distance = calculatedDistance;
                    duration = calculatedDuration;

                    // Crear el mensaje para mostrar en el AlertDialog
                    String message = String.format("Départ: %s\nDestination: ESIGELEC\nHeure: %s\nDistance(km): %.2f km\nTemps estimé(min): %.2f min\nPrix: %s",
                            startPointInsideExecutor, tripTimeInsideExecutor, calculatedDistance, calculatedDuration, contributionAmountInsideExecutor);

                    new AlertDialog.Builder(this)
                            .setTitle("Confirmer l'ajout")
                            .setMessage(message)
                            .setPositiveButton("Oui", (dialog, which) -> {
                                Map<String, Object> trip = new HashMap<>();
                                trip.put("transport_type", transportTypeInsideExecutor);
                                trip.put("start_point", startPointInsideExecutor);
                                trip.put("date", tripDateInsideExecutor);
                                trip.put("time", tripTimeInsideExecutor);
                                trip.put("delay_tolerance", delayToleranceInsideExecutor);
                                trip.put("seats_available", seatsAvailableInsideExecutor);
                                trip.put("contribution_amount", contributionAmountInsideExecutor);
                                trip.put("distance", distance);
                                trip.put("duration", duration);
                                trip.put("creator_id", firebaseAuth.getCurrentUser().getUid());
                                trip.put("participants", new ArrayList<>());

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
