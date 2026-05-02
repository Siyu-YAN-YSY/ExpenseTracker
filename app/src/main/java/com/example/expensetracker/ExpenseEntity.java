package com.example.expensetracker;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "expenses")
public class ExpenseEntity {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String amount;
    private String category;
    private String date;
    private String note;

    @ColumnInfo(name = "is_recurring", defaultValue = "0")
    private boolean recurring;

    @ColumnInfo(name = "recurring_interval", defaultValue = "'None'")
    private String recurringInterval;

    public ExpenseEntity(String amount, String category, String date, String note) {
        this.amount = amount;
        this.category = category;
        this.date = date;
        this.note = note;
        this.recurring = false;
        this.recurringInterval = "None";
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public boolean isRecurring() {
        return recurring;
    }

    public void setRecurring(boolean recurring) {
        this.recurring = recurring;
    }

    public String getRecurringInterval() {
        return recurringInterval == null ? "None" : recurringInterval;
    }

    public void setRecurringInterval(String recurringInterval) {
        if (recurringInterval == null || recurringInterval.trim().isEmpty()) {
            this.recurringInterval = "None";
        } else {
            this.recurringInterval = recurringInterval;
        }
    }

    // Converts stored amount string into numeric value and handles invalid formats safely.
    public double getAmountValue() {
        try {
            return Double.parseDouble(amount.replace("$", "").trim());
        } catch (Exception e) {
            return 0;
        }
    }
}
