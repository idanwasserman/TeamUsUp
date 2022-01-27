package com.idan.teamusup.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.idan.teamusup.R;
import com.idan.teamusup.data.Constants;
import com.idan.teamusup.data.Generator;
import com.idan.teamusup.logic.RandomPlayers;
import com.idan.teamusup.services.FirebaseRealtimeDB;
import com.idan.teamusup.data.Instance;
import com.idan.teamusup.data.Location;
import com.idan.teamusup.services.MyCamera;
import com.idan.teamusup.services.MySharedPreferences;
import com.idan.teamusup.services.UserDatabase;
import com.idan.teamusup.logic.InstanceServiceImpl;
import com.idan.teamusup.services.MyLocation;
import com.idan.teamusup.logic.Validator;
import com.idan.teamusup.logic.interfaces.InstanceService;

import java.util.List;


public class Activity_DataLoading extends AppCompatActivity {

    private static final String TAG = "Activity_DataLoading_TAG";

    private FirebaseUser user;
    private InstanceService instanceService;
    private Instance userInstance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_loading);

        if (!isUserLoggedIn()) {
            Log.d(TAG, "User has not logged in properly");
            finish();
        }

        initHelpingObjects();
        loadUserInstance();
        loadDatabase();
        MyLocation.getInstance().getLastLocation(this.callBack_location);
    }

    private boolean isUserLoggedIn() {
        this.user = FirebaseAuth.getInstance().getCurrentUser();
        return this.user != null;
    }

    private void initHelpingObjects() {
        MySharedPreferences.init(this, getUniqueFilename());
        MyLocation.init(this);
        MyCamera.init();
        Generator.init();
        RandomPlayers.init();
        Validator.init(this);
        FirebaseRealtimeDB.init();
        this.instanceService = InstanceServiceImpl.init();
    }

    private String getUniqueFilename() {
        String email = this.user.getEmail();
        if (email != null && !email.isEmpty()) {
            return email;
        }
        String phone = this.user.getPhoneNumber();
        if (phone != null && !phone.isEmpty()) {
            return phone;
        }
        return this.user.getUid();
    }

    private void loadUserInstance() {
        this.userInstance = this.instanceService.getUserInstance(this.user);
        Boolean isNew = (Boolean) this.userInstance.getAttributes().get(Constants.isNew.name());
        if (isNew != null) {
            if (isNew) {
                Toast.makeText(this, "Hello new user", Toast.LENGTH_SHORT).show();
                this.userInstance.getAttributes().put(Constants.isNew.name(), false);
            } else {
                Toast.makeText(this, "Welcome back", Toast.LENGTH_SHORT).show();
            }
        } else {
            this.userInstance.getAttributes().put(Constants.isNew.name(), false);
        }
    }

    private void loadDatabase() {
        List<Instance> instances = this.instanceService.getDatabaseInstances(this.user);
        Toast.makeText(
                this,
                "Loaded " + instances.size() + " instances", // TODO delete this
                Toast.LENGTH_SHORT).show();

        UserDatabase.init(this.userInstance, instances);
    }

    private void updateUserInstance(double lat, double lng) {
        if (lat == 0 && lng == 0) {
            MyLocation.getInstance().getLastLocation(this.callBack_location);
        }

        this.userInstance.setLocation(new Location(lat, lng));
        this.instanceService.updateUserLocation(this.userInstance);

        final int DELAY_IN_SECONDS = 2;
        Handler handler = new Handler();
        handler.postDelayed(this.runnableMethod, DELAY_IN_SECONDS * 1000);
    }

    private void openHomeActivity() {
        startActivity(new Intent(getApplicationContext(), Activity_Home.class));
        finish();
    }

    MyLocation.CallBack_Location callBack_location = this::updateUserInstance;

    private final Runnable runnableMethod = this::openHomeActivity;
}