package com.example.expensetracker;
import java.util.List;
public class ExpenseCalculator {

    // Calculate total amount from a list of expenses
    public static double getTotal(List<ExpenseEntity> expenses) {
        double total = 0;
        for (ExpenseEntity expense : expenses) {
            total += expense.getAmountValue();
        }
        return total;
    }


    // Calculate total amount for a specific category
    public static double getCategoryTotal(List<ExpenseEntity> expenses, String category) {
        double total = 0;
        for (ExpenseEntity expense : expenses) {
            if (expense.getCategory().equals(category)) {
                total += expense.getAmountValue();
            }
        }
        return total;
    }
}
