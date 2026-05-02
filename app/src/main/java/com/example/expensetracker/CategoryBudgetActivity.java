package com.example.expensetracker;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Locale;

public class CategoryBudgetActivity extends AppCompatActivity {

    private final String[] categories = {"Food", "Entertainment", "Transport", "Shopping", "Bills", "Other"};

    private TextInputEditText etFoodBudget;
    private TextInputEditText etEntertainmentBudget;
    private TextInputEditText etTransportBudget;
    private TextInputEditText etShoppingBudget;
    private TextInputEditText etBillsBudget;
    private TextInputEditText etOtherBudget;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_budget);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.category_budget_main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initializeViews();
        loadBudgets();

        MaterialButton btnSaveCategoryBudgets = findViewById(R.id.btnSaveCategoryBudgets);
        MaterialButton btnBackCategoryBudget = findViewById(R.id.btnBackCategoryBudget);

        btnSaveCategoryBudgets.setOnClickListener(v -> saveBudgets());
        btnBackCategoryBudget.setOnClickListener(v -> finish());
    }

    private void initializeViews() {
        etFoodBudget = findViewById(R.id.etFoodBudget);
        etEntertainmentBudget = findViewById(R.id.etEntertainmentBudget);
        etTransportBudget = findViewById(R.id.etTransportBudget);
        etShoppingBudget = findViewById(R.id.etShoppingBudget);
        etBillsBudget = findViewById(R.id.etBillsBudget);
        etOtherBudget = findViewById(R.id.etOtherBudget);
    }

    private void loadBudgets() {
        SharedPreferences prefs = getSharedPreferences("category_budgets", MODE_PRIVATE);
        setBudgetText(etFoodBudget, prefs.getFloat("budget_Food", 0f));
        setBudgetText(etEntertainmentBudget, prefs.getFloat("budget_Entertainment", 0f));
        setBudgetText(etTransportBudget, prefs.getFloat("budget_Transport", 0f));
        setBudgetText(etShoppingBudget, prefs.getFloat("budget_Shopping", 0f));
        setBudgetText(etBillsBudget, prefs.getFloat("budget_Bills", 0f));
        setBudgetText(etOtherBudget, prefs.getFloat("budget_Other", 0f));
    }

    private void setBudgetText(TextInputEditText editText, float amount) {
        if (amount > 0) {
            editText.setText(String.format(Locale.US, "%.2f", amount));
        } else {
            editText.setText("");
        }
    }

    private void saveBudgets() {
        Float food = readBudget(etFoodBudget, "Food");
        Float entertainment = readBudget(etEntertainmentBudget, "Entertainment");
        Float transport = readBudget(etTransportBudget, "Transport");
        Float shopping = readBudget(etShoppingBudget, "Shopping");
        Float bills = readBudget(etBillsBudget, "Bills");
        Float other = readBudget(etOtherBudget, "Other");

        if (food == null || entertainment == null || transport == null
                || shopping == null || bills == null || other == null) {
            return;
        }

        getSharedPreferences("category_budgets", MODE_PRIVATE)
                .edit()
                .putFloat("budget_Food", food)
                .putFloat("budget_Entertainment", entertainment)
                .putFloat("budget_Transport", transport)
                .putFloat("budget_Shopping", shopping)
                .putFloat("budget_Bills", bills)
                .putFloat("budget_Other", other)
                .apply();

        Toast.makeText(this, "Category budgets saved", Toast.LENGTH_SHORT).show();
        finish();
    }

    private Float readBudget(TextInputEditText editText, String label) {
        String value = editText.getText() == null ? "" : editText.getText().toString().trim();
        if (value.isEmpty()) {
            return 0f;
        }
        try {
            float amount = Float.parseFloat(value);
            if (amount < 0) {
                editText.setError(label + " budget cannot be negative");
                return null;
            }
            editText.setError(null);
            return amount;
        } catch (NumberFormatException e) {
            editText.setError("Enter a valid amount");
            return null;
        }
    }
}
