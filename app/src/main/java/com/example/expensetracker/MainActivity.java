package com.example.expensetracker;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.InputType;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private final ArrayList<ExpenseEntity> filteredList = new ArrayList<>();

    private ExpenseAdapterRoom expenseAdapter;
    private TextView tvTotalAmount;
    private TextView tvBudget;
    private TextView tvRemaining;
    private TextView tvEmptyState;
    private Spinner spinnerMonth;
    private Spinner spinnerCategory;
    private PieChart pieChart;
    private ExpenseDatabase database;

    private final ActivityResultLauncher<Intent> addExpenseLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    setupMonthSpinner();
                    loadExpensesFromDatabase(getSelectedCategory());
                }
            });

    // Responsible For Setting Up UI, database, and listener
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        applyWindowInsets();
        setupToolbar();
        initializeViews();

        database = ExpenseDatabase.getDatabase(this);

        setupSpinner();
        setupMonthSpinner();
        setupRecyclerView();
        setupListeners();

        loadExpensesFromDatabase("All");
        updateBudgetUI();
    }

    // Responsible for adjusting layout padding to avoid overlap with system UI
    private void applyWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }


    // Responsible for initialize the toolbar and attaches it to the activity
    // Enable menu (info + uninstall) to appear in top-right
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    // Responsible for finding and assigning all UI components from XML layout
    private void initializeViews() {
        tvTotalAmount = findViewById(R.id.tvTotalAmount);
        tvBudget = findViewById(R.id.tvBudget);
        tvRemaining = findViewById(R.id.tvRemaining);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        spinnerMonth = findViewById(R.id.spinnerMonth);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        pieChart = findViewById(R.id.pieChart);
    }


    // Responsible for setting up category filter dropdown (Spinner)
    // Load categories from resources and handles selection changes
    private void setupSpinner() {
        ArrayAdapter<CharSequence> filterAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.filter_categories,
                android.R.layout.simple_spinner_item
        );
        filterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(filterAdapter);

        spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                loadExpensesFromDatabase(getSelectedCategory());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void setupMonthSpinner() {
        String currentSelection = spinnerMonth.getSelectedItem() != null
                ? spinnerMonth.getSelectedItem().toString()
                : "All";

        List<String> months = new ArrayList<>();
        months.add("All");

        List<String> dbMonths = database.expenseDao().getAvailableMonths();
        if (dbMonths != null) {
            months.addAll(dbMonths);
        }

        ArrayAdapter<String> adapterMonth = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                months
        );
        adapterMonth.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMonth.setAdapter(adapterMonth);

        int index = months.indexOf(currentSelection);
        if (index >= 0) {
            spinnerMonth.setSelection(index);
        }

        spinnerMonth.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                loadExpensesFromDatabase(getSelectedCategory());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    // Responsible for initialize RecyclerView for displaying expense list
    // Also handles delete and edit actions for each item
    private void setupRecyclerView() {
        RecyclerView recyclerViewExpenses = findViewById(R.id.recyclerViewExpenses);

        expenseAdapter = new ExpenseAdapterRoom(
                filteredList,
                expense -> {
                    database.expenseDao().deleteExpense(expense);
                    loadExpensesFromDatabase(getSelectedCategory());
                },
                this::openEditExpense
        );

        recyclerViewExpenses.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewExpenses.setAdapter(expenseAdapter);
    }


    // Responsible for setting up click listener like
    // - Add button and Summary Button
    private void setupListeners() {
        FloatingActionButton fabAdd = findViewById(R.id.fabAdd);
        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddExpenseActivity.class);
            addExpenseLauncher.launch(intent);
        });

        findViewById(R.id.btnSummary).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SummaryActivity.class);
            intent.putExtra("selected_month", spinnerMonth.getSelectedItem() != null
                    ? spinnerMonth.getSelectedItem().toString()
                    : "All");
            startActivity(intent);
        });

        findViewById(R.id.btnSetBudget).setOnClickListener(v -> {
            showBudgetDialogue();
        });
    }


    // Responsible for open AddExpenseActivity in edit mode
    // Passes selected expense data through Intent
    private void openEditExpense(ExpenseEntity expense) {
        Intent intent = new Intent(MainActivity.this, AddExpenseActivity.class);
        intent.putExtra("expense_id", expense.getId());
        intent.putExtra("amount", expense.getAmount());
        intent.putExtra("category", expense.getCategory());
        intent.putExtra("date", expense.getDate());
        intent.putExtra("note", expense.getNote());
        addExpenseLauncher.launch(intent);
    }


    // Responsible for returns currently selected category from Spinner
    private String getSelectedCategory() {
        return spinnerCategory.getSelectedItem().toString();
    }


    // Responsible for loading expenses from room database on selected category
    // Also for updating list, total amount, and pie chart
    private void loadExpensesFromDatabase(String category) {
        String selectedMonth = spinnerMonth.getSelectedItem() != null
                ? spinnerMonth.getSelectedItem().toString()
                : "All";

        List<ExpenseEntity> expenses;

        if (selectedMonth.equals("All")) {
            expenses = database.expenseDao().getExpensesByCategory(category);
        } else {
            String[] components = selectedMonth.split("/");
            String month = components[0];
            String year = components[1];

            if (category.equals("All")) {
                expenses = database.expenseDao().getExpensesByMonth(month, year);
            } else {
                expenses = database.expenseDao().getExpensesByCategoryAndMonth(category, month, year);
            }
        }

        filteredList.clear();
        filteredList.addAll(expenses);

        expenseAdapter.notifyDataSetChanged();
        updateEmptyState();
        updateTotalExpense();
        updateBudgetUI();
        updatePieChart();
    }


    // Responsible for calculating total expense using ExpenseCalculator class
    // Updates total amount TextView
    private void updateTotalExpense() {
        double total = ExpenseCalculator.getTotal(filteredList);
        tvTotalAmount.setText(String.format(Locale.US, "$%.2f", total));
    }

    private void saveBudget(float value) {
        getSharedPreferences("budget", MODE_PRIVATE)
                .edit()
                .putFloat("budget", value)
                .apply();
    }

    private float getBudget() {
        return getSharedPreferences("budget", MODE_PRIVATE)
                .getFloat("budget", 0f);
    }

    private void showBudgetDialogue() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        TextView title = new TextView(this);
        title.setText("Set Monthly Budget");
        title.setTextSize(18f);
        title.setPadding(0, 30, 0, 10);
        title.setGravity(Gravity.CENTER);
        title.setTypeface(null, Typeface.BOLD);

        builder.setCustomTitle(title);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(60, 20, 60, 10);
        layout.setGravity(Gravity.CENTER);

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.setHint("Enter Amount");
        input.setGravity(Gravity.CENTER);
        input.setTextSize(16f);

        layout.addView(input);
        builder.setView(layout);

        builder.setPositiveButton("Save", (dialogue, which) -> {
            String value = input.getText().toString();

            if (!value.isEmpty()) {
                float budget = Float.parseFloat(value);
                saveBudget(budget);
                updateBudgetUI();
            }
        });
        builder.setNegativeButton("Cancel", null).show();
    }

    private void updateBudgetUI() {
        float budget = getBudget();
        double totalExpense = ExpenseCalculator.getTotal(filteredList);
        double remaining;

        String selectedMonth = spinnerMonth.getSelectedItem() != null
                ? spinnerMonth.getSelectedItem().toString()
                : "All";

        if (budget == 0f) {
            tvBudget.setText("Budget: Not Set");
            tvRemaining.setText("Set a budget to track spending");
            tvRemaining.setTextColor(Color.GRAY);
            return;
        } else {
            tvBudget.setText(String.format(Locale.US, "Budget: $%.2f", budget));
            if (selectedMonth.equals("All")) {
                tvRemaining.setText("Select a month to view remaining");
                tvRemaining.setTextColor(Color.GRAY);
                return;
            } else {
                remaining = budget - totalExpense;
            }
        }

        tvBudget.setText(String.format(Locale.US, "Budget: $%.2f", budget));
        tvRemaining.setText(String.format(Locale.US, "Remaining: $%.2f", remaining));

        if (remaining < 0) {
            tvRemaining.setTextColor(android.graphics.Color.RED);
        } else {
            tvRemaining.setTextColor(android.graphics.Color.parseColor("#2E7D32"));
        }
    }

    // Responsible for showing "No expense yet" message when list is empty
    // And hiding it when data exists
    private void updateEmptyState() {
        tvEmptyState.setVisibility(filteredList.isEmpty() ? View.VISIBLE : View.GONE);
    }


    // Responsible for generating and displaying pie chart based on expense categories
    // Also uses ExpenseCalculator Class to calculate category totals
    private void updatePieChart() {
        double food = ExpenseCalculator.getCategoryTotal(filteredList, "Food");
        double entertainment = ExpenseCalculator.getCategoryTotal(filteredList, "Entertainment");
        double transport = ExpenseCalculator.getCategoryTotal(filteredList, "Transport");
        double shopping = ExpenseCalculator.getCategoryTotal(filteredList, "Shopping");
        double bills = ExpenseCalculator.getCategoryTotal(filteredList, "Bills");
        double other = 0.0;

        for (ExpenseEntity expense : filteredList) {
            String category = expense.getCategory();
            if (!category.equals("Food") &&
                    !category.equals("Entertainment") &&
                    !category.equals("Transport") &&
                    !category.equals("Shopping") &&
                    !category.equals("Bills")) {
                other += expense.getAmountValue();
            }
        }

        ArrayList<PieEntry> entries = new ArrayList<>();
        addPieEntry(entries, food, "Food");
        addPieEntry(entries, entertainment, "Entertainment");
        addPieEntry(entries, transport, "Transport");
        addPieEntry(entries, shopping, "Shopping");
        addPieEntry(entries, bills, "Bills");
        addPieEntry(entries, other, "Other");

        if (entries.isEmpty()) {
            pieChart.clear();
            pieChart.setNoDataText("No chart data");
            return;
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        ArrayList<Integer> colors = new ArrayList<>();

        for (PieEntry entry : entries) {
            switch (entry.getLabel()) {
                case "Food":
                    colors.add(android.graphics.Color.parseColor("#4CAF50"));
                    break;
                case "Entertainment":
                    colors.add(android.graphics.Color.parseColor("#FF0000"));
                    break;
                case "Transport":
                    colors.add(android.graphics.Color.parseColor("#2196F3"));
                    break;
                case "Shopping":
                    colors.add(android.graphics.Color.parseColor("#9C27B0"));
                    break;
                case "Bills":
                    colors.add(android.graphics.Color.parseColor("#FF9800"));
                    break;
                case "Other":
                    colors.add(android.graphics.Color.parseColor("#607D8B"));
                    break;
                default:
                    colors.add(android.graphics.Color.parseColor("#BDBDBD"));
                    break;
            }
        }

        dataSet.setColors(colors);
        dataSet.setValueTextSize(12f);

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter(pieChart));

        pieChart.setUsePercentValues(true);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleRadius(50f);
        pieChart.setTransparentCircleRadius(50f);
        pieChart.setCenterText("Expenses");
        pieChart.setCenterTextSize(14f);
        pieChart.getDescription().setEnabled(false);
        pieChart.getLegend().setEnabled(true);
        pieChart.setEntryLabelTextSize(12f);
        pieChart.setData(data);
        pieChart.animateY(900);
        pieChart.invalidate();
    }



    // Responsible for add a slice to pie chart
    // Only adds entry if value is greater than 0
    private void addPieEntry(ArrayList<PieEntry> entries, double value, String label) {
        if (value > 0) {
            entries.add(new PieEntry((float) value, label));
        }
    }


    // Responsible for Inflates menu (info + uninstall) into toolbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }


    // Responsible for handling menu item clicks like Info and Unstall
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.info) {
            new AlertDialog.Builder(this)
                    .setTitle("Expense Tracker App")
                    .setMessage("Track your daily spending easily.\n\n"
                            + "• Add, edit, and delete expenses\n"
                            + "• Filter by category\n"
                            + "• View totals and charts\n"
                            + "• Understand your spending habits\n\n"
                            + "Stay organized and in control of your finances!")
                    .setPositiveButton("OK", null)
                    .show();
            return true;
        }

        if (id == R.id.uninstall) {
            Intent delete = new Intent(Intent.ACTION_DELETE,
                    Uri.parse("package:" + getPackageName()));
            startActivity(delete);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}