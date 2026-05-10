package com.example.expensetracker;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class ExpenseCalculatorTest {

    @Test
    public void getTotal_returnsCorrectTotal() {
        List<ExpenseEntity> expenses = Arrays.asList(
                new ExpenseEntity("10.50", "Food", "05/01/2025", "Lunch"),
                new ExpenseEntity("20.00", "Transport", "05/02/2025", "Bus"),
                new ExpenseEntity("5.25", "Shopping", "05/03/2025", "Pen")
        );

        double result = ExpenseCalculator.getTotal(expenses);

        assertEquals(35.75, result, 0.001);
    }

    @Test
    public void getTotal_emptyList_returnsZero() {
        List<ExpenseEntity> expenses = Arrays.asList();

        double result = ExpenseCalculator.getTotal(expenses);

        assertEquals(0.0, result, 0.001);
    }

    @Test
    public void getCategoryTotal_returnsOnlyMatchingCategoryTotal() {
        List<ExpenseEntity> expenses = Arrays.asList(
                new ExpenseEntity("10.00", "Food", "05/01/2025", "Lunch"),
                new ExpenseEntity("15.00", "Food", "05/02/2025", "Dinner"),
                new ExpenseEntity("20.00", "Transport", "05/03/2025", "Bus")
        );

        double result = ExpenseCalculator.getCategoryTotal(expenses, "Food");

        assertEquals(25.0, result, 0.001);
    }

    @Test
    public void getCategoryTotal_noMatchingCategory_returnsZero() {
        List<ExpenseEntity> expenses = Arrays.asList(
                new ExpenseEntity("10.00", "Food", "05/01/2025", "Lunch")
        );

        double result = ExpenseCalculator.getCategoryTotal(expenses, "Bills");

        assertEquals(0.0, result, 0.001);
    }
}
