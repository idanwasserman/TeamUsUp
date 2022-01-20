package com.idan.teamusup.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.idan.teamusup.R;

import java.util.Arrays;
import java.util.List;

public class Activity_Login extends AppCompatActivity {

    private static final String TAG = "TAG_Activity_Login";

    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;
    private FirebaseAuth.AuthStateListener authStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        this.firebaseAuth = FirebaseAuth.getInstance();
        this.authStateListener = firebaseAuth -> checkUserAuthentication();
    }

    private void checkUserAuthentication() {
        this.user = firebaseAuth.getCurrentUser();

        if (this.user != null) {
            // if the user is already authenticated then we will redirect to Home Page
            openDataLoadingActivity();
        } else {
            this.activityResultLauncher.launch(AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setIsSmartLockEnabled(false)
                    .setAvailableProviders(getProviders())
                    .setLogo(R.drawable.icn_logo).setTheme(R.style.Theme)
                    .build());
        }
    }

    final ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    openDataLoadingActivity();
                } else {
                    Log.d(TAG, "Login result is not OK!");
                }
            });

    private void openDataLoadingActivity() {
        startActivity(new Intent(getApplicationContext(), Activity_DataLoading.class));
        finish();
    }

    @NonNull
    private List<AuthUI.IdpConfig> getProviders() {
        return Arrays.asList(
                new AuthUI.IdpConfig.FacebookBuilder().build(), // Facebook builder
                new AuthUI.IdpConfig.GoogleBuilder().build(),   // Google builder
                new AuthUI.IdpConfig.EmailBuilder().build(),    // Email builder
                new AuthUI.IdpConfig.PhoneBuilder().build()     // Phone builder
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        firebaseAuth.removeAuthStateListener(authStateListener);
    }

}
