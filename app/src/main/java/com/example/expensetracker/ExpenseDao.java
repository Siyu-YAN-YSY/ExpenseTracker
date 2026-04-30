package com.example.expensetracker;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ExpenseDao {

    @Insert
    void insertExpense(ExpenseEntity expense);

    @Update
    void updateExpense(ExpenseEntity expense);

    @Delete
    void deleteExpense(ExpenseEntity expense);

    @Query("SELECT * FROM expenses")
    List<ExpenseEntity> getAllExpenses();

    @Query("SELECT * FROM expenses WHERE id = :id LIMIT 1")
    ExpenseEntity getExpenseById (int id);

    @Query("SELECT * FROM expenses WHERE (:category = 'All' OR category = :category)")
    List<ExpenseEntity> getExpensesByCategory(String category);

    @Query("SELECT DISTINCT substr(date, 1, 2) || '/' || substr(date, 7, 4) FROM expenses ORDER BY substr(date, 7, 4) DESC, substr(date, 1, 2) DESC")
    List<String> getAvailableMonths();

    @Query("SELECT * FROM expenses WHERE substr(date, 1, 2) = :month AND substr(date, 7, 4) = :year")
    List<ExpenseEntity> getExpensesByMonth(String month, String year);

    @Query("SELECT * FROM expenses WHERE category = :category AND substr(date, 1, 2) = :month AND substr(date, 7, 4) = :year")
    List<ExpenseEntity> getExpensesByCategoryAndMonth(String category, String month, String year);
}