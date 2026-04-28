package com.example.expensetracker;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Calendar;
import java.util.Locale;

public class AddExpenseActivity extends AppCompatActivity {

    private boolean isEditMode = false;
    private int editingExpenseId = -1;

    private ExpenseDatabase database;
    private Spinner spinnerAddCategory;
    private TextInputEditText etAmount;
    private TextInputEditText etDate;
    private TextInputEditText etNote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
        etAmount = findViewById(R.id.etAmount);
        etDate = findViewById(R.id.etDate);
        etNote = findViewById(R.id.etNote);
        TextInputLayout tilDate = findViewById(R.id.tilDate);
        MaterialButton btnSaveExpense = findViewById(R.id.btnSaveExpense);
        MaterialButton btnCancel = findViewById(R.id.btnCancel);

        ArrayAdapter<CharSequence> categoryAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.expense_categories,
                android.R.layout.simple_spinner_item
        );
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAddCategory.setAdapter(categoryAdapter);

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

            tvAddTitle.setText("Edit Expense");
            btnSaveExpense.setText("Update Expense");

            etAmount.setText(amount);
            etDate.setText(date);
            etNote.setText(note);

            String[] categories = getResources().getStringArray(R.array.expense_categories);
            for (int i = 0; i < categories.length; i++) {
                if (categories[i].equals(category)) {
                    spinnerAddCategory.setSelection(i);
                    break;
                }
            }
        } else {
            setTodayDate();
        }

        btnSaveExpense.setOnClickListener(v -> saveExpense());
        btnCancel.setOnClickListener(v -> finish());
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
        String category = spinnerAddCategory.getSelectedItem().toString();

        if (amountText.isEmpty()) {
            Toast.makeText(this, "Amount is required", Toast.LENGTH_SHORT).show();
            return;
        }

        double amountValue;
        try {
            amountValue = Double.parseDouble(amountText);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Enter a valid amount", Toast.LENGTH_SHORT).show();
            return;
        }

        if (amountValue <= 0) {
            Toast.makeText(this, "Amount must be greater than 0", Toast.LENGTH_SHORT).show();
            return;
        }

        if (date.isEmpty()) {
            Toast.makeText(this, "Date is required", Toast.LENGTH_SHORT).show();
            return;
        }

        String finalAmount = String.format(Locale.US, "%.2f", amountValue);

        if (isEditMode && editingExpenseId != -1) {
            ExpenseEntity expense = database.expenseDao().getExpenseById(editingExpenseId);
            if (expense != null) {
                expense.setAmount(finalAmount);
                expense.setCategory(category);
                expense.setDate(date);
                expense.setNote(note);
                database.expenseDao().updateExpense(expense);
            }
        } else {
            ExpenseEntity expense = new ExpenseEntity(finalAmount, category, date, note);
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
}
