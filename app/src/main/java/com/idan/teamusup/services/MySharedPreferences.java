package com.idan.teamusup.services;

import android.content.Context;
import android.content.SharedPreferences;

public class MySharedPreferences {

    private static MySharedPreferences instance;
    private final SharedPreferences sharedPreferences;

    public static MySharedPreferences getInstance() {
        return instance;
    }

    private MySharedPreferences(Context context, String filename) {
        sharedPreferences = context
                .getApplicationContext()
                .getSharedPreferences(filename, Context.MODE_PRIVATE);
    }

    public static MySharedPreferences init(Context context, String filename) {
        if (instance == null) {
            instance = new MySharedPreferences(context, filename);
        }
        return instance;
    }

    public String getString(String key, String defaultValue) {
        return sharedPreferences.getString(key, defaultValue);
    }

    public void putString(String key, String value) {
        sharedPreferences.edit().putString(key, value).apply();
    }

}
