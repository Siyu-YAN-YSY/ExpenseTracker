package com.example.expensetracker;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ExpenseEntityTest {

    @Test
    public void constructor_setsDefaultRecurringValues() {
        ExpenseEntity expense = new ExpenseEntity("10.00", "Food", "05/10/2025", "Lunch");

        assertFalse(expense.isRecurring());
        assertEquals("None", expense.getRecurringInterval());
    }

    @Test
    public void getAmountValue_validAmount_returnsDouble() {
        ExpenseEntity expense = new ExpenseEntity("25.50", "Food", "05/10/2025", "Lunch");

        double result = expense.getAmountValue();

        assertEquals(25.50, result, 0.001);
    }

    @Test
    public void getAmountValue_amountWithDollarSign_returnsDouble() {
        ExpenseEntity expense = new ExpenseEntity("$25.50", "Food", "05/10/2025", "Lunch");

        double result = expense.getAmountValue();

        assertEquals(25.50, result, 0.001);
    }

    @Test
    public void getAmountValue_invalidAmount_returnsZero() {
        ExpenseEntity expense = new ExpenseEntity("abc", "Food", "05/10/2025", "Lunch");

        double result = expense.getAmountValue();

        assertEquals(0.0, result, 0.001);
    }

    @Test
    public void setRecurringInterval_nullValue_setsNone() {
        ExpenseEntity expense = new ExpenseEntity("10.00", "Food", "05/10/2025", "Lunch");

        expense.setRecurringInterval(null);

        assertEquals("None", expense.getRecurringInterval());
    }

    @Test
    public void setRecurringInterval_emptyValue_setsNone() {
        ExpenseEntity expense = new ExpenseEntity("10.00", "Food", "05/10/2025", "Lunch");

        expense.setRecurringInterval("");

        assertEquals("None", expense.getRecurringInterval());
    }

    @Test
    public void setRecurring_true_updatesValue() {
        ExpenseEntity expense = new ExpenseEntity("10.00", "Food", "05/10/2025", "Lunch");

        expense.setRecurring(true);

        assertTrue(expense.isRecurring());
    }
}
