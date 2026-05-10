package com.example.expensetracker;

import android.app.Application;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ProcessLifecycleOwner;

public class MyApp extends Application implements DefaultLifecycleObserver {

    // Runs once when the application starts
    @Override
    public void onCreate() {
        super.onCreate();

        // Gets saved app settings
        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        boolean passcodeEnabled = prefs.getBoolean("passcode_enabled", false);

        // If passcode protection is enabled, require the user to unlock again
        if (passcodeEnabled) {
            prefs.edit()
                    .putBoolean("passcode_unlocked", false)
                    .apply();
        }

        // Observes the whole app lifecycle, not just one activity
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
    }

    // Runs when the app moves to the background
    @Override
    public void onStop(@NonNull LifecycleOwner owner) {
        // Locks the app again so the passcode is required next time
        getSharedPreferences("settings", MODE_PRIVATE)
                .edit()
                .putBoolean("passcode_unlocked", false)
                .apply();
    }
}
