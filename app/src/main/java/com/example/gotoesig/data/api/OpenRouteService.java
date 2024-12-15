package com.example.gotoesig.data.api;

import android.util.Log;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OpenRouteService {

    private static final String API_KEY = "5b3ce3597851110001cf6248f1cbe408569d4692bcf7b9c7e7a49044";  // Asegúrate de usar tu propia clave aquí
    private static OkHttpClient client = new OkHttpClient();

    // Método para obtener la distancia y la duración
    public static double[] getDistanceAndDuration(double[] startCoords, double[] endCoords, String transportMode) throws IOException, JSONException {
        // Construir la URL del endpoint
        Log.d("OpenRouteService", "metodo de tranporte: "+ transportMode);  // Mensaje de depuración
        String urlString = "https://api.openrouteservice.org/v2/matrix/" + getTransportType(transportMode);
        Log.d("OpenRouteService", "Request URL: " + urlString);  // Mensaje de depuración

        // Crear el cuerpo de la solicitud
        JSONObject body = new JSONObject();
        JSONArray coordinates = new JSONArray();

        // Utilizar las coordenadas en el orden que has mencionado: [lon, lat]
        coordinates.put(new JSONArray().put(startCoords[1]).put(startCoords[0]));  // [longitude, latitude] for startCoords
        coordinates.put(new JSONArray().put(endCoords[0]).put(endCoords[1]));      // [longitude, latitude] for endCoords

        // Añadir las coordenadas al cuerpo de la solicitud
        body.put("locations", coordinates);
        body.put("metrics", new JSONArray().put("distance").put("duration"));
        body.put("units", "km");

        Log.d("OpenRouteService", "Request Body: " + body.toString());  // Mensaje de depuración

        // Crear la solicitud HTTP
        RequestBody requestBody = RequestBody.create(body.toString(), MediaType.get("application/json"));
        Request request = new Request.Builder()
                .url(urlString)
                .post(requestBody)
                .addHeader("Authorization", "Bearer " + API_KEY)
                .build();

        // Enviar la solicitud y recibir la respuesta
        Log.d("OpenRouteService", "Sending request...");
        Response response = client.newCall(request).execute();

        if (!response.isSuccessful()) {
            Log.e("OpenRouteService", "Request failed: " + response.code());
            throw new IOException("Unexpected code " + response);
        }

        // Leer la respuesta
        String responseBody = response.body().string();
        Log.d("OpenRouteService", "Response Body: " + responseBody);  // Mensaje de depuración

        // Procesar la respuesta JSON
        JSONObject jsonResponse = new JSONObject(responseBody);

        // Obtener el array de distances
        JSONArray distances = jsonResponse.getJSONArray("distances");

        // Extraer el valor de la distancia entre el punto de inicio y el destino
        double distance = -1;
        if (distances.length() > 0 && distances.getJSONArray(0).length() > 1) {
            // El valor de distancia está en la segunda posición del array (índice 1)
            distance = distances.getJSONArray(0).getDouble(1); // Distancia entre el primer y segundo punto
            Log.d("OpenRouteService", "Distance: " + distance + " km");  // Mensaje de depuración
        }

        // Ahora, obtener la duración
        JSONArray durations = jsonResponse.getJSONArray("durations");
        double duration = -1;
        if (durations.length() > 0 && durations.getJSONArray(0).length() > 1) {
            duration = durations.getJSONArray(0).getDouble(1); // Duración entre el origen (0) y el destino (1)
            Log.d("OpenRouteService", "Duration: " + duration + " seconds");  // Mensaje de depuración
        }

        // Convertir la distancia de metros a kilómetros
        distance = distance ;  // Convertir de metros a kilómetros
        duration= duration/60;
        return new double[]{distance, duration};
    }









    // Método para obtener las coordenadas a partir de la dirección
    public static double[] getCoordinatesFromAddress(String address) throws IOException, JSONException {
        Log.d("OpenRouteService",".........................................");
        String url = "https://api.openrouteservice.org/geocode/search?api_key=" + API_KEY + "&text=" + address;
        Log.d("OpenRouteService",url);
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();
        Response response = client.newCall(request).execute();

        if (response.isSuccessful()) {
            String responseBody = response.body().string();
            Log.d("OpenRouteService", "Respuesta geocodificación: " + responseBody);  // Debugging de la respuesta

            JSONObject jsonResponse = new JSONObject(responseBody);
            JSONArray features = jsonResponse.getJSONArray("features");

            if (features.length() > 0) {
                JSONObject firstFeature = features.getJSONObject(0);
                JSONObject geometry = firstFeature.getJSONObject("geometry");
                JSONArray coordinates = geometry.getJSONArray("coordinates");

                double latitude = coordinates.getDouble(1);
                double longitude = coordinates.getDouble(0);
                Log.d("OpenRouteService", "Coordenadas obtenidas: Lat = " + latitude + ", Lon = " + longitude);
                return new double[]{latitude, longitude};
            }
        }
        Log.d("OpenRouteService", "Coordenadas predeterminadas: Lat = 49.447, Lon = 1.092");
        return new double[]{49.447, 1.092};  // Coordenadas ficticias si hay algún error
    }
    // Método para obtener las direcciones  entre dos coordenadas
    public static List<List<Double>> getDirections(double[] startCoords, double[] endCoords, String transportMode) throws IOException, JSONException {
        // Construir la URL del endpoint
        String urlString = "https://api.openrouteservice.org/v2/directions/" + getTransportType(transportMode) + "?api_key=" + API_KEY +
                "&start=" + startCoords[0] + "," + startCoords[1] +  // Coordenadas de inicio (lat, long)
                "&end=" + endCoords[0] + "," + endCoords[1];        // Coordenadas de fin (lat, long)

        // Imprimir la URL para el debug
        Log.d("OpenRouteService", "Request URL: " + urlString);

        // Crear la solicitud HTTP
        Request request = new Request.Builder()
                .url(urlString)
                .build();

        // Enviar la solicitud y recibir la respuesta
        Response response = client.newCall(request).execute();

        if (!response.isSuccessful()) {
            throw new IOException("Unexpected code " + response);
        }

        // Leer la respuesta
        String responseBody = response.body().string();

        // Imprimir la respuesta para el debug
        Log.d("OpenRouteService", "Response: " + responseBody);

        // Procesar la respuesta JSON
        JSONObject jsonResponse = new JSONObject(responseBody);

        // Obtener la geometría de la ruta (las coordenadas de la línea)
        JSONArray coordinatesArray = jsonResponse.getJSONArray("features")
                .getJSONObject(0)  // Tomar el primer objeto de "features"
                .getJSONObject("geometry")  // Acceder a "geometry"
                .getJSONArray("coordinates");  // Obtener las coordenadas

        // Convertir la geometría en una lista de coordenadas
        List<List<Double>> coordinates = new ArrayList<>();

        for (int i = 0; i < coordinatesArray.length(); i++) {
            JSONArray coord = coordinatesArray.getJSONArray(i);
            double longitude = coord.getDouble(0); // Longitud
            double latitude = coord.getDouble(1);  // Latitud
            coordinates.add(Arrays.asList(longitude, latitude));
        }

        // Imprimir las coordenadas para el debug
        Log.d("OpenRouteService", "Coordinates: " + coordinates.toString());

        return coordinates; // Devuelve la lista de coordenadas para usar en el mapa
    }







    // Método para mapear el nombre del modo de transporte en francés a la API de OpenRouteService
    // Este método convierte el tipo de transporte a la forma esperada por OpenRouteService.
    private static String getTransportType(String transportMode) {
        switch (transportMode.toLowerCase()) {
            case "véhicule":
                return "driving-car";
            case "vélo":
                return "cycling-regular";
            case "à pied":
                return "foot-walking";
            case "transport public":
                return "public_transport";  // Soporte para transporte público (metro)
            default:
                return "driving-car";  // Valor por defecto (puedes ajustarlo según tus necesidades)
        }
    }

}
