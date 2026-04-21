package com.example.expensetracker;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private ArrayList<ExpenseEntity> expenseList;
    private ArrayList<ExpenseEntity> filteredList;
    private ExpenseAdapterRoom expenseAdapter;
    private TextView tvTotalAmount;
    private TextView tvEmptyState;
    private Spinner spinnerCategory;
    private ExpenseDatabase database;

    private final ActivityResultLauncher<Intent> addExpenseLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    loadExpensesFromDatabase();
                    applyFilter(spinnerCategory.getSelectedItem().toString());
                    updateTotalExpense();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        database = ExpenseDatabase.getDatabase(this);

        tvTotalAmount = findViewById(R.id.tvTotalAmount);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        spinnerCategory = findViewById(R.id.spinnerCategory);

        ArrayAdapter<CharSequence> filterAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.filter_categories,
                android.R.layout.simple_spinner_item
        );
        filterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(filterAdapter);

        RecyclerView recyclerViewExpenses = findViewById(R.id.recyclerViewExpenses);

        expenseList = new ArrayList<>();
        filteredList = new ArrayList<>();

        expenseAdapter = new ExpenseAdapterRoom(
                filteredList,
                expense -> {
                    database.expenseDao().deleteExpense(expense);
                    loadExpensesFromDatabase();
                    applyFilter(spinnerCategory.getSelectedItem().toString());
                    updateTotalExpense();
                },
                expense -> {
                    Intent intent = new Intent(MainActivity.this, AddExpenseActivity.class);
                    intent.putExtra("expense_id", expense.getId());
                    intent.putExtra("amount", expense.getAmount());
                    intent.putExtra("category", expense.getCategory());
                    intent.putExtra("date", expense.getDate());
                    intent.putExtra("note", expense.getNote());
                    addExpenseLauncher.launch(intent);
                }
        );

        recyclerViewExpenses.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewExpenses.setAdapter(expenseAdapter);

        spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedCategory = parent.getItemAtPosition(position).toString();
                applyFilter(selectedCategory);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        FloatingActionButton fabAdd = findViewById(R.id.fabAdd);
        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddExpenseActivity.class);
            addExpenseLauncher.launch(intent);
        });

        findViewById(R.id.btnSummary).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SummaryActivity.class);
            startActivity(intent);
        });

        loadExpensesFromDatabase();
        applyFilter("All");
        updateTotalExpense();
    }

    private void loadExpensesFromDatabase() {
        List<ExpenseEntity> allExpenses = database.expenseDao().getAllExpenses();
        expenseList.clear();
        expenseList.addAll(allExpenses);
    }

    private void applyFilter(String category) {
        filteredList.clear();

        if (category.equals("All")) {
            filteredList.addAll(expenseList);
        } else {
            for (ExpenseEntity expense : expenseList) {
                if (expense.getCategory().equals(category)) {
                    filteredList.add(expense);
                }
            }
        }

        expenseAdapter.notifyDataSetChanged();
        updateEmptyState();
    }

    private void updateTotalExpense() {
        double total = 0.0;
        for (ExpenseEntity expense : expenseList) {
            try {
                total += Double.parseDouble(expense.getAmount());
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        tvTotalAmount.setText(String.format(Locale.US, "$%.2f", total));
    }

    private void updateEmptyState() {
        if (filteredList.isEmpty()) {
            tvEmptyState.setVisibility(View.VISIBLE);
        } else {
            tvEmptyState.setVisibility(View.GONE);
        }
    }
}