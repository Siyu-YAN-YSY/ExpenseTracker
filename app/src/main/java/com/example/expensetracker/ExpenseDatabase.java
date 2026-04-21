package com.example.expensetracker;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {ExpenseEntity.class}, version = 1, exportSchema = false)
public abstract class ExpenseDatabase extends RoomDatabase {

    public abstract ExpenseDao expenseDao();

    private static volatile ExpenseDatabase INSTANCE;

    public static ExpenseDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (ExpenseDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    ExpenseDatabase.class,
                                    "expense_database"
                            )
                            .allowMainThreadQueries()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}