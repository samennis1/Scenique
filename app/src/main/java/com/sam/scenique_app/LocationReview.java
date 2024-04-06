package com.sam.scenique_app;

public class LocationReview {
    public double latitude;
    public double longitude;
    public float rating; // x out of 5 stars
    public String reviewText;

    public LocationReview(double latitude, double longitude, float rating, String reviewText) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.rating = rating;
        this.reviewText = reviewText;
    }
}