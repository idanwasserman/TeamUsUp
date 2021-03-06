package com.idan.teamusup.logic.interfaces;

import com.google.firebase.auth.FirebaseUser;
import com.idan.teamusup.data.Instance;

import java.util.List;
import java.util.Map;

public interface InstanceService {

    Instance createInstance(Instance instance);

    List<Instance> getAllInstancesByType(String type);

    void updateUserLocation(Instance userInstance);

    void saveData();

    Instance getUserInstance(FirebaseUser user);

    List<Instance> getDatabaseInstances(FirebaseUser user);

    String getLevelStringFromAttributes(Map<String, Object> attributes);
}
