package com.example.expensetracker;

import android.graphics.Color;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;

import java.util.ArrayList;
import java.util.List;

public class PieChartManager {
    private final PieChart pieChart;

    public PieChartManager(PieChart pieChart) {
        this.pieChart = pieChart;
    }

    public void update(List<ExpenseEntity> expenses) {
        double food = ExpenseCalculator.getCategoryTotal(expenses, "Food");
        double entertainment = ExpenseCalculator.getCategoryTotal(expenses, "Entertainment");
        double transport = ExpenseCalculator.getCategoryTotal(expenses, "Transport");
        double shopping = ExpenseCalculator.getCategoryTotal(expenses, "Shopping");
        double bills = ExpenseCalculator.getCategoryTotal(expenses, "Bills");
        double other = calculateOtherTotal(expenses);

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
        dataSet.setColors(getColors(entries));
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

    private double calculateOtherTotal(List<ExpenseEntity> expenses) {
        double other = 0.0;
        for (ExpenseEntity expense : expenses) {
            String category = expense.getCategory();
            if (!"Food".equals(category)
                    && !"Entertainment".equals(category)
                    && !"Transport".equals(category)
                    && !"Shopping".equals(category)
                    && !"Bills".equals(category)) {
                other += expense.getAmountValue();
            }
        }
        return other;
    }

    private void addPieEntry(ArrayList<PieEntry> entries, double value, String label) {
        if (value > 0) {
            entries.add(new PieEntry((float) value, label));
        }
    }

    private ArrayList<Integer> getColors(ArrayList<PieEntry> entries) {
        ArrayList<Integer> colors = new ArrayList<>();
        for (PieEntry entry : entries) {
            colors.add(getCategoryColor(entry.getLabel()));
        }
        return colors;
    }

    private int getCategoryColor(String category) {
        switch (category) {
            case "Food":
                return Color.parseColor("#4CAF50");
            case "Entertainment":
                return Color.parseColor("#F44336");
            case "Transport":
                return Color.parseColor("#2196F3");
            case "Shopping":
                return Color.parseColor("#9C27B0");
            case "Bills":
                return Color.parseColor("#FF9800");
            case "Other":
                return Color.parseColor("#607D8B");
            default:
                return Color.parseColor("#BDBDBD");
        }
    }
}
