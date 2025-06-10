package com.project.yatrameter;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context; // Needed for attachBaseContext
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class RideDetailActivity extends AppCompatActivity {

    // UI Elements (Same as FareSummaryActivity and activity_ride_detail.xml)
    private TextView totalFareTextView;
    private TextView baseFareBreakdownTextView;
    private TextView distanceChargeTextView;
    private TextView timeChargeTextView;
    private TextView waitingChargeTextView;
    private TextView multiplierAdjustmentTextView;
    private TextView modeTextView;
    private TextView distanceCoveredTextView;
    private TextView totalDurationTextView;
    private TextView waitingTimeTextView;
    private TextView rideDateTimeTextView;

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.00");
    private static final String TAG = "RideDetailActivity"; // For Log messages

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride_detail); // Make sure this points to activity_ride_detail

        // Initialize UI elements by finding them by their IDs
        totalFareTextView = findViewById(R.id.total_fare_text_view);
        baseFareBreakdownTextView = findViewById(R.id.base_fare_breakdown_text_view);
        distanceChargeTextView = findViewById(R.id.distance_charge_text_view);
        timeChargeTextView = findViewById(R.id.time_charge_text_view);
        waitingChargeTextView = findViewById(R.id.waiting_charge_text_view);
        multiplierAdjustmentTextView = findViewById(R.id.multiplier_adjustment_text_view);
        modeTextView = findViewById(R.id.mode_text_view);
        distanceCoveredTextView = findViewById(R.id.distance_covered_text_view);
        totalDurationTextView = findViewById(R.id.total_duration_text_view);
        waitingTimeTextView = findViewById(R.id.waiting_time_text_view);
        rideDateTimeTextView = findViewById(R.id.ride_date_time_text_view);

        // Get the Ride object from the Intent
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("SELECTED_RIDE")) {
            Ride selectedRide = (Ride) intent.getSerializableExtra("SELECTED_RIDE");

            if (selectedRide != null) {
                // Populate UI with data from the Ride object
                totalFareTextView.setText(String.format(Locale.getDefault(), "₹ %.2f", selectedRide.getTotalFare()));
                baseFareBreakdownTextView.setText(String.format(Locale.getDefault(), "₹ %.2f", selectedRide.getBaseFareChargeRaw()));
                // Adjust charges by multiplier for display in breakdown (assuming it's already in totalFare for final total)
                distanceChargeTextView.setText(String.format(Locale.getDefault(), "₹ %.2f", selectedRide.getDistanceChargeRaw() * selectedRide.getFareMultiplierUsed()));
                timeChargeTextView.setText(String.format(Locale.getDefault(), "₹ %.2f", selectedRide.getTimeChargeRaw() * selectedRide.getFareMultiplierUsed()));
                waitingChargeTextView.setText(String.format(Locale.getDefault(), "₹ %.2f", selectedRide.getWaitingChargeRaw() * selectedRide.getFareMultiplierUsed()));
                multiplierAdjustmentTextView.setText(String.format(Locale.getDefault(), "x %.2f", selectedRide.getFareMultiplierUsed()));

                modeTextView.setText(selectedRide.getTransportMode());
                distanceCoveredTextView.setText(DECIMAL_FORMAT.format(selectedRide.getTotalDistanceKm()) + " km");
                totalDurationTextView.setText(formatTime(selectedRide.getTotalRideTimeSeconds()));
                waitingTimeTextView.setText(formatTime(selectedRide.getTotalWaitingDurationSeconds()));

                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                String formattedDateTime = sdf.format(new Date(selectedRide.getRideEndTimeMillis()));
                rideDateTimeTextView.setText(formattedDateTime);

            } else {
                Log.e(TAG, "Error: Selected Ride data is null.");
                Toast.makeText(this, "Error: Ride data not found.", Toast.LENGTH_LONG).show();
                finish(); // Close activity if data is null
            }
        } else {
            Log.e(TAG, "Error: No 'SELECTED_RIDE' extra found in Intent.");
            Toast.makeText(this, "Error: No ride selected.", Toast.LENGTH_LONG).show();
            finish(); // Close activity if no extra
        }
    }

    // Helper method to format seconds into HH:MM:SS
    private String formatTime(long totalSeconds) {
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
    }

    // Override attachBaseContext to apply the selected locale
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.setLocale(newBase, LocaleHelper.getLanguage(newBase)));
    }
}