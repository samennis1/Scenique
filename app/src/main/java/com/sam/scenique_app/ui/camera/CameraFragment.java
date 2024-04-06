package com.sam.scenique_app.ui.camera;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Firebase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.sam.scenique_app.R;

import org.w3c.dom.Document;

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
    private static final int REQUEST_IMAGE_CAPTURE = 1;


    private static final String[] PERMISSIONS_REQUIRED = new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    public CameraFragment() {
        // Required empty public constructor
    }

    public static CameraFragment newInstance() {
        return new CameraFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_camera, container, false);

        // This could be a button click listener where you check for permissions before opening the camera
        view.findViewById(R.id.open_camera_button).setOnClickListener(v -> handleCameraButtonClick());
        view.findViewById(R.id.submit_review_button).setOnClickListener(v -> submitReview());


        return view;
    }

    private void handleCameraButtonClick() {
        if (hasPermissions()) {
            // Permissions are granted, open the camera here
            openCamera();
        } else {
            // Permissions not granted, request them.
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
        // Ensure there's a camera activity to handle the intent
        System.out.println("Resolved activity: " + takePictureIntent.resolveActivity(getActivity().getPackageManager()));
        System.out.println("Works 0");
        // Create the File where the photo should go
        File photoFile = null;
        try {
            photoFile = createImageFile();
        } catch (IOException ex) {
            // Error occurred while creating the File
            Toast.makeText(getContext(), "Error creating image file.", Toast.LENGTH_SHORT).show();
        }
        // Continue only if the File was successfully created
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
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",   /* suffix */
                storageDir      /* directory */
        );
        return image;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length > 0 && Arrays.equals(grantResults, new int[]{PackageManager.PERMISSION_GRANTED, PackageManager.PERMISSION_GRANTED, PackageManager.PERMISSION_GRANTED})) {
                // All permissions have been granted
                openCamera(); // Proceed with opening the camera
            } else {
                // Permissions were denied. Handle the failure to obtain permission.
                Toast.makeText(getContext(), "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            System.out.println("Photo Taken!");
            // The image captured is saved in the file created (access using 'photoUri')
            // You can now upload the image to Firebase Storage and proceed with other tasks

            // Check if the photoUri is not null
            if (photoUri != null) {
                // Get the InputStream of the photo file
                try {
                    InputStream stream = getActivity().getContentResolver().openInputStream(photoUri);
                    if (stream != null) {
                        // Upload the photo to Firebase Storage
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
        // Create a reference to the Firebase Storage path where you want to upload the photo
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("photos").child("filename.jpg");
        uploadToFireStore();
        // Upload the photo to Firebase Storage
        storageRef.putStream(inputStream)
                .addOnSuccessListener(taskSnapshot -> {
                    // Photo uploaded successfully
                    Toast.makeText(getContext(), "Photo uploaded successfully.", Toast.LENGTH_SHORT).show();

                    // Proceed with other tasks or update UI as needed
                })
                .addOnFailureListener(e -> {
                    // Error occurred while uploading photo
                    Toast.makeText(getContext(), "Failed to upload photo.", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                });
    }

    private void uploadToFireStore() {
        // Create a reference to the Firebase Storage path where you want to upload the photo
        FirebaseFirestore firebase = FirebaseFirestore.getInstance();

        Map<String, Object> user = new HashMap<>();
        user.put("first", "Ada");
        user.put("last", "Lovelace");
        user.put("born", 1815);

        System.out.println("Uploading.,..");

        firebase.collection("users")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d("TAG", document.getId() + " => " + document.getData());
                            }
                        } else {
                            Log.w("TAG", "Error getting documents.", task.getException());
                        }
                    }
                });


        firebase.collection("users")
                .add(user)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d("TAG", "DocumentSnapshot added with ID: " + documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("TAG", "Error adding document", e);
                    }
                });


    }

    private void submitReview() {
        RatingBar ratingBar = getView().findViewById(R.id.photo_rating_bar);
        EditText reviewText = getView().findViewById(R.id.photo_review_text);
        float rating = ratingBar.getRating();
        String review = reviewText.getText().toString();

        // Check if a photo was taken
        if (photoUri == null) {
            Toast.makeText(getContext(), "Please take a photo first.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Assuming the photo has been uploaded and you have the URL
        String photoUrl = "URL of the uploaded photo"; // Replace this with the actual URL after upload

        // Create a new review object
        Map<String, Object> reviewMap = new HashMap<>();
        reviewMap.put("rating", rating);
        reviewMap.put("review", review);
        reviewMap.put("photoUrl", photoUrl);

        // Save review object to Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("reviews")
                .add(reviewMap)
                .addOnSuccessListener(documentReference -> Toast.makeText(getContext(), "Review submitted successfully.", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Error submitting review.", Toast.LENGTH_SHORT).show());
    }


}
