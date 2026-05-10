package com.example.expensetracker;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
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
import androidx.fragment.app.Fragment;

import com.google.android.material.imageview.ShapeableImageView;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ProfileFragment extends Fragment {

    // Profile image and text views shown on the profile screen
    private ShapeableImageView imgProfile;
    private TextView tvProfileName;
    private TextView tvProfileEmail;
    private TextView tvProfileJoined;
    private TextView tvProfileExpenseCount;
    private TextView tvProfileThisMonth;

    // Handles the result after the user picks a profile image
    private final ActivityResultLauncher<Intent> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == requireActivity().RESULT_OK
                        && result.getData() != null
                        && result.getData().getData() != null) {

                    Uri imageUri = result.getData().getData();

                    try {
                        // Keeps permission to read the selected image later
                        requireContext().getContentResolver().takePersistableUriPermission(
                                imageUri,
                                Intent.FLAG_GRANT_READ_URI_PERMISSION
                        );
                    } catch (Exception ignored) {
                        // Continues even if persistent permission cannot be saved
                    }

                    saveProfileImageUri(imageUri.toString());
                    loadProfileData();
                    Toast.makeText(requireContext(), "Profile photo updated", Toast.LENGTH_SHORT).show();
                }
            });

    // Required empty constructor for the Fragment
    public ProfileFragment() {
    }

    // Inflates the profile fragment layout
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    // Connects views and sets up button click actions
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Connects Java variables to XML views
        imgProfile = view.findViewById(R.id.imgProfile);
        tvProfileName = view.findViewById(R.id.tvProfileName);
        tvProfileEmail = view.findViewById(R.id.tvProfileEmail);
        tvProfileJoined = view.findViewById(R.id.tvProfileJoined);
        tvProfileExpenseCount = view.findViewById(R.id.tvProfileExpenseCount);
        tvProfileThisMonth = view.findViewById(R.id.tvProfileThisMonth);

        // Opens image picker when profile image is tapped
        imgProfile.setOnClickListener(v -> openImagePicker());

        // Opens dialog for editing name and email
        view.findViewById(R.id.btnEditProfile).setOnClickListener(v -> showEditProfileDialog());

        // Locks the app immediately if passcode is enabled
        view.findViewById(R.id.btnLockScreen).setOnClickListener(v -> lockScreenNow());

        // Clears saved profile data and reloads default values
        view.findViewById(R.id.btnLogout).setOnClickListener(v -> {
            requireContext()
                    .getSharedPreferences("profile", requireContext().MODE_PRIVATE)
                    .edit()
                    .clear()
                    .apply();

            loadProfileData();
            Toast.makeText(requireContext(), "Profile reset", Toast.LENGTH_SHORT).show();
        });

        loadProfileData();
    }

    // Reloads profile data when returning to this screen
    @Override
    public void onResume() {
        super.onResume();
        loadProfileData();
    }

    // Loads saved profile details and expense summary data
    private void loadProfileData() {
        if (getContext() == null) return;

        SharedPreferences prefs = getProfilePrefs();
        String savedName = prefs.getString("name", "ET Wallet User");
        String savedEmail = prefs.getString("email", "user@email.com");
        String savedImageUri = prefs.getString("image_uri", null);

        // Displays saved profile information
        tvProfileName.setText(savedName);
        tvProfileEmail.setText(savedEmail);
        tvProfileJoined.setText(getString(R.string.using_et_wallet_since_2025));

        // Loads total expense count from the database
        ExpenseDatabase database = ExpenseDatabase.getDatabase(requireContext());
        List<ExpenseEntity> allExpenses = database.expenseDao().getAllExpenses();
        tvProfileExpenseCount.setText(String.valueOf(allExpenses.size()));

        // Gets the current month and year
        Calendar now = Calendar.getInstance();
        String month = String.format(Locale.US, "%02d", now.get(Calendar.MONTH) + 1);
        String year = String.valueOf(now.get(Calendar.YEAR));

        // Calculates total spending for the current month
        List<ExpenseEntity> currentMonthExpenses = database.expenseDao().getExpensesByMonth(month, year);

        double total = 0;
        for (ExpenseEntity expense : currentMonthExpenses) {
            total += expense.getAmountValue();
        }

        tvProfileThisMonth.setText(CurrencyManager.formatAmount(requireContext(), total));

        // Loads saved profile image or falls back to the default logo
        if (savedImageUri != null && !savedImageUri.isEmpty()) {
            try {
                imgProfile.setImageURI(Uri.parse(savedImageUri));
            } catch (Exception e) {
                imgProfile.setImageResource(R.drawable.logo_wallet);
            }
        } else {
            imgProfile.setImageResource(R.drawable.logo_wallet);
        }
    }

    // Shows a dialog for editing the user's profile name and email
    private void showEditProfileDialog() {
        SharedPreferences prefs = getProfilePrefs();

        EditText etName = new EditText(requireContext());
        etName.setHint("Enter your name");
        etName.setText(prefs.getString("name", "ET Wallet User"));
        etName.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);

        EditText etEmail = new EditText(requireContext());
        etEmail.setHint("Enter your email");
        etEmail.setText(prefs.getString("email", "user@email.com"));
        etEmail.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

        // Places name and email fields in a vertical layout
        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 10);
        layout.addView(etName);
        layout.addView(etEmail);

        new AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.edit_profile))
                .setView(layout)
                .setPositiveButton(getString(R.string.save), (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    String email = etEmail.getText().toString().trim();

                    // Uses default values if fields are left empty
                    if (name.isEmpty()) name = "ET Wallet User";
                    if (email.isEmpty()) email = "user@email.com";

                    // Saves updated profile details
                    prefs.edit()
                            .putString("name", name)
                            .putString("email", email)
                            .apply();

                    loadProfileData();
                    Toast.makeText(requireContext(), "Profile updated", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(getString(R.string.cancel), null)
                .show();
    }

    // Opens Android's document picker for selecting a profile image
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        pickImageLauncher.launch(intent);
    }

    // Saves the selected profile image URI
    private void saveProfileImageUri(String uriString) {
        getProfilePrefs()
                .edit()
                .putString("image_uri", uriString)
                .apply();
    }

    // Returns the SharedPreferences file used for profile data
    private SharedPreferences getProfilePrefs() {
        return requireContext().getSharedPreferences("profile", requireContext().MODE_PRIVATE);
    }

    // Locks the app and opens the passcode screen
    private void lockScreenNow() {
        SharedPreferences settingsPrefs =
                requireContext().getSharedPreferences("settings", requireContext().MODE_PRIVATE);

        boolean passcodeEnabled = settingsPrefs.getBoolean("passcode_enabled", false);

        // Requires passcode setup before manual locking is allowed
        if (!passcodeEnabled) {
            Toast.makeText(requireContext(), getString(R.string.enable_passcode_first), Toast.LENGTH_SHORT).show();
            return;
        }

        // Marks app as locked
        settingsPrefs.edit()
                .putBoolean("passcode_unlocked", false)
                .apply();

        startActivity(new Intent(requireContext(), PasscodeActivity.class));
    }
}
