package com.sam.scenique_app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.SwitchCompat;

import com.google.firebase.auth.FirebaseAuth;


public class SettingsActivity extends AppCompatActivity implements OnClickListener{

    ImageView backButton;
    ImageView profileImage;
    TextView displayName;
    AppCompatButton editProfileButton;
    RelativeLayout aboutUsLayout;
    SwitchCompat nightModeSwitch, notificationSwitch, privateAccountSwitch;

    FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_settings);







      /* private void checkSignedInUser() {
            GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
            if (account != null) {
                displayName.setText(account.getDisplayName());
                String profilePictureUrl = String.valueOf(account.getPhotoUrl());
                if (profileImage != null) {
                    Glide.with(this).load(profilePictureUrl).into(profileImage);
                }


                String email = account.getEmail();
            }*/


    }

   /* public void onResume() {
        super.onResume();

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        assert getView() != null;
        TextView userEmailLabel = getView().findViewById(R.id.display_name);

        if (userEmailLabel != null && user != null) {
            displayName.setText(user.getDisplayName());
        }


    }

    private View getView() {
        return null;
    }*/

    @Override
    public void onClick(View v) {

    }
}


