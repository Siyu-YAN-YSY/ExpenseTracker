package com.example.expensetracker;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Locale;

public class CurrencyManager {

    // Private constructor prevents this utility class from being instantiated
    private CurrencyManager() {
    }

    // Gets the saved currency code from settings; defaults to USD if none is saved
    public static String getCurrencyCode(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
        return prefs.getString("currency", "USD");
    }

    // Returns the currency symbol that matches the selected currency code
    public static String getCurrencySymbol(Context context) {
        String code = getCurrencyCode(context);

        switch (code) {
            case "EUR":
                return "€";
            case "GBP":
                return "£";
            case "CAD":
                return "C$";
            case "AUD":
                return "A$";
            case "CNY":
                return "¥";
            case "KHR":
                return "៛";
            case "USD":
            default:
                return "$";
        }
    }

    // Formats a number as a currency amount using the selected currency symbol
    public static String formatAmount(Context context, double amount) {
        return String.format(Locale.US, "%s%.2f", getCurrencySymbol(context), amount);
    }

    // Converts a saved amount string into a formatted currency value
    public static String formatAmountString(Context context, String rawAmount) {
        try {
            double amount = Double.parseDouble(rawAmount);
            return formatAmount(context, amount);
        } catch (Exception e) {
            // If parsing fails, still show the selected currency symbol before the raw value
            return getCurrencySymbol(context) + rawAmount;
        }
    }

    // Creates a localized budget label with the formatted budget amount
    public static String formatBudgetLabel(Context context, float budget) {
        return context.getString(
                R.string.budget_s,
                formatAmount(context, budget)
        );
    }

    // Creates a localized remaining budget label with the formatted remaining amount
    public static String formatRemainingLabel(Context context, double remaining) {
        return context.getString(
                R.string.remaining_s,
                formatAmount(context, remaining)
        );
    }
}