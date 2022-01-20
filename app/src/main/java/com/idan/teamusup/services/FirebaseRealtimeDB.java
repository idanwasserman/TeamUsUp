package com.idan.teamusup.services;

import android.util.Log;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.idan.teamusup.data.Instance;

public class FirebaseRealtimeDB {

    private static FirebaseRealtimeDB realtimeDB;
    private final FirebaseDatabase database;
    private final GeoFire geoFire;
    private GeoQuery geoQuery;

    private static final String TAG = "FirebaseRealtimeDB_TAG";
    private static final String HOST = "10.0.2.2";
    private static final int PORT = 9000;

    private static final String USERS_PATH = "USERS";
    private static final String USERS_LOCATIONS_PATH = "USERS_LOCATIONS";


    private FirebaseRealtimeDB() {
        this.database = FirebaseDatabase.getInstance();
//        this.database.useEmulator(HOST, PORT);
        this.geoFire = new GeoFire(this.database.getReference(USERS_LOCATIONS_PATH));
    }

    public static void init() {
        if (realtimeDB == null) {
            realtimeDB = new FirebaseRealtimeDB();
        }
    }

    public static FirebaseRealtimeDB getRealtimeDB() {
        return realtimeDB;
    }

    public void updateUserLocation(Instance user) {
        String userId = user.getId();
        double lat = user.getLocation().getLat();
        double lng = user.getLocation().getLng();

        // Update user location
        this.geoFire.setLocation(userId, new GeoLocation(lat, lng), (key, error) -> {
            if (error != null) {
                Log.d(TAG, "There was an error saving the location to GeoFire: " + error);
            } else {
                Log.d(TAG, "Location saved on server successfully!");
            }
        });

        // Update user instance
        this.database.getReference(USERS_PATH).child(userId).setValue(user);
    }

    public void getNearbyUsers(double lat, double lng, double radius) {
        this.counter = 0;

        if (this.geoQuery == null) {
            this.geoQuery = this.geoFire.queryAtLocation(new GeoLocation(lat, lng), radius);
            this.geoQuery.addGeoQueryEventListener(this.geoQueryEventListener);
        } else {
            this.geoQuery.setCenter(new GeoLocation(lat, lng));
        }
    }

    private int counter;

    private final GeoQueryEventListener geoQueryEventListener = new GeoQueryEventListener() {
        @Override
        public void onKeyEntered(String key, GeoLocation location) {
            counter++;
            Log.d(TAG,
                    String.format(
                            "Key %s entered the search area at [%f,%f]",
                            key, location.latitude, location.longitude));
        }

        @Override
        public void onKeyExited(String key) {
            Log.d(TAG, String.format("Key %s is no longer in the search area", key));
        }

        @Override
        public void onKeyMoved(String key, GeoLocation location) {
            Log.d(TAG,
                    String.format(
                            "Key %s moved within the search area to [%f,%f]",
                            key, location.latitude, location.longitude));
        }

        @Override
        public void onGeoQueryReady() {
            Log.d(TAG, String.format("All initial data has been loaded and events have been fired! Found %d users", counter));
        }

        @Override
        public void onGeoQueryError(DatabaseError error) {
            Log.d(TAG, "There was an error with this query: " + error);
        }
    };

}
