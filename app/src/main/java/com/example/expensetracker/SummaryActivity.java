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

        ExpenseDatabase database = ExpenseDatabase.getDatabase(this);
        List<ExpenseEntity> expenses = database.expenseDao().getAllExpenses();

        double food = 0, transport = 0, shopping = 0, bills = 0, other = 0;

        for (ExpenseEntity expense : expenses) {
            double amount;
            try {
                amount = Double.parseDouble(expense.getAmount());
            } catch (NumberFormatException e) {
                continue;
            }

            switch (expense.getCategory()) {
                case "Food":
                    food += amount;
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

        ((TextView) findViewById(R.id.tvFoodTotal)).setText(String.format(Locale.US, "$%.2f", food));
        ((TextView) findViewById(R.id.tvTransportTotal)).setText(String.format(Locale.US, "$%.2f", transport));
        ((TextView) findViewById(R.id.tvShoppingTotal)).setText(String.format(Locale.US, "$%.2f", shopping));
        ((TextView) findViewById(R.id.tvBillsTotal)).setText(String.format(Locale.US, "$%.2f", bills));
        ((TextView) findViewById(R.id.tvOtherTotal)).setText(String.format(Locale.US, "$%.2f", other));
    }
}