package com.example.expensetracker;

import android.content.Context;
import android.content.SharedPreferences;

public class BudgetManager {

    // Name of the SharedPreferences file used to store budget data
    private static final String PREF_NAME = "budget";

    // Key used to save and retrieve the budget value
    private static final String KEY_BUDGET = "budget";

    // SharedPreferences object used for local data storage
    private final SharedPreferences preferences;

    // Constructor gets access to the budget SharedPreferences file
    public BudgetManager(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    // Saves the user's budget value locally
    public void saveBudget(float value) {
        preferences.edit().putFloat(KEY_BUDGET, value).apply();
    }

    // Retrieves the saved budget value; returns 0 if no budget has been saved
    public float getBudget() {
        return preferences.getFloat(KEY_BUDGET, 0f);
    }
}
