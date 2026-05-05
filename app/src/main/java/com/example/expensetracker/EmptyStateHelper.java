package com.example.expensetracker;

public class EmptyStateHelper {
    public String getMessage(boolean hasSearchQuery, String selectedMonth, String selectedCategory) {
        if (hasSearchQuery) {
            return "No matching expenses found. Try a different search.";
        }

        boolean hasFilter = !"All".equals(selectedMonth) || !"All".equals(selectedCategory);
        if (hasFilter) {
            return "No expenses match this filter yet.";
        }

        return "No expenses yet. Tap + to add your first expense.";
    }
}
