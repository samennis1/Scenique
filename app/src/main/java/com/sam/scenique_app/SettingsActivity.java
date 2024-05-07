package com.sam.scenique_app;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.sam.scenique_app.databinding.ActivityMainBinding;
import com.sam.scenique_app.ui.settings.SettingsFragment;

public class SettingsActivity extends AppCompatActivity {


    private ActivityMainBinding binding;

    private SignInClient oneTapClient;
    private FirebaseAuth mAuth;

    private BeginSignInRequest signInRequest;

    private TextView userEmailLabel;

    private FirebaseStorage storage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Load the SettingsFragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.settings_container, new SettingsFragment())
                .commit();
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onResume() {
        super.onResume();
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        userEmailLabel = findViewById(R.id.text_home);

        if (userEmailLabel != null && user != null) {
            userEmailLabel.setText(user.getDisplayName());
        }

    }

    SwitchCompat nightModeSwitch = findViewById(R.id.nightModeSwitch);

nightModeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
                // Apply night mode theme
                setTheme(R.style.Theme_Scenique_App_Night);
            } else {
                // Apply default theme
                setTheme(R.style.Theme_Scenique_App);
            }

            // Recreate the activity to apply the new theme
            recreate();
        }
    });
}