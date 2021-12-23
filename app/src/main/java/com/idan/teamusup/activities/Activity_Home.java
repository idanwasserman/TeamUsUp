package com.idan.teamusup.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.material.button.MaterialButton;
import com.idan.teamusup.R;


public class Activity_Home extends AppCompatActivity {

    private MaterialButton home_BTN_logout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        findViews();

        home_BTN_logout.setOnClickListener(v -> logout());
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
        home_BTN_logout = findViewById(R.id.home_BTN_logout);
    }
}