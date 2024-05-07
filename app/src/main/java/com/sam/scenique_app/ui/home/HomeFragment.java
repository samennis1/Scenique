package com.sam.scenique_app.ui.home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.sam.scenique_app.LocationReview;
import com.sam.scenique_app.R;
import com.sam.scenique_app.ReviewAdapter;
import com.sam.scenique_app.databinding.FragmentHomeBinding;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private FirebaseFirestore db;

    FirebaseAuth mAuth;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textHome;
        homeViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        binding.logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logoutUser();
            }
        });

        getData();

        return root;
    }

    public void getData() {
        RecyclerView reviewsCarousel = binding.reviewsCarousel;
        reviewsCarousel.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        final ReviewAdapter reviewAdapter = new ReviewAdapter();
        reviewsCarousel.setAdapter(reviewAdapter);

        db = FirebaseFirestore.getInstance();
        db.collection("reviews").limit(10).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<LocationReview> reviews = new ArrayList<>();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    double longitude = document.getDouble("longitude");
                    double latitude = document.getDouble("latitude");
                    String photoUrl = document.getString("photoUrl");
                    double rating = document.getDouble("rating");
                    String review = document.getString("review");

                    reviews.add(new LocationReview(longitude, latitude, (float) rating, review, photoUrl));
                }
                reviewAdapter.setReviews(reviews);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        TextView userEmailLabel = getView().findViewById(R.id.text_home);

        if (userEmailLabel != null && user != null) {
            userEmailLabel.setText("Welcome " + user.getDisplayName());
        }

        Button logoutButton = getView().findViewById(R.id.logoutBtn);
        Button signinButton = getView().findViewById(R.id.signinBtn);

        if (user != null && logoutButton != null && signinButton != null) {
            logoutButton.setVisibility(View.VISIBLE);
            signinButton.setVisibility(View.GONE);
        } else if(logoutButton != null && signinButton != null) {
            logoutButton.setVisibility(View.GONE);
            signinButton.setVisibility(View.VISIBLE);
        }
    }

    public void showLoggedTabs(boolean show) {
        BottomNavigationView navView = getActivity().findViewById(R.id.nav_view);
        Menu menu = navView.getMenu();

        MenuItem mapItem = menu.findItem(R.id.navigation_map);
        MenuItem cameraItem = menu.findItem(R.id.navigation_camera);
        if (mapItem != null && cameraItem != null) {
            System.out.println("Map Item found, Set visible");
            mapItem.setVisible(show);
            cameraItem.setVisible(show);
        }
    }

    private void logoutUser() {
        FirebaseAuth.getInstance().signOut();
        showLoggedTabs(false);

        Button logoutButton = getView().findViewById(R.id.logoutBtn);
        Button signinButton = getView().findViewById(R.id.signinBtn);
        TextView userText = getView().findViewById(R.id.text_home);
        logoutButton.setVisibility(View.GONE);
        signinButton.setVisibility(View.VISIBLE);
        userText.setText("Welcome to Scenique");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}