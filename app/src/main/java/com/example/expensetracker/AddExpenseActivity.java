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

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AddExpenseActivity extends AppCompatActivity {

    private boolean isEditMode = false;
    private int editingExpenseId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_expense);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.add_expense_main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ExpenseDatabase database = ExpenseDatabase.getDatabase(this);

        TextView tvAddTitle = findViewById(R.id.tvAddTitle);
        Spinner spinnerAddCategory = findViewById(R.id.spinnerAddCategory);
        TextInputEditText etAmount = findViewById(R.id.etAmount);
        TextInputEditText etDate = findViewById(R.id.etDate);
        TextInputEditText etNote = findViewById(R.id.etNote);
        MaterialButton btnSaveExpense = findViewById(R.id.btnSaveExpense);
        MaterialButton btnCancel = findViewById(R.id.btnCancel);

        ArrayAdapter<CharSequence> categoryAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.expense_categories,
                android.R.layout.simple_spinner_item
        );
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAddCategory.setAdapter(categoryAdapter);

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
        }

        etDate.setOnClickListener(v -> showDatePicker(etDate));

        btnSaveExpense.setOnClickListener(v -> {
            String amountText = etAmount.getText() != null ? etAmount.getText().toString().trim() : "";
            String date = etDate.getText() != null ? etDate.getText().toString().trim() : "";
            String note = etNote.getText() != null ? etNote.getText().toString().trim() : "";
            String category = spinnerAddCategory.getSelectedItem().toString();

            if (amountText.isEmpty()) {
                Toast.makeText(AddExpenseActivity.this, "Amount is required", Toast.LENGTH_SHORT).show();
                return;
            }

            double amountValue;
            try {
                amountValue = Double.parseDouble(amountText);
            } catch (NumberFormatException e) {
                Toast.makeText(AddExpenseActivity.this, "Enter a valid amount", Toast.LENGTH_SHORT).show();
                return;
            }

            if (amountValue <= 0) {
                Toast.makeText(AddExpenseActivity.this, "Amount must be greater than 0", Toast.LENGTH_SHORT).show();
                return;
            }

            if (date.isEmpty()) {
                Toast.makeText(AddExpenseActivity.this, "Date is required", Toast.LENGTH_SHORT).show();
                return;
            }

            String finalAmount = String.format(Locale.US, "%.2f", amountValue);

            if (isEditMode && editingExpenseId != -1) {
                List<ExpenseEntity> allExpenses = database.expenseDao().getAllExpenses();
                for (ExpenseEntity expense : allExpenses) {
                    if (expense.getId() == editingExpenseId) {
                        expense.setAmount(finalAmount);
                        expense.setCategory(category);
                        expense.setDate(date);
                        expense.setNote(note);
                        database.expenseDao().updateExpense(expense);
                        break;
                    }
                }
            } else {
                ExpenseEntity expense = new ExpenseEntity(finalAmount, category, date, note);
                database.expenseDao().insertExpense(expense);
            }

            setResult(RESULT_OK, new Intent());
            finish();
        });

        btnCancel.setOnClickListener(v -> finish());
    }

    private void showDatePicker(TextInputEditText etDate) {
        Calendar calendar = Calendar.getInstance();

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