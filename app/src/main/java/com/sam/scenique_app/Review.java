package com.sam.scenique_app;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class Review {
    private String photoUrl;
    private String reviewText;
    private double rating;

    public Review() {
    }

    public Review(String photoUrl, String reviewText, double rating) {
        this.photoUrl = photoUrl;
        this.reviewText = reviewText;
        this.rating = rating;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getReviewText() {
        return reviewText;
    }

    public void setReviewText(String reviewText) {
        this.reviewText = reviewText;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }
}
