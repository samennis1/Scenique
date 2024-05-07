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

import com.google.firebase.auth.FirebaseAuth;
import com.sam.scenique_app.R;
import com.sam.scenique_app.ReadWriteUser;

public class ProfileFragment extends Fragment {

    private ProfileViewModel mViewModel;
    private TextView profileEmail;

    private TextView profileUsername;
    //    private ProgressBar progressBar;
    private TextView profileDescription;
    private String email, name;
    private ImageView profilePhoto;
    private FirebaseAuth authProfile;
    private float reviewCount;

    public static ProfileFragment newInstance() {
        return new ProfileFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
//        view.getSupportActionBar().setTitle("Profile");
        profileEmail = view.findViewById(R.id.textView_show_email);
        profileDescription = view.findViewById(R.id.textView_show_description);

        profileEmail.setText(ReadWriteUser.email);
        return view;
    }



    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        // TODO: Use the ViewModel
    }

}