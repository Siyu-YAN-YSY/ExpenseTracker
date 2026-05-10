package com.example.expensetracker;

import android.content.Context;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.List;

public class SummaryActivity extends AppCompatActivity {

    // TextViews used to display totals for each category and the grand total
    private TextView tvFoodTotal;
    private TextView tvEntertainmentTotal;
    private TextView tvTransportTotal;
    private TextView tvShoppingTotal;
    private TextView tvBillsTotal;
    private TextView tvOtherTotal;
    private TextView tvGrandTotal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);

        // Adds padding so content does not overlap with system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.summary_main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Closes the summary screen and returns to the previous screen
        Button btnBackHome = findViewById(R.id.btnBackHome);
        btnBackHome.setOnClickListener(v -> finish());

        // Connects Java variables to XML views
        tvFoodTotal = findViewById(R.id.tvFoodTotal);
        tvEntertainmentTotal = findViewById(R.id.tvEntertainmentTotal);
        tvTransportTotal = findViewById(R.id.tvTransportTotal);
        tvShoppingTotal = findViewById(R.id.tvShoppingTotal);
        tvBillsTotal = findViewById(R.id.tvBillsTotal);
        tvOtherTotal = findViewById(R.id.tvOtherTotal);
        tvGrandTotal = findViewById(R.id.tvGrandTotal);

        // Gets the app database instance
        ExpenseDatabase database = ExpenseDatabase.getDatabase(this);

        // Gets selected filters passed from the home screen
        String selectedMonth = getIntent().getStringExtra("selected_month");
        String selectedCategory = getIntent().getStringExtra("selected_category");

        // Uses "All" as the default filter value
        if (selectedMonth == null) selectedMonth = "All";
        if (selectedCategory == null) selectedCategory = "All";

        List<ExpenseEntity> expenses;

        // Loads expenses based on selected month and category filters
        if (selectedMonth.equals("All")) {
            if (selectedCategory.equals("All")) {
                expenses = database.expenseDao().getAllExpenses();
            } else {
                expenses = database.expenseDao().getExpensesByCategory(selectedCategory);
            }
        } else {
            String[] parts = selectedMonth.split("/");

            if (parts.length >= 2) {
                String month = parts[0];
                String year = parts[1];

                if (selectedCategory.equals("All")) {
                    expenses = database.expenseDao().getExpensesByMonth(month, year);
                } else {
                    expenses = database.expenseDao().getExpensesByCategoryAndMonth(selectedCategory, month, year);
                }
            } else {
                // Falls back to category-only filtering if the month format is invalid
                if (selectedCategory.equals("All")) {
                    expenses = database.expenseDao().getAllExpenses();
                } else {
                    expenses = database.expenseDao().getExpensesByCategory(selectedCategory);
                }
            }
        }

        // Running totals for each expense category
        double food = 0;
        double entertainment = 0;
        double transport = 0;
        double shopping = 0;
        double bills = 0;
        double other = 0;

        // Adds each expense amount to the correct category total
        for (ExpenseEntity expense : expenses) {
            double amount = expense.getAmountValue();

            switch (expense.getCategory()) {
                case "Food":
                    food += amount;
                    break;
                case "Entertainment":
                    entertainment += amount;
                    break;
                case "Transport":
                    transport += amount;
                    break;
                case "Shopping":
                    shopping += amount;
                    break;
                case "Bills":
                    bills += amount;
                    break;
                default:
                    other += amount;
                    break;
            }
        }

        // Calculates the total of all categories
        double grandTotal = food + entertainment + transport + shopping + bills + other;

        // Displays formatted currency totals
        tvFoodTotal.setText(CurrencyManager.formatAmount(this, food));
        tvEntertainmentTotal.setText(CurrencyManager.formatAmount(this, entertainment));
        tvTransportTotal.setText(CurrencyManager.formatAmount(this, transport));
        tvShoppingTotal.setText(CurrencyManager.formatAmount(this, shopping));
        tvBillsTotal.setText(CurrencyManager.formatAmount(this, bills));
        tvOtherTotal.setText(CurrencyManager.formatAmount(this, other));
        tvGrandTotal.setText(CurrencyManager.formatAmount(this, grandTotal));
    }

    // Applies the selected app language before attaching the activity context
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleManager.applyLanguage(newBase));
    }
}