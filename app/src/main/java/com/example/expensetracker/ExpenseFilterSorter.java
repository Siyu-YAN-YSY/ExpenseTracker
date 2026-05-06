package com.example.expensetracker;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class ExpenseFilterSorter {

    private final ExpenseDateUtils dateUtils;

    public ExpenseFilterSorter(ExpenseDateUtils dateUtils) {
        this.dateUtils = dateUtils;
    }

    public void apply(List<ExpenseEntity> source, List<ExpenseEntity> target, String query, String sortOption) {
        target.clear();
        String cleanQuery = query == null ? "" : query.trim().toLowerCase(Locale.US);

        for (ExpenseEntity expense : source) {
            if (matchesSearch(expense, cleanQuery)) {
                target.add(expense);
            }
        }

        sort(target, sortOption);
    }

    private boolean matchesSearch(ExpenseEntity expense, String query) {
        if (query.isEmpty()) {
            return true;
        }

        String note = safeLower(expense.getNote());
        String category = safeLower(expense.getCategory());
        String date = safeLower(expense.getDate());
        String amount = safeLower(expense.getAmount());

        return note.contains(query)
                || category.contains(query)
                || date.contains(query)
                || amount.contains(query);
    }

    private String safeLower(String value) {
        return value == null ? "" : value.toLowerCase(Locale.US);
    }

    private void sort(List<ExpenseEntity> expenses, String sortOption) {
        String sort = sortOption == null ? "NEWEST" : sortOption;

        switch (sort) {
            case "OLDEST":
                Collections.sort(expenses, dateUtils::compareByDate);
                break;

            case "HIGHEST":
                Collections.sort(expenses, (a, b) ->
                        Double.compare(b.getAmountValue(), a.getAmountValue()));
                break;

            case "LOWEST":
                Collections.sort(expenses, (a, b) ->
                        Double.compare(a.getAmountValue(), b.getAmountValue()));
                break;

            case "CATEGORY":
                Collections.sort(expenses, (a, b) ->
                        safeLower(a.getCategory()).compareTo(safeLower(b.getCategory())));
                break;

            case "NEWEST":
            default:
                Collections.sort(expenses, (a, b) ->
                        -dateUtils.compareByDate(a, b));
                break;
        }
    }
}
