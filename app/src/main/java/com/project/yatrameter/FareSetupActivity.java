package com.project.yatrameter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;


public class FareSetupActivity extends AppCompatActivity {

    private Spinner transportModeSpinner;
    private TextInputEditText baseFareEditText;
    private TextInputEditText perKmRateEditText;
    private TextInputEditText perMinuteRateEditText;
    private TextInputEditText waitingChargeEditText;
    private MaterialSwitch peakTimeMultiplierSwitch;
    private TextInputLayout multiplierInputLayout;
    private TextInputEditText multiplierEditText;
    private Button startTrackingButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fare_setup);

        transportModeSpinner = findViewById(R.id.transport_mode_spinner);
        baseFareEditText = findViewById(R.id.base_fare_edit_text);
        perKmRateEditText = findViewById(R.id.per_km_rate_edit_text);
        perMinuteRateEditText = findViewById(R.id.per_minute_rate_edit_text);
        waitingChargeEditText = findViewById(R.id.waiting_charge_edit_text);
        peakTimeMultiplierSwitch = findViewById(R.id.peak_time_multiplier_switch);
        multiplierInputLayout = findViewById(R.id.multiplier_input_layout);
        multiplierEditText = findViewById(R.id.multiplier_edit_text);
        startTrackingButton = findViewById(R.id.start_tracking_button);

        // Standard Java way to set up the spinner adapter.
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.transport_modes,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        transportModeSpinner.setAdapter(adapter);

        peakTimeMultiplierSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                multiplierInputLayout.setVisibility(View.VISIBLE);
            } else {
                multiplierInputLayout.setVisibility(View.GONE);
                multiplierEditText.setText("1.0");
            }
        });

        startTrackingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String selectedMode = transportModeSpinner.getSelectedItem().toString();
                double baseFare = parseDouble(baseFareEditText.getText().toString(), 0.0);
                double perKmRate = parseDouble(perKmRateEditText.getText().toString(), 0.0);
                double perMinuteRate = parseDouble(perMinuteRateEditText.getText().toString(), 0.0);
                double waitingCharge = parseDouble(waitingChargeEditText.getText().toString(), 0.0);

                double multiplier = 1.0;
                if (peakTimeMultiplierSwitch.isChecked()) {
                    multiplier = parseDouble(multiplierEditText.getText().toString(), 1.0);
                }

                if (baseFare == 0.0 && perKmRate == 0.0 && perMinuteRate == 0.0 && waitingCharge == 0.0) {
                    Toast.makeText(FareSetupActivity.this, getString(R.string.fare_validation_error), Toast.LENGTH_LONG).show();
                } else if (multiplier == 0.0) {
                    Toast.makeText(FareSetupActivity.this, getString(R.string.multiplier_zero_error), Toast.LENGTH_LONG).show();
                } else {
                    Intent intent = new Intent(FareSetupActivity.this, RideTrackingActivity.class);
                    intent.putExtra("TRANSPORT_MODE", selectedMode);
                    intent.putExtra("BASE_FARE", baseFare);
                    intent.putExtra("PER_KM_RATE", perKmRate);
                    intent.putExtra("PER_MINUTE_RATE", perMinuteRate);
                    intent.putExtra("WAITING_CHARGE", waitingCharge);
                    intent.putExtra("FARE_MULTIPLIER", multiplier);
                    startActivity(intent);
                }
            }
        });
    }

    private double parseDouble(String value, double defaultValue) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    // Override attachBaseContext to apply the selected locale
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.setLocale(newBase, LocaleHelper.getLanguage(newBase)));
    }
}