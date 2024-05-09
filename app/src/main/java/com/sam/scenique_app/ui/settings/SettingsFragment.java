package com.sam.scenique_app.ui.settings; // Adjust your package name

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.sam.scenique_app.AboutUsActivity;
import com.sam.scenique_app.FAQActivity;
import com.sam.scenique_app.MainActivity;
import com.sam.scenique_app.R; // Import your R class
import com.sam.scenique_app.SettingsActivity;

public class SettingsFragment extends Fragment {
    private View myReviewsView;
    private View LogoutView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_settings, container, false);

        View FAQsView = rootView.findViewById(R.id.FAQsView);
        FAQsView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openActivity(FAQActivity.class);
            }
        });

        View aboutUsView = rootView.findViewById(R.id.AboutUsView);
       aboutUsView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openActivity(AboutUsActivity.class);
            }
        });

        return rootView;
    }






    private void openActivity(Class targetActivity) {
        try {
            Intent intent = new Intent(getActivity(), targetActivity);
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Log.e("SettingsFragment", "Error launching activity:", e);

            Toast.makeText(getActivity(), "Error occurred when opening activity", Toast.LENGTH_SHORT).show();
        }
    }

}



