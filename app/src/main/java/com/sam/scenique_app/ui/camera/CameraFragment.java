package com.sam.scenique_app.ui.camera;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.sam.scenique_app.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class CameraFragment extends Fragment {

    private static final int PERMISSIONS_REQUEST_CODE = 100;
    private Uri photoUri;
    private FusedLocationProviderClient fusedLocationClient;
    private String photoURL;

    private static final int REQUEST_IMAGE_CAPTURE = 1;

    private static final String[] PERMISSIONS_REQUIRED = new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    public CameraFragment() {

    }

    public static CameraFragment newInstance() {
        return new CameraFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_camera, container, false);

        view.findViewById(R.id.open_camera_button).setOnClickListener(v -> handleCameraButtonClick());
        view.findViewById(R.id.submit_review_button).setOnClickListener(v -> submitButtonClick());

        return view;
    }

    private void handleCameraButtonClick() {
        if (hasPermissions()) {

            openCamera();
        } else {

            requestPermissions(PERMISSIONS_REQUIRED, PERMISSIONS_REQUEST_CODE);
        }
    }

    private boolean hasPermissions() {
        for (String permission : PERMISSIONS_REQUIRED) {
            if (ContextCompat.checkSelfPermission(requireContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        System.out.println("Resolved activity: " + takePictureIntent.resolveActivity(getActivity().getPackageManager()));
        System.out.println("Works 0");

        File photoFile = null;
        try {
            photoFile = createImageFile();
        } catch (IOException ex) {

            Toast.makeText(getContext(), "Error creating image file.", Toast.LENGTH_SHORT).show();
        }

        if (photoFile != null) {
            System.out.println("Works 2");
            photoUri = FileProvider.getUriForFile(getContext(),
                    getContext().getApplicationContext().getPackageName() + ".provider",
                    photoFile);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private File createImageFile() throws IOException {

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );
        return image;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length > 0 && Arrays.equals(grantResults, new int[]{PackageManager.PERMISSION_GRANTED, PackageManager.PERMISSION_GRANTED, PackageManager.PERMISSION_GRANTED})) {

                openCamera();
            } else {

                Toast.makeText(getContext(), "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            System.out.println("Photo Taken!");

            if (photoUri != null) {

                try {
                    InputStream stream = getActivity().getContentResolver().openInputStream(photoUri);
                    if (stream != null) {

                        uploadPhotoToFirebase(stream);
                    } else {
                        Toast.makeText(getContext(), "Failed to open photo file.", Toast.LENGTH_SHORT).show();
                    }
                } catch (FileNotFoundException e) {
                    Toast.makeText(getContext(), "Photo file not found.", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(getContext(), "Photo URI is null.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void uploadPhotoToFirebase(InputStream inputStream) {

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";

        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("photos/" + imageFileName);

        storageRef.putStream(inputStream)
                .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl().addOnSuccessListener(uri -> {

                    photoURL = uri.toString();
                }))
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to upload photo.", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                });
    }

    private void submitButtonClick() {
        if (photoURL == null) return;

        requestSingleLocationUpdate((latitude, longitude) -> submitReview(latitude, longitude, photoURL));
    }

    private void submitReview(double latitude, double longitude, String photoUrl) {
        RatingBar ratingBar = getView().findViewById(R.id.photo_rating_bar);
        EditText reviewText = getView().findViewById(R.id.photo_review_text);
        float rating = ratingBar.getRating();
        String review = reviewText.getText().toString();

        Map<String, Object> reviewMap = new HashMap<>();
        reviewMap.put("rating", rating);
        reviewMap.put("review", review);
        reviewMap.put("photoUrl", photoUrl);
        reviewMap.put("latitude", latitude);
        reviewMap.put("longitude", longitude);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("reviews")
                .add(reviewMap)
                .addOnSuccessListener(documentReference -> Toast.makeText(getContext(), "Review submitted successfully.", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Error submitting review.", Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
    }

    public interface LocationCallback {
        void onLocationResult(double latitude, double longitude);
    }

    private void requestSingleLocationUpdate(final LocationCallback callback) {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            System.out.println("Invalid permissions.");

            return;
        }

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setNumUpdates(1);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        fusedLocationClient.requestLocationUpdates(locationRequest, new com.google.android.gms.location.LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult != null && locationResult.getLastLocation() != null) {
                    Location location = locationResult.getLastLocation();
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    System.out.println("Lat: " + latitude + ", Lon: " + longitude);
                    callback.onLocationResult(latitude, longitude);

                    fusedLocationClient.removeLocationUpdates(this);
                } else {
                    System.out.println("Location is null");

                }
            }
        }, Looper.getMainLooper());
    }

}