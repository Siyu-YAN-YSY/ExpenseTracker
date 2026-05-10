package com.example.expensetracker;

import java.util.ArrayList;
import java.util.List;

public class ExpenseRepository {

    // DAO used to access expense data from the Room database
    private final ExpenseDao expenseDao;

    // Constructor gets the DAO from the database instance
    public ExpenseRepository(ExpenseDatabase database) {
        this.expenseDao = database.expenseDao();
    }

    // Returns expenses based on the selected month and category filters
    public List<ExpenseEntity> getExpenses(String selectedMonth, String category) {
        String monthFilter = selectedMonth == null ? "All" : selectedMonth;
        String categoryFilter = category == null ? "All" : category;

        // If no month is selected, filter only by category
        if ("All".equals(monthFilter)) {
            return expenseDao.getExpensesByCategory(categoryFilter);
        }

        // Splits the month filter into month and year parts
        String[] components = monthFilter.split("/");
        if (components.length < 2) {
            return new ArrayList<>();
        }

        String month = components[0];
        String year = components[1];

        // If category is "All", return all expenses for the selected month
        if ("All".equals(categoryFilter)) {
            return expenseDao.getExpensesByMonth(month, year);
        }

        // Otherwise, return expenses that match both category and month
        return expenseDao.getExpensesByCategoryAndMonth(categoryFilter, month, year);
    }

    // Returns all months that have saved expenses
    public List<String> getAvailableMonths() {
        List<String> months = expenseDao.getAvailableMonths();

        // Prevents returning null to the rest of the app
        return months == null ? new ArrayList<>() : months;
    }

    // Returns all saved expenses
    public List<ExpenseEntity> getAllExpenses() {
        List<ExpenseEntity> expenses = expenseDao.getAllExpenses();

        // Prevents returning null to the rest of the app
        return expenses == null ? new ArrayList<>() : expenses;
    }

    // Returns all recurring expenses
    public List<ExpenseEntity> getRecurringExpenses() {
        List<ExpenseEntity> expenses = expenseDao.getRecurringExpenses();

        // Prevents returning null to the rest of the app
        return expenses == null ? new ArrayList<>() : expenses;
    }

    // Counts expenses that exactly match the provided details
    public int countExactExpense(String amount, String category, String date, String note) {
        return expenseDao.countExactExpense(amount, category, date, note);
    }

    // Inserts a new expense into the database
    public void insertExpense(ExpenseEntity expense) {
        expenseDao.insertExpense(expense);
    }

    // Deletes one expense from the database
    public void deleteExpense(ExpenseEntity expense) {
        expenseDao.deleteExpense(expense);
    }

    // Deletes all expenses from the database
    public void deleteAllExpenses() {
        expenseDao.deleteAllExpenses();
    }
}
