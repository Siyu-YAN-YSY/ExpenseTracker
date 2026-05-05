package com.example.expensetracker;

import android.content.Context;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class RecurringExpenseManager {
    private final Context context;
    private final ExpenseRepository repository;
    private final ExpenseDateUtils dateUtils;

    public RecurringExpenseManager(Context context, ExpenseRepository repository, ExpenseDateUtils dateUtils) {
        this.context = context;
        this.repository = repository;
        this.dateUtils = dateUtils;
    }

    public boolean generateDueRecurringExpenses() {
        List<ExpenseEntity> recurringExpenses = repository.getRecurringExpenses();
        if (recurringExpenses.isEmpty()) return false;

        Calendar today = Calendar.getInstance();
        resetTime(today);

        boolean insertedNewExpense = false;

        for (ExpenseEntity recurringExpense : recurringExpenses) {
            Date startDate = dateUtils.parseDate(recurringExpense.getDate());
            if (startDate == null) continue;

            Calendar nextDate = Calendar.getInstance();
            nextDate.setTime(startDate);
            resetTime(nextDate);

            int safetyCounter = 0;
            while (safetyCounter < 120) {
                dateUtils.moveToNextRecurringDate(nextDate, recurringExpense.getRecurringInterval());

                if (nextDate.after(today)) {
                    break;
                }

                String generatedDate = dateUtils.formatDate(nextDate.getTime());
                String note = recurringExpense.getNote() == null ? "" : recurringExpense.getNote();

                int duplicateCount = repository.countExactExpense(
                        recurringExpense.getAmount(),
                        recurringExpense.getCategory(),
                        generatedDate,
                        note
                );

                if (duplicateCount == 0) {
                    ExpenseEntity generated = new ExpenseEntity(
                            recurringExpense.getAmount(),
                            recurringExpense.getCategory(),
                            generatedDate,
                            note
                    );
                    generated.setRecurring(false);
                    generated.setRecurringInterval("None");
                    repository.insertExpense(generated);
                    insertedNewExpense = true;
                }
                safetyCounter++;
            }
        }

        if (insertedNewExpense) {
            Toast.makeText(context, "Recurring expenses updated", Toast.LENGTH_SHORT).show();
        }
        return insertedNewExpense;
    }

    private void resetTime(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }
}
