package com.example.expensetracker;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ExpenseAdapterRoom extends RecyclerView.Adapter<ExpenseAdapterRoom.ExpenseViewHolder> {

    // Category keys used internally for saving and comparing expense categories
    private static final String CATEGORY_FOOD = "Food";
    private static final String CATEGORY_ENTERTAINMENT = "Entertainment";
    private static final String CATEGORY_TRANSPORT = "Transport";
    private static final String CATEGORY_SHOPPING = "Shopping";
    private static final String CATEGORY_BILLS = "Bills";
    private static final String CATEGORY_OTHER = "Other";

    // List of Room expense entities displayed in the RecyclerView
    private final ArrayList<ExpenseEntity> expenseList;

    // Listeners used to send delete and edit actions back to the activity
    private final OnDeleteClickListener deleteClickListener;
    private final OnEditClickListener editClickListener;

    // Interface for handling delete button clicks
    public interface OnDeleteClickListener {
        void onDeleteClick(ExpenseEntity expense);
    }

    // Interface for handling edit button clicks
    public interface OnEditClickListener {
        void onEditClick(ExpenseEntity expense);
    }

    // Constructor receives the expense list and click listeners
    public ExpenseAdapterRoom(ArrayList<ExpenseEntity> expenseList,
                              OnDeleteClickListener deleteClickListener,
                              OnEditClickListener editClickListener) {
        this.expenseList = expenseList;
        this.deleteClickListener = deleteClickListener;
        this.editClickListener = editClickListener;
    }

    // Creates a new ViewHolder by inflating the expense item layout
    @NonNull
    @Override
    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_expense, parent, false);
        return new ExpenseViewHolder(view);
    }

    // Binds expense data to each item in the RecyclerView
    @Override
    public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {
        ExpenseEntity expense = expenseList.get(position);
        Context context = holder.itemView.getContext();

        // Gets the stored category key and converts it to a localized display name
        String categoryKey = expense.getCategory();
        String categoryDisplayName = getCategoryDisplayName(context, categoryKey);

        // Displays the amount using the selected app currency
        holder.tvItemAmount.setText(
                CurrencyManager.formatAmountString(context, expense.getAmount())
        );

        // Displays the category and date together
        holder.tvItemCategoryDate.setText(categoryDisplayName + " | " + expense.getDate());

        // Shows a default note label if the expense has no note
        if (expense.getNote() == null || expense.getNote().trim().isEmpty()) {
            holder.tvItemNote.setText(getNoNoteText(context));
        } else {
            holder.tvItemNote.setText(expense.getNote());
        }

        // Sets the category badge text and color
        holder.tvCategoryBadge.setText(categoryDisplayName);
        holder.tvCategoryBadge.setBackgroundColor(getCategoryColor(categoryKey));

        // Shows the recurring badge only for recurring expenses
        if (expense.isRecurring()) {
            holder.tvRecurringBadge.setVisibility(View.VISIBLE);
            holder.tvRecurringBadge.setText(
                    context.getString(R.string.repeats_interval, expense.getRecurringInterval())
            );
        } else {
            holder.tvRecurringBadge.setVisibility(View.GONE);
        }

        // Sends delete and edit actions back to the screen using listeners
        holder.btnDelete.setOnClickListener(v -> deleteClickListener.onDeleteClick(expense));
        holder.btnEdit.setOnClickListener(v -> editClickListener.onEditClick(expense));
    }

    // Returns the number of expenses shown in the RecyclerView
    @Override
    public int getItemCount() {
        return expenseList.size();
    }

    // Converts internal category keys into localized display names
    private String getCategoryDisplayName(Context context, String category) {
        if (CATEGORY_FOOD.equals(category)) {
            return context.getString(R.string.food);
        } else if (CATEGORY_ENTERTAINMENT.equals(category)) {
            return context.getString(R.string.entertainment);
        } else if (CATEGORY_TRANSPORT.equals(category)) {
            return context.getString(R.string.transport);
        } else if (CATEGORY_SHOPPING.equals(category)) {
            return context.getString(R.string.shopping);
        } else if (CATEGORY_BILLS.equals(category)) {
            return context.getString(R.string.bills);
        } else {
            return context.getString(R.string.other);
        }
    }

    // Returns the text shown when an expense has no note
    private String getNoNoteText(Context context) {
        return context.getString(R.string.note);
    }

    // Returns a badge color based on the expense category
    private int getCategoryColor(String category) {
        switch (category) {
            case CATEGORY_FOOD:
                return Color.parseColor("#4CAF50");
            case CATEGORY_ENTERTAINMENT:
                return Color.parseColor("#F44336");
            case CATEGORY_TRANSPORT:
                return Color.parseColor("#2196F3");
            case CATEGORY_SHOPPING:
                return Color.parseColor("#9C27B0");
            case CATEGORY_BILLS:
                return Color.parseColor("#FF9800");
            case CATEGORY_OTHER:
            default:
                return Color.parseColor("#607D8B");
        }
    }

    // ViewHolder stores references to the views inside each expense item
    public static class ExpenseViewHolder extends RecyclerView.ViewHolder {
        TextView tvItemAmount, tvItemCategoryDate, tvItemNote, tvCategoryBadge, tvRecurringBadge;
        ImageButton btnDelete;
        Button btnEdit;

        // Connects the item layout views to Java variables
        public ExpenseViewHolder(@NonNull View itemView) {
            super(itemView);
            tvItemAmount = itemView.findViewById(R.id.tvItemAmount);
            tvItemCategoryDate = itemView.findViewById(R.id.tvItemCategoryDate);
            tvItemNote = itemView.findViewById(R.id.tvItemNote);
            tvCategoryBadge = itemView.findViewById(R.id.tvCategoryBadge);
            tvRecurringBadge = itemView.findViewById(R.id.tvRecurringBadge);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnEdit = itemView.findViewById(R.id.btnEdit);
        }
    }
}
