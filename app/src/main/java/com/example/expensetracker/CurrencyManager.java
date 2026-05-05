package com.example.expensetracker;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Locale;

public class CurrencyManager {

    private CurrencyManager() {
    }

    public static String getCurrencyCode(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
        return prefs.getString("currency", "USD");
    }

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

    public static String formatAmount(Context context, double amount) {
        return String.format(Locale.US, "%s%.2f", getCurrencySymbol(context), amount);
    }

    public static String formatAmountString(Context context, String rawAmount) {
        try {
            double amount = Double.parseDouble(rawAmount);
            return formatAmount(context, amount);
        } catch (Exception e) {
            return getCurrencySymbol(context) + rawAmount;
        }
    }

    public static String formatBudgetLabel(Context context, double amount) {
        return "Budget: " + formatAmount(context, amount);
    }

    public static String formatRemainingLabel(Context context, double amount) {
        return "Remaining: " + formatAmount(context, amount);
    }
}