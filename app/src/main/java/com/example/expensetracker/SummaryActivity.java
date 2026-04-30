package com.example.expensetracker;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.List;
import java.util.Locale;

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
        if (selectedMonth == null) {
            selectedMonth = "All";
        }

        List<ExpenseEntity> expenses;

        if (selectedMonth.equals("All")) {
            expenses = database.expenseDao().getAllExpenses();
        } else {
            String[] parts = selectedMonth.split("/");
            if (parts.length >= 2) {
                String month = parts[0];
                String year = parts[1];
                expenses = database.expenseDao().getExpensesByMonth(month, year);
            } else {
                expenses = database.expenseDao().getAllExpenses();
            }
        }

        double food = 0, entertainment = 0, transport = 0, shopping = 0, bills = 0, other = 0;

        for (ExpenseEntity expense : expenses) {
            double amount;
            try {
                amount = Double.parseDouble(expense.getAmount().replace("$", "").trim());
            } catch (NumberFormatException e) {
                continue;
            }

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

        tvFoodTotal.setText(String.format(Locale.US, "$%.2f", food));
        tvEntertainmentTotal.setText(String.format(Locale.US, "$%.2f", entertainment));
        tvTransportTotal.setText(String.format(Locale.US, "$%.2f", transport));
        tvShoppingTotal.setText(String.format(Locale.US, "$%.2f", shopping));
        tvBillsTotal.setText(String.format(Locale.US, "$%.2f", bills));
        tvOtherTotal.setText(String.format(Locale.US, "$%.2f", other));
        tvGrandTotal.setText(String.format(Locale.US, "$%.2f", grandTotal));
    }
}