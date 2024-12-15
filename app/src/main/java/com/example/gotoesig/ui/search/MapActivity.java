package com.example.gotoesig.ui.search;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.gotoesig.MainActivity;
import com.example.gotoesig.R;
import com.example.gotoesig.data.api.OpenRouteService;
import com.example.gotoesig.model.Trip;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONException;
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MapActivity extends AppCompatActivity {

    private static final String TAG = "MapActivity";
    private static final double[] ESIGELEC_COORDS = {1.10879, 49.387083};

    private MapView mapView;
    private Button btnCancel;
    private Button btnJoin;
    private Trip trip;
    private Executor executor = Executors.newSingleThreadExecutor();
    private TextView tvTripDetails;  // TextView para mostrar los detalles del trayecto

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        trip = (Trip) getIntent().getSerializableExtra("trip");

        if (trip == null) {
            Toast.makeText(this, "Error: Trayecto no encontrado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Log.d(TAG, String.valueOf(trip));

        mapView = findViewById(R.id.mapview);
        btnCancel = findViewById(R.id.btn_cancel);
        btnJoin = findViewById(R.id.btn_join);
        tvTripDetails = findViewById(R.id.tv_trip_details);  // Inicialización del TextView

        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));

        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);

        btnCancel.setOnClickListener(v -> finish());
        btnJoin.setOnClickListener(v -> joinTrip(trip));

        // Actualizar el TextView con los detalles del trayecto
        updateTripDetails();

        // Limpiar las superposiciones existentes en el mapa
        mapView.getOverlays().clear();

        // Cargar la ruta en segundo plano
        loadRouteInBackground();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    private void loadRouteInBackground() {
        executor.execute(() -> {
            GeoPoint startPoint = getCoordinatesFromAddress(trip.getStartPoint());
            GeoPoint endPoint = new GeoPoint(ESIGELEC_COORDS[1], ESIGELEC_COORDS[0]);

            runOnUiThread(() -> setupMap(startPoint, endPoint));
        });
    }

    private GeoPoint getCoordinatesFromAddress(String address) {
        try {
            double[] coords = OpenRouteService.getCoordinatesFromAddress(address);
            return new GeoPoint(coords[0], coords[1]);
        } catch (IOException | JSONException e) {
            Log.e(TAG, "Error al obtener las coordenadas para la dirección: " + address, e);
            runOnUiThread(() -> Toast.makeText(this, "Error al obtener las coordenadas", Toast.LENGTH_SHORT).show());
            return new GeoPoint(0.0, 0.0); // Devolver un punto predeterminado en caso de error
        }
    }

    private void setupMap(GeoPoint startPoint, GeoPoint endPoint) {
        IMapController mapController = mapView.getController();

        // Centrar el mapa en el punto de inicio antes de agregar las superposiciones
        mapController.setCenter(startPoint);
        mapController.setZoom(15);

        // Agregar marcador para el punto de inicio
        Marker startMarker = new Marker(mapView);
        startMarker.setPosition(startPoint);
        startMarker.setTitle("Inicio: " + trip.getStartPoint());
        mapView.getOverlays().add(startMarker);

        // Agregar marcador para el punto de llegada
        Marker endMarker = new Marker(mapView);
        endMarker.setPosition(endPoint);
        endMarker.setTitle("Llegada: ESIGELEC");
        mapView.getOverlays().add(endMarker);

        // Obtener la ruta entre los puntos
        executor.execute(() -> {
            List<List<Double>> routePoints = getRouteBetweenPoints(startPoint, endPoint);

            if (!routePoints.isEmpty()) {
                runOnUiThread(() -> {
                    Polyline route = new Polyline();
                    ArrayList<GeoPoint> geoPoints = new ArrayList<>();
                    for (List<Double> point : routePoints) {
                        geoPoints.add(new GeoPoint(point.get(1), point.get(0))); // Convertir [longitud, latitud] a GeoPoint
                    }
                    route.setPoints(geoPoints);
                    route.setWidth(5f);
                    route.setColor(0xFF0000FF);
                    mapView.getOverlays().add(route);

                    // Recentrar el mapa en el punto de inicio después de agregar la ruta
                    mapController.setCenter(startPoint);
                });
            } else {
                runOnUiThread(() -> Toast.makeText(this, "Error al trazar la ruta", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private List<List<Double>> getRouteBetweenPoints(GeoPoint startPoint, GeoPoint endPoint) {
        try {
            double[] startCoords = {startPoint.getLongitude(), startPoint.getLatitude()};
            double[] endCoords = {endPoint.getLongitude(), endPoint.getLatitude()};
            String transportType = trip.getTransportType();  // Tipo de transporte desde la clase Trip

            return OpenRouteService.getDirections(startCoords, endCoords, transportType);
        } catch (IOException | JSONException e) {
            Log.e(TAG, "Error al obtener la ruta entre los puntos", e);
            return new ArrayList<>();
        }
    }

    private void joinTrip(Trip trip) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Verificar si hay asientos disponibles
        if (trip.getSeatsAvailable() > 0) {
            // Agregar al usuario a la lista de participantes
            trip.getParticipants().add(userId);

            // Reducir el número de asientos disponibles
            trip.setSeatsAvailable(trip.getSeatsAvailable() - 1);

            // Actualizar los datos en Firebase
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("trips").document(trip.getId())
                    .update("participants", trip.getParticipants(), "seats_available", trip.getSeatsAvailable())
                    .addOnSuccessListener(aVoid -> {
                        // Mostrar mensaje de éxito
                        Toast.makeText(MapActivity.this, "Te has unido al trayecto", Toast.LENGTH_SHORT).show();

                        // Volver a MainActivity
                        Intent intent = new Intent(MapActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish(); // Cerrar MapActivity
                    })
                    .addOnFailureListener(e -> {
                        // Mostrar mensaje de error
                        Toast.makeText(MapActivity.this, "Error al registrarse en el trayecto", Toast.LENGTH_SHORT).show();
                    });
        } else {
            // Mostrar mensaje si no hay asientos disponibles
            Toast.makeText(MapActivity.this, "No hay asientos disponibles", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateTripDetails() {
        // Texte informatif sur le trajet
        String tripDetailsText = "Voulez-vous rejoindre ce trajet ?";

        // Vérifier si le type de transport est "Véhicule"
        if ("Véhicule".equals(trip.getTransportType())) {
            // Si c'est un véhicule, ajouter le prix au message
            tripDetailsText += "\nPrix : " + trip.getContributionAmount() + "€";  // Supposons que contributionAmount soit une chaîne de caractères
        }

        // Mettre à jour le TextView avec le texte
        tvTripDetails.setText(tripDetailsText);
    }

}
