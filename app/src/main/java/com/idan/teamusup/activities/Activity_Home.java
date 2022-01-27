package com.idan.teamusup.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.bumptech.glide.Glide;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textview.MaterialTextView;
import com.idan.teamusup.R;
import com.idan.teamusup.data.Constants;
import com.idan.teamusup.data.Instance;
import com.idan.teamusup.fragments.Fragment_Profile;
import com.idan.teamusup.logic.InstanceServiceImpl;
import com.idan.teamusup.logic.interfaces.InstanceService;
import com.idan.teamusup.services.UserDatabase;


public class Activity_Home extends AppCompatActivity
        implements Fragment_Profile.OnCompleteEditingListener {

    private static final String TAG = "Activity_Home_TAG";

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private View headerView;
    private ImageView home_IMG_menu;
    private TextView home_TXT_title;

    private InstanceService instanceService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        findViews();

        this.home_IMG_menu.setOnClickListener(v -> this.drawerLayout.openDrawer(GravityCompat.START));
        this.instanceService = InstanceServiceImpl.getService();
        this.headerView = this.navigationView.getHeaderView(0);

        Instance userInstance = UserDatabase.getDatabase().getUser();

        setHeader(userInstance.getName(), this.headerView);
        setPhoto((String) userInstance.getAttributes().get(Constants.photoUrl.name()), this.headerView);
        setupNavigationView();
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.instanceService.saveData();
    }

    private void setupNavigationView() { this.navigationView.setItemIconTintList(null);
        NavController navController = Navigation.findNavController(
                this, R.id.navHostFragment);
        NavigationUI.setupWithNavController(this.navigationView, navController);
        navController.addOnDestinationChangedListener(
                (controller, destination, arguments) ->
                        this.home_TXT_title.setText(destination.getLabel()));
    }

    private void setHeader(String username, View headerView) {
        MaterialTextView header_username = headerView.findViewById(R.id.header_username);
        header_username.setText(username);
    }

    private void setPhoto(String url, View headerView) {
        ImageView header_imageProfile = headerView.findViewById(R.id.header_imageProfile);
        if (url != null && !url.isEmpty()) {
            Glide
                    .with(this)
                    .load(url)
                    .into(header_imageProfile);
        }
    }

    private void findViews() {
        this.drawerLayout = findViewById(R.id.home_drawerLayout);
        this.navigationView = findViewById(R.id.navigationView);
        this.home_IMG_menu = findViewById(R.id.home_IMG_menu);
        this.home_TXT_title = findViewById(R.id.home_TXT_title);
    }

    @Override
    public void editUsername(String username) {
        setHeader(username, this.headerView);
        UserDatabase.getDatabase()
                .getUser()
                .setName(username);
    }

    @Override
    public void editProfilePicture(String photoUrl) {
        setPhoto(photoUrl, this.headerView);
        UserDatabase.getDatabase()
                .getUser()
                .getAttributes()
                .put(Constants.photoUrl.name(), photoUrl);
    }
}

