package com.example.expensetracker;

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

    private ArrayList<ExpenseEntity> expenseList;
    private OnDeleteClickListener deleteClickListener;
    private OnEditClickListener editClickListener;

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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_expense, parent, false);
        return new ExpenseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {
        ExpenseEntity expense = expenseList.get(position);
        holder.tvItemAmount.setText("$" + expense.getAmount());
        holder.tvItemCategoryDate.setText(expense.getCategory() + " | " + expense.getDate());

        if (expense.getNote() == null || expense.getNote().trim().isEmpty()) {
            holder.tvItemNote.setText("No note");
        } else {
            holder.tvItemNote.setText(expense.getNote());
        }

        holder.tvCategoryBadge.setText(expense.getCategory());
        holder.tvCategoryBadge.setBackgroundColor(getCategoryColor(expense.getCategory()));

        holder.btnDelete.setOnClickListener(v -> deleteClickListener.onDeleteClick(expense));
        holder.btnEdit.setOnClickListener(v -> editClickListener.onEditClick(expense));
    }

    @Override
    public int getItemCount() {
        return expenseList.size();
    }

    private int getCategoryColor(String category) {
        switch (category) {
            case "Food":
                return Color.parseColor("#4CAF50");
            case "Entertainment":
                return Color.parseColor(("#FF0000"));
            case "Transport":
                return Color.parseColor("#2196F3");
            case "Shopping":
                return Color.parseColor("#9C27B0");
            case "Bills":
                return Color.parseColor("#FF9800");
            default:
                return Color.parseColor("#607D8B");
        }
    }

    public static class ExpenseViewHolder extends RecyclerView.ViewHolder {
        TextView tvItemAmount, tvItemCategoryDate, tvItemNote, tvCategoryBadge;
        ImageButton btnDelete;
        Button btnEdit;

        public ExpenseViewHolder(@NonNull View itemView) {
            super(itemView);
            tvItemAmount = itemView.findViewById(R.id.tvItemAmount);
            tvItemCategoryDate = itemView.findViewById(R.id.tvItemCategoryDate);
            tvItemNote = itemView.findViewById(R.id.tvItemNote);
            tvCategoryBadge = itemView.findViewById(R.id.tvCategoryBadge);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnEdit = itemView.findViewById(R.id.btnEdit);
        }
    }
}