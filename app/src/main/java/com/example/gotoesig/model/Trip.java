package com.example.gotoesig.model;

public class Trip {
    private String userId;
    private String departurePoint;
    private String date;
    private String time;
    private String delay;
    private String availablePlaces;
    private String transportMode;

    public Trip() {
        // Constructor vacío necesario para Firestore
    }

    public Trip(String userId, String departurePoint, String date, String time, String delay, String availablePlaces, String transportMode) {
        this.userId = userId;
        this.departurePoint = departurePoint;
        this.date = date;
        this.time = time;
        this.delay = delay;
        this.availablePlaces = availablePlaces;
        this.transportMode = transportMode;
    }

    // Getters y setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDeparturePoint() {
        return departurePoint;
    }

    public void setDeparturePoint(String departurePoint) {
        this.departurePoint = departurePoint;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDelay() {
        return delay;
    }

    public void setDelay(String delay) {
        this.delay = delay;
    }

    public String getAvailablePlaces() {
        return availablePlaces;
    }

    public void setAvailablePlaces(String availablePlaces) {
        this.availablePlaces = availablePlaces;
    }

    public String getTransportMode() {
        return transportMode;
    }

    public void setTransportMode(String transportMode) {
        this.transportMode = transportMode;
    }
}
