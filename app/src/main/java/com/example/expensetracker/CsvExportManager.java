package com.example.expensetracker;

import android.content.Context;
import android.net.Uri;
import android.widget.Toast;

import java.io.OutputStream;
import java.util.List;

public class CsvExportManager {

    // Context used to access the content resolver and show Toast messages
    private final Context context;

    // Repository used to retrieve saved expenses
    private final ExpenseRepository repository;

    // Constructor receives the app context and expense repository
    public CsvExportManager(Context context, ExpenseRepository repository) {
        this.context = context;
        this.repository = repository;
    }

    // Checks whether there are any expenses available to export
    public boolean hasExpensesToExport() {
        return !repository.getAllExpenses().isEmpty();
    }

    // Exports all saved expenses to a CSV file at the selected URI
    public void exportToCsv(Uri uri) {
        List<ExpenseEntity> allExpenses = repository.getAllExpenses();

        // Builds the CSV file content
        StringBuilder csv = new StringBuilder();
        csv.append("Amount,Category,Date,Note,Recurring,Recurring Interval\n");

        // Adds each expense as one row in the CSV file
        for (ExpenseEntity expense : allExpenses) {
            csv.append(csvEscape(expense.getAmount())).append(",")
                    .append(csvEscape(expense.getCategory())).append(",")
                    .append(csvEscape(expense.getDate())).append(",")
                    .append(csvEscape(expense.getNote())).append(",")
                    .append(expense.isRecurring() ? "Yes" : "No").append(",")
                    .append(csvEscape(expense.getRecurringInterval())).append("\n");
        }

        // Writes the CSV content to the file chosen by the user
        try (OutputStream outputStream = context.getContentResolver().openOutputStream(uri)) {
            if (outputStream != null) {
                outputStream.write(csv.toString().getBytes());
                Toast.makeText(context, "CSV exported successfully", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            // Shows an error message if exporting fails
            Toast.makeText(context, "Export failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // Escapes CSV values by wrapping them in quotes and handling existing quotation marks
    private String csvEscape(String value) {
        if (value == null) value = "";

        // Doubles quotation marks so they are safely stored in CSV format
        String escaped = value.replace("\"", "\"\"");

        return "\"" + escaped + "\"";
    }
}
