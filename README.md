# Expense Tracker App

## 📱 Overview

This is a simple Android application that helps users track and manage their daily expenses.

The app allows users to add, edit, delete, and view expenses in a clean and organized interface.

---

## ✨ Features

### Core Features

* Add expense (amount, category, date, note)
* Edit existing expense
* Delete expense
* View all expenses in a list
* Total expense calculation

### Advanced Features

* Category filter (Food, Transport, Shopping, Bills, Other)
* Date picker for selecting dates
* Summary page showing total spending by category
* Local data storage using Room Database
* Input validation (no empty or invalid values)
* Empty state UI ("No expenses yet")

---

## 🛠️ Technologies Used

* Java
* Android Studio
* XML (UI Design)
* RecyclerView
* Room Database
* Material Design Components

---

## 📂 Project Structure

* MainActivity → Home screen (list + total + filter)
* AddExpenseActivity → Add/Edit expense
* SummaryActivity → Category summary
* ExpenseEntity → Database model
* ExpenseDao → Database operations
* ExpenseDatabase → Room database
* ExpenseAdapterRoom → RecyclerView adapter

---

## 🚀 How to Run

1. Open the project in Android Studio
2. Sync Gradle
3. Run the app on an emulator or Android device

---

## 📊 Demo Flow

1. Add a new expense
2. View the expense list
3. Edit or delete an expense
4. Filter by category
5. View summary page
6. Data persists after restarting the app

---

## 📌 Notes

* The app uses Room Database, so data is saved locally
* The UI is designed to be simple and user-friendly
* This project was developed for a CS175 Android course

