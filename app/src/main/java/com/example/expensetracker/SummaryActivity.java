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
        tvTransportTotal = findViewById(R.id.tvTransportTotal);
        tvShoppingTotal = findViewById(R.id.tvShoppingTotal);
        tvBillsTotal = findViewById(R.id.tvBillsTotal);
        tvOtherTotal = findViewById(R.id.tvOtherTotal);
        tvGrandTotal = findViewById(R.id.tvGrandTotal);

        ExpenseDatabase database = ExpenseDatabase.getDatabase(this);
        List<ExpenseEntity> expenses = database.expenseDao().getAllExpenses();

        double food = 0, transport = 0, shopping = 0, bills = 0, other = 0;

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

        double grandTotal = food + transport + shopping + bills + other;

        tvFoodTotal.setText(String.format(Locale.US, "$%.2f", food));
        tvTransportTotal.setText(String.format(Locale.US, "$%.2f", transport));
        tvShoppingTotal.setText(String.format(Locale.US, "$%.2f", shopping));
        tvBillsTotal.setText(String.format(Locale.US, "$%.2f", bills));
        tvOtherTotal.setText(String.format(Locale.US, "$%.2f", other));
        tvGrandTotal.setText(String.format(Locale.US, "$%.2f", grandTotal));
    }
}