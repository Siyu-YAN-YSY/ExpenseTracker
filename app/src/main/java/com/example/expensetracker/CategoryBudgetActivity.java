package com.example.expensetracker;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Locale;

public class CategoryBudgetActivity extends AppCompatActivity {

    // TextInputLayouts for showing currency prefixes and validation errors
    private TextInputLayout tilFoodBudget;
    private TextInputLayout tilEntertainmentBudget;
    private TextInputLayout tilTransportBudget;
    private TextInputLayout tilShoppingBudget;
    private TextInputLayout tilBillsBudget;
    private TextInputLayout tilOtherBudget;

    // EditText fields where the user enters category budget amounts
    private TextInputEditText etFoodBudget;
    private TextInputEditText etEntertainmentBudget;
    private TextInputEditText etTransportBudget;
    private TextInputEditText etShoppingBudget;
    private TextInputEditText etBillsBudget;
    private TextInputEditText etOtherBudget;

    // Applies the selected app language before loading the activity
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleManager.applyLanguage(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_budget);

        // Adds padding so the layout does not overlap system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.category_budget_main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Sets up views, currency symbols, and existing saved values
        initializeViews();
        applyCurrencyHints();
        loadBudgets();

        MaterialButton btnSaveCategoryBudgets = findViewById(R.id.btnSaveCategoryBudgets);
        MaterialButton btnBackCategoryBudget = findViewById(R.id.btnBackCategoryBudget);

        // Saves category budgets when the save button is clicked
        btnSaveCategoryBudgets.setOnClickListener(v -> saveBudgets());

        // Closes the screen without saving
        btnBackCategoryBudget.setOnClickListener(v -> finish());
    }

    // Connects Java variables to their XML layout views
    private void initializeViews() {
        tilFoodBudget = findViewById(R.id.tilFoodBudget);
        tilEntertainmentBudget = findViewById(R.id.tilEntertainmentBudget);
        tilTransportBudget = findViewById(R.id.tilTransportBudget);
        tilShoppingBudget = findViewById(R.id.tilShoppingBudget);
        tilBillsBudget = findViewById(R.id.tilBillsBudget);
        tilOtherBudget = findViewById(R.id.tilOtherBudget);

        etFoodBudget = findViewById(R.id.etFoodBudget);
        etEntertainmentBudget = findViewById(R.id.etEntertainmentBudget);
        etTransportBudget = findViewById(R.id.etTransportBudget);
        etShoppingBudget = findViewById(R.id.etShoppingBudget);
        etBillsBudget = findViewById(R.id.etBillsBudget);
        etOtherBudget = findViewById(R.id.etOtherBudget);
    }

    // Displays the selected currency symbol before each budget input field
    private void applyCurrencyHints() {
        String symbol = CurrencyManager.getCurrencySymbol(this);

        tilFoodBudget.setPrefixText(symbol);
        tilEntertainmentBudget.setPrefixText(symbol);
        tilTransportBudget.setPrefixText(symbol);
        tilShoppingBudget.setPrefixText(symbol);
        tilBillsBudget.setPrefixText(symbol);
        tilOtherBudget.setPrefixText(symbol);
    }

    // Loads saved category budget values from SharedPreferences
    private void loadBudgets() {
        SharedPreferences prefs = getSharedPreferences("category_budgets", MODE_PRIVATE);

        setBudgetText(etFoodBudget, prefs.getFloat("budget_Food", 0f));
        setBudgetText(etEntertainmentBudget, prefs.getFloat("budget_Entertainment", 0f));
        setBudgetText(etTransportBudget, prefs.getFloat("budget_Transport", 0f));
        setBudgetText(etShoppingBudget, prefs.getFloat("budget_Shopping", 0f));
        setBudgetText(etBillsBudget, prefs.getFloat("budget_Bills", 0f));
        setBudgetText(etOtherBudget, prefs.getFloat("budget_Other", 0f));
    }

    // Shows the saved amount only if it is greater than zero
    private void setBudgetText(TextInputEditText editText, float amount) {
        if (amount > 0) {
            editText.setText(String.format(Locale.US, "%.2f", amount));
        } else {
            editText.setText("");
        }
    }

    // Validates all category budget fields and saves them locally
    private void saveBudgets() {
        Float food = readBudget(etFoodBudget, tilFoodBudget, getString(R.string.food_budget));
        Float entertainment = readBudget(etEntertainmentBudget, tilEntertainmentBudget, getString(R.string.entertainment_budget));
        Float transport = readBudget(etTransportBudget, tilTransportBudget, getString(R.string.transport_budget));
        Float shopping = readBudget(etShoppingBudget, tilShoppingBudget, getString(R.string.shopping_budget));
        Float bills = readBudget(etBillsBudget, tilBillsBudget, getString(R.string.bills_budget));
        Float other = readBudget(etOtherBudget, tilOtherBudget, getString(R.string.other_budget));

        // Stops saving if any input field contains invalid data
        if (food == null || entertainment == null || transport == null
                || shopping == null || bills == null || other == null) {
            return;
        }

        // Saves all category budgets in SharedPreferences
        getSharedPreferences("category_budgets", MODE_PRIVATE)
                .edit()
                .putFloat("budget_Food", food)
                .putFloat("budget_Entertainment", entertainment)
                .putFloat("budget_Transport", transport)
                .putFloat("budget_Shopping", shopping)
                .putFloat("budget_Bills", bills)
                .putFloat("budget_Other", other)
                .apply();

        // Confirms saving and returns to the previous screen
        Toast.makeText(this, getString(R.string.category_budgets_saved), Toast.LENGTH_SHORT).show();
        finish();
    }

    // Reads and validates one budget input field
    private Float readBudget(TextInputEditText editText, TextInputLayout inputLayout, String label) {
        String value = editText.getText() == null ? "" : editText.getText().toString().trim();

        // Empty fields are treated as a budget of 0
        if (value.isEmpty()) {
            inputLayout.setError(null);
            return 0f;
        }

        try {
            float amount = Float.parseFloat(value);

            // Prevents negative budget values
            if (amount < 0) {
                inputLayout.setError(label + " " + getString(R.string.cannot_be_negative));
                return null;
            }

            inputLayout.setError(null);
            return amount;
        } catch (NumberFormatException e) {
            // Shows an error if the user entered text that is not a number
            inputLayout.setError(getString(R.string.enter_a_valid_amount));
            return null;
        }
    }
}
