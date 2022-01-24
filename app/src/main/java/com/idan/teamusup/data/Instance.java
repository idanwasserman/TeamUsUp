package com.idan.teamusup.data;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseUser;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Instance {

    private String id;
    private InstanceType type;
    private String name;
    private Date createdTimestamp;
    private String createdByUserId;
    private Location location;
    private Map<String, Object> attributes;

    public Instance() {
        attributes = new HashMap<>();
    }

    public Instance(String userId, String username) {
        super();
        this.id = userId;
        this.type = InstanceType.User;
        this.name = username;
        this.createdTimestamp = new Date();
    }

    public String getId() {
        return id;
    }

    public Instance setId(String id) {
        this.id = id;
        return this;
    }

    public InstanceType getType() {
        return type;
    }

    public Instance setType(InstanceType type) {
        this.type = type;
        return this;
    }

    public String getName() {
        return name;
    }

    public Instance setName(String name) {
        this.name = name;
        return this;
    }

    public Date getCreatedTimestamp() {
        return createdTimestamp;
    }

    public Instance setCreatedTimestamp(Date createdTimestamp) {
        this.createdTimestamp = createdTimestamp;
        return this;
    }

    public String getCreatedByUserId() {
        return createdByUserId;
    }

    public Instance setCreatedByUserId(String createdByUserId) {
        this.createdByUserId = createdByUserId;
        return this;
    }

    public Location getLocation() {
        return location;
    }

    public Instance setLocation(Location location) {
        this.location = location;
        return this;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public Instance setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
        return this;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == this) return true;

        if (!(obj instanceof Instance)) return false;

        Instance i = (Instance) obj;
        return this.id.equals(i.getId());
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @NonNull
    @Override
    public String toString() {
        return "Instance{" +
                "id='" + id + '\'' +
                ", type=" + type +
                ", name='" + name + '\'' +
                ", createdTimestamp=" + createdTimestamp +
                ", createdByUserId='" + createdByUserId + '\'' +
                ", location=" + location +
                ", attributes=" + attributes +
                '}';
    }
}
