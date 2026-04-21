package com.example.expensetracker;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder> {

    private ArrayList<Expense> expenseList;
    private OnDeleteClickListener deleteClickListener;

    public interface OnDeleteClickListener {
        void onDeleteClick(Expense expense);
    }

    public ExpenseAdapter(ArrayList<Expense> expenseList, OnDeleteClickListener deleteClickListener) {
        this.expenseList = expenseList;
        this.deleteClickListener = deleteClickListener;
    }

    @NonNull
    @Override
    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_expense, parent, false);
        return new ExpenseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {
        Expense expense = expenseList.get(position);
        holder.tvItemAmount.setText("$" + expense.getAmount());
        holder.tvItemCategoryDate.setText(expense.getCategory() + " | " + expense.getDate());
        holder.tvItemNote.setText(expense.getNote());

        holder.btnDelete.setOnClickListener(v -> deleteClickListener.onDeleteClick(expense));
    }

    @Override
    public int getItemCount() {
        return expenseList.size();
    }

    public static class ExpenseViewHolder extends RecyclerView.ViewHolder {
        TextView tvItemAmount, tvItemCategoryDate, tvItemNote;
        ImageButton btnDelete;

        public ExpenseViewHolder(@NonNull View itemView) {
            super(itemView);
            tvItemAmount = itemView.findViewById(R.id.tvItemAmount);
            tvItemCategoryDate = itemView.findViewById(R.id.tvItemCategoryDate);
            tvItemNote = itemView.findViewById(R.id.tvItemNote);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}