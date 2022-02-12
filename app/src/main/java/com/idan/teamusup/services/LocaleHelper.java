package com.idan.teamusup.services;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.preference.PreferenceManager;

import com.idan.teamusup.data.Constants;

import java.util.Locale;

public class LocaleHelper {

    private static final String SELECTED_LANGUAGE = "Locale.Helper.Selected.Language";

    public static final String HEBREW = "iw";
    public static final String ENGLISH = "en";
    private static String currLanguage = Locale.getDefault().getLanguage();

    public static String getCurrLanguage() {
        return currLanguage;
    }

    public static void onCreate(Context context) {
        String language;
        if (getLanguage(context).isEmpty()) {
            language = getPersistedData(context, Locale.getDefault().getLanguage());
        } else {
            language = getLanguage(context);
        }

        setLocale(context, language);
    }

    public static void onCreate(Context context, String defaultLanguage) {
        String language = getPersistedData(context, defaultLanguage);
        setLocale(context, language);
    }

    public static String getLanguage(Context context) {
        return getPersistedData(context, Locale.getDefault().getLanguage());
    }

    public static void setLocale(Context context, String language) {
        currLanguage = language;
        String languageKey = Generator.getInstance().createKey(
                UserDatabase.getDatabase().getUser().getId(),
                Constants.language.name());
        MySharedPreferences.getInstance().putString(languageKey, language);

        persist(context, language);
        updateResources(context, language);
        updateDirections(context, language);
    }

    private static String getPersistedData(Context context, String defaultLanguage) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(SELECTED_LANGUAGE, defaultLanguage);
    }

    private static void persist(Context context, String language) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putString(SELECTED_LANGUAGE, language);
        editor.apply();
    }

    private static void updateResources(Context context, String language) {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        Resources resources = context.getResources();

        Configuration configuration = resources.getConfiguration();
        configuration.locale = locale;

        resources.updateConfiguration(configuration, resources.getDisplayMetrics());
    }

    private static void updateDirections(Context context, String language) {
        Resources resources = context.getResources();
        Configuration configuration = resources.getConfiguration();
        configuration.setLayoutDirection(new Locale(language));
        resources.updateConfiguration(configuration, resources.getDisplayMetrics());
    }
}
