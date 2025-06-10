package com.project.yatrameter;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.preference.PreferenceManager;
import java.util.Locale;

public class LocaleHelper {

    private static final String SELECTED_LANGUAGE = "selected_language"; // Key for SharedPreferences

    // Method to persist the chosen language and apply it to the context
    public static Context setLocale(Context context, String language) {
        persist(context, language); // Save the language preference
        return updateResources(context, language); // Apply to current context
    }

    // Method to get the language code from preferences
    public static String getLanguage(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(SELECTED_LANGUAGE, "en"); // Default to English
    }

    // Saves the selected language to SharedPreferences
    private static void persist(Context context, String language) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(SELECTED_LANGUAGE, language);
        editor.apply();
    }

    // Updates the resources configuration for the given context
    private static Context updateResources(Context context, String language) {
        Locale locale = new Locale(language); // Create a new Locale object
        Locale.setDefault(locale); // Set it as default for the app process

        Resources resources = context.getResources();
        Configuration configuration = resources.getConfiguration();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // For Android Nougat (API 24) and above
            configuration.setLocale(locale);
            return context.createConfigurationContext(configuration);
        } else {
            // For older Android versions (deprecated but for compatibility)
            configuration.locale = locale;
            resources.updateConfiguration(configuration, resources.getDisplayMetrics());
            return context;
        }
    }
}