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

    private static final String CATEGORY_FOOD = "Food";
    private static final String CATEGORY_ENTERTAINMENT = "Entertainment";
    private static final String CATEGORY_TRANSPORT = "Transport";
    private static final String CATEGORY_SHOPPING = "Shopping";
    private static final String CATEGORY_BILLS = "Bills";
    private static final String CATEGORY_OTHER = "Other";

    private final ArrayList<ExpenseEntity> expenseList;
    private final OnDeleteClickListener deleteClickListener;
    private final OnEditClickListener editClickListener;

    public interface OnDeleteClickListener {
        void onDeleteClick(ExpenseEntity expense);
    }

    public interface OnEditClickListener {
        void onEditClick(ExpenseEntity expense);
    }

    public ExpenseAdapterRoom(ArrayList<ExpenseEntity> expenseList,
                              OnDeleteClickListener deleteClickListener,
                              OnEditClickListener editClickListener) {
        this.expenseList = expenseList;
        this.deleteClickListener = deleteClickListener;
        this.editClickListener = editClickListener;
    }

    @NonNull
    @Override
    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_expense, parent, false);
        return new ExpenseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {
        ExpenseEntity expense = expenseList.get(position);
        Context context = holder.itemView.getContext();

        String categoryKey = expense.getCategory();
        String categoryDisplayName = getCategoryDisplayName(context, categoryKey);

        holder.tvItemAmount.setText(
                CurrencyManager.formatAmountString(context, expense.getAmount())
        );

        holder.tvItemCategoryDate.setText(categoryDisplayName + " | " + expense.getDate());

        if (expense.getNote() == null || expense.getNote().trim().isEmpty()) {
            holder.tvItemNote.setText(getNoNoteText(context));
        } else {
            holder.tvItemNote.setText(expense.getNote());
        }

        holder.tvCategoryBadge.setText(categoryDisplayName);
        holder.tvCategoryBadge.setBackgroundColor(getCategoryColor(categoryKey));

        if (expense.isRecurring()) {
            holder.tvRecurringBadge.setVisibility(View.VISIBLE);
            holder.tvRecurringBadge.setText(
                    context.getString(R.string.repeats_interval, expense.getRecurringInterval())
            );
        } else {
            holder.tvRecurringBadge.setVisibility(View.GONE);
        }

        holder.btnDelete.setOnClickListener(v -> deleteClickListener.onDeleteClick(expense));
        holder.btnEdit.setOnClickListener(v -> editClickListener.onEditClick(expense));
    }

    @Override
    public int getItemCount() {
        return expenseList.size();
    }

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

    private String getNoNoteText(Context context) {
        return context.getString(R.string.note);
    }

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

    public static class ExpenseViewHolder extends RecyclerView.ViewHolder {
        TextView tvItemAmount, tvItemCategoryDate, tvItemNote, tvCategoryBadge, tvRecurringBadge;
        ImageButton btnDelete;
        Button btnEdit;

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
