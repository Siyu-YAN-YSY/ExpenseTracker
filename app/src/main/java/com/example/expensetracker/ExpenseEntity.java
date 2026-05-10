package com.example.expensetracker;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

// Represents one expense record in the Room database
@Entity(tableName = "expenses")
public class ExpenseEntity {

    // Unique ID for each expense, generated automatically by Room
    @PrimaryKey(autoGenerate = true)
    private int id;

    // Expense details stored in the database
    private String amount;
    private String category;
    private String date;
    private String note;

    // Stores whether this expense repeats over time
    @ColumnInfo(name = "is_recurring", defaultValue = "0")
    private boolean recurring;

    // Stores how often the expense repeats, such as Weekly, Monthly, or Yearly
    @ColumnInfo(name = "recurring_interval", defaultValue = "'None'")
    private String recurringInterval;

    // Constructor creates a new expense with default recurring settings
    public ExpenseEntity(String amount, String category, String date, String note) {
        this.amount = amount;
        this.category = category;
        this.date = date;
        this.note = note;
        this.recurring = false;
        this.recurringInterval = "None";
    }

    // Returns the expense ID
    public int getId() {
        return id;
    }

    // Sets the expense ID
    public void setId(int id) {
        this.id = id;
    }

    // Returns the expense amount
    public String getAmount() {
        return amount;
    }

    // Updates the expense amount
    public void setAmount(String amount) {
        this.amount = amount;
    }

    // Returns the expense category
    public String getCategory() {
        return category;
    }

    // Updates the expense category
    public void setCategory(String category) {
        this.category = category;
    }

    // Returns the expense date
    public String getDate() {
        return date;
    }

    // Updates the expense date
    public void setDate(String date) {
        this.date = date;
    }

    // Returns the expense note
    public String getNote() {
        return note;
    }

    // Updates the expense note
    public void setNote(String note) {
        this.note = note;
    }

    // Returns whether the expense is recurring
    public boolean isRecurring() {
        return recurring;
    }

    // Updates whether the expense is recurring
    public void setRecurring(boolean recurring) {
        this.recurring = recurring;
    }

    // Returns the recurring interval, or "None" if it was not set
    public String getRecurringInterval() {
        return recurringInterval == null ? "None" : recurringInterval;
    }

    // Updates the recurring interval and prevents empty values from being saved
    public void setRecurringInterval(String recurringInterval) {
        if (recurringInterval == null || recurringInterval.trim().isEmpty()) {
            this.recurringInterval = "None";
        } else {
            this.recurringInterval = recurringInterval;
        }
    }

    // Converts the saved amount text into a number for calculations
    public double getAmountValue() {
        try {
            return Double.parseDouble(amount.replace("$", "").trim());
        } catch (Exception e) {
            // Returns 0 if the amount is empty or invalid
            return 0;
        }
    }
}
