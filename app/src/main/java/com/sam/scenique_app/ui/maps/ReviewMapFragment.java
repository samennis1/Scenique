package com.sam.scenique_app.ui.maps;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.sam.scenique_app.LocationReview;
import com.sam.scenique_app.R;
import com.sam.scenique_app.databinding.FragmentReviewMapBinding;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ReviewMapFragment extends Fragment implements OnMapReadyCallback {
    private GoogleMap mMap;
    private FragmentReviewMapBinding binding;

    private LocationReview currentReview;
    private float currentRadius;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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
        setupMapStyle();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("reviews").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && !task.getResult().isEmpty()) {
                processDocuments(task.getResult());
            } else {
                Log.d("ReviewMapFragment", "Error getting documents: ", task.getException());
            }
        });

        mMap.setOnMarkerClickListener(marker -> {
            LocationReview review = (LocationReview) marker.getTag();
            if (review != null) {
                showReviewOverlay(review);
            }
            return true;
        });
    }

    private void setupMapStyle() {
        try {
            boolean success = mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getContext(), R.raw.map_style));
            if (!success) {
                Log.e("MapsActivityRaw", "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e("MapsActivityRaw", "Can't find style. Error: ", e);
        }
    }

    private void showReviewOverlay(LocationReview review) {
        Context context = getContext();

        ImageView image = new ImageView(context);
        image.setAdjustViewBounds(true);
        image.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        int maxHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 200, getResources().getDisplayMetrics());
        image.setMaxHeight(maxHeight);

        String imageUrl = review.getPhotoUrl();
        Glide.with(context)
                .load(imageUrl)
                .into(image);

        RatingBar ratingBar = new RatingBar(context, null, android.R.attr.ratingBarStyle);
        LinearLayout.LayoutParams ratingParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        ratingBar.setLayoutParams(ratingParams);
        ratingBar.setNumStars(5);
        ratingBar.setStepSize(1.0f);
        ratingBar.setRating(review.getRating());
        ratingBar.setIsIndicator(true);

        TextView messageView = new TextView(context);
        messageView.setText(review.getReviewText());
        messageView.setTextColor(Color.WHITE);
        messageView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);

        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(10, 10, 10, 10);
        layout.addView(image);
        layout.addView(ratingBar);
        layout.addView(messageView);

        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.CustomAlertDialog);
        builder.setTitle("Review");
        builder.setView(layout);
        builder.setPositiveButton("Close", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void processDocuments(QuerySnapshot result) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (QueryDocumentSnapshot document : result) {
            LocationReview review = new LocationReview(
                    document.getDouble("latitude"),
                    document.getDouble("longitude"),
                    document.getDouble("rating").floatValue(),
                    document.getString("review"),
                    document.getString("photoUrl")
            );
            LatLng position = new LatLng(review.getLatitude(), review.getLongitude());
            builder.include(position);
            mMap.addMarker(new MarkerOptions().position(position)).setTag(review);

            currentReview = review;
            currentRadius = 1000;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                addGeofence();
            }
        }
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 100));
    }

    @SuppressLint("MissingPermission")
    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void addGeofence() {
        if (!checkPermissions()) {
            return;
        }

        Geofence geofence = new Geofence.Builder()
                .setRequestId(createGeofenceRequestId(currentReview, currentRadius))
                .setCircularRegion(currentReview.getLatitude(), currentReview.getLongitude(), currentRadius)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                .build();

        GeofencingRequest geofencingRequest = new GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofence(geofence)
                .build();

        GeofencingClient geofencingClient = LocationServices.getGeofencingClient(getContext());
        geofencingClient.addGeofences(geofencingRequest, getGeofencePendingIntent())
                .addOnSuccessListener(aVoid -> Log.d("AddGeofence", "Geofences added successfully"))
                .addOnFailureListener(e -> Log.d("AddGeofence", "Failed to add geofences", e));
    }

    private boolean checkPermissions() {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 7);
            return false;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
                ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, 8);
            return false;
        }
        return true;
    }

    private String createGeofenceRequestId(LocationReview review, float radius) {
        return review.getLatitude() + "," + review.getLongitude() + "_" + radius;
    }

    private PendingIntent getGeofencePendingIntent() {
        Intent intent = new Intent(getContext(), GeofenceBroadcastReceiver.class);
        intent.setAction(GeofenceBroadcastReceiver.ACTION_RECEIVE_GEOFENCE);
        return PendingIntent.getBroadcast(getContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
    }
}