package com.example.expensetracker;

import android.app.Activity;
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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.MenuHost;
import androidx.core.view.MenuProvider;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.PieChart;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {

    // Stores all expenses loaded from the database before search and sort are applied
    private final ArrayList<ExpenseEntity> masterList = new ArrayList<>();

    // Stores the expenses currently displayed after filtering and sorting
    private final ArrayList<ExpenseEntity> filteredList = new ArrayList<>();

    // UI components and helper classes used on the home screen
    private ExpenseAdapterRoom expenseAdapter;
    private TextView tvWelcomeUser;
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

    // Internal category values used for filtering database results
    private final String[] filterCategoryValues = {
            "All", "Food", "Entertainment", "Transport", "Shopping", "Bills", "Other"
    };

    // Internal sort keys that match the selected sort spinner position
    private final String[] sortValueKeys = {
            "NEWEST", "OLDEST", "HIGHEST", "LOWEST", "CATEGORY"
    };

    // Handles the result after adding or editing an expense
    private final ActivityResultLauncher<Intent> addExpenseLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    refreshHomeData();
                }
            });

    // Handles the result after the user chooses where to save the CSV file
    private final ActivityResultLauncher<Intent> exportCsvLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        csvExportManager.exportToCsv(uri);
                    }
                }
            });

    // Required empty constructor for the Fragment
    public HomeFragment() {
    }

    // Inflates the home fragment layout
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    // Sets up the UI after the layout has been created
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        applyWindowInsets(view);
        setupToolbar(view);
        setupMenu();
        initializeViews(view);
        setupRecyclerView(view);
        setupCategorySpinner();
        setupSortSpinner();
        setupMonthSpinner();
        setupListeners(view);
        updateWelcomeMessage();
        refreshHomeData();
    }

    // Initializes helper classes when the fragment is attached to a context
    @Override
    public void onAttach(@NonNull android.content.Context context) {
        super.onAttach(context);
        initializeHelpers(context);
    }

    // Updates greeting when returning to this screen
    @Override
    public void onResume() {
        super.onResume();
        updateWelcomeMessage();
    }

    // Adds system bar padding so content is not hidden behind the status/navigation bars
    private void applyWindowInsets(View root) {
        View rootView = root.findViewById(R.id.homeRoot);
        ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    // Sets the fragment toolbar as the activity action bar
    private void setupToolbar(View view) {
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        AppCompatActivity activity = (AppCompatActivity) requireActivity();
        activity.setSupportActionBar(toolbar);

        // Hides the default title because the layout has its own title/header
        if (activity.getSupportActionBar() != null) {
            activity.getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
    }

    // Adds toolbar menu actions for info and uninstall
    private void setupMenu() {
        MenuHost menuHost = requireActivity();
        menuHost.addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.menu, menu);
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                // Opens app information dialog
                if (id == R.id.info) {
                    showInfoDialog();
                    return true;
                }

                // Opens the system uninstall screen for this app
                if (id == R.id.uninstall) {
                    Intent delete = new Intent(Intent.ACTION_DELETE,
                            Uri.parse("package:" + requireContext().getPackageName()));
                    startActivity(delete);
                    return true;
                }

                return false;
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);
    }

    // Connects Java variables to their XML layout views
    private void initializeViews(View view) {
        tvWelcomeUser = view.findViewById(R.id.tvWelcomeUser);
        tvTotalAmount = view.findViewById(R.id.tvTotalAmount);
        tvBudget = view.findViewById(R.id.tvBudget);
        tvRemaining = view.findViewById(R.id.tvRemaining);
        tvEmptyState = view.findViewById(R.id.tvEmptyState);
        spinnerMonth = view.findViewById(R.id.spinnerMonth);
        spinnerCategory = view.findViewById(R.id.spinnerCategory);
        spinnerSort = view.findViewById(R.id.spinnerSort);
        etSearchExpense = view.findViewById(R.id.etSearchExpense);

        // Creates the helper that controls the pie chart
        PieChart pieChart = view.findViewById(R.id.pieChart);
        pieChartManager = new PieChartManager(requireContext(), pieChart);
    }

    // Creates helper classes used for database access, budgets, export, filtering, and recurring expenses
    private void initializeHelpers(android.content.Context context) {
        ExpenseDatabase database = ExpenseDatabase.getDatabase(context);
        ExpenseDateUtils dateUtils = new ExpenseDateUtils();

        expenseRepository = new ExpenseRepository(database);
        budgetManager = new BudgetManager(context);
        csvExportManager = new CsvExportManager(context, expenseRepository);
        recurringExpenseManager = new RecurringExpenseManager(context, expenseRepository, dateUtils);
        expenseFilterSorter = new ExpenseFilterSorter(dateUtils);
        emptyStateHelper = new EmptyStateHelper();
    }

    // Sets up the RecyclerView that displays the expense list
    private void setupRecyclerView(View view) {
        RecyclerView recyclerViewExpenses = view.findViewById(R.id.recyclerViewExpenses);

        expenseAdapter = new ExpenseAdapterRoom(
                filteredList,
                expense -> {
                    expenseRepository.deleteExpense(expense);
                    refreshHomeData();
                },
                this::openEditExpense
        );

        recyclerViewExpenses.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerViewExpenses.setAdapter(expenseAdapter);
        recyclerViewExpenses.setNestedScrollingEnabled(true);
    }

    // Sets up the category filter dropdown
    private void setupCategorySpinner() {
        String[] displayCategories = getResources().getStringArray(R.array.filter_categories_display);

        ArrayAdapter<String> filterAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                displayCategories
        );
        filterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(filterAdapter);

        // Reloads expenses when the selected category changes
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

    // Sets up the sort dropdown and search field listener
    private void setupSortSpinner() {
        String[] sortOptions = {
                getString(R.string.newest_first),
                getString(R.string.oldest_first),
                getString(R.string.highest_amount),
                getString(R.string.lowest_amount),
                getString(R.string.category_a_z)
        };

        ArrayAdapter<String> sortAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                sortOptions
        );
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSort.setAdapter(sortAdapter);

        // Re-sorts the current list when the sort option changes
        spinnerSort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                applySearchAndSort();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Re-filters the list whenever the search text changes
        etSearchExpense.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                applySearchAndSort();
            }

            @Override public void afterTextChanged(Editable s) {}
        });
    }

    // Sets up the month filter dropdown using months found in the database
    private void setupMonthSpinner() {
        String currentSelection = getSelectedMonthValue();

        List<String> months = new ArrayList<>();
        months.add(getString(R.string.all));
        months.addAll(expenseRepository.getAvailableMonths());

        ArrayAdapter<String> adapterMonth = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                months
        );
        adapterMonth.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMonth.setAdapter(adapterMonth);

        // Restores the previous selected month if it still exists
        int index = 0;
        if (!"All".equals(currentSelection)) {
            index = months.indexOf(currentSelection);
            if (index < 0) {
                index = 0;
            }
        }

        spinnerMonth.setSelection(index);

        // Reloads expenses when the selected month changes
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

    // Returns the selected month filter value
    private String getSelectedMonthValue() {
        if (spinnerMonth == null) {
            return "All";
        }

        int position = spinnerMonth.getSelectedItemPosition();
        if (position <= 0) {
            return "All";
        }

        Object item = spinnerMonth.getSelectedItem();
        return item != null ? item.toString() : "All";
    }

    // Sets up button and card click actions on the home screen
    private void setupListeners(View view) {
        FloatingActionButton fabAdd = view.findViewById(R.id.fabAdd);

        // Opens the add expense screen
        fabAdd.setOnClickListener(v ->
                addExpenseLauncher.launch(new Intent(requireContext(), AddExpenseActivity.class))
        );

        view.findViewById(R.id.btnSummary).setOnClickListener(v -> openSummary());
        view.findViewById(R.id.btnInsights).setOnClickListener(v -> openInsights());
        view.findViewById(R.id.btnCategoryBudgets).setOnClickListener(v -> openCategoryBudgets());
        view.findViewById(R.id.btnExportCsv).setOnClickListener(v -> startCsvExport());
        view.findViewById(R.id.btnSetBudget).setOnClickListener(v -> showBudgetDialog());
    }

    // Shows a greeting based on the current time and saved profile name
    private void updateWelcomeMessage() {
        String name = requireContext()
                .getSharedPreferences("profile", requireContext().MODE_PRIVATE)
                .getString("name", "User");

        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);

        String greeting;
        if (hour < 12) {
            greeting = getString(R.string.good_morning);
        } else if (hour < 18) {
            greeting = getString(R.string.good_afternoon);
        } else {
            greeting = getString(R.string.good_evening);
        }

        tvWelcomeUser.setText(greeting + ", " + name);
    }

    // Refreshes recurring expenses, month filters, and displayed data
    public void refreshHomeData() {
        if (!isAdded() || recurringExpenseManager == null || spinnerMonth == null) {
            return;
        }

        recurringExpenseManager.generateDueRecurringExpenses();
        setupMonthSpinner();
        loadExpensesFromDatabase();
    }

    // Loads expenses from the database using the selected month and category filters
    private void loadExpensesFromDatabase() {
        masterList.clear();
        masterList.addAll(expenseRepository.getExpenses(getSelectedMonthValue(), getSelectedCategory()));
        applySearchAndSort();
    }

    // Applies search and sorting to the loaded expense list
    private void applySearchAndSort() {
        expenseFilterSorter.apply(masterList, filteredList, getSearchQuery(), getSelectedSort());

        if (expenseAdapter != null) {
            expenseAdapter.notifyDataSetChanged();
        }

        updateDashboard();
    }

    // Updates all dashboard sections after data changes
    private void updateDashboard() {
        updateTotalExpense();
        updateBudgetUI();
        updateEmptyState();
        pieChartManager.update(filteredList);
    }

    // Calculates and displays the total expense amount
    private void updateTotalExpense() {
        double total = ExpenseCalculator.getTotal(filteredList);
        tvTotalAmount.setText(CurrencyManager.formatAmount(requireContext(), total));
    }

    // Updates the budget and remaining balance display
    private void updateBudgetUI() {
        float budget = budgetManager.getBudget();
        double totalExpense = ExpenseCalculator.getTotal(filteredList);

        // Shows setup message when no monthly budget has been saved
        if (budget == 0f) {
            tvBudget.setText(getString(R.string.budget_not_set));
            tvRemaining.setText(getString(R.string.set_a_monthly_budget_to_track_spending));
            tvRemaining.setTextColor(Color.GRAY);
            return;
        }

        tvBudget.setText(CurrencyManager.formatBudgetLabel(requireContext(), budget));

        // Remaining budget is only meaningful when a specific month is selected
        if ("All".equals(getSelectedMonthValue())) {
            tvRemaining.setText(getString(R.string.select_a_month_to_view_remaining_budget));
            tvRemaining.setTextColor(Color.GRAY);
            return;
        }

        double remaining = budget - totalExpense;
        tvRemaining.setText(CurrencyManager.formatRemainingLabel(requireContext(), remaining));

        // Shows red when spending goes over budget, otherwise shows success color
        int color = remaining < 0
                ? ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark)
                : ContextCompat.getColor(requireContext(), R.color.success);
        tvRemaining.setTextColor(color);
    }

    // Shows or hides the empty-state message depending on the current list
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

    // Opens a dialog where the user can set or update the monthly budget
    private void showBudgetDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());

        // Custom dialog title
        TextView title = new TextView(requireContext());
        title.setText(R.string.set_monthly_budget);
        title.setTextSize(18f);
        title.setPadding(0, 30, 0, 10);
        title.setGravity(Gravity.CENTER);
        title.setTypeface(null, Typeface.BOLD);
        builder.setCustomTitle(title);

        // Dialog layout containing the budget input
        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(60, 20, 60, 10);
        layout.setGravity(Gravity.CENTER);

        EditText input = new EditText(requireContext());
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.setHint(R.string.enter_amount);
        input.setGravity(Gravity.CENTER);
        input.setTextSize(16f);

        // Prefills the dialog with the current budget if one exists
        float currentBudget = budgetManager.getBudget();
        if (currentBudget > 0) {
            input.setText(String.format(Locale.US, "%.2f", currentBudget));
        }

        layout.addView(input);
        builder.setView(layout);

        // Saves the entered budget value
        builder.setPositiveButton(R.string.save, (dialog, which) -> {
            String value = input.getText().toString().trim();
            if (!value.isEmpty()) {
                try {
                    budgetManager.saveBudget(Float.parseFloat(value));
                    updateBudgetUI();
                } catch (NumberFormatException e) {
                    Toast.makeText(requireContext(), "Enter a valid budget", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton(R.string.cancel, null).show();
    }

    // Starts the Android file picker so the user can choose where to export the CSV
    private void startCsvExport() {
        if (!csvExportManager.hasExpensesToExport()) {
            Toast.makeText(requireContext(), "No expenses to export yet", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/csv");
        intent.putExtra(Intent.EXTRA_TITLE, "expense_tracker_export.csv");
        exportCsvLauncher.launch(intent);
    }

    // Opens AddExpenseActivity in edit mode with the selected expense details
    private void openEditExpense(ExpenseEntity expense) {
        Intent intent = new Intent(requireContext(), AddExpenseActivity.class);
        intent.putExtra("expense_id", expense.getId());
        intent.putExtra("amount", expense.getAmount());
        intent.putExtra("category", expense.getCategory());
        intent.putExtra("date", expense.getDate());
        intent.putExtra("note", expense.getNote());
        intent.putExtra("is_recurring", expense.isRecurring());
        intent.putExtra("recurring_interval", expense.getRecurringInterval());
        addExpenseLauncher.launch(intent);
    }

    // Opens the summary screen with the current filters
    private void openSummary() {
        Intent intent = new Intent(requireContext(), SummaryActivity.class);
        intent.putExtra("selected_month", getSelectedMonthValue());
        intent.putExtra("selected_category", getSelectedCategory());
        startActivity(intent);
    }

    // Opens the insights screen for the currently selected month
    private void openInsights() {
        Intent intent = new Intent(requireContext(), InsightsActivity.class);
        intent.putExtra("selected_month", getSelectedMonthValue());
        startActivity(intent);
    }

    // Opens the category budget screen
    private void openCategoryBudgets() {
        startActivity(new Intent(requireContext(), CategoryBudgetActivity.class));
    }

    // Shows information about the app
    private void showInfoDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.info_title)
                .setMessage(R.string.info_message)
                .setPositiveButton(R.string.ok, null)
                .show();
    }

    // Returns the internal category value based on the selected spinner position
    private String getSelectedCategory() {
        int position = spinnerCategory != null ? spinnerCategory.getSelectedItemPosition() : 0;

        if (position < 0 || position >= filterCategoryValues.length) {
            return "All";
        }

        return filterCategoryValues[position];
    }

    // Returns the displayed selected month text
    private String getSelectedMonth() {
        return spinnerMonth != null && spinnerMonth.getSelectedItem() != null
                ? spinnerMonth.getSelectedItem().toString()
                : getString(R.string.all);
    }

    // Returns the internal sort key based on the selected spinner position
    private String getSelectedSort() {
        int position = spinnerSort != null ? spinnerSort.getSelectedItemPosition() : 0;

        if (position < 0 || position >= sortValueKeys.length) {
            return "NEWEST";
        }

        return sortValueKeys[position];
    }

    // Returns the current search text
    private String getSearchQuery() {
        return etSearchExpense != null && etSearchExpense.getText() != null
                ? etSearchExpense.getText().toString().trim()
                : "";
    }
}