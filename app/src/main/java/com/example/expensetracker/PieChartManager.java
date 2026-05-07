package com.example.expensetracker;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;

import java.util.ArrayList;
import java.util.List;

public class PieChartManager {

    // These are fixed category values stored in the database.
    // Do not translate these values, because they are used for comparison.
    private static final String CATEGORY_FOOD = "Food";
    private static final String CATEGORY_ENTERTAINMENT = "Entertainment";
    private static final String CATEGORY_TRANSPORT = "Transport";
    private static final String CATEGORY_SHOPPING = "Shopping";
    private static final String CATEGORY_BILLS = "Bills";

    // Context is used to access strings.xml resources.
    private final Context context;

    // The PieChart view from the layout.
    private final PieChart pieChart;

    public PieChartManager(Context context, PieChart pieChart) {
        this.context = context;
        this.pieChart = pieChart;
    }

    // Updates the pie chart using the expense list.
    public void update(List<ExpenseEntity> expenses) {

        // Calculate total expense for each fixed category.
        double food = ExpenseCalculator.getCategoryTotal(expenses, CATEGORY_FOOD);
        double entertainment = ExpenseCalculator.getCategoryTotal(expenses, CATEGORY_ENTERTAINMENT);
        double transport = ExpenseCalculator.getCategoryTotal(expenses, CATEGORY_TRANSPORT);
        double shopping = ExpenseCalculator.getCategoryTotal(expenses, CATEGORY_SHOPPING);
        double bills = ExpenseCalculator.getCategoryTotal(expenses, CATEGORY_BILLS);

        // Calculate expenses that do not belong to the fixed categories.
        double other = calculateOtherTotal(expenses);

        // Create pie chart entries.
        // Labels use strings.xml, so they can change by language.
        ArrayList<PieEntry> entries = new ArrayList<>();
        addPieEntry(entries, food, context.getString(R.string.food));
        addPieEntry(entries, entertainment, context.getString(R.string.entertainment));
        addPieEntry(entries, transport, context.getString(R.string.transport));
        addPieEntry(entries, shopping, context.getString(R.string.shopping));
        addPieEntry(entries, bills, context.getString(R.string.bills));
        addPieEntry(entries, other, context.getString(R.string.other));

        // If there is no data, clear the chart and show empty message.
        if (entries.isEmpty()) {
            pieChart.clear();
            pieChart.setNoDataText(context.getString(R.string.no_chart_data));
            pieChart.invalidate();
            return;
        }

        // Text color changes depending on light mode or dark mode.
        int chartTextColor = getChartTextColor();

        // Create dataset for the pie chart.
        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(getColors(entries));
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(chartTextColor);

        // Convert values to percentage format.
        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter(pieChart));

        // Show values as percentages.
        pieChart.setUsePercentValues(true);

        // Make the pie chart a donut chart.
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleRadius(50f);
        pieChart.setTransparentCircleRadius(50f);

        // Make chart background transparent.
        pieChart.setBackgroundColor(Color.TRANSPARENT);
        pieChart.setHoleColor(Color.TRANSPARENT);

        // Set center text.
        pieChart.setCenterText(context.getString(R.string.expenses));
        pieChart.setCenterTextSize(14f);
        pieChart.setCenterTextColor(chartTextColor);

        // Hide default description.
        pieChart.getDescription().setEnabled(false);

        // Show legend and set legend text color.
        pieChart.getLegend().setEnabled(true);
        pieChart.getLegend().setTextColor(chartTextColor);

        // Set label style.
        pieChart.setEntryLabelTextSize(12f);
        pieChart.setEntryLabelColor(chartTextColor);

        // Apply data to chart.
        pieChart.setData(data);

        // Animate chart and redraw it.
        pieChart.animateY(900);
        pieChart.invalidate();
    }

    // Calculates total amount for categories that are not Food, Entertainment,
    // Transport, Shopping, or Bills.
    private double calculateOtherTotal(List<ExpenseEntity> expenses) {
        double other = 0.0;

        for (ExpenseEntity expense : expenses) {
            String category = expense.getCategory();

            if (!CATEGORY_FOOD.equals(category)
                    && !CATEGORY_ENTERTAINMENT.equals(category)
                    && !CATEGORY_TRANSPORT.equals(category)
                    && !CATEGORY_SHOPPING.equals(category)
                    && !CATEGORY_BILLS.equals(category)) {
                other += expense.getAmountValue();
            }
        }

        return other;
    }

    // Adds a pie chart entry only if the value is greater than 0.
    private void addPieEntry(ArrayList<PieEntry> entries, double value, String label) {
        if (value > 0) {
            entries.add(new PieEntry((float) value, label));
        }
    }

    // Gets colors for all pie chart entries.
    private ArrayList<Integer> getColors(ArrayList<PieEntry> entries) {
        ArrayList<Integer> colors = new ArrayList<>();

        for (PieEntry entry : entries) {
            colors.add(getCategoryColor(entry.getLabel()));
        }

        return colors;
    }

    // Returns a color based on the displayed category label.
    private int getCategoryColor(String label) {
        if (context.getString(R.string.food).equals(label)) {
            return Color.parseColor("#4CAF50");
        } else if (context.getString(R.string.entertainment).equals(label)) {
            return Color.parseColor("#F44336");
        } else if (context.getString(R.string.transport).equals(label)) {
            return Color.parseColor("#2196F3");
        } else if (context.getString(R.string.shopping).equals(label)) {
            return Color.parseColor("#9C27B0");
        } else if (context.getString(R.string.bills).equals(label)) {
            return Color.parseColor("#FF9800");
        } else if (context.getString(R.string.other).equals(label)) {
            return Color.parseColor("#607D8B");
        } else {
            return Color.parseColor("#BDBDBD");
        }
    }

    // Returns white text in dark mode and black text in light mode.
    private int getChartTextColor() {
        int nightModeFlags = context.getResources()
                .getConfiguration()
                .uiMode & Configuration.UI_MODE_NIGHT_MASK;

        if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
            return Color.WHITE;
        } else {
            return Color.BLACK;
        }
    }
}
