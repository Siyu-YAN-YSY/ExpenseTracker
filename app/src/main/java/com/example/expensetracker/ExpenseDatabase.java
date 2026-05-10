package com.example.expensetracker;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

// Defines the Room database and lists the entities it stores
@Database(entities = {ExpenseEntity.class}, version = 2, exportSchema = false)
public abstract class ExpenseDatabase extends RoomDatabase {

    // Provides access to expense database operations
    public abstract ExpenseDao expenseDao();

    // Singleton instance of the database
    private static volatile ExpenseDatabase INSTANCE;

    // Migration from database version 1 to version 2
    private static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {

            // Adds a column to track whether an expense is recurring
            database.execSQL("ALTER TABLE expenses ADD COLUMN is_recurring INTEGER NOT NULL DEFAULT 0");

            // Adds a column to store the recurring interval
            database.execSQL("ALTER TABLE expenses ADD COLUMN recurring_interval TEXT DEFAULT 'None'");
        }
    };

    // Returns the single shared database instance
    public static ExpenseDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (ExpenseDatabase.class) {

                // Creates the database only once
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    ExpenseDatabase.class,
                                    "expense_database"
                            )
                            // Allows database queries on the main thread
                            .allowMainThreadQueries()

                            // Applies migration when upgrading from version 1 to 2
                            .addMigrations(MIGRATION_1_2)
                            .build();
                }
            }
        }

        return INSTANCE;
    }
}