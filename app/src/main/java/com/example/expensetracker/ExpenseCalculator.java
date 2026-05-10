package com.example.expensetracker;

import java.util.List;

public class ExpenseCalculator {

    // Calculates the total amount from all expenses in the list
    public static double getTotal(List<ExpenseEntity> expenses) {
        double total = 0;

        // Adds each expense amount to the running total
        for (ExpenseEntity expense : expenses) {
            total += expense.getAmountValue();
        }

        return total;
    }

    // Calculates the total amount for one specific category
    public static double getCategoryTotal(List<ExpenseEntity> expenses, String category) {
        double total = 0;

        // Adds only the expenses that match the selected category
        for (ExpenseEntity expense : expenses) {
            if (expense.getCategory().equals(category)) {
                total += expense.getAmountValue();
            }
        }

        return total;
    }
}
