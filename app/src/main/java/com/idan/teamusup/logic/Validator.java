package com.idan.teamusup.logic;

import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class Validator {

    private static Validator instance;
    private final AppCompatActivity activity;

    private Validator(AppCompatActivity activity) {
        this.activity = activity;
    }

    public static void init(AppCompatActivity activity) {
        if (instance == null) {
            instance = new Validator(activity);
        }
    }

    public static Validator getInstance() {
        return instance;
    }

    public boolean isInvalidString(String str, String text) {
        if (str == null || str.isEmpty()) {
            Toast.makeText(
                    this.activity,
                    text,
                    Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }
}
