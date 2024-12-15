package com.example.gotoesig.model;

import java.util.List;
import java.io.Serializable;

public class Trip implements Serializable {
    private String contributionAmount;
    private String creatorId;
    private String date;
    private String delayTolerance;
    private double distance;
    private double duration;
    private List<String> participants;
    private int seatsAvailable;
    private String startPoint;
    private String time;
    private String transportType;


    public Trip() {
    }


    public Trip(String id,String contributionAmount, String creatorId, String date, String delayTolerance,
                double distance, double duration, List<String> participants, int seatsAvailable,
                String startPoint, String time, String transportType) {
        this.id=id;
        this.contributionAmount = contributionAmount;
        this.creatorId = creatorId;
        this.date = date;
        this.delayTolerance = delayTolerance;
        this.distance = distance;
        this.duration = duration;
        this.participants = participants;
        this.seatsAvailable = seatsAvailable;
        this.startPoint = startPoint;
        this.time = time;
        this.transportType = transportType;
    }
    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    // Getters y setters
    public String getContributionAmount() {
        return contributionAmount;
    }

    public void setContributionAmount(String contributionAmount) {
        this.contributionAmount = contributionAmount;
    }

    public String getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDelayTolerance() {
        return delayTolerance;
    }

    public void setDelayTolerance(String delayTolerance) {
        this.delayTolerance = delayTolerance;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public double getDuration() {
        return duration;
    }

    public void setDuration(double duration) {
        this.duration = duration;
    }

    public List<String> getParticipants() {
        return participants;
    }

    public void setParticipants(List<String> participants) {
        this.participants = participants;
    }

    public int getSeatsAvailable() {
        return seatsAvailable;
    }

    public void setSeatsAvailable(int seatsAvailable) {
        this.seatsAvailable = seatsAvailable;
    }

    public String getStartPoint() {
        return startPoint;
    }

    public void setStartPoint(String startPoint) {
        this.startPoint = startPoint;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getTransportType() {
        return transportType;
    }

    public void setTransportType(String transportType) {
        this.transportType = transportType;
    }


    @Override
    public String toString() {
        return "Trip{" +
                "contributionAmount='" + contributionAmount + '\'' +
                ", creatorId='" + creatorId + '\'' +
                ", date='" + date + '\'' +
                ", delayTolerance='" + delayTolerance + '\'' +
                ", distance=" + distance +
                ", duration=" + duration +
                ", participants=" + participants +
                ", seatsAvailable='" + seatsAvailable + '\'' +
                ", startPoint='" + startPoint + '\'' +
                ", time='" + time + '\'' +
                ", transportType='" + transportType + '\'' +
                '}';
    }


}