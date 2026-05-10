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

    // Key used to save and restore the selected bottom navigation tab
    private static final String KEY_SELECTED_TAB = "selected_tab";

    // Main fragments shown by the bottom navigation
    private HomeFragment homeFragment;
    private ProfileFragment profileFragment;
    private SettingsFragment settingsFragment;

    // Tracks the currently visible fragment
    private Fragment activeFragment;

    // Stores the currently selected tab ID
    private int selectedTabId = R.id.home;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        applySavedTheme();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        FragmentManager fm = getSupportFragmentManager();

        // Restores the previously selected tab after screen rotation or recreation
        if (savedInstanceState != null) {
            selectedTabId = savedInstanceState.getInt(KEY_SELECTED_TAB, R.id.home);
        }

        // Tries to reuse existing fragments if they already exist
        homeFragment = (HomeFragment) fm.findFragmentByTag("HOME");
        profileFragment = (ProfileFragment) fm.findFragmentByTag("PROFILE");
        settingsFragment = (SettingsFragment) fm.findFragmentByTag("SETTINGS");

        // Creates fragments if they were not found
        if (homeFragment == null) homeFragment = new HomeFragment();
        if (profileFragment == null) profileFragment = new ProfileFragment();
        if (settingsFragment == null) settingsFragment = new SettingsFragment();

        if (savedInstanceState == null) {
            // Adds all fragments once, then hides the inactive ones
            fm.beginTransaction()
                    .add(R.id.flFragment, settingsFragment, "SETTINGS").hide(settingsFragment)
                    .add(R.id.flFragment, profileFragment, "PROFILE").hide(profileFragment)
                    .add(R.id.flFragment, homeFragment, "HOME")
                    .commitNow();

            activeFragment = homeFragment;
            selectedTabId = R.id.home;
        } else {
            // Restores the active fragment based on the saved selected tab
            if (selectedTabId == R.id.profile) {
                activeFragment = profileFragment;
            } else if (selectedTabId == R.id.settings) {
                activeFragment = settingsFragment;
            } else {
                activeFragment = homeFragment;
                selectedTabId = R.id.home;
            }
        }

        // Handles bottom navigation tab changes
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            Fragment target;

            // Chooses which fragment should be shown
            if (id == R.id.profile) {
                target = profileFragment;
            } else if (id == R.id.settings) {
                target = settingsFragment;
            } else {
                target = homeFragment;
            }

            // Refreshes the current tab if the user taps it again
            if (target == activeFragment) {
                if (target instanceof HomeFragment && target.isAdded() && target.getView() != null) {
                    ((HomeFragment) target).refreshHomeData();
                } else if (target instanceof SettingsFragment && target.isAdded() && target.getView() != null) {
                    ((SettingsFragment) target).refreshSettingsData();
                }
                return true;
            }

            // Switches from the current fragment to the selected fragment
            fm.beginTransaction()
                    .hide(activeFragment)
                    .show(target)
                    .commit();

            activeFragment = target;
            selectedTabId = id;

            // Refreshes data when opening Home or Settings
            if (target instanceof HomeFragment && target.isAdded() && target.getView() != null) {
                ((HomeFragment) target).refreshHomeData();
            } else if (target instanceof SettingsFragment && target.isAdded() && target.getView() != null) {
                ((SettingsFragment) target).refreshSettingsData();
            }

            return true;
        });

        // Updates the bottom navigation UI to match the selected tab
        bottomNav.setSelectedItemId(selectedTabId);
    }

    // Checks passcode protection whenever the app returns to the foreground
    @Override
    protected void onResume() {
        super.onResume();
        checkPasscodeLock();
    }

    // Saves the selected tab before the activity is recreated
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(KEY_SELECTED_TAB, selectedTabId);
        super.onSaveInstanceState(outState);
    }

    // Applies the saved light or dark theme before the activity loads
    private void applySavedTheme() {
        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        boolean darkMode = prefs.getBoolean("dark_mode", false);

        AppCompatDelegate.setDefaultNightMode(
                darkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );
    }

    // Opens the passcode screen if passcode protection is enabled and not unlocked
    private void checkPasscodeLock() {
        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);

        boolean passcodeEnabled = prefs.getBoolean("passcode_enabled", false);
        boolean unlocked = prefs.getBoolean("passcode_unlocked", false);

        if (passcodeEnabled && !unlocked) {
            startActivity(new Intent(this, PasscodeActivity.class));
        }
    }

    // Applies the selected app language before attaching the activity context
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleManager.applyLanguage(newBase));
    }
}