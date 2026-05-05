package com.example.expensetracker;

import android.content.Context;
import android.content.SharedPreferences;

public class BudgetManager {
    private static final String PREF_NAME = "budget";
    private static final String KEY_BUDGET = "budget";

    private final SharedPreferences preferences;

    public BudgetManager(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveBudget(float value) {
        preferences.edit().putFloat(KEY_BUDGET, value).apply();
    }

    public float getBudget() {
        return preferences.getFloat(KEY_BUDGET, 0f);
    }
}
