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
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
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
    private PieChart pieChart;
    private ExpenseDatabase database;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.US);

    private final ActivityResultLauncher<Intent> addExpenseLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    generateDueRecurringExpenses();
                    setupMonthSpinner();
                    loadExpensesFromDatabase(getSelectedCategory());
                }
            });

    private final ActivityResultLauncher<Intent> exportCsvLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        exportExpensesToCsv(uri);
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

        database = ExpenseDatabase.getDatabase(this);
        generateDueRecurringExpenses();

        setupRecyclerView();
        setupSpinner();
        setupSortSpinner();
        setupMonthSpinner();
        setupListeners();

        loadExpensesFromDatabase("All");
        updateBudgetUI();
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
        pieChart = findViewById(R.id.pieChart);
    }

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

    private void setupRecyclerView() {
        RecyclerView recyclerViewExpenses = findViewById(R.id.recyclerViewExpenses);

        expenseAdapter = new ExpenseAdapterRoom(
                filteredList,
                expense -> {
                    database.expenseDao().deleteExpense(expense);
                    generateDueRecurringExpenses();
                    setupMonthSpinner();
                    loadExpensesFromDatabase(getSelectedCategory());
                },
                this::openEditExpense
        );

        recyclerViewExpenses.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewExpenses.setAdapter(expenseAdapter);
    }

    private void setupListeners() {
        FloatingActionButton fabAdd = findViewById(R.id.fabAdd);
        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddExpenseActivity.class);
            addExpenseLauncher.launch(intent);
        });

        findViewById(R.id.btnSummary).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SummaryActivity.class);
            intent.putExtra("selected_month", getSelectedMonth());
            intent.putExtra("selected_category", getSelectedCategory());
            startActivity(intent);
        });

        findViewById(R.id.btnInsights).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, InsightsActivity.class);
            intent.putExtra("selected_month", getSelectedMonth());
            startActivity(intent);
        });

        findViewById(R.id.btnCategoryBudgets).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CategoryBudgetActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.btnExportCsv).setOnClickListener(v -> startCsvExport());

        findViewById(R.id.btnSetBudget).setOnClickListener(v -> showBudgetDialogue());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (database != null) {
            generateDueRecurringExpenses();
            setupMonthSpinner();
            loadExpensesFromDatabase(getSelectedCategory());
        }
    }

    private void openEditExpense(ExpenseEntity expense) {
        Intent intent = new Intent(MainActivity.this, AddExpenseActivity.class);
        intent.putExtra("expense_id", expense.getId());
        intent.putExtra("amount", expense.getAmount());
        intent.putExtra("category", expense.getCategory());
        intent.putExtra("date", expense.getDate());
        intent.putExtra("note", expense.getNote());
        intent.putExtra("is_recurring", expense.isRecurring());
        intent.putExtra("recurring_interval", expense.getRecurringInterval());
        addExpenseLauncher.launch(intent);
    }

    private String getSelectedCategory() {
        return spinnerCategory.getSelectedItem() != null
                ? spinnerCategory.getSelectedItem().toString()
                : "All";
    }

    private String getSelectedMonth() {
        return spinnerMonth.getSelectedItem() != null
                ? spinnerMonth.getSelectedItem().toString()
                : "All";
    }

    private void loadExpensesFromDatabase(String category) {
        if (database == null) return;

        String selectedMonth = getSelectedMonth();
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

        masterList.clear();
        masterList.addAll(expenses);
        applySearchAndSort();
    }

    private void applySearchAndSort() {
        if (filteredList == null) return;

        String query = etSearchExpense != null && etSearchExpense.getText() != null
                ? etSearchExpense.getText().toString().trim().toLowerCase(Locale.US)
                : "";

        filteredList.clear();
        for (ExpenseEntity expense : masterList) {
            String note = expense.getNote() == null ? "" : expense.getNote().toLowerCase(Locale.US);
            String category = expense.getCategory() == null ? "" : expense.getCategory().toLowerCase(Locale.US);
            String date = expense.getDate() == null ? "" : expense.getDate().toLowerCase(Locale.US);
            String amount = expense.getAmount() == null ? "" : expense.getAmount().toLowerCase(Locale.US);

            if (query.isEmpty()
                    || note.contains(query)
                    || category.contains(query)
                    || date.contains(query)
                    || amount.contains(query)) {
                filteredList.add(expense);
            }
        }

        String sort = spinnerSort != null && spinnerSort.getSelectedItem() != null
                ? spinnerSort.getSelectedItem().toString()
                : "Newest First";

        switch (sort) {
            case "Oldest First":
                Collections.sort(filteredList, (a, b) -> compareDates(a, b));
                break;
            case "Highest Amount":
                Collections.sort(filteredList, (a, b) -> Double.compare(b.getAmountValue(), a.getAmountValue()));
                break;
            case "Lowest Amount":
                Collections.sort(filteredList, (a, b) -> Double.compare(a.getAmountValue(), b.getAmountValue()));
                break;
            case "Category A-Z":
                Collections.sort(filteredList, (a, b) -> a.getCategory().compareToIgnoreCase(b.getCategory()));
                break;
            case "Newest First":
            default:
                Collections.sort(filteredList, (a, b) -> -compareDates(a, b));
                break;
        }

        if (expenseAdapter != null) {
            expenseAdapter.notifyDataSetChanged();
        }
        updateEmptyState();
        updateTotalExpense();
        updateBudgetUI();
        updatePieChart();
    }

    private int compareDates(ExpenseEntity a, ExpenseEntity b) {
        Date dateA = parseDate(a.getDate());
        Date dateB = parseDate(b.getDate());
        if (dateA == null && dateB == null) return Integer.compare(a.getId(), b.getId());
        if (dateA == null) return -1;
        if (dateB == null) return 1;
        int result = dateA.compareTo(dateB);
        if (result == 0) result = Integer.compare(a.getId(), b.getId());
        return result;
    }

    private Date parseDate(String value) {
        try {
            return dateFormat.parse(value);
        } catch (ParseException | NullPointerException e) {
            return null;
        }
    }

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

        float currentBudget = getBudget();
        if (currentBudget > 0) {
            input.setText(String.format(Locale.US, "%.2f", currentBudget));
        }

        layout.addView(input);
        builder.setView(layout);

        builder.setPositiveButton("Save", (dialogue, which) -> {
            String value = input.getText().toString().trim();
            if (!value.isEmpty()) {
                try {
                    float budget = Float.parseFloat(value);
                    saveBudget(budget);
                    updateBudgetUI();
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Enter a valid budget", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Cancel", null).show();
    }

    private void updateBudgetUI() {
        float budget = getBudget();
        double totalExpense = ExpenseCalculator.getTotal(filteredList);
        double remaining;

        String selectedMonth = getSelectedMonth();

        if (budget == 0f) {
            tvBudget.setText("Budget: Not Set");
            tvRemaining.setText("Set a monthly budget to track spending");
            tvRemaining.setTextColor(Color.GRAY);
            return;
        } else {
            tvBudget.setText(String.format(Locale.US, "Budget: $%.2f", budget));
            if (selectedMonth.equals("All")) {
                tvRemaining.setText("Select a month to view remaining budget");
                tvRemaining.setTextColor(Color.GRAY);
                return;
            } else {
                remaining = budget - totalExpense;
            }
        }

        tvBudget.setText(String.format(Locale.US, "Budget: $%.2f", budget));
        tvRemaining.setText(String.format(Locale.US, "Remaining: $%.2f", remaining));

        if (remaining < 0) {
            tvRemaining.setTextColor(Color.RED);
        } else {
            tvRemaining.setTextColor(Color.parseColor("#2E7D32"));
        }
    }

    private void updateEmptyState() {
        if (!filteredList.isEmpty()) {
            tvEmptyState.setVisibility(View.GONE);
            return;
        }

        tvEmptyState.setVisibility(View.VISIBLE);
        String query = etSearchExpense != null && etSearchExpense.getText() != null
                ? etSearchExpense.getText().toString().trim()
                : "";

        if (!query.isEmpty()) {
            tvEmptyState.setText("No matching expenses found. Try a different search.");
        } else if (!getSelectedMonth().equals("All") || !getSelectedCategory().equals("All")) {
            tvEmptyState.setText("No expenses match this filter yet.");
        } else {
            tvEmptyState.setText("No expenses yet. Tap + to add your first expense.");
        }
    }

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
                    colors.add(Color.parseColor("#4CAF50"));
                    break;
                case "Entertainment":
                    colors.add(Color.parseColor("#F44336"));
                    break;
                case "Transport":
                    colors.add(Color.parseColor("#2196F3"));
                    break;
                case "Shopping":
                    colors.add(Color.parseColor("#9C27B0"));
                    break;
                case "Bills":
                    colors.add(Color.parseColor("#FF9800"));
                    break;
                case "Other":
                    colors.add(Color.parseColor("#607D8B"));
                    break;
                default:
                    colors.add(Color.parseColor("#BDBDBD"));
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
        pieChart.setBackgroundColor(Color.TRANSPARENT);
        pieChart.setHoleColor(Color.TRANSPARENT);
        pieChart.setCenterText("Expenses");
        pieChart.setCenterTextSize(14f);
        pieChart.getDescription().setEnabled(false);
        pieChart.getLegend().setEnabled(true);
        pieChart.setEntryLabelTextSize(12f);
        pieChart.setData(data);
        pieChart.animateY(900);
        pieChart.invalidate();
    }

    private void addPieEntry(ArrayList<PieEntry> entries, double value, String label) {
        if (value > 0) {
            entries.add(new PieEntry((float) value, label));
        }
    }

    private void startCsvExport() {
        List<ExpenseEntity> allExpenses = database.expenseDao().getAllExpenses();
        if (allExpenses == null || allExpenses.isEmpty()) {
            Toast.makeText(this, "No expenses to export yet", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/csv");
        intent.putExtra(Intent.EXTRA_TITLE, "expense_tracker_export.csv");
        exportCsvLauncher.launch(intent);
    }

    private void exportExpensesToCsv(Uri uri) {
        List<ExpenseEntity> allExpenses = database.expenseDao().getAllExpenses();
        StringBuilder csv = new StringBuilder();
        csv.append("Amount,Category,Date,Note,Recurring,Recurring Interval\n");

        for (ExpenseEntity expense : allExpenses) {
            csv.append(csvEscape(expense.getAmount())).append(",")
                    .append(csvEscape(expense.getCategory())).append(",")
                    .append(csvEscape(expense.getDate())).append(",")
                    .append(csvEscape(expense.getNote())).append(",")
                    .append(expense.isRecurring() ? "Yes" : "No").append(",")
                    .append(csvEscape(expense.getRecurringInterval())).append("\n");
        }

        try (OutputStream outputStream = getContentResolver().openOutputStream(uri)) {
            if (outputStream != null) {
                outputStream.write(csv.toString().getBytes());
                Toast.makeText(this, "CSV exported successfully", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Export failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private String csvEscape(String value) {
        if (value == null) value = "";
        String escaped = value.replace("\"", "\"\"");
        return "\"" + escaped + "\"";
    }

    private void generateDueRecurringExpenses() {
        if (database == null) return;

        List<ExpenseEntity> recurringExpenses = database.expenseDao().getRecurringExpenses();
        if (recurringExpenses == null || recurringExpenses.isEmpty()) return;

        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

        boolean insertedNewExpense = false;

        for (ExpenseEntity recurringExpense : recurringExpenses) {
            Date startDate = parseDate(recurringExpense.getDate());
            if (startDate == null) continue;

            Calendar nextDate = Calendar.getInstance();
            nextDate.setTime(startDate);
            nextDate.set(Calendar.HOUR_OF_DAY, 0);
            nextDate.set(Calendar.MINUTE, 0);
            nextDate.set(Calendar.SECOND, 0);
            nextDate.set(Calendar.MILLISECOND, 0);

            int safetyCounter = 0;
            while (safetyCounter < 120) {
                addRecurringInterval(nextDate, recurringExpense.getRecurringInterval());

                if (nextDate.after(today)) {
                    break;
                }

                String generatedDate = dateFormat.format(nextDate.getTime());
                String note = recurringExpense.getNote() == null ? "" : recurringExpense.getNote();

                int duplicateCount = database.expenseDao().countExactExpense(
                        recurringExpense.getAmount(),
                        recurringExpense.getCategory(),
                        generatedDate,
                        note
                );

                if (duplicateCount == 0) {
                    ExpenseEntity generated = new ExpenseEntity(
                            recurringExpense.getAmount(),
                            recurringExpense.getCategory(),
                            generatedDate,
                            note
                    );
                    generated.setRecurring(false);
                    generated.setRecurringInterval("None");
                    database.expenseDao().insertExpense(generated);
                    insertedNewExpense = true;
                }
                safetyCounter++;
            }
        }

        if (insertedNewExpense) {
            Toast.makeText(this, "Recurring expenses updated", Toast.LENGTH_SHORT).show();
        }
    }

    private void addRecurringInterval(Calendar calendar, String interval) {
        if ("Weekly".equals(interval)) {
            calendar.add(Calendar.WEEK_OF_YEAR, 1);
        } else if ("Yearly".equals(interval)) {
            calendar.add(Calendar.YEAR, 1);
        } else {
            calendar.add(Calendar.MONTH, 1);
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
