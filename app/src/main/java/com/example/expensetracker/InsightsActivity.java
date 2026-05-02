package com.example.expensetracker;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
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

    private final String[] categories = {"Food", "Entertainment", "Transport", "Shopping", "Bills", "Other"};

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insights);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.insights_main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        database = ExpenseDatabase.getDatabase(this);
        initializeViews();

        MaterialButton btnBackInsights = findViewById(R.id.btnBackInsights);
        btnBackInsights.setOnClickListener(v -> finish());

        loadInsights();
    }

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

    private void loadInsights() {
        Calendar selected = getSelectedCalendar();
        Calendar previous = (Calendar) selected.clone();
        previous.add(Calendar.MONTH, -1);

        String currentMonth = String.format(Locale.US, "%02d", selected.get(Calendar.MONTH) + 1);
        String currentYear = String.valueOf(selected.get(Calendar.YEAR));
        String previousMonth = String.format(Locale.US, "%02d", previous.get(Calendar.MONTH) + 1);
        String previousYear = String.valueOf(previous.get(Calendar.YEAR));

        List<ExpenseEntity> currentExpenses = database.expenseDao().getExpensesByMonth(currentMonth, currentYear);
        List<ExpenseEntity> previousExpenses = database.expenseDao().getExpensesByMonth(previousMonth, previousYear);

        double currentTotal = getTotal(currentExpenses);
        double previousTotal = getTotal(previousExpenses);
        double difference = currentTotal - previousTotal;
        double percentChange = previousTotal == 0 ? 0 : (difference / previousTotal) * 100.0;

        Map<String, Double> currentCategoryTotals = getCategoryTotals(currentExpenses);
        Map<String, Double> previousCategoryTotals = getCategoryTotals(previousExpenses);

        ExpenseEntity highestExpense = findHighestExpense(currentExpenses);
        String topCategory = findTopCategory(currentCategoryTotals);
        double topCategoryTotal = currentCategoryTotals.get(topCategory) == null ? 0 : currentCategoryTotals.get(topCategory);

        int dayOfMonth = selected.get(Calendar.DAY_OF_MONTH);
        int daysInMonth = selected.getActualMaximum(Calendar.DAY_OF_MONTH);
        boolean selectedMonthIsCurrent = isSameMonthAndYear(selected, Calendar.getInstance());
        int daysUsedForAverage = selectedMonthIsCurrent ? Math.max(1, dayOfMonth) : daysInMonth;
        double dailyAverage = currentTotal / daysUsedForAverage;
        double projectedMonthlySpending = selectedMonthIsCurrent ? dailyAverage * daysInMonth : currentTotal;

        float monthlyBudget = getSharedPreferences("budget", MODE_PRIVATE).getFloat("budget", 0f);
        String healthStatus = getHealthStatus(currentTotal, projectedMonthlySpending, monthlyBudget);
        int healthScore = getHealthScore(currentTotal, projectedMonthlySpending, monthlyBudget);

        tvInsightsSubtitle.setText(String.format(Locale.US,
                "Analyzing %02d/%d compared with %02d/%d",
                selected.get(Calendar.MONTH) + 1,
                selected.get(Calendar.YEAR),
                previous.get(Calendar.MONTH) + 1,
                previous.get(Calendar.YEAR)));

        tvThisMonthTotal.setText(String.format(Locale.US, "$%.2f", currentTotal));
        tvLastMonthTotal.setText(String.format(Locale.US, "$%.2f", previousTotal));

        if (previousTotal == 0 && currentTotal > 0) {
            tvDifference.setText(String.format(Locale.US, "+$%.2f compared to last month", difference));
        } else {
            tvDifference.setText(String.format(Locale.US, "%s$%.2f (%.1f%%) compared to last month",
                    difference >= 0 ? "+" : "-",
                    Math.abs(difference),
                    Math.abs(percentChange)));
        }
        tvDifference.setTextColor(difference > 0 ? Color.parseColor("#C62828") : Color.parseColor("#2E7D32"));

        tvHealthScore.setText(String.format(Locale.US, "%d/100", healthScore));
        tvHealthMessage.setText(healthStatus);
        tvHealthMessage.setTextColor(getHealthColor(healthStatus));

        if (monthlyBudget <= 0) {
            tvBudgetRisk.setText("Monthly budget is not set yet.");
        } else {
            double usedPercent = (currentTotal / monthlyBudget) * 100.0;
            double projectedPercent = (projectedMonthlySpending / monthlyBudget) * 100.0;
            tvBudgetRisk.setText(String.format(Locale.US,
                    "Used %.1f%% of monthly budget. Projected usage: %.1f%%.",
                    usedPercent,
                    projectedPercent));
        }

        if (currentTotal == 0) {
            tvTopCategory.setText("No category data yet.");
            tvHighestExpense.setText("No expenses recorded for this month.");
        } else {
            tvTopCategory.setText(String.format(Locale.US, "%s is highest at $%.2f", topCategory, topCategoryTotal));
            if (highestExpense != null) {
                String note = highestExpense.getNote() == null || highestExpense.getNote().trim().isEmpty()
                        ? "No note"
                        : highestExpense.getNote();
                tvHighestExpense.setText(String.format(Locale.US,
                        "$%.2f • %s • %s • %s",
                        highestExpense.getAmountValue(),
                        highestExpense.getCategory(),
                        highestExpense.getDate(),
                        note));
            }
        }

        tvDailyAverage.setText(String.format(Locale.US, "$%.2f per day", dailyAverage));
        tvProjectedSpending.setText(String.format(Locale.US, "$%.2f projected for the month", projectedMonthlySpending));

        tvInsightsList.setText(buildRecommendationText(
                currentTotal,
                previousTotal,
                difference,
                topCategory,
                topCategoryTotal,
                dailyAverage,
                projectedMonthlySpending,
                monthlyBudget,
                currentCategoryTotals));

        setupBarChart(currentCategoryTotals, previousCategoryTotals);
    }

    private Calendar getSelectedCalendar() {
        Calendar calendar = Calendar.getInstance();
        String selectedMonth = getIntent().getStringExtra("selected_month");

        if (selectedMonth != null && !selectedMonth.equals("All") && selectedMonth.contains("/")) {
            String[] parts = selectedMonth.split("/");
            if (parts.length == 2) {
                try {
                    int month = Integer.parseInt(parts[0]) - 1;
                    int year = Integer.parseInt(parts[1]);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.DAY_OF_MONTH, 1);
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return calendar;
    }

    private boolean isSameMonthAndYear(Calendar a, Calendar b) {
        return a.get(Calendar.MONTH) == b.get(Calendar.MONTH)
                && a.get(Calendar.YEAR) == b.get(Calendar.YEAR);
    }

    private double getTotal(List<ExpenseEntity> expenses) {
        double total = 0;
        for (ExpenseEntity expense : expenses) {
            total += expense.getAmountValue();
        }
        return total;
    }

    private Map<String, Double> getCategoryTotals(List<ExpenseEntity> expenses) {
        Map<String, Double> totals = new HashMap<>();
        for (String category : categories) {
            totals.put(category, 0.0);
        }

        for (ExpenseEntity expense : expenses) {
            String category = expense.getCategory();
            if (!totals.containsKey(category)) {
                category = "Other";
            }
            totals.put(category, totals.get(category) + expense.getAmountValue());
        }
        return totals;
    }

    private ExpenseEntity findHighestExpense(List<ExpenseEntity> expenses) {
        ExpenseEntity highest = null;
        for (ExpenseEntity expense : expenses) {
            if (highest == null || expense.getAmountValue() > highest.getAmountValue()) {
                highest = expense;
            }
        }
        return highest;
    }

    private String findTopCategory(Map<String, Double> totals) {
        String topCategory = "Other";
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

    private String getHealthStatus(double currentTotal, double projectedMonthlySpending, float monthlyBudget) {
        if (currentTotal == 0) return "No Data";
        if (monthlyBudget <= 0) return "Budget Needed";
        if (currentTotal > monthlyBudget || projectedMonthlySpending > monthlyBudget) return "Over Budget Risk";
        if (projectedMonthlySpending >= monthlyBudget * 0.80) return "Warning";
        return "Good";
    }

    private int getHealthScore(double currentTotal, double projectedMonthlySpending, float monthlyBudget) {
        if (currentTotal == 0) return 100;
        if (monthlyBudget <= 0) return 70;
        double ratio = projectedMonthlySpending / monthlyBudget;
        int score = (int) Math.round(100 - ((ratio - 0.50) * 100));
        if (ratio <= 0.50) score = 100;
        if (score < 0) score = 0;
        if (score > 100) score = 100;
        return score;
    }

    private int getHealthColor(String healthStatus) {
        if ("Good".equals(healthStatus)) return Color.parseColor("#2E7D32");
        if ("Warning".equals(healthStatus)) return Color.parseColor("#EF6C00");
        if ("Over Budget Risk".equals(healthStatus)) return Color.parseColor("#C62828");
        return Color.parseColor("#607D8B");
    }

    private String buildRecommendationText(double currentTotal,
                                           double previousTotal,
                                           double difference,
                                           String topCategory,
                                           double topCategoryTotal,
                                           double dailyAverage,
                                           double projectedMonthlySpending,
                                           float monthlyBudget,
                                           Map<String, Double> categoryTotals) {
        StringBuilder builder = new StringBuilder();

        if (currentTotal == 0) {
            builder.append("• Add expenses for this month to unlock personalized insights.\n");
            return builder.toString();
        }

        if (previousTotal == 0) {
            builder.append("• This is the first month with comparison data, so next month will be more meaningful.\n");
        } else if (difference > 0) {
            builder.append(String.format(Locale.US,
                    "• Spending increased by $%.2f compared to last month. Check which categories changed the most.\n",
                    difference));
        } else if (difference < 0) {
            builder.append(String.format(Locale.US,
                    "• Spending decreased by $%.2f compared to last month. Nice improvement.\n",
                    Math.abs(difference)));
        } else {
            builder.append("• Spending is exactly the same as last month.\n");
        }

        builder.append(String.format(Locale.US,
                "• Your highest category is %s at $%.2f.\n",
                topCategory,
                topCategoryTotal));

        builder.append(String.format(Locale.US,
                "• You are spending about $%.2f per day.\n",
                dailyAverage));

        if (monthlyBudget > 0) {
            double remaining = monthlyBudget - currentTotal;
            if (remaining < 0) {
                builder.append(String.format(Locale.US,
                        "• You are already $%.2f over your monthly budget.\n",
                        Math.abs(remaining)));
            } else if (projectedMonthlySpending > monthlyBudget) {
                builder.append(String.format(Locale.US,
                        "• At this pace, you may go over budget by $%.2f.\n",
                        projectedMonthlySpending - monthlyBudget));
            } else {
                builder.append(String.format(Locale.US,
                        "• At this pace, you should stay under budget by about $%.2f.\n",
                        monthlyBudget - projectedMonthlySpending));
            }
        } else {
            builder.append("• Set a monthly budget to unlock stronger budget warnings.\n");
        }

        SharedPreferences categoryBudgetPrefs = getSharedPreferences("category_budgets", MODE_PRIVATE);
        for (String category : categories) {
            float categoryBudget = categoryBudgetPrefs.getFloat("budget_" + category, 0f);
            double spent = categoryTotals.get(category) == null ? 0 : categoryTotals.get(category);
            if (categoryBudget > 0 && spent > categoryBudget) {
                builder.append(String.format(Locale.US,
                        "• %s is over its category budget by $%.2f.\n",
                        category,
                        spent - categoryBudget));
            } else if (categoryBudget > 0 && spent >= categoryBudget * 0.80) {
                builder.append(String.format(Locale.US,
                        "• %s is close to its category budget.\n",
                        category));
            }
        }

        return builder.toString();
    }

    private void setupBarChart(Map<String, Double> currentCategoryTotals, Map<String, Double> previousCategoryTotals) {
        ArrayList<BarEntry> currentEntries = new ArrayList<>();
        ArrayList<BarEntry> previousEntries = new ArrayList<>();

        for (int i = 0; i < categories.length; i++) {
            String category = categories[i];
            currentEntries.add(new BarEntry(i, currentCategoryTotals.get(category).floatValue()));
            previousEntries.add(new BarEntry(i, previousCategoryTotals.get(category).floatValue()));
        }

        BarDataSet currentDataSet = new BarDataSet(currentEntries, "Selected Month");
        currentDataSet.setColor(Color.parseColor("#4CAF50"));
        currentDataSet.setValueTextSize(10f);

        BarDataSet previousDataSet = new BarDataSet(previousEntries, "Previous Month");
        previousDataSet.setColor(Color.parseColor("#9E9E9E"));
        previousDataSet.setValueTextSize(10f);

        BarData data = new BarData(currentDataSet, previousDataSet);
        float groupSpace = 0.30f;
        float barSpace = 0.05f;
        float barWidth = 0.30f;
        data.setBarWidth(barWidth);

        barChartComparison.setData(data);
        barChartComparison.groupBars(-0.5f, groupSpace, barSpace);
        barChartComparison.getDescription().setEnabled(false);
        barChartComparison.getAxisRight().setEnabled(false);
        barChartComparison.getLegend().setEnabled(true);
        barChartComparison.setFitBars(true);
        barChartComparison.setScaleEnabled(false);

        XAxis xAxis = barChartComparison.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(categories));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);
        xAxis.setLabelRotationAngle(-35f);
        xAxis.setDrawGridLines(false);

        barChartComparison.animateY(900);
        barChartComparison.invalidate();
    }
}
