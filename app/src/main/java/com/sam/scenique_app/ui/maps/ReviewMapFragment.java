package com.sam.scenique_app.ui.maps;

import androidx.fragment.app.Fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
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

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
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
        try {
            boolean success = mMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            getContext(), R.raw.map_style));

            if (!success) {
                Log.e("MapsActivityRaw", "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e("MapsActivityRaw", "Can't find style. Error: ", e);
        }

        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("reviews")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            double latitude = document.getDouble("latitude");
                            double longitude = document.getDouble("longitude");
                            float rating = document.getDouble("rating").floatValue();
                            String reviewText = document.getString("review");
                            String photoUrl = document.getString("photoUrl");

                            LocationReview review = new LocationReview(latitude, longitude, rating, reviewText, photoUrl);
                            LatLng position = new LatLng(review.getLatitude(), review.getLongitude());
                            builder.include(position);
                            Marker marker = mMap.addMarker(new MarkerOptions().position(position));
                            marker.setTag(review);
                        }

                        LatLngBounds bounds = builder.build();

                        int padding = 100;
                        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));
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



}