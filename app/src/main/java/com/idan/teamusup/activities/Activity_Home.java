package com.idan.teamusup.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.facebook.AccessToken;
import com.firebase.ui.auth.AuthUI;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.idan.teamusup.R;


public class Activity_Home extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ImageView home_IMG_menu;
    private TextView home_TXT_title;

//    private ImageView home_IMG_user;
//    private MaterialTextView home_TXT_username;
//    private MaterialButton home_BTN_logout;
//    private MaterialButton home_BTN_players;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        findViews();
        initButtons();
        setupNavigationView();
        setUserDetails();
    }

    private void setupNavigationView() {
        navigationView.setItemIconTintList(null);
        NavController navController = Navigation.findNavController(
                this, R.id.navHostFragment);
        NavigationUI.setupWithNavController(navigationView, navController);
        navController.addOnDestinationChangedListener(
                (controller, destination, arguments) ->
                        home_TXT_title.setText(destination.getLabel()));
    }

    private void initButtons() {
        home_IMG_menu.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        //home_BTN_logout.setOnClickListener(v -> logout());

    }

    private void setUserDetails() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Log.d("Activity_Home", "setUserDetails() - user is null");
            return;
        }

        View headerView = navigationView.getHeaderView(0);
        MaterialTextView header_username = headerView.findViewById(R.id.header_username);
        ImageView header_imageProfile = headerView.findViewById(R.id.header_imageProfile);

        if (user.getDisplayName() != null) {
            header_username.setText(user.getDisplayName());
        }

        if (user.getPhotoUrl() != null) {
            String url = user.getPhotoUrl().toString() + "?access_token="
                    + AccessToken.getCurrentAccessToken().getToken();

            Log.d("Activity_Home", url);
            Glide
                    .with(this)
                    .load(url)
                    .into(header_imageProfile);
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
        drawerLayout = findViewById(R.id.home_drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        home_IMG_menu = findViewById(R.id.home_IMG_menu);
        home_TXT_title = findViewById(R.id.home_TXT_title);


/*        home_IMG_user = findViewById(R.id.home_IMG_user);
        home_TXT_username = findViewById(R.id.home_TXT_username);
        home_BTN_logout = findViewById(R.id.home_BTN_logout);
        home_BTN_players = findViewById(R.id.home_BTN_players);*/
    }
}