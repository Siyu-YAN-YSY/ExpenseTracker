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

    private final ArrayList<ExpenseEntity> masterList = new ArrayList<>();
    private final ArrayList<ExpenseEntity> filteredList = new ArrayList<>();



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

    private final String[] filterCategoryValues = {
            "All", "Food", "Entertainment", "Transport", "Shopping", "Bills", "Other"
    };

    private final String[] sortValueKeys = {
            "NEWEST", "OLDEST", "HIGHEST", "LOWEST", "CATEGORY"
    };

    private final ActivityResultLauncher<Intent> addExpenseLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    refreshHomeData();
                }
            });

    private final ActivityResultLauncher<Intent> exportCsvLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        csvExportManager.exportToCsv(uri);
                    }
                }
            });

    public HomeFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

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

    @Override
    public void onAttach(@NonNull android.content.Context context) {
        super.onAttach(context);
        initializeHelpers(context);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateWelcomeMessage();
    }

    private void applyWindowInsets(View root) {
        View rootView = root.findViewById(R.id.homeRoot);
        ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void setupToolbar(View view) {
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        AppCompatActivity activity = (AppCompatActivity) requireActivity();
        activity.setSupportActionBar(toolbar);

        if (activity.getSupportActionBar() != null) {
            activity.getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
    }

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

                if (id == R.id.info) {
                    showInfoDialog();
                    return true;
                }

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

        PieChart pieChart = view.findViewById(R.id.pieChart);
        pieChartManager = new PieChartManager(requireContext(), pieChart);
    }

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

    private void setupCategorySpinner() {
        String[] displayCategories = getResources().getStringArray(R.array.filter_categories_display);

        ArrayAdapter<String> filterAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                displayCategories
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
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                applySearchAndSort();
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

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

        int index = 0;
        if (!"All".equals(currentSelection)) {
            index = months.indexOf(currentSelection);
            if (index < 0) {
                index = 0;
            }
        }

        spinnerMonth.setSelection(index);

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

    private void setupListeners(View view) {
        FloatingActionButton fabAdd = view.findViewById(R.id.fabAdd);
        fabAdd.setOnClickListener(v ->
                addExpenseLauncher.launch(new Intent(requireContext(), AddExpenseActivity.class))
        );

        view.findViewById(R.id.btnSummary).setOnClickListener(v -> openSummary());
        view.findViewById(R.id.btnInsights).setOnClickListener(v -> openInsights());
        view.findViewById(R.id.btnCategoryBudgets).setOnClickListener(v -> openCategoryBudgets());
        view.findViewById(R.id.btnExportCsv).setOnClickListener(v -> startCsvExport());
        view.findViewById(R.id.btnSetBudget).setOnClickListener(v -> showBudgetDialog());
    }

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

        tvWelcomeUser.setText(greeting +", "+ name);
    }

    public void refreshHomeData() {
        if (!isAdded() || recurringExpenseManager == null || spinnerMonth == null) {
            return;
        }
        recurringExpenseManager.generateDueRecurringExpenses();
        setupMonthSpinner();
        loadExpensesFromDatabase();
    }

    private void loadExpensesFromDatabase() {
        masterList.clear();
        masterList.addAll(expenseRepository.getExpenses(getSelectedMonthValue(), getSelectedCategory()));
        applySearchAndSort();
    }

    private void applySearchAndSort() {
        expenseFilterSorter.apply(masterList, filteredList, getSearchQuery(), getSelectedSort());

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
        tvTotalAmount.setText(CurrencyManager.formatAmount(requireContext(), total));
    }

    private void updateBudgetUI() {
        float budget = budgetManager.getBudget();
        double totalExpense = ExpenseCalculator.getTotal(filteredList);

        if (budget == 0f) {
            tvBudget.setText(getString(R.string.budget_not_set));
            tvRemaining.setText(getString(R.string.set_a_monthly_budget_to_track_spending));
            tvRemaining.setTextColor(Color.GRAY);
            return;
        }

        tvBudget.setText(CurrencyManager.formatBudgetLabel(requireContext(), budget));

        if ("All".equals(getSelectedMonthValue())) {
            tvRemaining.setText(getString(R.string.select_a_month_to_view_remaining_budget));
            tvRemaining.setTextColor(Color.GRAY);
            return;
        }

        double remaining = budget - totalExpense;
        tvRemaining.setText(CurrencyManager.formatRemainingLabel(requireContext(), remaining));

        int color = remaining < 0
                ? ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark)
                : ContextCompat.getColor(requireContext(), R.color.success);
        tvRemaining.setTextColor(color);
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

    private void showBudgetDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());

        TextView title = new TextView(requireContext());
        title.setText(R.string.set_monthly_budget);
        title.setTextSize(18f);
        title.setPadding(0, 30, 0, 10);
        title.setGravity(Gravity.CENTER);
        title.setTypeface(null, Typeface.BOLD);
        builder.setCustomTitle(title);

        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(60, 20, 60, 10);
        layout.setGravity(Gravity.CENTER);

        EditText input = new EditText(requireContext());
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.setHint(R.string.enter_amount);
        input.setGravity(Gravity.CENTER);
        input.setTextSize(16f);

        float currentBudget = budgetManager.getBudget();
        if (currentBudget > 0) {
            input.setText(String.format(Locale.US, "%.2f", currentBudget));
        }

        layout.addView(input);
        builder.setView(layout);

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

    private void openSummary() {
        Intent intent = new Intent(requireContext(), SummaryActivity.class);
        intent.putExtra("selected_month", getSelectedMonthValue());
        intent.putExtra("selected_category", getSelectedCategory());
        startActivity(intent);
    }

    private void openInsights() {
        Intent intent = new Intent(requireContext(), InsightsActivity.class);
        intent.putExtra("selected_month", getSelectedMonthValue());
        startActivity(intent);
    }

    private void openCategoryBudgets() {
        startActivity(new Intent(requireContext(), CategoryBudgetActivity.class));
    }

    private void showInfoDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.info_title)
                .setMessage(R.string.info_message)
                .setPositiveButton(R.string.ok, null)
                .show();
    }

    private String getSelectedCategory() {
        int position = spinnerCategory != null ? spinnerCategory.getSelectedItemPosition() : 0;

        if (position < 0 || position >= filterCategoryValues.length) {
            return "All";
        }

        return filterCategoryValues[position];
    }

    private String getSelectedMonth() {
        return spinnerMonth != null && spinnerMonth.getSelectedItem() != null
                ? spinnerMonth.getSelectedItem().toString()
                : getString(R.string.all);
    }

    private String getSelectedSort() {
        int position = spinnerSort != null ? spinnerSort.getSelectedItemPosition() : 0;

        if (position < 0 || position >= sortValueKeys.length) {
            return "NEWEST";
        }

        return sortValueKeys[position];
    }

    private String getSearchQuery() {
        return etSearchExpense != null && etSearchExpense.getText() != null
                ? etSearchExpense.getText().toString().trim()
                : "";
    }
}