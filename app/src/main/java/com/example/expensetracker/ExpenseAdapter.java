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

    // List of expenses displayed in the RecyclerView
    private ArrayList<Expense> expenseList;

    // Listener used to handle delete button clicks
    private OnDeleteClickListener deleteClickListener;

    // Interface for sending delete click events back to the activity
    public interface OnDeleteClickListener {
        void onDeleteClick(Expense expense);
    }

    // Constructor receives the expense list and delete click listener
    public ExpenseAdapter(ArrayList<Expense> expenseList, OnDeleteClickListener deleteClickListener) {
        this.expenseList = expenseList;
        this.deleteClickListener = deleteClickListener;
    }

    // Creates a new ViewHolder by inflating the expense item layout
    @NonNull
    @Override
    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_expense, parent, false);
        return new ExpenseViewHolder(view);
    }

    // Binds expense data to the views inside each list item
    @Override
    public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {
        Expense expense = expenseList.get(position);

        // Displays the amount, category/date, and note for this expense
        holder.tvItemAmount.setText("$" + expense.getAmount());
        holder.tvItemCategoryDate.setText(expense.getCategory() + " | " + expense.getDate());
        holder.tvItemNote.setText(expense.getNote());

        // Calls the delete listener when the delete button is tapped
        holder.btnDelete.setOnClickListener(v -> deleteClickListener.onDeleteClick(expense));
    }

    // Returns the number of expenses in the list
    @Override
    public int getItemCount() {
        return expenseList.size();
    }

    // ViewHolder stores references to the views in each expense item
    public static class ExpenseViewHolder extends RecyclerView.ViewHolder {
        TextView tvItemAmount, tvItemCategoryDate, tvItemNote;
        ImageButton btnDelete;

        // Connects item layout views to Java variables
        public ExpenseViewHolder(@NonNull View itemView) {
            super(itemView);
            tvItemAmount = itemView.findViewById(R.id.tvItemAmount);
            tvItemCategoryDate = itemView.findViewById(R.id.tvItemCategoryDate);
            tvItemNote = itemView.findViewById(R.id.tvItemNote);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}