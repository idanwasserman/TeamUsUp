package com.idan.teamusup.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.facebook.AccessToken;
import com.firebase.ui.auth.AuthUI;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.idan.teamusup.R;


public class Activity_Home extends AppCompatActivity {

    private ImageView home_IMG_user;
    private MaterialTextView home_TXT_username;
    private MaterialButton home_BTN_logout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        findViews();

        setUserDetails();

        home_BTN_logout.setOnClickListener(v -> logout());
    }

    private void setUserDetails() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Log.d("Activity_Home", "setUserDetails() - user is null");
            return;
        }

        home_TXT_username.setText(user.getDisplayName());

        Uri photoUrl = user.getPhotoUrl();

        if (photoUrl != null) {
            String url = photoUrl.toString() + "?access_token=" + AccessToken.getCurrentAccessToken().getToken();

            Log.d("Activity_Home", url);
            Glide
                    .with(this)
                    .load(url)
                    .into(home_IMG_user);
        } else {
            Log.d("Activity_Home", "user.getPhotoUrl() is null");
        }
    }

    private void logout() {
        AuthUI.getInstance()
                .signOut(Activity_Home.this)
                .addOnCompleteListener(task -> {

                    Toast.makeText(
                            Activity_Home.this,
                            "User Signed Out",
                            Toast.LENGTH_SHORT)
                            .show();

                    Intent i = new Intent(Activity_Home.this, MainActivity.class);
                    startActivity(i);
                });
    }

    private void findViews() {
        home_IMG_user = findViewById(R.id.home_IMG_user);
        home_TXT_username = findViewById(R.id.home_TXT_username);
        home_BTN_logout = findViewById(R.id.home_BTN_logout);
    }
}