package com.sam.scenique_app.ui.home;

import static com.sam.scenique_app.MainActivity.profileFragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.sam.scenique_app.MainActivity;
import com.sam.scenique_app.R;
import com.sam.scenique_app.databinding.FragmentHomeBinding;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        HomeViewModel homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
//        if (profileFragment != null) {
//            Log.i("HomeFraqgmgnet", "onCreateView: ");
//            FragmentTransaction fragmentTransaction =getActivity().getSupportFragmentManager().beginTransaction();
//
//            fragmentTransaction.replace(R.id.nav_host_fragment_activity_main, profileFragment);
//            fragmentTransaction.addToBackStack("viewHome");
//            fragmentTransaction.commit();
//
//        }

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

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}