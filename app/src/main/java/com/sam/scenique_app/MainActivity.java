package com.sam.scenique_app;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.identity.BeginSignInResult;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.auth.api.identity.SignInCredential;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.sam.scenique_app.databinding.ActivityMainBinding;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    private SignInClient oneTapClient;
    private FirebaseAuth mAuth;

    private BeginSignInRequest signInRequest;

    private TextView userEmailLabel;

    private FirebaseStorage storage;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView navView = findViewById(R.id.nav_view);
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_map)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        signInRequest = BeginSignInRequest.builder()
                .setPasswordRequestOptions(BeginSignInRequest.PasswordRequestOptions.builder()
                        .setSupported(true)
                        .build())
                .setGoogleIdTokenRequestOptions(BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                        .setSupported(true)
                        .setServerClientId(getString(R.string.default_web_client_id))
                        .build())
                .setAutoSelectEnabled(true)
                .build();

        oneTapClient = Identity.getSignInClient(this);
        storage = FirebaseStorage.getInstance("gs://sample-app-61683.appspot.com");
        firestore = FirebaseFirestore.getInstance();
    }

    private void upload() {
        StorageReference ref = storage.getReference();
        StorageReference mountainsRef = ref.child("mountains.jpg");

        int imageResource = getResources().getIdentifier("image", "drawable", getPackageName());
        try {
            InputStream stream = getResources().openRawResource(imageResource);
            mountainsRef.putStream(stream);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onResume() {
        super.onResume();
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        userEmailLabel = findViewById(R.id.text_home);

        if(userEmailLabel != null && user != null) {
            userEmailLabel.setText("Welcome " + user.getDisplayName());
        }

        if (user != null) {
            showLoggedTabs(true);
        } else {
            showLoggedTabs(false);
        }
    }

    public void showLoggedTabs(boolean show) {
        BottomNavigationView navView = findViewById(R.id.nav_view);
        Menu menu = navView.getMenu();

        MenuItem mapItem = menu.findItem(R.id.navigation_map);
        MenuItem cameraItem = menu.findItem(R.id.navigation_camera);
        if (mapItem != null && cameraItem != null) {
            System.out.println("Map Item found, Set visible");
            mapItem.setVisible(show);
            cameraItem.setVisible(show);
        }
    }

    public void onBtnClick(View view) {
        oneTapClient.beginSignIn(signInRequest)
                .addOnSuccessListener(this, new OnSuccessListener<BeginSignInResult>() {
                    @Override
                    public void onSuccess(BeginSignInResult result) {
                        try {
                            startIntentSenderForResult(
                                    result.getPendingIntent().getIntentSender(), 202,
                                    null, 0, 0, 0);
                        } catch (IntentSender.SendIntentException e) {
                            System.out.println(e.getLocalizedMessage());
                        }
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        System.out.println(e.getLocalizedMessage());
                    }
                });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 202:
                try {
                    SignInCredential credential = oneTapClient.getSignInCredentialFromIntent(data);
                    String idToken = credential.getGoogleIdToken();
                    if (idToken !=  null) {
                        AuthCredential firebaseCredential = GoogleAuthProvider.getCredential(idToken, null);
                        mAuth.signInWithCredential(firebaseCredential)
                                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (task.isSuccessful()) {

                                            System.out.println("Success");

                                            FirebaseUser user = mAuth.getCurrentUser();

                                            if(user != null) {
                                                userEmailLabel.setText("Welcome " + user.getEmail());
                                                showLoggedTabs(true);
                                            }
                                        } else {
                                            System.out.println("Fail");
                                        }
                                    }
                                });

                        System.out.println("Logged in " + credential.getGoogleIdToken());

                    }
                } catch (ApiException e) {
                    System.out.println("ERROR");
                    System.out.println(e.getMessage());
                }
                break;
        }
    }
}