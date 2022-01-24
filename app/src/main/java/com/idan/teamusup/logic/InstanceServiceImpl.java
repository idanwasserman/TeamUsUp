package com.idan.teamusup.logic;

import android.util.Log;

import com.facebook.AccessToken;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.idan.teamusup.data.Constants;
import com.idan.teamusup.data.Generator;
import com.idan.teamusup.services.FirebaseRealtimeDB;
import com.idan.teamusup.data.Instance;
import com.idan.teamusup.data.InstanceType;
import com.idan.teamusup.data.Location;
import com.idan.teamusup.services.MySharedPreferences;
import com.idan.teamusup.services.UserDatabase;
import com.idan.teamusup.logic.interfaces.InstanceService;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

public class InstanceServiceImpl implements InstanceService {

    private static final String TAG = "InstanceServiceImpl_TAG";

    @Override
    public Instance createInstance(Instance user, Instance instance) {
        instance.setId(UUID.randomUUID().toString());
        if (instance.getCreatedTimestamp() == null) {
            instance.setCreatedTimestamp(new Date());
        }
        instance.setCreatedByUserId(user.getId());
        instance.setLocation(user.getLocation());

        // save instance in user database
        UserDatabase.getDatabase().addInstance(instance);

        return instance;
    }

    @Override
    public List<Instance> getAllInstancesByType(String type) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            return UserDatabase
                    .getDatabase()
                    .getInstances()
                    .stream()
                    .filter(i -> i.getType().name().equals(type))
                    .collect(Collectors.toList());
        }
        return null;
    }

    @Override
    public void updateUserLocation(Instance userInstance) {
        UserDatabase.getDatabase().getUser().setLocation(new Location(
                userInstance.getLocation().getLat(),
                userInstance.getLocation().getLng()));
        FirebaseRealtimeDB.getRealtimeDB().updateUserLocation(userInstance);
    }

    @Override
    public void saveData() {
        UserDatabase database = UserDatabase.getDatabase();
        Instance user = database.getUser();
        String userId = user.getId();

        String userJson = new Gson().toJson(user);
        String instancesJson = new Gson().toJson(database.getInstances());

        MySharedPreferences.getInstance().putString(
                Generator.getInstance()
                        .createKey(userId, Constants.user.name()), userJson);
        MySharedPreferences.getInstance().putString(
                Generator.getInstance()
                        .createKey(userId, Constants.instances.name()), instancesJson);
    }

    @Override
    public Instance getUserInstance(FirebaseUser user) {
        String userJson = MySharedPreferences.getInstance().getString(
                Generator.getInstance()
                        .createKey(user.getUid(), Constants.user.name()), null);
        if (userJson == null) {
            Log.d(TAG, "Creates new user instance");
            return createNewUser(user);
        } else {
            return new Gson().fromJson(userJson, Instance.class);
        }
    }

    @Override
    public List<Instance> getDatabaseInstances(FirebaseUser user) {
        String instancesJson = MySharedPreferences.getInstance().getString(
                Generator.getInstance()
                        .createKey(user.getUid(), Constants.instances.name()), null);
        if (instancesJson == null) {
            return new ArrayList<>();
        } else {
            return new Gson().fromJson(
                    instancesJson, new TypeToken<ArrayList<Instance>>(){}.getType());
        }
    }

    private Instance createNewUser(FirebaseUser user) {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put(Constants.photoUrl.name(), getPhotoUrl(user));
        attributes.put(Constants.isNew.name(), true);

        return new Instance()
                .setCreatedTimestamp(new Date())
                .setId(user.getUid())
                .setType(InstanceType.User)
                .setName(getDisplayName(user))
                .setCreatedByUserId(Constants.ADMIN.name())
                .setAttributes(attributes);
    }

    private String getDisplayName(FirebaseUser user) {
        String displayName = user.getDisplayName();
        if (displayName != null && !displayName.isEmpty()) {
            return displayName;
        }

        String email = user.getEmail();
        if (email != null && !email.isEmpty()) {
            return email;
        }

        String phoneNumber = user.getPhoneNumber();
        if (phoneNumber != null && !phoneNumber.isEmpty()) {
            return phoneNumber;
        }

        return "Empty username";
    }

    private String getPhotoUrl(FirebaseUser user) {
        if (user.getPhotoUrl() == null) return "";
        StringBuilder url = new StringBuilder(user.getPhotoUrl().toString());
        for (UserInfo userInfo : user.getProviderData()) {
            if (userInfo.getProviderId().equals("facebook.com")) {
                if (AccessToken.getCurrentAccessToken() != null) {
                    url.append("?access_token=").append(
                            Objects.requireNonNull(
                                    AccessToken.getCurrentAccessToken()).getToken());
                }
            }
        }
        return url.toString();
    }

}
