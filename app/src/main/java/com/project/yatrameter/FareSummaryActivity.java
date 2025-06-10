package com.project.yatrameter;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context; // Needed for attachBaseContext
import android.content.Intent; // Needed for Intent.ACTION_SEND for sharing
import android.os.Bundle;
import android.util.Log; // For logging errors
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat; // For formatting numbers
import java.text.SimpleDateFormat; // For formatting dates
import java.util.Date; // For Date object
import java.util.Locale; // For locale-specific formatting
import java.util.concurrent.ExecutorService; // For background threading
import java.util.concurrent.Executors;     // For creating ExecutorService

public class FareSummaryActivity extends AppCompatActivity {

    // UI Elements
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
    private Button saveToHistoryButton;
    private Button shareRideButton;

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.00");
    private ExecutorService databaseWriteExecutor; // Executor for Room database operations

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fare_summary);

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
        saveToHistoryButton = findViewById(R.id.save_to_history_button);
        shareRideButton = findViewById(R.id.share_ride_button);

        // Initialize the ExecutorService for database writes
        databaseWriteExecutor = Executors.newSingleThreadExecutor();

        // Get data from the Intent (passed from RideTrackingActivity)
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            // Retrieve all data passed from RideTrackingActivity. Make them final for use in inner classes.
            final double finalTotalFare = extras.getDouble("TOTAL_FARE", 0.0);
            final double finalTotalDistanceKm = extras.getDouble("TOTAL_DISTANCE_KM", 0.0);
            final long finalTotalRideTimeSeconds = extras.getLong("TOTAL_RIDE_TIME_SECONDS", 0);
            final long finalTotalWaitingDurationSeconds = extras.getLong("TOTAL_WAITING_DURATION_SECONDS", 0);
            final double finalBaseFareRaw = extras.getDouble("BASE_FARE_CHARGE_RAW", 0.0);
            final double finalDistanceChargeRaw = extras.getDouble("DISTANCE_CHARGE_RAW", 0.0);
            final double finalTimeChargeRaw = extras.getDouble("TIME_CHARGE_RAW", 0.0);
            final double finalWaitingChargeRaw = extras.getDouble("WAITING_CHARGE_RAW", 0.0);
            final double finalFareMultiplierUsed = extras.getDouble("FARE_MULTIPLIER_USED", 1.0);
            final String finalTransportModeUsed = extras.getString("TRANSPORT_MODE_USED", "N/A");
            final long finalRideEndTimeMillis = extras.getLong("RIDE_END_TIME_MILLIS", System.currentTimeMillis());

            // Calculate breakdown charges (adjusted by multiplier for display in breakdown)
            double distanceChargeAdjusted = finalDistanceChargeRaw * finalFareMultiplierUsed;
            double timeChargeAdjusted = finalTimeChargeRaw * finalFareMultiplierUsed;
            double waitingChargeAdjusted = finalWaitingChargeRaw * finalFareMultiplierUsed;

            // Update UI elements with retrieved and calculated data
            totalFareTextView.setText(String.format(Locale.getDefault(), "₹ %.2f", finalTotalFare));
            baseFareBreakdownTextView.setText(String.format(Locale.getDefault(), "₹ %.2f", finalBaseFareRaw)); // Base is usually raw
            distanceChargeTextView.setText(String.format(Locale.getDefault(), "₹ %.2f", distanceChargeAdjusted));
            timeChargeTextView.setText(String.format(Locale.getDefault(), "₹ %.2f", timeChargeAdjusted));
            waitingChargeTextView.setText(String.format(Locale.getDefault(), "₹ %.2f", waitingChargeAdjusted));
            multiplierAdjustmentTextView.setText(String.format(Locale.getDefault(), "x %.2f", finalFareMultiplierUsed));

            modeTextView.setText(finalTransportModeUsed);
            distanceCoveredTextView.setText(DECIMAL_FORMAT.format(finalTotalDistanceKm) + " km");
            totalDurationTextView.setText(formatTime(finalTotalRideTimeSeconds));
            waitingTimeTextView.setText(formatTime(finalTotalWaitingDurationSeconds));

            // Format and display the ride end date and time
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            String formattedDateTime = sdf.format(new Date(finalRideEndTimeMillis));
            rideDateTimeTextView.setText(formattedDateTime);

            // Set up button listener for saving ride to database
            saveToHistoryButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Create a new Ride object from the collected data
                    final Ride newRide = new Ride(
                            finalTransportModeUsed,
                            finalTotalFare,
                            finalTotalDistanceKm,
                            finalTotalRideTimeSeconds,
                            finalTotalWaitingDurationSeconds,
                            finalBaseFareRaw,
                            finalDistanceChargeRaw,
                            finalTimeChargeRaw,
                            finalWaitingChargeRaw,
                            finalFareMultiplierUsed,
                            finalRideEndTimeMillis
                    );

                    // Insert the ride into the database on a background thread
                    databaseWriteExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                AppDatabase db = AppDatabase.getDatabase(FareSummaryActivity.this);
                                RideDao rideDao = db.rideDao();
                                rideDao.insertRide(newRide);

                                // Post a success message back to the UI thread
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(FareSummaryActivity.this, "Ride Saved to History!", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } catch (Exception e) {
                                Log.e("FareSummaryActivity", "Error saving ride: " + e.getMessage());
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(FareSummaryActivity.this, "Failed to save ride: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                });
                            }
                        }
                    });
                }
            });

            // Set up button listener for sharing ride details
            shareRideButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Build the shareable text summary using data from UI Textviews
                    StringBuilder shareTextBuilder = new StringBuilder();
                    shareTextBuilder.append(getString(R.string.fare_summary_title)).append("\n");
                    shareTextBuilder.append("----------------------------\n");
                    shareTextBuilder.append(getString(R.string.total_fare_label)).append(": ").append(totalFareTextView.getText()).append("\n\n");

                    shareTextBuilder.append(getString(R.string.fare_breakdown_label)).append(":\n");
                    shareTextBuilder.append(getString(R.string.base_fare_label)).append(": ").append(baseFareBreakdownTextView.getText()).append("\n");
                    shareTextBuilder.append(getString(R.string.distance_charge_label)).append(": ").append(distanceChargeTextView.getText()).append("\n");
                    shareTextBuilder.append(getString(R.string.time_charge_label)).append(": ").append(timeChargeTextView.getText()).append("\n");
                    shareTextBuilder.append(getString(R.string.waiting_charge_label)).append(": ").append(waitingChargeTextView.getText()).append("\n");
                    shareTextBuilder.append(getString(R.string.multiplier_adjustment_label)).append(": ").append(multiplierAdjustmentTextView.getText()).append("\n\n");

                    shareTextBuilder.append(getString(R.string.ride_stats_label)).append(":\n");
                    shareTextBuilder.append(getString(R.string.mode_label)).append(": ").append(modeTextView.getText()).append("\n");
                    shareTextBuilder.append(getString(R.string.distance_covered_label)).append(": ").append(distanceCoveredTextView.getText()).append("\n");
                    shareTextBuilder.append(getString(R.string.total_duration_label)).append(": ").append(totalDurationTextView.getText()).append("\n");
                    shareTextBuilder.append(getString(R.string.waiting_time_label)).append(": ").append(waitingTimeTextView.getText()).append("\n");
                    shareTextBuilder.append(getString(R.string.ride_date_time_label)).append(": ").append(rideDateTimeTextView.getText()).append("\n");
                    shareTextBuilder.append("\nGenerated by YatraMeter App!"); // Add your app's branding

                    String shareMessage = shareTextBuilder.toString();

                    // Create and launch the share Intent
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("text/plain"); // Set MIME type to plain text
                    shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.fare_summary_title)); // Optional subject line
                    shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage); // The actual message to share

                    // Use Intent.createChooser to show a dialog of apps that can handle the intent
                    startActivity(Intent.createChooser(shareIntent, getString(R.string.share_ride_button_text)));
                }
            });

        } else { // If extras are null (no ride data was passed)
            Toast.makeText(this, "No ride summary data available.", Toast.LENGTH_LONG).show();
            finish(); // Go back if no data
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (databaseWriteExecutor != null) {
            databaseWriteExecutor.shutdown(); // Shuts down the executor service
        }
    }

    // Helper method to format seconds into HH:MM:SS
    private String formatTime(long totalSeconds) {
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
    }

    // Override attachBaseContext to apply the selected locale for this Activity
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.setLocale(newBase, LocaleHelper.getLanguage(newBase)));
    }
}