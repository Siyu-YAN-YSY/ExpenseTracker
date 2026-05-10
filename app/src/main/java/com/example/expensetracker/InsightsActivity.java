package com.example.expensetracker;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class InsightsActivity extends AppCompatActivity {

    // Category keys used internally for grouping expenses
    private static final String CATEGORY_FOOD = "Food";
    private static final String CATEGORY_ENTERTAINMENT = "Entertainment";
    private static final String CATEGORY_TRANSPORT = "Transport";
    private static final String CATEGORY_SHOPPING = "Shopping";
    private static final String CATEGORY_BILLS = "Bills";
    private static final String CATEGORY_OTHER = "Other";

    // List of categories used for totals, comparisons, and chart display
    private final String[] categories = {
            CATEGORY_FOOD,
            CATEGORY_ENTERTAINMENT,
            CATEGORY_TRANSPORT,
            CATEGORY_SHOPPING,
            CATEGORY_BILLS,
            CATEGORY_OTHER
    };

    // Database and UI components used on the insights screen
    private ExpenseDatabase database;
    private BarChart barChartComparison;
    private TextView tvInsightsSubtitle;
    private TextView tvHealthScore;
    private TextView tvHealthMessage;
    private TextView tvThisMonthTotal;
    private TextView tvLastMonthTotal;
    private TextView tvDifference;
    private TextView tvBudgetRisk;
    private TextView tvTopCategory;
    private TextView tvHighestExpense;
    private TextView tvDailyAverage;
    private TextView tvProjectedSpending;
    private TextView tvInsightsList;

    // Applies the saved light or dark theme before the activity loads
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
        setContentView(R.layout.activity_insights);

        // Adds padding so content does not overlap with system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.insights_main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Gets database instance and connects all views
        database = ExpenseDatabase.getDatabase(this);
        initializeViews();

        // Closes the insights screen
        MaterialButton btnBackInsights = findViewById(R.id.btnBackInsights);
        btnBackInsights.setOnClickListener(v -> finish());

        // Loads and displays all insight data
        loadInsights();
    }

    // Connects Java variables to their XML layout views
    private void initializeViews() {
        barChartComparison = findViewById(R.id.barChartComparison);
        tvInsightsSubtitle = findViewById(R.id.tvInsightsSubtitle);
        tvHealthScore = findViewById(R.id.tvHealthScore);
        tvHealthMessage = findViewById(R.id.tvHealthMessage);
        tvThisMonthTotal = findViewById(R.id.tvThisMonthTotal);
        tvLastMonthTotal = findViewById(R.id.tvLastMonthTotal);
        tvDifference = findViewById(R.id.tvDifference);
        tvBudgetRisk = findViewById(R.id.tvBudgetRisk);
        tvTopCategory = findViewById(R.id.tvTopCategory);
        tvHighestExpense = findViewById(R.id.tvHighestExpense);
        tvDailyAverage = findViewById(R.id.tvDailyAverage);
        tvProjectedSpending = findViewById(R.id.tvProjectedSpending);
        tvInsightsList = findViewById(R.id.tvInsightsList);
    }

    // Loads expenses, calculates insights, updates text views, and builds the chart
    private void loadInsights() {
        // Gets the selected month and the previous month for comparison
        Calendar selected = getSelectedCalendar();
        Calendar previous = (Calendar) selected.clone();
        previous.add(Calendar.MONTH, -1);

        String currentMonth = String.format(Locale.US, "%02d", selected.get(Calendar.MONTH) + 1);
        String currentYear = String.valueOf(selected.get(Calendar.YEAR));
        String previousMonth = String.format(Locale.US, "%02d", previous.get(Calendar.MONTH) + 1);
        String previousYear = String.valueOf(previous.get(Calendar.YEAR));

        // Loads current and previous month expenses from the database
        List<ExpenseEntity> currentExpenses =
                database.expenseDao().getExpensesByMonth(currentMonth, currentYear);

        List<ExpenseEntity> previousExpenses =
                database.expenseDao().getExpensesByMonth(previousMonth, previousYear);

        // Calculates monthly totals and month-to-month difference
        double currentTotal = getTotal(currentExpenses);
        double previousTotal = getTotal(previousExpenses);
        double difference = currentTotal - previousTotal;
        double percentChange = previousTotal == 0 ? 0 : (difference / previousTotal) * 100.0;

        // Calculates totals for each category
        Map<String, Double> currentCategoryTotals = getCategoryTotals(currentExpenses);
        Map<String, Double> previousCategoryTotals = getCategoryTotals(previousExpenses);

        // Finds highest expense and top spending category
        ExpenseEntity highestExpense = findHighestExpense(currentExpenses);
        String topCategory = findTopCategory(currentCategoryTotals);
        double topCategoryTotal = currentCategoryTotals.get(topCategory) == null
                ? 0
                : currentCategoryTotals.get(topCategory);

        // Calculates daily average and projected monthly spending
        int dayOfMonth = selected.get(Calendar.DAY_OF_MONTH);
        int daysInMonth = selected.getActualMaximum(Calendar.DAY_OF_MONTH);
        boolean selectedMonthIsCurrent = isSameMonthAndYear(selected, Calendar.getInstance());
        int daysUsedForAverage = selectedMonthIsCurrent ? Math.max(1, dayOfMonth) : daysInMonth;
        int dayForInsight = selectedMonthIsCurrent ? dayOfMonth : daysInMonth;

        double dailyAverage = currentTotal / daysUsedForAverage;
        double projectedMonthlySpending = selectedMonthIsCurrent ? dailyAverage * daysInMonth : currentTotal;

        // Gets saved monthly budget
        float monthlyBudget = getSharedPreferences("budget", MODE_PRIVATE).getFloat("budget", 0f);

        // Creates smart insight status and message
        String smartInsightStatus = getSmartInsightStatus(currentTotal, monthlyBudget, dayForInsight);
        String smartInsightMessage = getSmartInsightMessage(currentTotal, monthlyBudget, dayForInsight);

        // Updates subtitle showing selected and previous month
        tvInsightsSubtitle.setText(getString(
                R.string.analyzing_month_compared,
                selected.get(Calendar.MONTH) + 1,
                selected.get(Calendar.YEAR),
                previous.get(Calendar.MONTH) + 1,
                previous.get(Calendar.YEAR)
        ));

        // Displays current and previous month totals
        tvThisMonthTotal.setText(CurrencyManager.formatAmount(this, currentTotal));
        tvLastMonthTotal.setText(CurrencyManager.formatAmount(this, previousTotal));

        // Displays spending difference compared to previous month
        if (previousTotal == 0 && currentTotal > 0) {
            tvDifference.setText(getString(
                    R.string.compared_to_last_month,
                    "+" + CurrencyManager.formatAmount(this, difference)
            ));
        } else {
            tvDifference.setText(getString(
                    R.string.difference_percent_compared_to_last_month,
                    difference >= 0 ? "+" : "-",
                    CurrencyManager.formatAmount(this, Math.abs(difference)),
                    Math.abs(percentChange)
            ));
        }

        // Red means spending increased; green means spending decreased or stayed below
        tvDifference.setTextColor(difference > 0
                ? Color.parseColor("#C62828")
                : Color.parseColor("#2E7D32"));

        // Updates smart insight message and color
        tvHealthScore.setText(getString(R.string.smart_insight));
        tvHealthMessage.setText(smartInsightMessage);
        tvHealthMessage.setTextColor(getSmartInsightColor(smartInsightStatus));

        // Shows monthly budget usage and projected budget usage
        if (monthlyBudget <= 0) {
            tvBudgetRisk.setText(getString(R.string.monthly_budget_is_not_set_yet));
        } else {
            double usedPercent = (currentTotal / monthlyBudget) * 100.0;
            double projectedPercent = (projectedMonthlySpending / monthlyBudget) * 100.0;

            tvBudgetRisk.setText(getString(
                    R.string.budget_usage_projected,
                    usedPercent,
                    projectedPercent
            ));
        }

        // Shows top category and highest expense details if there is spending data
        if (currentTotal == 0) {
            tvTopCategory.setText(getString(R.string.no_category_data_yet));
            tvHighestExpense.setText(getString(R.string.no_expenses_recorded_for_this_month));
        } else {
            tvTopCategory.setText(getString(
                    R.string.top_category_highest,
                    getCategoryDisplayName(topCategory),
                    CurrencyManager.formatAmount(this, topCategoryTotal)
            ));

            if (highestExpense != null) {
                String note = highestExpense.getNote() == null || highestExpense.getNote().trim().isEmpty()
                        ? getString(R.string.no_note)
                        : highestExpense.getNote();

                tvHighestExpense.setText(getString(
                        R.string.highest_expense_detail,
                        CurrencyManager.formatAmount(this, highestExpense.getAmountValue()),
                        getCategoryDisplayName(highestExpense.getCategory()),
                        highestExpense.getDate(),
                        note
                ));
            }
        }

        // Displays average daily spending
        tvDailyAverage.setText(getString(
                R.string.amount_per_day,
                CurrencyManager.formatAmount(this, dailyAverage)
        ));

        // Displays projected total spending for the month
        tvProjectedSpending.setText(getString(
                R.string.amount_projected_for_month,
                CurrencyManager.formatAmount(this, projectedMonthlySpending)
        ));

        // Builds personalized recommendation text
        tvInsightsList.setText(buildRecommendationText(
                currentTotal,
                previousTotal,
                difference,
                topCategory,
                topCategoryTotal,
                dailyAverage,
                projectedMonthlySpending,
                monthlyBudget,
                currentCategoryTotals,
                dayForInsight
        ));

        // Updates bar chart comparing current and previous month spending
        setupBarChart(currentCategoryTotals, previousCategoryTotals);
    }

    // Gets the selected month from the Intent, or uses the current month by default
    private Calendar getSelectedCalendar() {
        Calendar calendar = Calendar.getInstance();
        String selectedMonth = getIntent().getStringExtra("selected_month");

        if (selectedMonth != null
                && !selectedMonth.equals(getString(R.string.all))
                && !selectedMonth.equals("All")
                && selectedMonth.contains("/")) {

            String[] parts = selectedMonth.split("/");

            if (parts.length == 2) {
                try {
                    int month = Integer.parseInt(parts[0]) - 1;
                    int year = Integer.parseInt(parts[1]);

                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.DAY_OF_MONTH, 1);
                } catch (NumberFormatException ignored) {
                    // Keeps the current month if the selected month cannot be parsed
                }
            }
        }

        return calendar;
    }

    // Checks whether two Calendar objects are in the same month and year
    private boolean isSameMonthAndYear(Calendar a, Calendar b) {
        return a.get(Calendar.MONTH) == b.get(Calendar.MONTH)
                && a.get(Calendar.YEAR) == b.get(Calendar.YEAR);
    }

    // Calculates the total amount from a list of expenses
    private double getTotal(List<ExpenseEntity> expenses) {
        double total = 0;

        for (ExpenseEntity expense : expenses) {
            total += expense.getAmountValue();
        }

        return total;
    }

    // Calculates total spending for each category
    private Map<String, Double> getCategoryTotals(List<ExpenseEntity> expenses) {
        Map<String, Double> totals = new HashMap<>();

        // Starts each category at zero so chart values always exist
        for (String category : categories) {
            totals.put(category, 0.0);
        }

        // Adds each expense amount to its category total
        for (ExpenseEntity expense : expenses) {
            String category = expense.getCategory();

            if (!totals.containsKey(category)) {
                category = CATEGORY_OTHER;
            }

            totals.put(category, totals.get(category) + expense.getAmountValue());
        }

        return totals;
    }

    // Finds the single highest expense in the list
    private ExpenseEntity findHighestExpense(List<ExpenseEntity> expenses) {
        ExpenseEntity highest = null;

        for (ExpenseEntity expense : expenses) {
            if (highest == null || expense.getAmountValue() > highest.getAmountValue()) {
                highest = expense;
            }
        }

        return highest;
    }

    // Finds the category with the highest total spending
    private String findTopCategory(Map<String, Double> totals) {
        String topCategory = CATEGORY_OTHER;
        double topAmount = -1;

        for (String category : categories) {
            double amount = totals.get(category) == null ? 0 : totals.get(category);

            if (amount > topAmount) {
                topAmount = amount;
                topCategory = category;
            }
        }

        return topCategory;
    }

    // Returns a short status based on spending compared to the monthly budget
    private String getSmartInsightStatus(double currentTotal, float monthlyBudget, int dayOfMonth) {
        if (monthlyBudget <= 0) return "Budget Needed";
        if (currentTotal <= 0) return "No Data";

        double usageRatio = currentTotal / monthlyBudget;

        if (usageRatio < 0.10 && dayOfMonth > 15) return "Budget Too High";
        if (usageRatio <= 0.60) return "Good";
        if (usageRatio <= 0.80) return "Warning";
        if (usageRatio <= 1.00) return "Close To Limit";

        return "Over Budget";
    }

    // Returns a detailed smart insight message based on budget usage
    private String getSmartInsightMessage(double currentTotal, float monthlyBudget, int dayOfMonth) {
        if (monthlyBudget <= 0) {
            return getString(R.string.smart_insight_budget_needed);
        }

        if (currentTotal <= 0) {
            return getString(R.string.no_spending_recorded_yet_this_month);
        }

        double usagePercent = (currentTotal / monthlyBudget) * 100.0;

        if (usagePercent < 10.0 && dayOfMonth > 15) {
            return getString(R.string.smart_insight_budget_may_be_high, usagePercent);
        }

        if (usagePercent <= 60.0) {
            return getString(R.string.smart_insight_good, usagePercent);
        }

        if (usagePercent <= 80.0) {
            return getString(R.string.smart_insight_warning, usagePercent);
        }

        if (usagePercent <= 100.0) {
            return getString(R.string.smart_insight_close_limit, usagePercent);
        }

        return getString(R.string.smart_insight_over_budget, usagePercent);
    }

    // Returns the text color for the smart insight status
    private int getSmartInsightColor(String status) {
        if ("Good".equals(status)) return Color.parseColor("#2E7D32");
        if ("Warning".equals(status)) return Color.parseColor("#EF6C00");
        if ("Close To Limit".equals(status)) return Color.parseColor("#C62828");
        if ("Over Budget".equals(status)) return Color.parseColor("#C62828");
        if ("Budget Too High".equals(status)) return Color.parseColor("#1565C0");

        return Color.parseColor("#607D8B");
    }

    // Builds a bullet-point list of personalized spending recommendations
    private String buildRecommendationText(double currentTotal,
                                           double previousTotal,
                                           double difference,
                                           String topCategory,
                                           double topCategoryTotal,
                                           double dailyAverage,
                                           double projectedMonthlySpending,
                                           float monthlyBudget,
                                           Map<String, Double> categoryTotals,
                                           int dayForInsight) {
        StringBuilder builder = new StringBuilder();

        // Shows a starter message when there are no expenses for the selected month
        if (currentTotal == 0) {
            builder.append("• ")
                    .append(getString(R.string.add_expenses_for_this_month_to_unlock_personalized_insights))
                    .append("\n");
            return builder.toString();
        }

        // Adds a comparison insight against the previous month
        if (previousTotal == 0) {
            builder.append("• ")
                    .append(getString(R.string.insight_first_month_comparison))
                    .append("\n");
        } else if (difference > 0) {
            builder.append("• ")
                    .append(getString(
                            R.string.insight_spending_increased,
                            CurrencyManager.formatAmount(this, difference)
                    ))
                    .append("\n");
        } else if (difference < 0) {
            builder.append("• ")
                    .append(getString(
                            R.string.insight_spending_decreased,
                            CurrencyManager.formatAmount(this, Math.abs(difference))
                    ))
                    .append("\n");
        } else {
            builder.append("• ")
                    .append(getString(R.string.insight_spending_same))
                    .append("\n");
        }

        // Adds the top spending category insight
        builder.append("• ")
                .append(getString(
                        R.string.insight_highest_category,
                        getCategoryDisplayName(topCategory),
                        CurrencyManager.formatAmount(this, topCategoryTotal)
                ))
                .append("\n");

        // Adds the daily average insight
        builder.append("• ")
                .append(getString(
                        R.string.insight_daily_average,
                        CurrencyManager.formatAmount(this, dailyAverage)
                ))
                .append("\n");

        // Adds budget-related insights if a monthly budget exists
        if (monthlyBudget > 0) {
            double usagePercent = (currentTotal / monthlyBudget) * 100.0;
            double remaining = monthlyBudget - currentTotal;

            if (usagePercent < 10.0 && dayForInsight > 15) {
                builder.append("• ")
                        .append(getString(R.string.smart_insight_budget_may_be_high, usagePercent))
                        .append("\n");
            } else if (remaining < 0) {
                builder.append("• ")
                        .append(getString(
                                R.string.insight_over_monthly_budget,
                                CurrencyManager.formatAmount(this, Math.abs(remaining))
                        ))
                        .append("\n");
            } else if (usagePercent <= 60.0) {
                builder.append("• ")
                        .append(getString(R.string.smart_insight_good, usagePercent))
                        .append("\n");
            } else if (usagePercent <= 80.0) {
                builder.append("• ")
                        .append(getString(R.string.smart_insight_warning, usagePercent))
                        .append("\n");
            } else if (usagePercent <= 100.0) {
                builder.append("• ")
                        .append(getString(R.string.smart_insight_close_limit, usagePercent))
                        .append("\n");
            }

            // Warns the user if projected spending may go over budget
            if (remaining >= 0 && projectedMonthlySpending > monthlyBudget) {
                builder.append("• ")
                        .append(getString(
                                R.string.insight_may_go_over_budget,
                                CurrencyManager.formatAmount(this, projectedMonthlySpending - monthlyBudget)
                        ))
                        .append("\n");
            }
        } else {
            // Suggests setting a monthly budget if none exists
            builder.append("• ")
                    .append(getString(R.string.insight_set_monthly_budget))
                    .append("\n");
        }

        // Adds category-budget warnings if any category is close to or over budget
        SharedPreferences categoryBudgetPrefs =
                getSharedPreferences("category_budgets", MODE_PRIVATE);

        for (String category : categories) {
            float categoryBudget = categoryBudgetPrefs.getFloat("budget_" + category, 0f);
            double spent = categoryTotals.get(category) == null ? 0 : categoryTotals.get(category);

            if (categoryBudget > 0 && spent > categoryBudget) {
                builder.append("• ")
                        .append(getString(
                                R.string.insight_category_over_budget,
                                getCategoryDisplayName(category),
                                CurrencyManager.formatAmount(this, spent - categoryBudget)
                        ))
                        .append("\n");
            } else if (categoryBudget > 0 && spent >= categoryBudget * 0.80) {
                builder.append("• ")
                        .append(getString(
                                R.string.insight_category_close_budget,
                                getCategoryDisplayName(category)
                        ))
                        .append("\n");
            }
        }

        return builder.toString();
    }

    // Sets up a grouped bar chart comparing category spending between two months
    private void setupBarChart(Map<String, Double> currentCategoryTotals,
                               Map<String, Double> previousCategoryTotals) {
        ArrayList<BarEntry> currentEntries = new ArrayList<>();
        ArrayList<BarEntry> previousEntries = new ArrayList<>();

        // Creates one chart entry per category for both months
        for (int i = 0; i < categories.length; i++) {
            String category = categories[i];

            currentEntries.add(new BarEntry(i, currentCategoryTotals.get(category).floatValue()));
            previousEntries.add(new BarEntry(i, previousCategoryTotals.get(category).floatValue()));
        }

        int chartTextColor = getChartTextColor();

        // Data set for the selected month
        BarDataSet currentDataSet = new BarDataSet(
                currentEntries,
                getString(R.string.selected_month)
        );
        currentDataSet.setColor(Color.parseColor("#4CAF50"));
        currentDataSet.setValueTextSize(10f);
        currentDataSet.setValueTextColor(chartTextColor);

        // Data set for the previous month
        BarDataSet previousDataSet = new BarDataSet(
                previousEntries,
                getString(R.string.previous_month)
        );
        previousDataSet.setColor(Color.parseColor("#9E9E9E"));
        previousDataSet.setValueTextSize(10f);
        previousDataSet.setValueTextColor(chartTextColor);

        BarData data = new BarData(currentDataSet, previousDataSet);

        // Controls spacing between grouped bars
        float groupSpace = 0.30f;
        float barSpace = 0.05f;
        float barWidth = 0.30f;
        data.setBarWidth(barWidth);

        // Applies data and basic chart settings
        barChartComparison.setData(data);
        barChartComparison.groupBars(-0.5f, groupSpace, barSpace);
        barChartComparison.getDescription().setEnabled(false);
        barChartComparison.getAxisRight().setEnabled(false);
        barChartComparison.getLegend().setEnabled(true);
        barChartComparison.getLegend().setTextColor(chartTextColor);
        barChartComparison.setFitBars(true);
        barChartComparison.setScaleEnabled(false);

        // Updates axis colors based on light or dark mode
        barChartComparison.getAxisLeft().setTextColor(chartTextColor);
        barChartComparison.getXAxis().setTextColor(chartTextColor);

        // Configures category labels on the X axis
        XAxis xAxis = barChartComparison.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(getCategoryDisplayNames()));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);
        xAxis.setLabelRotationAngle(-35f);
        xAxis.setDrawGridLines(false);

        // Animates and refreshes the chart
        barChartComparison.animateY(900);
        barChartComparison.invalidate();
    }

    // Converts an internal category key into a localized display name
    private String getCategoryDisplayName(String category) {
        if (CATEGORY_FOOD.equals(category)) {
            return getString(R.string.food);
        } else if (CATEGORY_ENTERTAINMENT.equals(category)) {
            return getString(R.string.entertainment);
        } else if (CATEGORY_TRANSPORT.equals(category)) {
            return getString(R.string.transport);
        } else if (CATEGORY_SHOPPING.equals(category)) {
            return getString(R.string.shopping);
        } else if (CATEGORY_BILLS.equals(category)) {
            return getString(R.string.bills);
        } else {
            return getString(R.string.other);
        }
    }

    // Builds localized category names for the chart X-axis labels
    private String[] getCategoryDisplayNames() {
        String[] displayNames = new String[categories.length];

        for (int i = 0; i < categories.length; i++) {
            displayNames[i] = getCategoryDisplayName(categories[i]);
        }

        return displayNames;
    }

    // Returns black or white chart text depending on the current theme
    private int getChartTextColor() {
        int nightModeFlags = getResources()
                .getConfiguration()
                .uiMode & Configuration.UI_MODE_NIGHT_MASK;

        return nightModeFlags == Configuration.UI_MODE_NIGHT_YES
                ? Color.WHITE
                : Color.BLACK;
    }

    // Applies the selected app language before attaching the activity context
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleManager.applyLanguage(newBase));
    }
}
