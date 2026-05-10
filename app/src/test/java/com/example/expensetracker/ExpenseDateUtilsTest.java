package com.example.expensetracker;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import java.util.Calendar;
import java.util.Date;

public class ExpenseDateUtilsTest {

    @Test
    public void parseDate_validDate_returnsDate() {
        ExpenseDateUtils utils = new ExpenseDateUtils();

        Date result = utils.parseDate("05/10/2025");

        assertNotNull(result);
    }

    @Test
    public void parseDate_invalidDate_returnsNull() {
        ExpenseDateUtils utils = new ExpenseDateUtils();

        Date result = utils.parseDate("invalid-date");

        assertNull(result);
    }

    @Test
    public void formatDate_returnsCorrectFormat() {
        ExpenseDateUtils utils = new ExpenseDateUtils();

        Calendar calendar = Calendar.getInstance();
        calendar.set(2025, Calendar.MAY, 10);

        String result = utils.formatDate(calendar.getTime());

        assertEquals("05/10/2025", result);
    }

    @Test
    public void moveToNextRecurringDate_weekly_addsOneWeek() {
        ExpenseDateUtils utils = new ExpenseDateUtils();

        Calendar calendar = Calendar.getInstance();
        calendar.set(2025, Calendar.MAY, 10);

        utils.moveToNextRecurringDate(calendar, "Weekly");

        assertEquals(Calendar.MAY, calendar.get(Calendar.MONTH));
        assertEquals(17, calendar.get(Calendar.DAY_OF_MONTH));
    }

    @Test
    public void moveToNextRecurringDate_monthly_addsOneMonth() {
        ExpenseDateUtils utils = new ExpenseDateUtils();

        Calendar calendar = Calendar.getInstance();
        calendar.set(2025, Calendar.MAY, 10);

        utils.moveToNextRecurringDate(calendar, "Monthly");

        assertEquals(Calendar.JUNE, calendar.get(Calendar.MONTH));
        assertEquals(10, calendar.get(Calendar.DAY_OF_MONTH));
    }

    @Test
    public void moveToNextRecurringDate_yearly_addsOneYear() {
        ExpenseDateUtils utils = new ExpenseDateUtils();

        Calendar calendar = Calendar.getInstance();
        calendar.set(2025, Calendar.MAY, 10);

        utils.moveToNextRecurringDate(calendar, "Yearly");

        assertEquals(2026, calendar.get(Calendar.YEAR));
        assertEquals(Calendar.MAY, calendar.get(Calendar.MONTH));
        assertEquals(10, calendar.get(Calendar.DAY_OF_MONTH));
    }
}
