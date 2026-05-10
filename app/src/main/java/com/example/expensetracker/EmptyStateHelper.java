package com.example.expensetracker;

public class EmptyStateHelper {

    // Returns the correct empty-state message based on search and filter status
    public String getMessage(boolean hasSearchQuery, String selectedMonth, String selectedCategory) {

        // Shows this message when the user searches but no expenses match
        if (hasSearchQuery) {
            return "No matching expenses found. Try a different search.";
        }

        // Checks whether the user selected a month or category filter
        boolean hasFilter = !"All".equals(selectedMonth) || !"All".equals(selectedCategory);

        // Shows this message when filters are active but no expenses match
        if (hasFilter) {
            return "No expenses match this filter yet.";
        }

        // Default message shown when there are no saved expenses at all
        return "No expenses yet. Tap + to add your first expense.";
    }
}
