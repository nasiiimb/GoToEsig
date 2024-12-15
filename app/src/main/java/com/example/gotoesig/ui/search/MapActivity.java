    package com.example.gotoesig.ui.search;

    import android.content.Context;
    import android.content.Intent;
    import android.os.Bundle;
    import android.preference.PreferenceManager;
    import android.util.Log;
    import android.widget.Button;
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

    public class MapActivity extends AppCompatActivity {

        private static final String TAG = "MapActivity";

        private MapView mapView;
        private Button btnCancel;
        private Button btnJoin;

        private Trip trip; // Declaración del objeto Trip que se recuperará

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_map);

            // Recuperar el objeto Trip de la actividad anterior
            trip = (Trip) getIntent().getSerializableExtra("trip");

            // Verificar si el objeto se recuperó correctamente
            if (trip == null) {
                Toast.makeText(this, "Error: Trayecto no encontrado", Toast.LENGTH_SHORT).show();
                finish(); // Finalizar la actividad si no se encuentra el trayecto
                return;
            }

            // Mensaje de depuración para verificar los detalles del trayecto
            Log.d(TAG, String.valueOf(trip));

            // Inicializa las vistas
            mapView = findViewById(R.id.mapview);
            btnCancel = findViewById(R.id.btn_cancel);
            btnJoin = findViewById(R.id.btn_join);

            // Inicialización de la configuración de osmdroid
            Context ctx = getApplicationContext();
            Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));

            // Configuración del MapView
            mapView.setTileSource(TileSourceFactory.MAPNIK);
            mapView.setBuiltInZoomControls(true);
            mapView.setMultiTouchControls(true);
            double[] startCoords;
            // Simular coordenadas del trayecto usando datos del objeto Trip
            //startCoords = OpenRouteService.getCoordinatesFromAddress(trip.getStartPoint());
            //Log.d("MapActivity",startCoords[0]+", "+startCoords[1] );
            GeoPoint startPoint = getCoordinatesFromLocation(trip.getStartPoint());
            GeoPoint endPoint = new GeoPoint(49.387083,1.10879);

            // Configurar el mapa
            setupMap(startPoint, endPoint);

            // Comportamiento del botón Cancelar
            btnCancel.setOnClickListener(v -> finish());

            // Comportamiento del botón Unirse
            btnJoin.setOnClickListener(v -> joinTrip());
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

        /**
         * Configura el mapa con marcadores y ruta entre los puntos.
         */
        private void setupMap(GeoPoint startPoint, GeoPoint endPoint) {
            // Centrar el mapa en el punto de inicio
            IMapController mapController = mapView.getController();
            mapController.setCenter(startPoint);
            mapController.setZoom(15);

            // Añadir marcador de inicio
            Marker startMarker = new Marker(mapView);
            startMarker.setPosition(startPoint);
            startMarker.setTitle("Start: " + trip.getStartPoint());
            mapView.getOverlays().add(startMarker);

            // Añadir marcador de fin
            Marker endMarker = new Marker(mapView);
            endMarker.setPosition(endPoint);
            endMarker.setTitle("End: ESIGELEC" );
            mapView.getOverlays().add(endMarker);

            // Dibujar la ruta entre los puntos
            ArrayList<GeoPoint> routePoints = getRouteBetweenPoints(startPoint, endPoint);
            Polyline route = new Polyline();
            route.setPoints(routePoints);
            route.setWidth(5f);
            route.setColor(0xFF0000FF);
            mapView.getOverlays().add(route);
        }

        /**
         * Simula la creación de una ruta entre dos puntos.
         */
        private ArrayList<GeoPoint> getRouteBetweenPoints(GeoPoint startPoint, GeoPoint endPoint) {
            ArrayList<GeoPoint> routePoints = new ArrayList<>();
            routePoints.add(startPoint);
            routePoints.add(new GeoPoint(
                    (startPoint.getLatitude() + endPoint.getLatitude()) / 2,
                    (startPoint.getLongitude() + endPoint.getLongitude()) / 2));
            routePoints.add(endPoint);
            return routePoints;
        }

        /**
         * Simula obtener coordenadas a partir de una ubicación dada.
         */
        private GeoPoint getCoordinatesFromLocation(String location) {
            switch (location) {
                case "Rouen":
                    return new GeoPoint(49.4144, 1.0979);
                case "ESIGELEC":
                    return new GeoPoint(49.4445, 1.0725);
                default:
                    return new GeoPoint(0.0, 0.0);
            }
        }

        /**
         * Permite al usuario unirse al trayecto y actualizar Firestore.
         */
        private void joinTrip() {
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

            // Verificar si hay asientos disponibles
            if (trip.getSeatsAvailable() > 0) {
                trip.getParticipants().add(userId);
                trip.setSeatsAvailable(trip.getSeatsAvailable() - 1);

                FirebaseFirestore db = FirebaseFirestore.getInstance();
                db.collection("trips").document(trip.getId())
                        .update("participants", trip.getParticipants(),
                                "seatsAvailable", trip.getSeatsAvailable())
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, "Te has unido al trayecto", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(this, MainActivity.class));
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Error al unirse al trayecto", Toast.LENGTH_SHORT).show();
                        });
            } else {
                Toast.makeText(this, "No hay asientos disponibles", Toast.LENGTH_SHORT).show();
            }
        }
    }
