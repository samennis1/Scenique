package com.sam.scenique_app.ui.maps;

import androidx.fragment.app.Fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.sam.scenique_app.LocationReview;
import com.sam.scenique_app.R;
import com.sam.scenique_app.databinding.FragmentReviewMapBinding;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ReviewMapFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FragmentReviewMapBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentReviewMapBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NotNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("reviews")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            double latitude = document.getDouble("latitude");
                            double longitude = document.getDouble("longitude");
                            float rating = document.getDouble("rating").floatValue();
                            String reviewText = document.getString("review");
                            String photoUrl = document.getString("photoUrl");

                            LocationReview review = new LocationReview(latitude, longitude, rating, reviewText, photoUrl);
                            LatLng position = new LatLng(review.getLatitude(), review.getLongitude());
                            Marker marker = mMap.addMarker(new MarkerOptions().position(position));
                            marker.setTag(review);
                        }

                        if (!task.getResult().isEmpty()) {
                            QueryDocumentSnapshot firstDocument = (QueryDocumentSnapshot) task.getResult().getDocuments().get(0);
                            double latitude = firstDocument.getDouble("latitude");
                            double longitude = firstDocument.getDouble("longitude");
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 10));
                        }
                    } else {
                        Log.d("ReviewMapFragment", "Error getting documents: ", task.getException());
                    }
                });

        mMap.setOnMarkerClickListener(marker -> {
            LocationReview review = (LocationReview) marker.getTag();
            if (review != null) {
                showReviewOverlay(review.getRating(), review.getReviewText());
            }
            return true;
        });
    }

    private void showReviewOverlay(float rating, String reviewText) {
        Context context = getContext();

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Review");

        String message = "Rating: " + rating + "/5 stars\n\nReview:\n" + reviewText;
        builder.setMessage(message);

        builder.setPositiveButton("Close", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

}