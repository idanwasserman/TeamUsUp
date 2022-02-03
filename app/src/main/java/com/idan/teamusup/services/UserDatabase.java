package com.idan.teamusup.services;

import androidx.annotation.NonNull;

import com.idan.teamusup.data.Instance;

import java.util.List;

public class UserDatabase {

    private static UserDatabase database;

    Instance user;
    List<Instance> instances;
    List<Instance> tempList;

    private UserDatabase(Instance user, List<Instance> instances) {
        this.user = user;
        this.instances = instances;
        this.instances.add(user);
    }

    public static void init(Instance user, List<Instance> instances) {
        if (database == null) {
            database = new UserDatabase(user, instances);
        }
    }

    public static UserDatabase getDatabase() {
        return database;
    }

    public Instance getUser() {
        return user;
    }

    public List<Instance> getInstances() {
        return instances;
    }

    public List<Instance> getTempList() {
        return tempList;
    }

    public void setTempList(List<Instance> tempList) {
        this.tempList = tempList;
    }

    public void addInstance(Instance instance) {
        instances.add(instance);
    }

    public Instance getInstanceById(String id) {
        for (Instance instance : instances) {
            if (instance.getId().equals(id)) {
                return instance;
            }
        }
        return null;
    }

    public void removeInstanceById(String id) {
        Instance toDelete = null;
        for (Instance instance : instances) {
            if (instance.getId().equals(id)) {
                toDelete = instance;
                break;
            }
        }
        instances.remove(toDelete);
    }

    @NonNull
    @Override
    public String toString() {
        return "UserDatabase{" +
                "user=" + user +
                ", instances=" + instances +
                '}';
    }
}
