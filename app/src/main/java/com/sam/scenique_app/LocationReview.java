package com.sam.scenique_app;

public class LocationReview {
    private String uid;
    private double latitude;
    private double longitude;
    private String photoUrl;
    private float rating;
    private String reviewText;

    public LocationReview(double latitude, double longitude, float rating, String reviewText, String photoUrl) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.rating = rating;
        this.reviewText = reviewText;
        this.photoUrl = photoUrl;
    }
    public LocationReview(String uid, double latitude, double longitude, float rating, String reviewText, String photoUrl){
        this.uid = uid;
        this.latitude = latitude;
        this.longitude = longitude;
        this.rating = rating;
        this.reviewText = reviewText;
        this.photoUrl = photoUrl;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public float getRating() {
        return rating;
    }

    public String getReviewText() {
        return reviewText;
    }
}