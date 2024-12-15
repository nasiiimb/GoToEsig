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

    private static final String API_KEY = "5b3ce3597851110001cf6248f1cbe408569d4692bcf7b9c7e7a49044";  // Assurez-vous d'utiliser votre propre clé ici
    private static OkHttpClient client = new OkHttpClient();

    // Méthode pour obtenir la distance et la durée
    public static double[] getDistanceAndDuration(double[] startCoords, double[] endCoords, String transportMode) throws IOException, JSONException {
        // Construire l'URL de l'endpoint
        Log.d("OpenRouteService", "Méthode de transport : " + transportMode);  // Message de débogage
        String urlString = "https://api.openrouteservice.org/v2/matrix/" + getTransportType(transportMode);
        Log.d("OpenRouteService", "URL de la requête : " + urlString);  // Message de débogage

        // Créer le corps de la requête
        JSONObject body = new JSONObject();
        JSONArray coordinates = new JSONArray();

        // Utiliser les coordonnées dans l'ordre mentionné : [lon, lat]
        coordinates.put(new JSONArray().put(startCoords[1]).put(startCoords[0]));  // [longitude, latitude] pour startCoords
        coordinates.put(new JSONArray().put(endCoords[0]).put(endCoords[1]));      // [longitude, latitude] pour endCoords

        // Ajouter les coordonnées au corps de la requête
        body.put("locations", coordinates);
        body.put("metrics", new JSONArray().put("distance").put("duration"));
        body.put("units", "km");

        Log.d("OpenRouteService", "Corps de la requête : " + body.toString());  // Message de débogage

        // Créer la requête HTTP
        RequestBody requestBody = RequestBody.create(body.toString(), MediaType.get("application/json"));
        Request request = new Request.Builder()
                .url(urlString)
                .post(requestBody)
                .addHeader("Authorization", "Bearer " + API_KEY)
                .build();

        // Envoyer la requête et recevoir la réponse
        Log.d("OpenRouteService", "Envoi de la requête...");
        Response response = client.newCall(request).execute();

        if (!response.isSuccessful()) {
            Log.e("OpenRouteService", "Échec de la requête : " + response.code());
            throw new IOException("Code inattendu " + response);
        }

        // Lire la réponse
        String responseBody = response.body().string();
        Log.d("OpenRouteService", "Corps de la réponse : " + responseBody);  // Message de débogage

        // Traiter la réponse JSON
        JSONObject jsonResponse = new JSONObject(responseBody);

        // Obtenir le tableau des distances
        JSONArray distances = jsonResponse.getJSONArray("distances");

        // Extraire la valeur de la distance entre le point de départ et la destination
        double distance = -1;
        if (distances.length() > 0 && distances.getJSONArray(0).length() > 1) {
            // La valeur de la distance se trouve à la deuxième position du tableau (index 1)
            distance = distances.getJSONArray(0).getDouble(1); // Distance entre le premier et le deuxième point
            Log.d("OpenRouteService", "Distance : " + distance + " km");  // Message de débogage
        }

        // Maintenant, obtenir la durée
        JSONArray durations = jsonResponse.getJSONArray("durations");
        double duration = -1;
        if (durations.length() > 0 && durations.getJSONArray(0).length() > 1) {
            duration = durations.getJSONArray(0).getDouble(1); // Durée entre l'origine (0) et la destination (1)
            Log.d("OpenRouteService", "Durée : " + duration + " secondes");  // Message de débogage
        }

        // Convertir la distance de mètres en kilomètres
        distance = distance ;  // Convertir de mètres en kilomètres
        duration = duration / 60;
        return new double[]{distance, duration};
    }

    // Méthode pour obtenir les coordonnées à partir d'une adresse
    public static double[] getCoordinatesFromAddress(String address) throws IOException, JSONException {
        Log.d("OpenRouteService",".........................................");
        String url = "https://api.openrouteservice.org/geocode/search?api_key=" + API_KEY + "&text=" + address;
        Log.d("OpenRouteService", url);
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();
        Response response = client.newCall(request).execute();

        if (response.isSuccessful()) {
            String responseBody = response.body().string();
            Log.d("OpenRouteService", "Réponse de géocodage : " + responseBody);  // Débogage de la réponse

            JSONObject jsonResponse = new JSONObject(responseBody);
            JSONArray features = jsonResponse.getJSONArray("features");

            if (features.length() > 0) {
                JSONObject firstFeature = features.getJSONObject(0);
                JSONObject geometry = firstFeature.getJSONObject("geometry");
                JSONArray coordinates = geometry.getJSONArray("coordinates");

                double latitude = coordinates.getDouble(1);
                double longitude = coordinates.getDouble(0);
                Log.d("OpenRouteService", "Coordonnées obtenues : Lat = " + latitude + ", Lon = " + longitude);
                return new double[]{latitude, longitude};
            }
        }
        Log.d("OpenRouteService", "Coordonnées par défaut : Lat = 49.447, Lon = 1.092");
        return new double[]{49.447, 1.092};  // Coordonnées fictives en cas d'erreur
    }

    // Méthode pour obtenir les directions entre deux coordonnées
    public static List<List<Double>> getDirections(double[] startCoords, double[] endCoords, String transportMode) throws IOException, JSONException {
        // Construire l'URL de l'endpoint
        String urlString = "https://api.openrouteservice.org/v2/directions/" + getTransportType(transportMode) + "?api_key=" + API_KEY +
                "&start=" + startCoords[0] + "," + startCoords[1] +  // Coordonnées de départ (lat, long)
                "&end=" + endCoords[0] + "," + endCoords[1];        // Coordonnées de destination (lat, long)

        // Afficher l'URL pour le débogage
        Log.d("OpenRouteService", "URL de la requête : " + urlString);

        // Créer la requête HTTP
        Request request = new Request.Builder()
                .url(urlString)
                .build();

        // Envoyer la requête et recevoir la réponse
        Response response = client.newCall(request).execute();

        if (!response.isSuccessful()) {
            throw new IOException("Code inattendu " + response);
        }

        // Lire la réponse
        String responseBody = response.body().string();

        // Afficher la réponse pour le débogage
        Log.d("OpenRouteService", "Réponse : " + responseBody);

        // Traiter la réponse JSON
        JSONObject jsonResponse = new JSONObject(responseBody);

        // Obtenir la géométrie de l'itinéraire (les coordonnées de la ligne)
        JSONArray coordinatesArray = jsonResponse.getJSONArray("features")
                .getJSONObject(0)  // Prendre le premier objet de "features"
                .getJSONObject("geometry")  // Accéder à "geometry"
                .getJSONArray("coordinates");  // Obtenir les coordonnées

        // Convertir la géométrie en une liste de coordonnées
        List<List<Double>> coordinates = new ArrayList<>();

        for (int i = 0; i < coordinatesArray.length(); i++) {
            JSONArray coord = coordinatesArray.getJSONArray(i);
            double longitude = coord.getDouble(0); // Longitude
            double latitude = coord.getDouble(1);  // Latitude
            coordinates.add(Arrays.asList(longitude, latitude));
        }

        // Afficher les coordonnées pour le débogage
        Log.d("OpenRouteService", "Coordonnées : " + coordinates.toString());

        return coordinates; // Retourner la liste des coordonnées à utiliser sur la carte
    }

    // Méthode pour mapper le nom du mode de transport en français au format de l'API OpenRouteService
    // Cette méthode convertit le type de transport au format attendu par OpenRouteService.
    private static String getTransportType(String transportMode) {
        switch (transportMode.toLowerCase()) {
            case "véhicule":
                return "driving-car";
            case "vélo":
                return "cycling-regular";
            case "à pied":
                return "foot-walking";
            case "transport public":
                return "public_transport";  // Support pour les transports publics (métro)
            default:
                return "driving-car";  // Valeur par défaut (vous pouvez l'ajuster selon vos besoins)
        }
    }

}
