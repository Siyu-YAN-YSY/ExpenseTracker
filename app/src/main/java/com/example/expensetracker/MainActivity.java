package com.example.expensetracker;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;


import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private static final String KEY_SELECTED_TAB = "selected_tab";

    private HomeFragment homeFragment;
    private ProfileFragment profileFragment;
    private SettingsFragment settingsFragment;
    private Fragment activeFragment;
    private int selectedTabId = R.id.home;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        applySavedTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        FragmentManager fm = getSupportFragmentManager();

        if (savedInstanceState != null) {
            selectedTabId = savedInstanceState.getInt(KEY_SELECTED_TAB, R.id.home);
        }

        homeFragment = (HomeFragment) fm.findFragmentByTag("HOME");
        profileFragment = (ProfileFragment) fm.findFragmentByTag("PROFILE");
        settingsFragment = (SettingsFragment) fm.findFragmentByTag("SETTINGS");

        if (homeFragment == null) homeFragment = new HomeFragment();
        if (profileFragment == null) profileFragment = new ProfileFragment();
        if (settingsFragment == null) settingsFragment = new SettingsFragment();

        if (savedInstanceState == null) {
            fm.beginTransaction()
                    .add(R.id.flFragment, settingsFragment, "SETTINGS").hide(settingsFragment)
                    .add(R.id.flFragment, profileFragment, "PROFILE").hide(profileFragment)
                    .add(R.id.flFragment, homeFragment, "HOME")
                    .commitNow();

            activeFragment = homeFragment;
            selectedTabId = R.id.home;
        } else {
            if (selectedTabId == R.id.profile) {
                activeFragment = profileFragment;
            } else if (selectedTabId == R.id.settings) {
                activeFragment = settingsFragment;
            } else {
                activeFragment = homeFragment;
                selectedTabId = R.id.home;
            }
        }

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            Fragment target;

            if (id == R.id.profile) {
                target = profileFragment;
            } else if (id == R.id.settings) {
                target = settingsFragment;
            } else {
                target = homeFragment;
            }

            if (target == activeFragment) {
                if (target instanceof HomeFragment && target.isAdded() && target.getView() != null) {
                    ((HomeFragment) target).refreshHomeData();
                } else if (target instanceof SettingsFragment && target.isAdded() && target.getView() != null) {
                    ((SettingsFragment) target).refreshSettingsData();
                }
                return true;
            }

            fm.beginTransaction()
                    .hide(activeFragment)
                    .show(target)
                    .commit();

            activeFragment = target;
            selectedTabId = id;

            if (target instanceof HomeFragment && target.isAdded() && target.getView() != null) {
                ((HomeFragment) target).refreshHomeData();
            } else if (target instanceof SettingsFragment && target.isAdded() && target.getView() != null) {
                ((SettingsFragment) target).refreshSettingsData();
            }

            return true;
        });

        bottomNav.setSelectedItemId(selectedTabId);
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPasscodeLock();
    }



    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(KEY_SELECTED_TAB, selectedTabId);
        super.onSaveInstanceState(outState);
    }


    private void applySavedTheme() {
        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        boolean darkMode = prefs.getBoolean("dark_mode", false);

        AppCompatDelegate.setDefaultNightMode(
                darkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );
    }

    private void checkPasscodeLock() {
        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);

        boolean passcodeEnabled = prefs.getBoolean("passcode_enabled", false);
        boolean unlocked = prefs.getBoolean("passcode_unlocked", false);

        if (passcodeEnabled && !unlocked) {
            startActivity(new Intent(this, PasscodeActivity.class));
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleManager.applyLanguage(newBase));
    }


}