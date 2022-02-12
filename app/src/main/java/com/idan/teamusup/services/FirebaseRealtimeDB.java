package com.idan.teamusup.services;

import android.util.Log;

import androidx.annotation.NonNull;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.idan.teamusup.data.Instance;

import java.util.ArrayList;
import java.util.List;

public class FirebaseRealtimeDB {

    private static FirebaseRealtimeDB realtimeDB;
    private final FirebaseDatabase database;
    private final GeoFire geoFire;
    private GeoQuery geoQuery;

    private static final String TAG = "FirebaseRealtimeDB_TAG";

    private static final String USERS_PATH = "USERS";
    private static final String USERS_LOCATIONS_PATH = "USERS_LOCATIONS";


    private FirebaseRealtimeDB() {
        this.database = FirebaseDatabase.getInstance();
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

    public interface CallBack_UsersIds {
        void usersIdsReady(List<String> usersIds, CallBack_Users callBack_users);
    }

    public interface CallBack_Users {
        void usersReady(List<Instance> users);
    }

    /**
     * updating the user's location in the db
     * @param user the user that will be updated
     */
    public void updateUserLocation(Instance user) {
        String userId = user.getId();
        double lat = user.getLocation().getLat();
        double lng = user.getLocation().getLng();

        // Update user location
        this.geoFire.setLocation(
                userId,
                new GeoLocation(lat, lng),
                (key, error) -> {
            if (error != null) {
                Log.d(TAG, "There was an error saving the location to GeoFire: " + error);
            } else {
                Log.d(TAG, "Location saved on server successfully!");
            }
        });

        // Update user instance
        this.database.getReference(USERS_PATH).child(userId).setValue(user);
    }

    /**
     * get all nearby users that are in the db and close to the using user
     * @param user the user that invoked the query and to search around his location
     * @param radius the radius [KM] to search
     * @param callBack_usersIds call back to return a list of ids around the user
     * @param callBack_users call back to pass on the next method
     */
    public void getNearbyUsersIds(Instance user, double radius, CallBack_UsersIds callBack_usersIds, CallBack_Users callBack_users) {
        List<String> usersIds = new ArrayList<>();
        if (this.geoQuery == null) {
            this.geoQuery = this.geoFire.queryAtLocation(
                    new GeoLocation(
                            user.getLocation().getLat(),
                            user.getLocation().getLng()),
                    radius);
            this.geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
                @Override
                public void onKeyEntered(String key, GeoLocation location) {
                    usersIds.add(key);
                }

                @Override
                public void onKeyExited(String key) {

                }

                @Override
                public void onKeyMoved(String key, GeoLocation location) {

                }

                @Override
                public void onGeoQueryReady() {
                    if (callBack_usersIds == null) {
                        Log.d(TAG, "CallBack_UsersIds is null");
                    } else {
                        usersIds.remove(user.getId());
                        callBack_usersIds.usersIdsReady(usersIds, callBack_users);
                    }
                }

                @Override
                public void onGeoQueryError(DatabaseError error) {
                    Log.d(TAG, "There was an error with this query: " + error);
                }
            });
        } else {
            this.geoQuery.setCenter(new GeoLocation(
                    user.getLocation().getLat(),
                    user.getLocation().getLng()));
        }
    }

    /**
     * get all users in database by a list of ids
     * @param usersIds a list of users ids to find in db
     * @param callBack_users call back to return list of users when process is done
     */
    public void getAllUsersByIds(List<String> usersIds, CallBack_Users callBack_users) {
        this.database
                .getReference(USERS_PATH)
                .addListenerForSingleValueEvent(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                List<Instance> users = new ArrayList<>();
                                for (DataSnapshot child : snapshot.getChildren()) {
                                    try {
                                        Instance user = child.getValue(Instance.class);
                                        assert user != null;
                                        if (usersIds.contains(user.getId())) {
                                            users.add(user);
                                        }
                                    } catch (Exception ignored) {}
                                }
                                if (callBack_users != null) {
                                    callBack_users.usersReady(users);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
    }

}
