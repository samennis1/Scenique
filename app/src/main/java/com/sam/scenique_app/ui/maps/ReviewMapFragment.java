package com.sam.scenique_app.ui.maps;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

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
import com.sam.scenique_app.LocationReview;
import com.sam.scenique_app.R;
import com.sam.scenique_app.databinding.FragmentReviewMapBinding;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ReviewMapFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FragmentReviewMapBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
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
        LatLng sydney = new LatLng(-34, 151);
        List<LocationReview> locationReviews = new ArrayList<>();
        locationReviews.add(new LocationReview(-34.0, 151.0, 4.5f, "A breathtaking view of the ocean."));
        locationReviews.add(new LocationReview(-33.872, 151.205, 4.0f, "Lovely place for a morning run."));
        for (LocationReview review : locationReviews) {
            LatLng position = new LatLng(review.latitude, review.longitude);
            Marker marker = mMap.addMarker(new MarkerOptions().position(position));
            marker.setTag(review); // Store the review object for later retrieval
        }

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                LocationReview review = (LocationReview) marker.getTag();
                if (review != null) {
                    showReviewOverlay(review.rating, review.reviewText);
                }
                return true;
            }
        });

        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
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
