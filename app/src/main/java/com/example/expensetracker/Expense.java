package com.example.expensetracker;

public class Expense {

    // Stores the expense amount as text
    private String amount;

    // Stores the category of the expense
    private String category;

    // Stores the date when the expense occurred
    private String date;

    // Stores an optional note or description for the expense
    private String note;

    // Constructor creates a new Expense object with amount, category, date, and note
    public Expense(String amount, String category, String date, String note) {
        this.amount = amount;
        this.category = category;
        this.date = date;
        this.note = note;
    }

    // Returns the expense amount
    public String getAmount() {
        return amount;
    }

    // Returns the expense category
    public String getCategory() {
        return category;
    }

    // Returns the expense date
    public String getDate() {
        return date;
    }

    // Returns the expense note
    public String getNote() {
        return note;
    }
}