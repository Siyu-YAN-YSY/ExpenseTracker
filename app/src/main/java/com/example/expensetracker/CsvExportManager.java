package com.example.expensetracker;

import android.content.Context;
import android.net.Uri;
import android.widget.Toast;

import java.io.OutputStream;
import java.util.List;

public class CsvExportManager {
    private final Context context;
    private final ExpenseRepository repository;

    public CsvExportManager(Context context, ExpenseRepository repository) {
        this.context = context;
        this.repository = repository;
    }

    public boolean hasExpensesToExport() {
        return !repository.getAllExpenses().isEmpty();
    }

    public void exportToCsv(Uri uri) {
        List<ExpenseEntity> allExpenses = repository.getAllExpenses();
        StringBuilder csv = new StringBuilder();
        csv.append("Amount,Category,Date,Note,Recurring,Recurring Interval\n");

        for (ExpenseEntity expense : allExpenses) {
            csv.append(csvEscape(expense.getAmount())).append(",")
                    .append(csvEscape(expense.getCategory())).append(",")
                    .append(csvEscape(expense.getDate())).append(",")
                    .append(csvEscape(expense.getNote())).append(",")
                    .append(expense.isRecurring() ? "Yes" : "No").append(",")
                    .append(csvEscape(expense.getRecurringInterval())).append("\n");
        }

        try (OutputStream outputStream = context.getContentResolver().openOutputStream(uri)) {
            if (outputStream != null) {
                outputStream.write(csv.toString().getBytes());
                Toast.makeText(context, "CSV exported successfully", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(context, "Export failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private String csvEscape(String value) {
        if (value == null) value = "";
        String escaped = value.replace("\"", "\"\"");
        return "\"" + escaped + "\"";
    }
}
