package com.openclassrooms.tourguide.dto;

public class AttractionUserDistance {

    private String attractionName;
    private double attractionLatitude;
    private double attractionLongitude;
    private double userLatitude;
    private double userLongitude;
    private double distanceUser;
    private double reward;

    public AttractionUserDistance(String attractionName, double attractionLatitude, double attractionLongitude,
            double userLatitude, double userLongitude, double distanceUser, double reward) {
        this.attractionName = attractionName;
        this.attractionLatitude = attractionLatitude;
        this.attractionLongitude = attractionLongitude;
        this.userLatitude = userLatitude;
        this.userLongitude = userLongitude;
        this.distanceUser = distanceUser;
        this.reward = reward;

    }

    public String getAttractionName() {
        return attractionName;
    }

    public void setAttractionName(String attractionName) {
        this.attractionName = attractionName;
    }

    public double getAttractionLatitude() {
        return attractionLatitude;
    }

    public void setAttractionLatitude(double attractionLatitude) {
        this.attractionLatitude = attractionLatitude;
    }

    public double getAttractionLongitude() {
        return attractionLongitude;
    }

    public void setAttractionLongitude(double attractionLongitude) {
        this.attractionLongitude = attractionLongitude;
    }

    public double getUserLatitude() {
        return userLatitude;
    }

    public void setUserLatitude(double userLatitude) {
        this.userLatitude = userLatitude;
    }

    public double getUserLongitude() {
        return userLongitude;
    }

    public void setUserLongitude(double userLongitude) {
        this.userLongitude = userLongitude;
    }

    public double getDistanceUser() {
        return distanceUser;
    }

    public void setDistanceUser(double distanceUser) {
        this.distanceUser = distanceUser;
    }

    public double getReward() {
        return reward;
    }

    public void setReward(double reward) {
        this.reward = reward;
    }

}
