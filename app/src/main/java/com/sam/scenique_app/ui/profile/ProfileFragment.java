package com.sam.scenique_app.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.sam.scenique_app.LocationReview;
import com.sam.scenique_app.MyReviewAdapter;
import com.sam.scenique_app.R;
import com.sam.scenique_app.ReadWriteUser;
import com.sam.scenique_app.ReviewAdapter;
import com.sam.scenique_app.databinding.FragmentProfileBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ProfileFragment extends Fragment {

    private ImageView profilePhoto;
    private FirebaseFirestore db;

    private TextView profileDescription;
    private TextView profileEmail;

    private FragmentProfileBinding binding;
    FirebaseAuth mAuth;

    public static ProfileFragment newInstance() {
        return new ProfileFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        ProfileViewModel profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        View view = inflater.inflate(R.layout.fragment_profile, container, false);


        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textViewShowUsername;;
        profileViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);


        profileEmail = view.findViewById(R.id.textView_show_username);
        profileDescription = view.findViewById(R.id.textView_show_description);
        profilePhoto = view.findViewById(R.id.imageView_profile_image);

        profileEmail.setText(ReadWriteUser.email);

        getReviews();

        return root;
    }

    public void onResume(){
        super.onResume();

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        TextView userName = getView().findViewById(R.id.textView_show_username);

        if(userName != null || user != null){
            userName.setText(user.getDisplayName());
        }
    }

    public void getReviews(){
        RecyclerView myReviews = binding.myRecyclerView;
        myReviews.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        FirebaseUser userf = FirebaseAuth.getInstance().getCurrentUser();
        String uid = userf.getUid();
        final MyReviewAdapter reviewAdapter = new MyReviewAdapter();
        myReviews.setAdapter(reviewAdapter);
        db = FirebaseFirestore.getInstance();
        db.collection("reviews").limit(10).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<LocationReview> reviews = new ArrayList<>();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String user = document.getString("uid");
                    if(Objects.equals(user, uid)) {
                        double longitude = document.getDouble("longitude");
                        double latitude = document.getDouble("latitude");
                        String photoUrl = document.getString("photoUrl");
                        double rating = document.getDouble("rating");
                        String review = document.getString("review");

                        System.out.println("Review added");
                        reviews.add(new LocationReview(user,longitude, latitude, (float) rating, review, photoUrl));
                    }
                }
                reviewAdapter.setReviews(reviews);
            }
        });
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}