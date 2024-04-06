package com.sam.scenique_app.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.sam.scenique_app.R;
import com.sam.scenique_app.databinding.FragmentHomeBinding;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textHome;
        homeViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        // Assuming the logout button ID is logoutBtn in your fragment_home.xml
        binding.logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logoutUser();
            }
        });

        return root;
    }

    public void showLoggedTabs(boolean show) {
        BottomNavigationView navView = getActivity().findViewById(R.id.nav_view);
        Menu menu = navView.getMenu();

        // Assuming the ID for the map tab is navigation_map
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
        // Navigate back to the login screen or another appropriate screen
        // Make sure to define the navigation action in your nav_graph.xml if using Navigation Component
        // This example assumes you have an action defined as action_homeFragment_to_loginFragment
//        Navigation.findNavController(binding.getRoot()).navigate(R.id.action_homeFragment_to_loginFragment);

        // If you're not using the Navigation Component, use other methods to navigate.
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
