package com.idan.teamusup.activities;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.idan.teamusup.services.LocaleHelper;
import com.idan.teamusup.R;
import com.idan.teamusup.data.Constants;
import com.idan.teamusup.services.Generator;
import com.idan.teamusup.logic.GameServiceImpl;
import com.idan.teamusup.logic.MyRandom;
import com.idan.teamusup.logic.PlayerServiceImpl;
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
    private static final int MILLIS_IN_ONE_SEC = 1000;

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
        changeLanguage();
        anotherInit();
        MyLocation.getInstance().getLastLocation(this.callBack_location);
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        for (int result : grantResults) {
            if (result == PackageManager.PERMISSION_DENIED) {
                String text = getResources().getString(R.string.location_permissions_denied_msg);
                Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
                openHomeActivity();
                return;
            }
        }
        MyLocation.getInstance().getLastLocation(this.callBack_location);
    }

    private void changeLanguage() {
        String currLanguage = LocaleHelper.getCurrLanguage();
        String lastLanguage = MySharedPreferences.getInstance().getString(
                Generator.getInstance().createKey(this.userInstance.getId(), Constants.language.name()),
                currLanguage);
        if (currLanguage.equals(lastLanguage)) return;

        LocaleHelper.setLocale(this, lastLanguage);
    }

    private void anotherInit() {
        GameServiceImpl.init(getResources());
    }

    private boolean isUserLoggedIn() {
        this.user = FirebaseAuth.getInstance().getCurrentUser();
        return this.user != null;
    }

    private void initHelpingObjects() {
        this.instanceService = InstanceServiceImpl.init();
        MySharedPreferences.init(this, getUniqueFilename());
        MyLocation.init(this);
        MyCamera.init();
        Generator.init();
        MyRandom.init();
        Validator.init(this);
        FirebaseRealtimeDB.init();
        PlayerServiceImpl.init();
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
                Toast.makeText(
                        this,
                        getResources().getString(R.string.hello_new_user),
                        Toast.LENGTH_SHORT).show();
                this.userInstance.getAttributes().put(Constants.isNew.name(), false);
            } else {
                String text = new StringBuilder(getResources().getString(R.string.welcome_back))
                        .append(" ")
                        .append(this.userInstance.getName())
                        .toString();
                Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
            }
        } else {
            this.userInstance.getAttributes().put(Constants.isNew.name(), false);
        }
    }

    private void loadDatabase() {
        List<Instance> instances = this.instanceService.getDatabaseInstances(this.user);
        UserDatabase.init(this.userInstance, instances);
    }

    private void updateUserInstance(double lat, double lng) {
        this.userInstance.setLocation(new Location(lat, lng));
        this.instanceService.updateUserLocation(this.userInstance);

        final int DELAY_IN_SECONDS = 2;
        Handler handler = new Handler();
        handler.postDelayed(this.runnableMethod, DELAY_IN_SECONDS * MILLIS_IN_ONE_SEC);
    }

    private void openHomeActivity() {
        startActivity(new Intent(getApplicationContext(), Activity_Home.class));
        finish();
    }

    MyLocation.CallBack_Location callBack_location = this::updateUserInstance;

    private final Runnable runnableMethod = this::openHomeActivity;
}