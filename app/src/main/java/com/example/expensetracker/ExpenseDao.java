package com.example.expensetracker;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

// Marks this interface as a Room DAO for accessing expense data
@Dao
public interface ExpenseDao {

    // Inserts a new expense into the database
    @Insert
    void insertExpense(ExpenseEntity expense);

    // Updates an existing expense in the database
    @Update
    void updateExpense(ExpenseEntity expense);

    // Deletes an expense from the database
    @Delete
    void deleteExpense(ExpenseEntity expense);

    // Returns all saved expenses
    @Query("SELECT * FROM expenses")
    List<ExpenseEntity> getAllExpenses();

    // Returns one expense that matches the given ID
    @Query("SELECT * FROM expenses WHERE id = :id LIMIT 1")
    ExpenseEntity getExpenseById(int id);

    // Returns expenses for a selected category, or all expenses if category is "All"
    @Query("SELECT * FROM expenses WHERE (:category = 'All' OR category = :category)")
    List<ExpenseEntity> getExpensesByCategory(String category);

    // Returns unique months that contain expenses, sorted from newest to oldest
    @Query("SELECT DISTINCT substr(date, 1, 2) || '/' || substr(date, 7, 4) FROM expenses ORDER BY substr(date, 7, 4) DESC, substr(date, 1, 2) DESC")
    List<String> getAvailableMonths();

    // Returns expenses for a specific month and year
    @Query("SELECT * FROM expenses WHERE substr(date, 1, 2) = :month AND substr(date, 7, 4) = :year")
    List<ExpenseEntity> getExpensesByMonth(String month, String year);

    // Returns expenses that match both a category and a specific month/year
    @Query("SELECT * FROM expenses WHERE category = :category AND substr(date, 1, 2) = :month AND substr(date, 7, 4) = :year")
    List<ExpenseEntity> getExpensesByCategoryAndMonth(String category, String month, String year);

    // Returns all expenses marked as recurring
    @Query("SELECT * FROM expenses WHERE is_recurring = 1")
    List<ExpenseEntity> getRecurringExpenses();

    // Counts how many expenses exactly match the given details
    @Query("SELECT COUNT(*) FROM expenses WHERE amount = :amount AND category = :category AND date = :date AND note = :note")
    int countExactExpense(String amount, String category, String date, String note);

    // Deletes every expense from the database
    @Query("DELETE FROM expenses")
    void deleteAllExpenses();
}
