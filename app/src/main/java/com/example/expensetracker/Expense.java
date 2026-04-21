package com.example.expensetracker;

public class Expense {
    private String amount;
    private String category;
    private String date;
    private String note;

    public Expense(String amount, String category, String date, String note) {
        this.amount = amount;
        this.category = category;
        this.date = date;
        this.note = note;
    }

    public String getAmount() {
        return amount;
    }

    public String getCategory() {
        return category;
    }

    public String getDate() {
        return date;
    }

    public String getNote() {
        return note;
    }
}