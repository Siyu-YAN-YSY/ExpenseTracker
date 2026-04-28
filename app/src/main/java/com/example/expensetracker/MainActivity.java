package com.example.expensetracker;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private ArrayList<ExpenseEntity> filteredList;
    private ExpenseAdapterRoom expenseAdapter;
    private TextView tvTotalAmount;
    private TextView tvEmptyState;
    private Spinner spinnerCategory;
    private ExpenseDatabase database;

    private final ActivityResultLauncher<Intent> addExpenseLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    loadExpensesFromDatabase(spinnerCategory.getSelectedItem().toString());
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

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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

        filteredList = new ArrayList<>();

        expenseAdapter = new ExpenseAdapterRoom(
                filteredList,
                expense -> {
                    database.expenseDao().deleteExpense(expense);
                    loadExpensesFromDatabase(spinnerCategory.getSelectedItem().toString());
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
                loadExpensesFromDatabase(selectedCategory);
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

        loadExpensesFromDatabase("All");
    }

    private void loadExpensesFromDatabase(String category) {
        List<ExpenseEntity> expenses = database.expenseDao().getExpensesByCategory(category);

        filteredList.clear();
        filteredList.addAll(expenses);

        expenseAdapter.notifyDataSetChanged();
        updateEmptyState();
        updateTotalExpense();
    }

    private void updateTotalExpense() {
        double total = 0.0;

        for (ExpenseEntity expense : filteredList) {
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.info) {
            new AlertDialog.Builder(this)
                    .setTitle("Information")
                    .setMessage("Expense Tracker helps you track, filter, edit, and summarize your expenses.")
                    .setPositiveButton("OK", null)
                    .show();
            return true;
        }

        if (id == R.id.uninstall) {
            new AlertDialog.Builder(this)
                    .setTitle("Uninstall App")
                    .setMessage("Do you want to open the uninstall screen?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        intent.setData(Uri.parse("package:" + getPackageName()));
                        startActivity(intent);
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}