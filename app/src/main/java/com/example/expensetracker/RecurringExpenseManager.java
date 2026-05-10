package com.example.expensetracker;

import android.content.Context;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class RecurringExpenseManager {

    // Context used to show Toast messages
    private final Context context;

    // Repository used to read and insert expenses
    private final ExpenseRepository repository;

    // Utility class used for date parsing, formatting, and recurring date movement
    private final ExpenseDateUtils dateUtils;

    // Constructor receives the context, repository, and date utility dependency
    public RecurringExpenseManager(Context context, ExpenseRepository repository, ExpenseDateUtils dateUtils) {
        this.context = context;
        this.repository = repository;
        this.dateUtils = dateUtils;
    }

    // Generates missing recurring expenses that are due up to today's date
    public boolean generateDueRecurringExpenses() {
        List<ExpenseEntity> recurringExpenses = repository.getRecurringExpenses();

        // Stops if there are no recurring expenses saved
        if (recurringExpenses.isEmpty()) return false;

        // Gets today's date and removes the time portion for accurate date comparison
        Calendar today = Calendar.getInstance();
        resetTime(today);

        boolean insertedNewExpense = false;

        // Checks each recurring expense
        for (ExpenseEntity recurringExpense : recurringExpenses) {
            Date startDate = dateUtils.parseDate(recurringExpense.getDate());

            // Skips expenses with invalid dates
            if (startDate == null) continue;

            Calendar nextDate = Calendar.getInstance();
            nextDate.setTime(startDate);
            resetTime(nextDate);

            // Safety counter prevents an infinite loop if recurrence logic fails
            int safetyCounter = 0;

            while (safetyCounter < 120) {
                // Moves to the next recurring date based on weekly, monthly, or yearly interval
                dateUtils.moveToNextRecurringDate(nextDate, recurringExpense.getRecurringInterval());

                // Stops when the next recurring date is in the future
                if (nextDate.after(today)) {
                    break;
                }

                String generatedDate = dateUtils.formatDate(nextDate.getTime());
                String note = recurringExpense.getNote() == null ? "" : recurringExpense.getNote();

                // Checks if the generated expense already exists to avoid duplicates
                int duplicateCount = repository.countExactExpense(
                        recurringExpense.getAmount(),
                        recurringExpense.getCategory(),
                        generatedDate,
                        note
                );

                // Inserts the generated expense only if it does not already exist
                if (duplicateCount == 0) {
                    ExpenseEntity generated = new ExpenseEntity(
                            recurringExpense.getAmount(),
                            recurringExpense.getCategory(),
                            generatedDate,
                            note
                    );

                    // Generated copies are regular expenses, not recurring templates
                    generated.setRecurring(false);
                    generated.setRecurringInterval("None");

                    repository.insertExpense(generated);
                    insertedNewExpense = true;
                }

                safetyCounter++;
            }
        }

        // Notifies the user only when new recurring expenses were created
        if (insertedNewExpense) {
            Toast.makeText(context, "Recurring expenses updated", Toast.LENGTH_SHORT).show();
        }

        return insertedNewExpense;
    }

    // Resets the time portion of a Calendar so only the date is compared
    private void resetTime(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }
}
