package com.example.expensetracker;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.imageview.ShapeableImageView;

public class PasscodeActivity extends AppCompatActivity {

    // Stores the digits entered by the user
    private final StringBuilder enteredPasscode = new StringBuilder();

    // Passcode dot indicators and UI elements
    private View dot1;
    private View dot2;
    private View dot3;
    private View dot4;
    private TextView tvPasscodeError;
    private ShapeableImageView imgPasscodeLogo;

    // Applies the selected app language before loading the activity
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleManager.applyLanguage(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passcode);

        // Connects Java variables to XML views
        imgPasscodeLogo = findViewById(R.id.imgPasscodeLogo);
        dot1 = findViewById(R.id.dot1);
        dot2 = findViewById(R.id.dot2);
        dot3 = findViewById(R.id.dot3);
        dot4 = findViewById(R.id.dot4);
        tvPasscodeError = findViewById(R.id.tvPasscodeError);

        // Loads the user's profile image or the default logo
        loadProfileImage();

        // Sets up number buttons for passcode input
        setupNumberButton(R.id.btn0, "0");
        setupNumberButton(R.id.btn1, "1");
        setupNumberButton(R.id.btn2, "2");
        setupNumberButton(R.id.btn3, "3");
        setupNumberButton(R.id.btn4, "4");
        setupNumberButton(R.id.btn5, "5");
        setupNumberButton(R.id.btn6, "6");
        setupNumberButton(R.id.btn7, "7");
        setupNumberButton(R.id.btn8, "8");
        setupNumberButton(R.id.btn9, "9");

        Button btnDelete = findViewById(R.id.btnDelete);
        Button btnClear = findViewById(R.id.btnClear);

        // Sets up delete and clear actions
        btnDelete.setOnClickListener(v -> deleteDigit());
        btnClear.setOnClickListener(v -> clearDigits());

        // Shows the initial empty dot state
        updateDots();
    }

    // Loads the saved profile image, or shows the default wallet logo if unavailable
    private void loadProfileImage() {
        SharedPreferences prefs = getSharedPreferences("profile", MODE_PRIVATE);
        String imageUriString = prefs.getString("image_uri", null);

        if (imageUriString != null && !imageUriString.isEmpty()) {
            try {
                imgPasscodeLogo.setImageURI(Uri.parse(imageUriString));
            } catch (Exception e) {
                imgPasscodeLogo.setImageResource(R.drawable.logo_wallet);
            }
        } else {
            imgPasscodeLogo.setImageResource(R.drawable.logo_wallet);
        }
    }

    // Assigns a digit click action to a number button
    private void setupNumberButton(int buttonId, String digit) {
        Button button = findViewById(buttonId);
        button.setOnClickListener(v -> addDigit(digit));
    }

    // Adds a digit to the entered passcode
    private void addDigit(String digit) {
        if (enteredPasscode.length() >= 4) {
            return;
        }

        enteredPasscode.append(digit);
        tvPasscodeError.setText("");
        updateDots();

        // Checks the passcode automatically after 4 digits are entered
        if (enteredPasscode.length() == 4) {
            checkPasscode();
        }
    }

    // Deletes the last entered digit
    private void deleteDigit() {
        int length = enteredPasscode.length();

        if (length > 0) {
            enteredPasscode.deleteCharAt(length - 1);
            tvPasscodeError.setText("");
            updateDots();
        }
    }

    // Clears all entered digits
    private void clearDigits() {
        enteredPasscode.setLength(0);
        tvPasscodeError.setText("");
        updateDots();
    }

    // Updates the four dot indicators based on how many digits were entered
    private void updateDots() {
        setDotState(dot1, enteredPasscode.length() >= 1);
        setDotState(dot2, enteredPasscode.length() >= 2);
        setDotState(dot3, enteredPasscode.length() >= 3);
        setDotState(dot4, enteredPasscode.length() >= 4);
    }

    // Changes one dot between filled and empty
    private void setDotState(View dot, boolean filled) {
        dot.setBackgroundResource(
                filled ? R.drawable.bg_passcode_dot_filled : R.drawable.bg_passcode_dot_empty
        );
    }

    // Compares the entered passcode with the saved passcode
    private void checkPasscode() {
        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        String savedPasscode = prefs.getString("passcode_value", "");

        if (enteredPasscode.toString().equals(savedPasscode)) {
            // Marks the app as unlocked and closes the passcode screen
            prefs.edit().putBoolean("passcode_unlocked", true).apply();
            finish();
        } else {
            // Shows an error and clears the entered digits
            tvPasscodeError.setText(getString(R.string.incorrect_passcode));
            Toast.makeText(this, getString(R.string.incorrect_passcode), Toast.LENGTH_SHORT).show();
            clearDigits();
        }
    }

    // Sends the app to the background instead of allowing back navigation
    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }
}