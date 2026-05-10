package com.example.expensetracker;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ExpenseDateUtils {

    // Date formatter used to parse and format dates in MM/DD/YYYY format
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MM/dd/yyyy", Locale.US);

    // Converts a date string into a Date object
    public Date parseDate(String value) {
        try {
            return DATE_FORMAT.parse(value);
        } catch (ParseException | NullPointerException e) {
            // Returns null if the date is empty or invalid
            return null;
        }
    }

    // Converts a Date object into a formatted date string
    public String formatDate(Date date) {
        return DATE_FORMAT.format(date);
    }

    // Compares two expenses by date, then by ID if the dates are the same
    public int compareByDate(ExpenseEntity first, ExpenseEntity second) {
        Date firstDate = parseDate(first.getDate());
        Date secondDate = parseDate(second.getDate());

        // If both dates are invalid, compare by ID instead
        if (firstDate == null && secondDate == null) {
            return Integer.compare(first.getId(), second.getId());
        }

        // Places expenses with invalid dates before expenses with valid dates
        if (firstDate == null) return -1;
        if (secondDate == null) return 1;

        // Compares expenses by their parsed dates
        int result = firstDate.compareTo(secondDate);

        // If dates are equal, use the expense ID as a backup comparison
        if (result == 0) {
            result = Integer.compare(first.getId(), second.getId());
        }

        return result;
    }

    // Moves the calendar forward based on the recurring interval
    public void moveToNextRecurringDate(Calendar calendar, String interval) {
        if ("Weekly".equals(interval)) {
            calendar.add(Calendar.WEEK_OF_YEAR, 1);
        } else if ("Yearly".equals(interval)) {
            calendar.add(Calendar.YEAR, 1);
        } else {
            // Defaults to monthly recurrence
            calendar.add(Calendar.MONTH, 1);
        }
    }
}
