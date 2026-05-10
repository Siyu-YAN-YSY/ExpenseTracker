package com.example.expensetracker;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class ExpenseFilterSorter {

    // Utility class used for comparing expense dates
    private final ExpenseDateUtils dateUtils;

    // Constructor receives the date utility dependency
    public ExpenseFilterSorter(ExpenseDateUtils dateUtils) {
        this.dateUtils = dateUtils;
    }

    // Filters expenses by search query, then sorts the filtered results
    public void apply(List<ExpenseEntity> source, List<ExpenseEntity> target, String query, String sortOption) {
        target.clear();

        // Cleans the search query so matching is case-insensitive
        String cleanQuery = query == null ? "" : query.trim().toLowerCase(Locale.US);

        // Adds only expenses that match the search query
        for (ExpenseEntity expense : source) {
            if (matchesSearch(expense, cleanQuery)) {
                target.add(expense);
            }
        }

        // Sorts the filtered expense list using the selected sort option
        sort(target, sortOption);
    }

    // Checks whether an expense matches the search query
    private boolean matchesSearch(ExpenseEntity expense, String query) {
        if (query.isEmpty()) {
            return true;
        }

        // Converts searchable fields to lowercase safely
        String note = safeLower(expense.getNote());
        String category = safeLower(expense.getCategory());
        String date = safeLower(expense.getDate());
        String amount = safeLower(expense.getAmount());

        // Matches the query against note, category, date, or amount
        return note.contains(query)
                || category.contains(query)
                || date.contains(query)
                || amount.contains(query);
    }

    // Converts text to lowercase and handles null values safely
    private String safeLower(String value) {
        return value == null ? "" : value.toLowerCase(Locale.US);
    }

    // Sorts expenses based on the selected sort option
    private void sort(List<ExpenseEntity> expenses, String sortOption) {
        String sort = sortOption == null ? "NEWEST" : sortOption;

        switch (sort) {
            case "OLDEST":
                // Sorts expenses from the oldest date to the newest date
                Collections.sort(expenses, dateUtils::compareByDate);
                break;

            case "HIGHEST":
                // Sorts expenses from the highest amount to the lowest amount
                Collections.sort(expenses, (a, b) ->
                        Double.compare(b.getAmountValue(), a.getAmountValue()));
                break;

            case "LOWEST":
                // Sorts expenses from the lowest amount to the highest amount
                Collections.sort(expenses, (a, b) ->
                        Double.compare(a.getAmountValue(), b.getAmountValue()));
                break;

            case "CATEGORY":
                // Sorts expenses alphabetically by category
                Collections.sort(expenses, (a, b) ->
                        safeLower(a.getCategory()).compareTo(safeLower(b.getCategory())));
                break;

            case "NEWEST":
            default:
                // Sorts expenses from the newest date to the oldest date
                Collections.sort(expenses, (a, b) ->
                        -dateUtils.compareByDate(a, b));
                break;
        }
    }
}
