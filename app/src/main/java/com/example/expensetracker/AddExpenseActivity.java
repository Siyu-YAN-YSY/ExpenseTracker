package com.example.expensetracker;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Calendar;
import java.util.Locale;

public class AddExpenseActivity extends AppCompatActivity {

    private final String[] expenseCategoryValues = {
            "Food", "Entertainment", "Transport", "Shopping", "Bills", "Other"
    };

    private final String[] recurringIntervalValues = {
            "Weekly", "Monthly", "Yearly"
    };

    private boolean isEditMode = false;
    private int editingExpenseId = -1;

    private ExpenseDatabase database;
    private Spinner spinnerAddCategory;
    private Spinner spinnerRecurringInterval;
    private TextInputEditText etAmount;
    private TextInputEditText etDate;
    private TextInputEditText etNote;
    private CheckBox cbRecurring;

    private void applySavedTheme() {
        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        boolean darkMode = prefs.getBoolean("dark_mode", false);

        AppCompatDelegate.setDefaultNightMode(
                darkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        applySavedTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_expense);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.add_expense_main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        database = ExpenseDatabase.getDatabase(this);

        TextView tvAddTitle = findViewById(R.id.tvAddTitle);
        spinnerAddCategory = findViewById(R.id.spinnerAddCategory);
        spinnerRecurringInterval = findViewById(R.id.spinnerRecurringInterval);
        etAmount = findViewById(R.id.etAmount);
        etDate = findViewById(R.id.etDate);
        etNote = findViewById(R.id.etNote);
        cbRecurring = findViewById(R.id.cbRecurring);
        TextInputLayout tilDate = findViewById(R.id.tilDate);
        MaterialButton btnSaveExpense = findViewById(R.id.btnSaveExpense);
        MaterialButton btnCancel = findViewById(R.id.btnCancel);

        setupCategorySpinner();
        setupRecurringSpinner();

        cbRecurring.setOnCheckedChangeListener((buttonView, isChecked) ->
                spinnerRecurringInterval.setVisibility(isChecked ? View.VISIBLE : View.GONE));

        etDate.setOnClickListener(v -> showDatePicker());
        tilDate.setEndIconOnClickListener(v -> showDatePicker());

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("expense_id")) {
            isEditMode = true;
            editingExpenseId = intent.getIntExtra("expense_id", -1);

            String amount = intent.getStringExtra("amount");
            String category = intent.getStringExtra("category");
            String date = intent.getStringExtra("date");
            String note = intent.getStringExtra("note");
            boolean isRecurring = intent.getBooleanExtra("is_recurring", false);
            String recurringInterval = intent.getStringExtra("recurring_interval");

            tvAddTitle.setText(R.string.edit_expense);
            btnSaveExpense.setText(R.string.update_expense);

            etAmount.setText(amount);
            etDate.setText(date);
            etNote.setText(note);
            cbRecurring.setChecked(isRecurring);
            spinnerRecurringInterval.setVisibility(isRecurring ? View.VISIBLE : View.GONE);

            for (int i = 0; i < expenseCategoryValues.length; i++) {
                if (expenseCategoryValues[i].equals(category)) {
                    spinnerAddCategory.setSelection(i);
                    break;
                }
            }

            if (recurringInterval != null) {
                for (int i = 0; i < recurringIntervalValues.length; i++) {
                    if (recurringIntervalValues[i].equals(recurringInterval)) {
                        spinnerRecurringInterval.setSelection(i);
                        break;
                    }
                }
            }
        } else {
            setTodayDate();
        }

        btnSaveExpense.setOnClickListener(v -> saveExpense());
        btnCancel.setOnClickListener(v -> finish());
    }

    private void setupCategorySpinner() {
        String[] displayCategories = getResources().getStringArray(R.array.expense_categories_display);

        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                displayCategories
        );
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAddCategory.setAdapter(categoryAdapter);
    }

    private void setupRecurringSpinner() {
        String[] recurringDisplay = {
                getString(R.string.weekly),
                getString(R.string.monthly),
                getString(R.string.yearly)
        };

        ArrayAdapter<String> recurringAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                recurringDisplay
        );
        recurringAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRecurringInterval.setAdapter(recurringAdapter);
        spinnerRecurringInterval.setVisibility(View.GONE);
    }

    private void setTodayDate() {
        Calendar calendar = Calendar.getInstance();
        String today = String.format(
                Locale.US,
                "%02d/%02d/%04d",
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH),
                calendar.get(Calendar.YEAR)
        );
        etDate.setText(today);
    }

    private void saveExpense() {
        String amountText = etAmount.getText() != null ? etAmount.getText().toString().trim() : "";
        String date = etDate.getText() != null ? etDate.getText().toString().trim() : "";
        String note = etNote.getText() != null ? etNote.getText().toString().trim() : "";
        String category = expenseCategoryValues[spinnerAddCategory.getSelectedItemPosition()];
        boolean isRecurring = cbRecurring.isChecked();
        String recurringInterval = isRecurring
                ? recurringIntervalValues[spinnerRecurringInterval.getSelectedItemPosition()]
                : "None";

        TextInputLayout tilAmount = findViewById(R.id.tilAmount);
        if (amountText.isEmpty()) {
            tilAmount.setError("Amount is required");
            return;
        } else {
            tilAmount.setError(null);
        }

        if (!amountText.matches("^\\d+(\\.\\d{1,2})?$")) {
            tilAmount.setError("Invalid input (e.g., 10.99)");
            return;
        } else {
            tilAmount.setError(null);
        }

        double amountValue;
        try {
            amountValue = Double.parseDouble(amountText);
        } catch (NumberFormatException e) {
            tilAmount.setError("Enter a valid amount");
            return;
        }

        if (amountValue >= 100000) {
            tilAmount.setError("Amount is too large");
            return;
        } else {
            tilAmount.setError(null);
        }

        TextInputLayout tilDate = findViewById(R.id.tilDate);
        if (!date.matches("^(0[1-9]|1[0-2])/(0[1-9]|[12]\\d|3[01])/\\d{4}$")) {
            tilDate.setError("Use MM/DD/YYYY");
            return;
        } else {
            tilDate.setError(null);
        }

        String finalAmount = String.format(Locale.US, "%.2f", amountValue);

        if (isEditMode && editingExpenseId != -1) {
            ExpenseEntity expense = database.expenseDao().getExpenseById(editingExpenseId);
            if (expense != null) {
                expense.setAmount(finalAmount);
                expense.setCategory(category);
                expense.setDate(date);
                expense.setNote(note);
                expense.setRecurring(isRecurring);
                expense.setRecurringInterval(recurringInterval);
                database.expenseDao().updateExpense(expense);
            }
        } else {
            ExpenseEntity expense = new ExpenseEntity(finalAmount, category, date, note);
            expense.setRecurring(isRecurring);
            expense.setRecurringInterval(recurringInterval);
            database.expenseDao().insertExpense(expense);
        }

        setResult(RESULT_OK, new Intent());
        finish();
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();

        String existingDate = etDate.getText() != null ? etDate.getText().toString().trim() : "";
        if (!existingDate.isEmpty()) {
            String[] parts = existingDate.split("/");
            if (parts.length == 3) {
                try {
                    int month = Integer.parseInt(parts[0]) - 1;
                    int day = Integer.parseInt(parts[1]);
                    int year = Integer.parseInt(parts[2]);

                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, day);
                } catch (NumberFormatException ignored) {
                }
            }
        }

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String formattedDate = String.format(
                            Locale.US,
                            "%02d/%02d/%04d",
                            selectedMonth + 1,
                            selectedDay,
                            selectedYear
                    );
                    etDate.setText(formattedDate);
                },
                year,
                month,
                day
        );
        datePickerDialog.show();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleManager.applyLanguage(newBase));
    }
}
