package com.example.expensetracker;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;

import java.util.Locale;

public class LocaleManager {

    // Private constructor prevents this utility class from being instantiated
    private LocaleManager() {
    }

    // Saves the selected language code in SharedPreferences
    public static void saveLanguage(Context context, String languageCode) {
        SharedPreferences prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
        prefs.edit().putString("language", languageCode).apply();
    }

    // Gets the saved language code; defaults to English if none is saved
    public static String getSavedLanguage(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
        return prefs.getString("language", "en");
    }

    // Applies the saved language to the app context
    public static Context applyLanguage(Context context) {
        String language = getSavedLanguage(context);
        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        // Creates a new configuration using the selected locale
        Configuration config = new Configuration(context.getResources().getConfiguration());
        config.setLocale(locale);

        // Returns a context updated with the selected language
        return context.createConfigurationContext(config);
    }

    // Returns the display name for each supported language
    public static String getLanguageDisplayName(Context context, String code) {
        if (code.equals("zh")) {
            return context.getString(R.string.chinese);
        }

        return context.getString(R.string.english);
    }
}
