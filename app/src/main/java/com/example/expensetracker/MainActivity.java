package com.example.expensetracker;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
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
import android.widget.Toast;

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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private final ArrayList<ExpenseEntity> masterList = new ArrayList<>();
    private final ArrayList<ExpenseEntity> filteredList = new ArrayList<>();

    private ExpenseAdapterRoom expenseAdapter;
    private TextView tvTotalAmount;
    private TextView tvBudget;
    private TextView tvRemaining;
    private TextView tvEmptyState;
    private Spinner spinnerMonth;
    private Spinner spinnerCategory;
    private Spinner spinnerSort;
    private TextInputEditText etSearchExpense;

    private ExpenseRepository expenseRepository;
    private BudgetManager budgetManager;
    private CsvExportManager csvExportManager;
    private RecurringExpenseManager recurringExpenseManager;
    private ExpenseFilterSorter expenseFilterSorter;
    private PieChartManager pieChartManager;
    private EmptyStateHelper emptyStateHelper;

    private final ActivityResultLauncher<Intent> addExpenseLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    refreshExpenseData();
                }
            });

    private final ActivityResultLauncher<Intent> exportCsvLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        csvExportManager.exportToCsv(uri);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        applyWindowInsets();
        setupToolbar();
        initializeViews();
        initializeHelpers();
        setupRecyclerView();
        setupCategorySpinner();
        setupSortSpinner();
        setupMonthSpinner();
        setupListeners();
        refreshExpenseData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (expenseRepository != null) {
            refreshExpenseData();
        }
    }

    private void applyWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
    }

    private void initializeViews() {
        tvTotalAmount = findViewById(R.id.tvTotalAmount);
        tvBudget = findViewById(R.id.tvBudget);
        tvRemaining = findViewById(R.id.tvRemaining);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        spinnerMonth = findViewById(R.id.spinnerMonth);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        spinnerSort = findViewById(R.id.spinnerSort);
        etSearchExpense = findViewById(R.id.etSearchExpense);
        PieChart pieChart = findViewById(R.id.pieChart);
        pieChartManager = new PieChartManager(pieChart);
    }

    private void initializeHelpers() {
        ExpenseDatabase database = ExpenseDatabase.getDatabase(this);
        ExpenseDateUtils dateUtils = new ExpenseDateUtils();

        expenseRepository = new ExpenseRepository(database);
        budgetManager = new BudgetManager(this);
        csvExportManager = new CsvExportManager(this, expenseRepository);
        recurringExpenseManager = new RecurringExpenseManager(this, expenseRepository, dateUtils);
        expenseFilterSorter = new ExpenseFilterSorter(dateUtils);
        emptyStateHelper = new EmptyStateHelper();
    }

    private void setupRecyclerView() {
        RecyclerView recyclerViewExpenses = findViewById(R.id.recyclerViewExpenses);

        expenseAdapter = new ExpenseAdapterRoom(
                filteredList,
                expense -> {
                    expenseRepository.deleteExpense(expense);
                    refreshExpenseData();
                },
                this::openEditExpense
        );

        recyclerViewExpenses.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewExpenses.setAdapter(expenseAdapter);
    }

    private void setupCategorySpinner() {
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
                loadExpensesFromDatabase();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void setupSortSpinner() {
        String[] sortOptions = {
                "Newest First",
                "Oldest First",
                "Highest Amount",
                "Lowest Amount",
                "Category A-Z"
        };

        ArrayAdapter<String> sortAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                sortOptions
        );
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSort.setAdapter(sortAdapter);

        spinnerSort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                applySearchAndSort();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        etSearchExpense.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applySearchAndSort();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void setupMonthSpinner() {
        String currentSelection = getSelectedMonth();
        List<String> months = new ArrayList<>();
        months.add("All");
        months.addAll(expenseRepository.getAvailableMonths());

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
                loadExpensesFromDatabase();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void setupListeners() {
        FloatingActionButton fabAdd = findViewById(R.id.fabAdd);
        fabAdd.setOnClickListener(v -> addExpenseLauncher.launch(new Intent(this, AddExpenseActivity.class)));

        findViewById(R.id.btnSummary).setOnClickListener(v -> openSummary());
        findViewById(R.id.btnInsights).setOnClickListener(v -> openInsights());
        findViewById(R.id.btnCategoryBudgets).setOnClickListener(v -> openCategoryBudgets());
        findViewById(R.id.btnExportCsv).setOnClickListener(v -> startCsvExport());
        findViewById(R.id.btnSetBudget).setOnClickListener(v -> showBudgetDialogue());
    }

    private void refreshExpenseData() {
        recurringExpenseManager.generateDueRecurringExpenses();
        setupMonthSpinner();
        loadExpensesFromDatabase();
    }

    private void loadExpensesFromDatabase() {
        masterList.clear();
        masterList.addAll(expenseRepository.getExpenses(getSelectedMonth(), getSelectedCategory()));
        applySearchAndSort();
    }

    private void applySearchAndSort() {
        String query = getSearchQuery();
        String sortOption = getSelectedSort();

        expenseFilterSorter.apply(masterList, filteredList, query, sortOption);

        if (expenseAdapter != null) {
            expenseAdapter.notifyDataSetChanged();
        }
        updateDashboard();
    }

    private void updateDashboard() {
        updateTotalExpense();
        updateBudgetUI();
        updateEmptyState();
        pieChartManager.update(filteredList);
    }

    private void updateTotalExpense() {
        double total = ExpenseCalculator.getTotal(filteredList);
        tvTotalAmount.setText(String.format(Locale.US, "$%.2f", total));
    }

    private void updateBudgetUI() {
        float budget = budgetManager.getBudget();
        double totalExpense = ExpenseCalculator.getTotal(filteredList);

        if (budget == 0f) {
            tvBudget.setText("Budget: Not Set");
            tvRemaining.setText("Set a monthly budget to track spending");
            tvRemaining.setTextColor(Color.GRAY);
            return;
        }

        tvBudget.setText(String.format(Locale.US, "Budget: $%.2f", budget));

        if ("All".equals(getSelectedMonth())) {
            tvRemaining.setText("Select a month to view remaining budget");
            tvRemaining.setTextColor(Color.GRAY);
            return;
        }

        double remaining = budget - totalExpense;
        tvRemaining.setText(String.format(Locale.US, "Remaining: $%.2f", remaining));
        tvRemaining.setTextColor(remaining < 0 ? Color.RED : Color.parseColor("#2E7D32"));
    }

    private void updateEmptyState() {
        if (!filteredList.isEmpty()) {
            tvEmptyState.setVisibility(View.GONE);
            return;
        }

        tvEmptyState.setVisibility(View.VISIBLE);
        tvEmptyState.setText(emptyStateHelper.getMessage(
                !getSearchQuery().isEmpty(),
                getSelectedMonth(),
                getSelectedCategory()
        ));
    }

    private void showBudgetDialogue() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCustomTitle(createBudgetDialogTitle());
        builder.setView(createBudgetDialogLayout());
        builder.setPositiveButton("Save", null);
        builder.setNegativeButton("Cancel", null);

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(dialogInterface -> {
            EditText input = dialog.findViewById(1001);
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> saveBudgetFromInput(input, dialog));
        });
        dialog.show();
    }

    private TextView createBudgetDialogTitle() {
        TextView title = new TextView(this);
        title.setText("Set Monthly Budget");
        title.setTextSize(18f);
        title.setPadding(0, 30, 0, 10);
        title.setGravity(Gravity.CENTER);
        title.setTypeface(null, Typeface.BOLD);
        return title;
    }

    private LinearLayout createBudgetDialogLayout() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(60, 20, 60, 10);
        layout.setGravity(Gravity.CENTER);

        EditText input = new EditText(this);
        input.setId(1001);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.setHint("Enter Amount");
        input.setGravity(Gravity.CENTER);
        input.setTextSize(16f);

        float currentBudget = budgetManager.getBudget();
        if (currentBudget > 0) {
            input.setText(String.format(Locale.US, "%.2f", currentBudget));
        }

        layout.addView(input);
        return layout;
    }

    private void saveBudgetFromInput(EditText input, AlertDialog dialog) {
        if (input == null) return;

        String value = input.getText().toString().trim();
        if (value.isEmpty()) {
            Toast.makeText(this, "Enter a budget amount", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            budgetManager.saveBudget(Float.parseFloat(value));
            updateBudgetUI();
            dialog.dismiss();
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Enter a valid budget", Toast.LENGTH_SHORT).show();
        }
    }

    private void startCsvExport() {
        if (!csvExportManager.hasExpensesToExport()) {
            Toast.makeText(this, "No expenses to export yet", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/csv");
        intent.putExtra(Intent.EXTRA_TITLE, "expense_tracker_export.csv");
        exportCsvLauncher.launch(intent);
    }

    private void openEditExpense(ExpenseEntity expense) {
        Intent intent = new Intent(this, AddExpenseActivity.class);
        intent.putExtra("expense_id", expense.getId());
        intent.putExtra("amount", expense.getAmount());
        intent.putExtra("category", expense.getCategory());
        intent.putExtra("date", expense.getDate());
        intent.putExtra("note", expense.getNote());
        intent.putExtra("is_recurring", expense.isRecurring());
        intent.putExtra("recurring_interval", expense.getRecurringInterval());
        addExpenseLauncher.launch(intent);
    }

    private void openSummary() {
        Intent intent = new Intent(this, SummaryActivity.class);
        intent.putExtra("selected_month", getSelectedMonth());
        intent.putExtra("selected_category", getSelectedCategory());
        startActivity(intent);
    }

    private void openInsights() {
        Intent intent = new Intent(this, InsightsActivity.class);
        intent.putExtra("selected_month", getSelectedMonth());
        startActivity(intent);
    }

    private void openCategoryBudgets() {
        startActivity(new Intent(this, CategoryBudgetActivity.class));
    }

    private String getSelectedCategory() {
        return spinnerCategory.getSelectedItem() != null
                ? spinnerCategory.getSelectedItem().toString()
                : "All";
    }

    private String getSelectedMonth() {
        return spinnerMonth != null && spinnerMonth.getSelectedItem() != null
                ? spinnerMonth.getSelectedItem().toString()
                : "All";
    }

    private String getSelectedSort() {
        return spinnerSort != null && spinnerSort.getSelectedItem() != null
                ? spinnerSort.getSelectedItem().toString()
                : "Newest First";
    }

    private String getSearchQuery() {
        return etSearchExpense != null && etSearchExpense.getText() != null
                ? etSearchExpense.getText().toString().trim()
                : "";
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
            showInfoDialog();
            return true;
        }

        if (id == R.id.uninstall) {
            Intent delete = new Intent(Intent.ACTION_DELETE, Uri.parse("package:" + getPackageName()));
            startActivity(delete);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showInfoDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Expense Tracker App")
                .setMessage("Track your daily spending easily.\n\n"
                        + "• Add, edit, and delete expenses\n"
                        + "• Filter, search, and sort expenses\n"
                        + "• Compare this month to last month\n"
                        + "• View charts, smart insights, and budget warnings\n"
                        + "• Export your expense history to CSV\n"
                        + "• Mark expenses as recurring\n\n"
                        + "Stay organized and in control of your finances!")
                .setPositiveButton("OK", null)
                .show();
    }
}
