package com.sam.scenique_app;

import static android.content.ContentValues.TAG;
import static androidx.core.app.ActivityCompat.startIntentSenderForResult;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class SignIn extends AppCompatActivity {
    private static final int REQ_ONE_TAP = 2;  // Can be any integer unique to the Activity.

    private Context context; // Pass context from MainActivity
    private SignInClient oneTapClient;
    private BeginSignInRequest signInRequest;
    private boolean showOneTapUI = true;

    private FirebaseAuth mAuth;

    public SignIn(Context context) {
        this.context = context;
        mAuth = FirebaseAuth.getInstance();

        oneTapClient = Identity.getSignInClient(context);
        signInRequest = BeginSignInRequest.builder()
                .setGoogleIdTokenRequestOptions(BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                        .setSupported(true)
                        // Your server's client ID, not your Android client ID.
                        .setServerClientId(context.getString(R.string.default_web_client_id))
                        // Only show accounts previously used to sign in.
                        .setFilterByAuthorizedAccounts(false)
                        .build())
                // Automatically sign in when exactly one credential is retrieved.
                .setAutoSelectEnabled(true)
                .build();
    }

    public void startSignIn(ActivityResultLauncher<Intent> launcher) {
        oneTapClient.beginSignIn(signInRequest)
                .addOnSuccessListener(this, result -> {
                    loginResultHandler.launch(new IntentSenderRequest.Builder(result.getPendingIntent().getIntentSender()).build());
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // No saved credentials found. Launch the One Tap sign-up flow, or
                        // do nothing and continue presenting the signed-out UI.
                        Log.d(TAG, e.getLocalizedMessage());
                    }
                });
    }

    private ActivityResultLauncher<IntentSenderRequest> loginResultHandler = registerForActivityResult(new ActivityResultContracts.StartIntentSenderForResult(), result -> {
        // handle intent result here
        if (result.getResultCode() == RESULT_OK) {
            SignInCredential credential = null;
            try {
                credential = oneTapClient.getSignInCredentialFromIntent(result.getData());
                String idToken = credential.getGoogleIdToken();
                String username = credential.getId();
                String password = credential.getPassword();
                if (idToken != null) {
                    // Got an ID token from Google. Use it to authenticate
                    // with your backend.
                    Log.d(TAG, "Got ID token.");
                } else if (password != null) {
                    // Got a saved username and password. Use them to authenticate
                    // with your backend.
                    Log.d(TAG, "Got password.");
                }
            } catch (ApiException e) {
                e.printStackTrace();
            }
        }
        else {
            //...
        }
    });

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_ONE_TAP:
                try {
                    SignInCredential credential = oneTapClient.getSignInCredentialFromIntent(data);
                    String idToken = credential.getGoogleIdToken();
                    if (idToken !=  null) {
                        // Got an ID token from Google. Use it to authenticate
                        // with Firebase.
                        Log.d(TAG, "Got ID token.");
                        // Got an ID token from Google. Use it to authenticate
                        // with Firebase.
                        AuthCredential firebaseCredential = GoogleAuthProvider.getCredential(idToken, null);
                        mAuth.signInWithCredential(firebaseCredential)
                                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (task.isSuccessful()) {
                                            // Sign in success, update UI with the signed-in user's information
                                            Log.d(TAG, "signInWithCredential:success");
                                            FirebaseUser user = mAuth.getCurrentUser();
//                                            updateUI(user);
                                        } else {
                                            // If sign in fails, display a message to the user.
                                            Log.w(TAG, "signInWithCredential:failure", task.getException());
//                                            updateUI(null);
                                        }
                                    }
                                });
                    }
                } catch (ApiException e) {
                    // ...
                }
                break;
        }
    }
}
