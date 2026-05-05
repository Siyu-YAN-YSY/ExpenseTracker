package com.example.expensetracker;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ExpenseDateUtils {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MM/dd/yyyy", Locale.US);

    public Date parseDate(String value) {
        try {
            return DATE_FORMAT.parse(value);
        } catch (ParseException | NullPointerException e) {
            return null;
        }
    }

    public String formatDate(Date date) {
        return DATE_FORMAT.format(date);
    }

    public int compareByDate(ExpenseEntity first, ExpenseEntity second) {
        Date firstDate = parseDate(first.getDate());
        Date secondDate = parseDate(second.getDate());

        if (firstDate == null && secondDate == null) {
            return Integer.compare(first.getId(), second.getId());
        }
        if (firstDate == null) return -1;
        if (secondDate == null) return 1;

        int result = firstDate.compareTo(secondDate);
        if (result == 0) {
            result = Integer.compare(first.getId(), second.getId());
        }
        return result;
    }

    public void moveToNextRecurringDate(Calendar calendar, String interval) {
        if ("Weekly".equals(interval)) {
            calendar.add(Calendar.WEEK_OF_YEAR, 1);
        } else if ("Yearly".equals(interval)) {
            calendar.add(Calendar.YEAR, 1);
        } else {
            calendar.add(Calendar.MONTH, 1);
        }
    }
}
