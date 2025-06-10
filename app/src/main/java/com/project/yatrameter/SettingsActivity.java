package com.project.yatrameter;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceManager;

import android.app.AlertDialog;
import android.content.Context; // Needed for attachBaseContext
import android.content.DialogInterface; // For AlertDialog
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType; // NEW: For EditText input type
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText; // NEW: For EditText in PIN dialog
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Locale;

public class SettingsActivity extends AppCompatActivity {

    // UI Elements for Default Fare Presets
    private TextInputEditText defaultBaseFareEditText;
    private TextInputEditText defaultPerKmRateEditText;
    private TextInputEditText defaultPerMinuteRateEditText;
    private TextInputEditText defaultWaitingChargeEditText;
    private TextInputEditText defaultMultiplierEditText;
    private Button saveDefaultFareButton;

    // UI Elements for Unit Preferences
    private RadioGroup unitRadioGroup;
    private RadioButton unitKmRadioButton;
    private RadioButton unitMiRadioButton;

    // UI Elements for Theme Selection
    private MaterialSwitch darkThemeSwitch;

    // UI Elements for Fare Lock PIN
    private MaterialSwitch fareLockPinSwitch;
    private TextInputLayout fareLockPinInputLayout;
    private TextInputEditText fareLockPinEditText;
    private Button savePinButton;

    // UI Element for Reset All Settings Button
    private Button resetAllSettingsButton;

    // UI Element for View Fare Charts Button
    private Button viewFareChartsButton;

    // UI Element for Change Language Button
    private Button changeLanguageSettingsButton;

    // SharedPreferences objects for persistent storage
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    // Tag for Log messages
    private static final String TAG = "SettingsActivity";

    // Constants for SharedPreferences keys
    private static final String KEY_DEFAULT_BASE_FARE = "default_base_fare";
    private static final String KEY_DEFAULT_PER_KM_RATE = "default_per_km_rate";
    private static final String KEY_DEFAULT_PER_MINUTE_RATE = "default_per_minute_rate";
    private static final String KEY_DEFAULT_WAITING_CHARGE = "default_waiting_charge";
    private static final String KEY_DEFAULT_MULTIPLIER = "default_multiplier";
    private static final String KEY_UNIT_PREFERENCE = "unit_preference"; // Stores "km" or "mi"
    private static final String KEY_DARK_THEME_ENABLED = "dark_theme_enabled";
    private static final String KEY_FARE_LOCK_PIN_ENABLED = "fare_lock_pin_enabled";
    private static final String KEY_FARE_LOCK_PIN_VALUE = "fare_lock_pin_value";
    private static final String KEY_SELECTED_LANGUAGE = "selected_language";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Initialize UI elements by finding them by their IDs
        defaultBaseFareEditText = findViewById(R.id.default_base_fare_edit_text);
        defaultPerKmRateEditText = findViewById(R.id.default_per_km_rate_edit_text);
        defaultPerMinuteRateEditText = findViewById(R.id.default_per_minute_rate_edit_text);
        defaultWaitingChargeEditText = findViewById(R.id.default_waiting_charge_edit_text);
        defaultMultiplierEditText = findViewById(R.id.default_multiplier_edit_text);
        saveDefaultFareButton = findViewById(R.id.save_default_fare_button);

        unitRadioGroup = findViewById(R.id.unit_radio_group);
        unitKmRadioButton = findViewById(R.id.unit_km_radio_button);
        unitMiRadioButton = findViewById(R.id.unit_mi_radio_button);

        darkThemeSwitch = findViewById(R.id.dark_theme_switch);

        fareLockPinSwitch = findViewById(R.id.fare_lock_pin_switch);
        fareLockPinInputLayout = findViewById(R.id.fare_lock_pin_input_layout);
        fareLockPinEditText = findViewById(R.id.fare_lock_pin_edit_text);
        savePinButton = findViewById(R.id.save_pin_button);

        resetAllSettingsButton = findViewById(R.id.reset_all_settings_button);

        viewFareChartsButton = findViewById(R.id.view_fare_charts_button);
        changeLanguageSettingsButton = findViewById(R.id.change_language_settings_button);

        // Initialize SharedPreferences
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor = sharedPreferences.edit();

        // --- Load existing settings and populate UI ---
        loadSettings();

        // --- Set up Listeners for saving settings ---

        // Listener for Save Default Fare Button
        saveDefaultFareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Apply PIN verification before saving default fare presets
                if (sharedPreferences.getBoolean(KEY_FARE_LOCK_PIN_ENABLED, false)) {
                    verifyPin(new Runnable() {
                        @Override
                        public void run() {
                            saveDefaultFarePresets();
                        }
                    });
                } else {
                    saveDefaultFarePresets();
                }
            }
        });

        // Listener for Unit Radio Group changes
        unitRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                saveUnitPreference(checkedId);
            }
        });

        // Listener for Dark Theme Switch changes
        darkThemeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveThemePreference(isChecked);
        });

        // Listener for Fare Lock PIN Switch changes
        fareLockPinSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            togglePinInputVisibility(isChecked);
            if (!isChecked) { // If PIN is disabled, ensure PIN is cleared from preferences (optional)
                editor.remove(KEY_FARE_LOCK_PIN_VALUE).apply();
                Toast.makeText(SettingsActivity.this, "Fare Lock PIN disabled.", Toast.LENGTH_SHORT).show();
            } else {
                // Prompt user to set a new PIN if enabled
                Toast.makeText(SettingsActivity.this, "Please set a new PIN.", Toast.LENGTH_LONG).show();
            }
        });

        // Listener for Save PIN Button
        savePinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveFareLockPin();
            }
        });

        // Listener for Reset All Settings Button
        resetAllSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Apply PIN verification before resetting all settings
                if (sharedPreferences.getBoolean(KEY_FARE_LOCK_PIN_ENABLED, false)) {
                    verifyPin(new Runnable() {
                        @Override
                        public void run() {
                            resetAllSettings();
                        }
                    });
                } else {
                    resetAllSettings();
                }
            }
        });

        // Listener for View Fare Charts Button
        viewFareChartsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Apply PIN verification before viewing fare charts
                if (sharedPreferences.getBoolean(KEY_FARE_LOCK_PIN_ENABLED, false)) {
                    verifyPin(new Runnable() {
                        @Override
                        public void run() {
                            Intent intent = new Intent(SettingsActivity.this, FareChartsActivity.class);
                            startActivity(intent);
                        }
                    });
                } else {
                    Intent intent = new Intent(SettingsActivity.this, FareChartsActivity.class);
                    startActivity(intent);
                }
            }
        });

        // Listener for Change Language Button
        changeLanguageSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLanguageSelectionDialog();
            }
        });
    }

    // --- Methods for Loading and Saving Settings ---

    private void loadSettings() {
        // Load Default Fare Presets
        defaultBaseFareEditText.setText(String.format(Locale.getDefault(), "%.1f", sharedPreferences.getFloat(KEY_DEFAULT_BASE_FARE, 50.0f)));
        defaultPerKmRateEditText.setText(String.format(Locale.getDefault(), "%.1f", sharedPreferences.getFloat(KEY_DEFAULT_PER_KM_RATE, 10.0f)));
        defaultPerMinuteRateEditText.setText(String.format(Locale.getDefault(), "%.1f", sharedPreferences.getFloat(KEY_DEFAULT_PER_MINUTE_RATE, 1.0f)));
        defaultWaitingChargeEditText.setText(String.format(Locale.getDefault(), "%.1f", sharedPreferences.getFloat(KEY_DEFAULT_WAITING_CHARGE, 0.5f)));
        defaultMultiplierEditText.setText(String.format(Locale.getDefault(), "%.1f", sharedPreferences.getFloat(KEY_DEFAULT_MULTIPLIER, 1.0f)));

        // Load Unit Preference
        String unitPref = sharedPreferences.getString(KEY_UNIT_PREFERENCE, "km");
        if (unitPref != null && unitPref.equals("km")) {
            unitKmRadioButton.setChecked(true);
        } else if (unitPref != null && unitPref.equals("mi")) {
            unitMiRadioButton.setChecked(true);
        } else {
            unitKmRadioButton.setChecked(true); // Default if no preference or unexpected value
        }

        // Load Theme Preference
        boolean isDarkThemeEnabled = sharedPreferences.getBoolean(KEY_DARK_THEME_ENABLED, false);
        darkThemeSwitch.setChecked(isDarkThemeEnabled);
        applyTheme(isDarkThemeEnabled); // Apply theme immediately

        // Load Fare Lock PIN state
        boolean isPinEnabled = sharedPreferences.getBoolean(KEY_FARE_LOCK_PIN_ENABLED, false);
        fareLockPinSwitch.setChecked(isPinEnabled);
        togglePinInputVisibility(isPinEnabled); // Show/hide PIN input based on saved state
    }

    private void saveDefaultFarePresets() {
        try {
            editor.putFloat(KEY_DEFAULT_BASE_FARE, parseFloat(defaultBaseFareEditText.getText().toString(), 50.0f));
            editor.putFloat(KEY_DEFAULT_PER_KM_RATE, parseFloat(defaultPerKmRateEditText.getText().toString(), 10.0f));
            editor.putFloat(KEY_DEFAULT_PER_MINUTE_RATE, parseFloat(defaultPerMinuteRateEditText.getText().toString(), 1.0f));
            editor.putFloat(KEY_DEFAULT_WAITING_CHARGE, parseFloat(defaultWaitingChargeEditText.getText().toString(), 0.5f));
            editor.putFloat(KEY_DEFAULT_MULTIPLIER, parseFloat(defaultMultiplierEditText.getText().toString(), 1.0f));
            editor.apply(); // Apply changes asynchronously
            Toast.makeText(this, R.string.default_fare_saved_toast, Toast.LENGTH_SHORT).show();
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid number format in fare presets.", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "NumberFormatException in saveDefaultFarePresets: " + e.getMessage());
        }
    }

    private void saveUnitPreference(int checkedId) {
        String unit = "km";
        if (checkedId == R.id.unit_mi_radio_button) {
            unit = "mi";
        }
        editor.putString(KEY_UNIT_PREFERENCE, unit).apply();
        Toast.makeText(this, "Unit preference saved to: " + unit, Toast.LENGTH_SHORT).show();
    }

    private void saveThemePreference(boolean isChecked) {
        editor.putBoolean(KEY_DARK_THEME_ENABLED, isChecked).apply();
        applyTheme(isChecked);
        Toast.makeText(this, "Theme preference saved.", Toast.LENGTH_SHORT).show();
    }

    private void applyTheme(boolean isDarkThemeEnabled) {
        if (isDarkThemeEnabled) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        // For theme changes to apply fully across the app, you often need to recreate activities
        // or set the theme in your Application class. 'recreate()' causes a quick restart.
        // recreate();
    }

    private void togglePinInputVisibility(boolean isChecked) {
        if (isChecked) {
            fareLockPinInputLayout.setVisibility(View.VISIBLE);
            savePinButton.setVisibility(View.VISIBLE);
            // Optionally load existing PIN if any and display it
            String savedPin = sharedPreferences.getString(KEY_FARE_LOCK_PIN_VALUE, "");
            fareLockPinEditText.setText(savedPin);
        } else {
            fareLockPinInputLayout.setVisibility(View.GONE);
            savePinButton.setVisibility(View.GONE);
        }
        editor.putBoolean(KEY_FARE_LOCK_PIN_ENABLED, isChecked).apply();
    }

    private void saveFareLockPin() {
        String pin = fareLockPinEditText.getText().toString();
        if (pin.isEmpty()) {
            fareLockPinEditText.setError("PIN cannot be empty!");
            return;
        }
        // Basic PIN validation (e.g., minimum length)
        if (pin.length() < 4) {
            fareLockPinEditText.setError("PIN must be at least 4 digits.");
            return;
        }

        editor.putString(KEY_FARE_LOCK_PIN_VALUE, pin).apply();
        Toast.makeText(this, R.string.pin_saved_toast, Toast.LENGTH_SHORT).show();
    }

    private void resetAllSettings() {
        // Clear all SharedPreferences
        editor.clear().apply();
        // Reload default values to the UI
        loadSettings(); // This will load the hardcoded defaults
        Toast.makeText(this, R.string.settings_reset_toast, Toast.LENGTH_SHORT).show();
        // Also reset theme
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        // recreate(); // Recreate to apply theme reset
    }

    // Helper to safely parse float
    private float parseFloat(String value, float defaultValue) {
        try {
            return Float.parseFloat(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    // Method: Show Language Selection Dialog
    private void showLanguageSelectionDialog() {
        final String[] languages = {getString(R.string.language_english), getString(R.string.language_hindi)};
        final String[] languageCodes = {"en", "hi"}; // Corresponding language codes

        // Determine currently selected language index
        String currentLangCode = sharedPreferences.getString(KEY_SELECTED_LANGUAGE, "en");
        int checkedItem = 0; // Default to English
        if (currentLangCode != null && currentLangCode.equals("hi")) {
            checkedItem = 1;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.select_language_title);
        builder.setSingleChoiceItems(languages, checkedItem, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Language selected
                String selectedLangCode = languageCodes[which];
                editor.putString(KEY_SELECTED_LANGUAGE, selectedLangCode).apply(); // Save selected language

                dialog.dismiss(); // Close dialog

                Toast.makeText(SettingsActivity.this, R.string.language_changed_toast, Toast.LENGTH_LONG).show();

                // Recreate this activity to apply the language change immediately
                recreate();
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() { // Use android.R.string.cancel for generic cancel button
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss(); // Just close the dialog if canceled
                Toast.makeText(SettingsActivity.this, R.string.language_not_changed_toast, Toast.LENGTH_SHORT).show();
            }
        });
        builder.create().show();
    }

    // NEW Method: PIN verification dialog
    private void verifyPin(final Runnable onSuccessAction) {
        String savedPin = sharedPreferences.getString(KEY_FARE_LOCK_PIN_VALUE, "");

        // If PIN is enabled but no PIN is actually set, allow access
        if (savedPin == null || savedPin.isEmpty()) {
            onSuccessAction.run();
            return;
        }

        // Show an AlertDialog to get the PIN from the user
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.enter_pin);

        // Set up the input field
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD); // Numeric password
        builder.setView(input);

        // Set up the OK and Cancel buttons
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String enteredPin = input.getText().toString();
                if (enteredPin.equals(savedPin)) {
                    // Correct PIN, proceed with the action
                    onSuccessAction.run();
                } else {
                    // Incorrect PIN
                    Toast.makeText(SettingsActivity.this, R.string.incorrect_pin, Toast.LENGTH_SHORT).show();
                    // Optionally, you could also close the settings activity or prevent further interaction
                }
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel(); // Dismiss the dialog
                Toast.makeText(SettingsActivity.this, R.string.cancel, Toast.LENGTH_SHORT).show(); // Inform user it was cancelled
            }
        });

        builder.show();
    }

    // Override attachBaseContext to apply the selected locale
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.setLocale(newBase, LocaleHelper.getLanguage(newBase)));
    }
}