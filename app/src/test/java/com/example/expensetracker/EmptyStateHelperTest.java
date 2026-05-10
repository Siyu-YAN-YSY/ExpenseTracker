package com.example.expensetracker;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class EmptyStateHelperTest {

    @Test
    public void getMessage_withSearchQuery_returnsSearchMessage() {
        EmptyStateHelper helper = new EmptyStateHelper();

        String result = helper.getMessage(true, "All", "All");

        assertEquals("No matching expenses found. Try a different search.", result);
    }

    @Test
    public void getMessage_withMonthFilter_returnsFilterMessage() {
        EmptyStateHelper helper = new EmptyStateHelper();

        String result = helper.getMessage(false, "05/2025", "All");

        assertEquals("No expenses match this filter yet.", result);
    }

    @Test
    public void getMessage_withCategoryFilter_returnsFilterMessage() {
        EmptyStateHelper helper = new EmptyStateHelper();

        String result = helper.getMessage(false, "All", "Food");

        assertEquals("No expenses match this filter yet.", result);
    }

    @Test
    public void getMessage_withoutSearchOrFilter_returnsDefaultMessage() {
        EmptyStateHelper helper = new EmptyStateHelper();

        String result = helper.getMessage(false, "All", "All");

        assertEquals("No expenses yet. Tap + to add your first expense.", result);
    }
}
