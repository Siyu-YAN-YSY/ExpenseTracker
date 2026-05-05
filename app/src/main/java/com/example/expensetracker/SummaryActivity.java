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

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.summary_main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button btnBackHome = findViewById(R.id.btnBackHome);
        btnBackHome.setOnClickListener(v -> finish());

        tvFoodTotal = findViewById(R.id.tvFoodTotal);
        tvEntertainmentTotal = findViewById(R.id.tvEntertainmentTotal);
        tvTransportTotal = findViewById(R.id.tvTransportTotal);
        tvShoppingTotal = findViewById(R.id.tvShoppingTotal);
        tvBillsTotal = findViewById(R.id.tvBillsTotal);
        tvOtherTotal = findViewById(R.id.tvOtherTotal);
        tvGrandTotal = findViewById(R.id.tvGrandTotal);

        ExpenseDatabase database = ExpenseDatabase.getDatabase(this);

        String selectedMonth = getIntent().getStringExtra("selected_month");
        String selectedCategory = getIntent().getStringExtra("selected_category");

        if (selectedMonth == null) selectedMonth = "All";
        if (selectedCategory == null) selectedCategory = "All";

        List<ExpenseEntity> expenses;

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
                if (selectedCategory.equals("All")) {
                    expenses = database.expenseDao().getAllExpenses();
                } else {
                    expenses = database.expenseDao().getExpensesByCategory(selectedCategory);
                }
            }
        }

        double food = 0;
        double entertainment = 0;
        double transport = 0;
        double shopping = 0;
        double bills = 0;
        double other = 0;

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

        double grandTotal = food + entertainment + transport + shopping + bills + other;

        tvFoodTotal.setText(CurrencyManager.formatAmount(this, food));
        tvEntertainmentTotal.setText(CurrencyManager.formatAmount(this, entertainment));
        tvTransportTotal.setText(CurrencyManager.formatAmount(this, transport));
        tvShoppingTotal.setText(CurrencyManager.formatAmount(this, shopping));
        tvBillsTotal.setText(CurrencyManager.formatAmount(this, bills));
        tvOtherTotal.setText(CurrencyManager.formatAmount(this, other));
        tvGrandTotal.setText(CurrencyManager.formatAmount(this, grandTotal));
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleManager.applyLanguage(newBase));
    }
}