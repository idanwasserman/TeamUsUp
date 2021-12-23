package com.idan.teamusup.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.idan.teamusup.R;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final int RC_SIGN_IN = 123;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFirebaseAuth = FirebaseAuth.getInstance();

        mAuthStateListener = firebaseAuth -> {

            FirebaseUser user = firebaseAuth.getCurrentUser();

            if (user != null) {

                // if the user is already authenticated then we will redirect to Home Page
                Intent i = new Intent(MainActivity.this, Activity_Home.class);
                startActivity(i);
                finish();

            } else {

                startActivityForResult(
                        AuthUI.getInstance()
                                .createSignInIntentBuilder()
                                .setIsSmartLockEnabled(false)
                                .setAvailableProviders(getProviders())
                                .setLogo(R.drawable.icn_logo).setTheme(R.style.Theme)
                                .build(),
                        RC_SIGN_IN
                );

            }
        };
    }

    @NonNull
    private List<AuthUI.IdpConfig> getProviders() {
        final List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.FacebookBuilder().build(), // Facebook builder
                new AuthUI.IdpConfig.GoogleBuilder().build(),   // Google builder
                new AuthUI.IdpConfig.EmailBuilder().build(),    // Email builder
                new AuthUI.IdpConfig.PhoneBuilder().build()     // Phone builder
        );
        return providers;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
    }

}

