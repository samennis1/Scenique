package com.sam.scenique_app.ui.home;

import android.graphics.Color;
import android.os.Bundle;
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
import androidx.navigation.Navigation;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.sam.scenique_app.R;
import com.sam.scenique_app.databinding.FragmentHomeBinding;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
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

        return root;
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
            logoutButton.setBackgroundColor(getResources().getColor(R.color.button_color));
            signinButton.setBackgroundColor(Color.GRAY);
        } else if(logoutButton != null && signinButton != null) {
            logoutButton.setBackgroundColor(Color.GRAY);
            signinButton.setBackgroundColor(getResources().getColor(R.color.button_color));
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
        logoutButton.setBackgroundColor(Color.GRAY);
        signinButton.setBackgroundColor(getResources().getColor(R.color.button_color));
        userText.setText("Welcome to Scenique");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}