package com.example.expensetracker;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.Locale;

public class SettingsFragment extends Fragment {

    private BudgetManager budgetManager;
    private ExpenseRepository expenseRepository;
    private CsvExportManager csvExportManager;

    private TextView tvMonthlyBudget;
    private TextView tvCurrency;
    private TextView tvLanguage;
    private TextView tvPasscode;
    private TextView tvChangePasswordSetting;
    private TextView tvAppVersion;

    private SwitchMaterial switchDarkMode;
    private SwitchMaterial switchBudgetAlerts;
    private SwitchMaterial switchMonthlyReminder;
    private SwitchMaterial switchPasscode;

    private final ActivityResultLauncher<Intent> exportCsvLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        csvExportManager.exportToCsv(uri);
                        Toast.makeText(requireContext(), "Data exported successfully", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    public SettingsFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeViews(view);
        refreshSettingsData();
        setupListeners(view);
    }

    @Override
    public void onAttach(@NonNull android.content.Context context) {
        super.onAttach(context);
        initializeHelpers(context);
    }

    private void initializeHelpers(android.content.Context context) {
        ExpenseDatabase database = ExpenseDatabase.getDatabase(context);
        expenseRepository = new ExpenseRepository(database);
        budgetManager = new BudgetManager(context);
        csvExportManager = new CsvExportManager(context, expenseRepository);
    }

    private void initializeViews(View view) {
        tvMonthlyBudget = view.findViewById(R.id.tvMonthlyBudget);
        tvCurrency = view.findViewById(R.id.tvCurrency);
        tvLanguage = view.findViewById(R.id.tvLanguage);
        tvPasscode = view.findViewById(R.id.tvPasscode);
        tvChangePasswordSetting = view.findViewById(R.id.tvChangePasswordSetting);
        tvAppVersion = view.findViewById(R.id.tvAppVersion);

        switchDarkMode = view.findViewById(R.id.switchDarkMode);
        switchBudgetAlerts = view.findViewById(R.id.switchBudgetAlerts);
        switchMonthlyReminder = view.findViewById(R.id.switchMonthlyReminder);
        switchPasscode = view.findViewById(R.id.switchPasscode);
    }

    public void refreshSettingsData() {
        if (budgetManager == null || tvMonthlyBudget == null || tvCurrency == null ||
                tvLanguage == null || tvPasscode == null || tvAppVersion == null ||
                switchPasscode == null || switchDarkMode == null ||
                switchBudgetAlerts == null || switchMonthlyReminder == null) {
            return;
        }

        float budget = budgetManager.getBudget();
        if (budget > 0) {
            tvMonthlyBudget.setText(getString(R.string.monthly_budget) + ": "
                    + CurrencyManager.formatAmount(requireContext(), budget));
        } else {
            tvMonthlyBudget.setText(getString(R.string.budget_not_set));
        }

        String currency = getPrefs().getString("currency", "USD");
        tvCurrency.setText(getString(R.string.currency) + ": " + currency);

        String languageCode = getPrefs().getString("language", "en");
        tvLanguage.setText(getString(R.string.language) + ": "
                + LocaleManager.getLanguageDisplayName(languageCode));

        boolean passcodeEnabled = getPrefs().getBoolean("passcode_enabled", false);
        switchPasscode.setChecked(passcodeEnabled);
        tvPasscode.setText(passcodeEnabled
                ? getString(R.string.passcode_on)
                : getString(R.string.passcode_off));

        updateChangePasswordState(passcodeEnabled);

        tvAppVersion.setText(getString(R.string.app_version_1_0));

        switchDarkMode.setChecked(getPrefs().getBoolean("dark_mode", false));
        switchBudgetAlerts.setChecked(getPrefs().getBoolean("budget_alerts", true));
        switchMonthlyReminder.setChecked(getPrefs().getBoolean("monthly_reminder", false));
    }

    private void setupListeners(View view) {
        tvMonthlyBudget.setOnClickListener(v -> showBudgetDialog());
        tvCurrency.setOnClickListener(v -> showCurrencyDialog());
        tvLanguage.setOnClickListener(v -> showLanguageDialog());

        tvPasscode.setOnClickListener(v -> {
            if (switchPasscode.isChecked()) {
                showSetPasscodeDialog(true);
            } else {
                showSetPasscodeDialog(false);
            }
        });

        tvChangePasswordSetting.setOnClickListener(v -> {
            if (!getPrefs().getBoolean("passcode_enabled", false)) {
                Toast.makeText(requireContext(), getString(R.string.enable_passcode_first), Toast.LENGTH_SHORT).show();
                return;
            }
            showChangePasswordDialog();
        });

        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            getPrefs().edit().putBoolean("dark_mode", isChecked).apply();

            AppCompatDelegate.setDefaultNightMode(
                    isChecked
                            ? AppCompatDelegate.MODE_NIGHT_YES
                            : AppCompatDelegate.MODE_NIGHT_NO
            );
        });

        switchBudgetAlerts.setOnCheckedChangeListener((buttonView, isChecked) ->
                getPrefs().edit().putBoolean("budget_alerts", isChecked).apply()
        );

        switchMonthlyReminder.setOnCheckedChangeListener((buttonView, isChecked) ->
                getPrefs().edit().putBoolean("monthly_reminder", isChecked).apply()
        );

        switchPasscode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isPressed()) {
                if (isChecked) {
                    showSetPasscodeDialog(false);
                } else {
                    showDisablePasscodeDialog();
                }
            }
        });

        view.findViewById(R.id.btnExportData).setOnClickListener(v -> exportData());
        view.findViewById(R.id.btnClearData).setOnClickListener(v -> showClearDataDialog());
    }

    private void updateChangePasswordState(boolean enabled) {
        tvChangePasswordSetting.setEnabled(enabled);
        tvChangePasswordSetting.setClickable(enabled);
        tvChangePasswordSetting.setAlpha(enabled ? 1f : 0.5f);
    }

    private void showBudgetDialog() {
        EditText input = new EditText(requireContext());
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.setHint("Enter monthly budget");

        float currentBudget = budgetManager.getBudget();
        if (currentBudget > 0) {
            input.setText(String.format(Locale.US, "%.2f", currentBudget));
        }

        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.monthly_budget)
                .setView(input)
                .setPositiveButton(R.string.save, (dialog, which) -> {
                    String value = input.getText() == null ? "" : input.getText().toString().trim();
                    if (!value.isEmpty()) {
                        try {
                            float budget = Float.parseFloat(value);
                            budgetManager.saveBudget(budget);
                            tvMonthlyBudget.setText(String.format(
                                    getString(R.string.monthly_budget_s),
                                    CurrencyManager.formatAmount(requireContext(), budget)
                            ));
                        } catch (NumberFormatException e) {
                            Toast.makeText(requireContext(), R.string.invalid_budget, Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void showCurrencyDialog() {
        String[] currencies = {"USD", "EUR", "GBP", "CAD", "AUD", "CNY", "KHR"};

        new AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.choose_currency))
                .setItems(currencies, (dialog, which) -> {
                    String selectedCurrency = currencies[which];
                    getPrefs().edit().putString("currency", selectedCurrency).apply();
                    tvCurrency.setText(getString(R.string.currency) + ": " + selectedCurrency);
                    requireActivity().recreate();
                })
                .show();
    }

    private void showLanguageDialog() {
        String[] labels = {
                getString(R.string.english),
                getString(R.string.chinese)
        };
        String[] codes = {"en", "zh"};

        new AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.choose_language))
                .setItems(labels, (dialog, which) -> {
                    String selectedCode = codes[which];
                    getPrefs().edit().putString("language", selectedCode).apply();
                    LocaleManager.saveLanguage(requireContext(), selectedCode);
                    tvLanguage.setText(getString(R.string.language) + ": " + labels[which]);
                    requireActivity().recreate();
                })
                .show();
    }

    private void showSetPasscodeDialog(boolean changingExisting) {
        EditText input = new EditText(requireContext());
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        input.setHint(getString(R.string.enter_4_digit_passcode));
        input.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});

        new AlertDialog.Builder(requireContext())
                .setTitle(changingExisting ? R.string.change_passcode : R.string.set_passcode)
                .setView(input)
                .setPositiveButton(R.string.save, (dialog, which) -> {
                    String passcode = input.getText() == null ? "" : input.getText().toString().trim();

                    if (!passcode.matches("\\d{4}")) {
                        Toast.makeText(requireContext(), R.string.passcode_must_be_4_digits, Toast.LENGTH_SHORT).show();
                        switchPasscode.setChecked(getPrefs().getBoolean("passcode_enabled", false));
                        updateChangePasswordState(getPrefs().getBoolean("passcode_enabled", false));
                        return;
                    }

                    getPrefs().edit()
                            .putBoolean("passcode_enabled", true)
                            .putString("passcode_value", passcode)
                            .putBoolean("passcode_unlocked", false)
                            .apply();

                    switchPasscode.setChecked(true);
                    tvPasscode.setText(getString(R.string.passcode_on));
                    updateChangePasswordState(true);
                    Toast.makeText(requireContext(), R.string.passcode_saved, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> {
                    boolean enabled = getPrefs().getBoolean("passcode_enabled", false);
                    switchPasscode.setChecked(enabled);
                    updateChangePasswordState(enabled);
                })
                .show();
    }

    private void showDisablePasscodeDialog() {
        EditText input = new EditText(requireContext());
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        input.setHint(getString(R.string.enter_current_passcode));
        input.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});

        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.turn_off_passcode)
                .setView(input)
                .setPositiveButton(R.string.confirm, (dialog, which) -> {
                    String enteredPasscode = input.getText() == null
                            ? ""
                            : input.getText().toString().trim();

                    String savedPasscode = getPrefs().getString("passcode_value", "");

                    if (!enteredPasscode.equals(savedPasscode)) {
                        switchPasscode.setChecked(true);
                        updateChangePasswordState(true);
                        Toast.makeText(requireContext(), R.string.incorrect_passcode, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    getPrefs().edit()
                            .putBoolean("passcode_enabled", false)
                            .remove("passcode_value")
                            .remove("passcode_unlocked")
                            .apply();

                    switchPasscode.setChecked(false);
                    tvPasscode.setText(getString(R.string.passcode_off));
                    updateChangePasswordState(false);
                    Toast.makeText(requireContext(), R.string.passcode_removed, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> {
                    switchPasscode.setChecked(true);
                    updateChangePasswordState(true);
                })
                .setOnCancelListener(dialog -> {
                    switchPasscode.setChecked(true);
                    updateChangePasswordState(true);
                })
                .show();
    }

    private void showChangePasswordDialog() {
        SharedPreferences prefs = getPrefs();
        String savedPassword = prefs.getString("app_password", "");

        EditText etCurrentPassword = new EditText(requireContext());
        etCurrentPassword.setHint(getString(R.string.current_password));
        etCurrentPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        EditText etNewPassword = new EditText(requireContext());
        etNewPassword.setHint(getString(R.string.new_password));
        etNewPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        EditText etConfirmPassword = new EditText(requireContext());
        etConfirmPassword.setHint(getString(R.string.confirm_password));
        etConfirmPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 10);

        if (!savedPassword.isEmpty()) {
            layout.addView(etCurrentPassword);
        }
        layout.addView(etNewPassword);
        layout.addView(etConfirmPassword);

        new AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.change_password))
                .setView(layout)
                .setPositiveButton(getString(R.string.save), (dialog, which) -> {
                    String currentPassword = etCurrentPassword.getText() == null
                            ? ""
                            : etCurrentPassword.getText().toString().trim();
                    String newPassword = etNewPassword.getText() == null
                            ? ""
                            : etNewPassword.getText().toString().trim();
                    String confirmPassword = etConfirmPassword.getText() == null
                            ? ""
                            : etConfirmPassword.getText().toString().trim();

                    if (!savedPassword.isEmpty() && !savedPassword.equals(currentPassword)) {
                        Toast.makeText(requireContext(), getString(R.string.current_password_incorrect), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (newPassword.length() < 4) {
                        Toast.makeText(requireContext(), getString(R.string.password_must_be_at_least_4_characters), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (!newPassword.equals(confirmPassword)) {
                        Toast.makeText(requireContext(), getString(R.string.passwords_do_not_match), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    prefs.edit().putString("app_password", newPassword).apply();
                    Toast.makeText(requireContext(), getString(R.string.password_updated), Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(getString(R.string.cancel), null)
                .show();
    }

    private void exportData() {
        if (!csvExportManager.hasExpensesToExport()) {
            Toast.makeText(requireContext(), "No expenses to export", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/csv");
        intent.putExtra(Intent.EXTRA_TITLE, "expense_tracker_export.csv");
        exportCsvLauncher.launch(intent);
    }

    private void showClearDataDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.clear_all_expenses)
                .setMessage(R.string.are_you_sure_you_want_to_delete_all_expense_data_this_cannot_be_undone)
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    expenseRepository.deleteAllExpenses();
                    Toast.makeText(requireContext(), R.string.all_expenses_deleted, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private SharedPreferences getPrefs() {
        return requireContext().getSharedPreferences("settings", requireContext().MODE_PRIVATE);
    }
}