package com.example.expensetracker;

import java.util.ArrayList;
import java.util.List;

public class ExpenseRepository {
    private final ExpenseDao expenseDao;

    public ExpenseRepository(ExpenseDatabase database) {
        this.expenseDao = database.expenseDao();
    }

    public List<ExpenseEntity> getExpenses(String selectedMonth, String category) {
        String monthFilter = selectedMonth == null ? "All" : selectedMonth;
        String categoryFilter = category == null ? "All" : category;

        if ("All".equals(monthFilter)) {
            return expenseDao.getExpensesByCategory(categoryFilter);
        }

        String[] components = monthFilter.split("/");
        if (components.length < 2) {
            return new ArrayList<>();
        }

        String month = components[0];
        String year = components[1];

        if ("All".equals(categoryFilter)) {
            return expenseDao.getExpensesByMonth(month, year);
        }
        return expenseDao.getExpensesByCategoryAndMonth(categoryFilter, month, year);
    }

    public List<String> getAvailableMonths() {
        List<String> months = expenseDao.getAvailableMonths();
        return months == null ? new ArrayList<>() : months;
    }

    public List<ExpenseEntity> getAllExpenses() {
        List<ExpenseEntity> expenses = expenseDao.getAllExpenses();
        return expenses == null ? new ArrayList<>() : expenses;
    }

    public List<ExpenseEntity> getRecurringExpenses() {
        List<ExpenseEntity> expenses = expenseDao.getRecurringExpenses();
        return expenses == null ? new ArrayList<>() : expenses;
    }

    public int countExactExpense(String amount, String category, String date, String note) {
        return expenseDao.countExactExpense(amount, category, date, note);
    }

    public void insertExpense(ExpenseEntity expense) {
        expenseDao.insertExpense(expense);
    }

    public void deleteExpense(ExpenseEntity expense) {
        expenseDao.deleteExpense(expense);
    }
}
