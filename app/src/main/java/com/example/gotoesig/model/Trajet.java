package com.example.gotoesig.model;

public class Trajet {
    private String transport;
    private String departurePoint;
    private String date;
    private String time;
    private int delay;
    private int availablePlaces;
    private double contribution;

    // Constructor for the Trajet class
    public Trajet(String transport, String departurePoint, String date, String time, int delay, int availablePlaces, double contribution) {
        this.transport = transport;
        this.departurePoint = departurePoint;
        this.date = date;
        this.time = time;
        this.delay = delay;
        this.availablePlaces = availablePlaces;
        this.contribution = contribution;
    }

    // Getters and Setters
    public String getTransport() {
        return transport;
    }

    public void setTransport(String transport) {
        this.transport = transport;
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

    public int getDelay() {
        return delay;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }

    public int getAvailablePlaces() {
        return availablePlaces;
    }

    public void setAvailablePlaces(int availablePlaces) {
        this.availablePlaces = availablePlaces;
    }

    public double getContribution() {
        return contribution;
    }

    public void setContribution(double contribution) {
        this.contribution = contribution;
    }
}
