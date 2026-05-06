package com.example.expensetracker;

import android.app.Application;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ProcessLifecycleOwner;

public class MyApp extends Application implements DefaultLifecycleObserver {

    @Override
    public void onCreate() {
        super.onCreate();

        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        boolean passcodeEnabled = prefs.getBoolean("passcode_enabled", false);

        if (passcodeEnabled) {
            prefs.edit()
                    .putBoolean("passcode_unlocked", false)
                    .apply();
        }

        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
    }

    @Override
    public void onStop(@NonNull LifecycleOwner owner) {
        getSharedPreferences("settings", MODE_PRIVATE)
                .edit()
                .putBoolean("passcode_unlocked", false)
                .apply();
    }
}
