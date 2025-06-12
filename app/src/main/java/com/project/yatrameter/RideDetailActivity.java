package com.project.yatrameter;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
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

    private Button exportPdfButton;

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.00");
    private static final String TAG = "RideDetailActivity";
    private static final int CREATE_PDF_REQUEST_CODE = 2001;

    private Ride currentDisplayedRide;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride_detail);

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

        exportPdfButton = findViewById(R.id.export_pdf_button);

        // Get the Ride object from the Intent
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("SELECTED_RIDE")) {
            currentDisplayedRide = (Ride) intent.getSerializableExtra("SELECTED_RIDE");

            if (currentDisplayedRide != null) {
                // Populate UI with data from the Ride object
                totalFareTextView.setText(String.format(Locale.getDefault(), "₹ %.2f", currentDisplayedRide.getTotalFare()));
                baseFareBreakdownTextView.setText(String.format(Locale.getDefault(), "₹ %.2f", currentDisplayedRide.getBaseFareChargeRaw()));
                distanceChargeTextView.setText(String.format(Locale.getDefault(), "₹ %.2f", currentDisplayedRide.getDistanceChargeRaw() * currentDisplayedRide.getFareMultiplierUsed()));
                timeChargeTextView.setText(String.format(Locale.getDefault(), "₹ %.2f", currentDisplayedRide.getTimeChargeRaw() * currentDisplayedRide.getFareMultiplierUsed()));
                waitingChargeTextView.setText(String.format(Locale.getDefault(), "₹ %.2f", currentDisplayedRide.getWaitingChargeRaw() * currentDisplayedRide.getFareMultiplierUsed()));
                multiplierAdjustmentTextView.setText(String.format(Locale.getDefault(), "x %.2f", currentDisplayedRide.getFareMultiplierUsed()));

                modeTextView.setText(currentDisplayedRide.getTransportMode());
                distanceCoveredTextView.setText(DECIMAL_FORMAT.format(currentDisplayedRide.getTotalDistanceKm()) + " km");
                totalDurationTextView.setText(formatTime(currentDisplayedRide.getTotalRideTimeSeconds()));
                waitingTimeTextView.setText(formatTime(currentDisplayedRide.getTotalWaitingDurationSeconds()));

                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                String formattedDateTime = sdf.format(new Date(currentDisplayedRide.getRideEndTimeMillis()));
                rideDateTimeTextView.setText(formattedDateTime);

            } else {
                Log.e(TAG, "Error: Selected Ride data is null.");
                Toast.makeText(this, "Error: Ride data not found.", Toast.LENGTH_LONG).show();
                finish();
            }
        } else {
            Log.e(TAG, "Error: No 'SELECTED_RIDE' extra found in Intent.");
            Toast.makeText(this, "Error: No ride selected.", Toast.LENGTH_LONG).show();
            finish();
        }

        // Set listener for Export PDF button
        exportPdfButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentDisplayedRide != null) {
                    exportRideAsPdf(currentDisplayedRide);
                } else {
                    Toast.makeText(RideDetailActivity.this, "No ride data to export.", Toast.LENGTH_SHORT).show();
                }
            }
        });
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

    // Method to export ride data as PDF
    private void exportRideAsPdf(Ride ride) {
        // Define page dimensions (e.g., A4 size in points: 595x842)
        int pageHeight = 842;
        int pageWidth = 595;
        PdfDocument document = new PdfDocument(); // Declared and initialized here
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);

        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();
        paint.setColor(Color.BLACK); // Default text color

        // Draw content onto the PDF page using a helper method
        drawPdfContent(canvas, paint, ride);

        document.finishPage(page);

        // Create an Intent to let the user choose where to save the PDF
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE); // Indicates that it's a file that can be opened
        intent.setType("application/pdf"); // Set MIME type to PDF
        SimpleDateFormat filenameSdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
        String fileName = String.format(Locale.getDefault(), getString(R.string.pdf_file_name_format), filenameSdf.format(new Date(ride.getRideEndTimeMillis())));
        intent.putExtra(Intent.EXTRA_TITLE, fileName); // Suggest a file name

        try {
            startActivityForResult(intent, CREATE_PDF_REQUEST_CODE);
            Log.d(TAG, "Intent to create PDF launched.");
        } catch (Exception e) {
            Log.e(TAG, "Error launching PDF creation intent: " + e.getMessage());
            Toast.makeText(this, String.format(Locale.getDefault(), getString(R.string.pdf_export_failed), "Failed to open file picker."), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Declare document outside try block
        PdfDocument document = null; // Corrected: Declared here and initialized to null
        try {
            if (requestCode == CREATE_PDF_REQUEST_CODE && resultCode == RESULT_OK) {
                if (data != null && data.getData() != null) {
                    Uri uri = data.getData(); // The URI where the user wants to save the file
                    FileOutputStream fos = (FileOutputStream) getContentResolver().openOutputStream(uri);
                    if (fos != null) {
                        document = new PdfDocument(); // Initialized inside try block
                        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
                        PdfDocument.Page page = document.startPage(pageInfo);
                        Canvas canvas = page.getCanvas();
                        Paint paint = new Paint();
                        paint.setColor(Color.BLACK);

                        // Re-draw the content onto this new page for writing
                        drawPdfContent(canvas, paint, currentDisplayedRide);

                        document.finishPage(page);
                        document.writeTo(fos); // Write the PDF content to the file stream
                        // document.close(); // Don't close here, close in finally
                        fos.close(); // Close the file stream

                        Toast.makeText(this, R.string.pdf_export_success, Toast.LENGTH_LONG).show();
                        Log.d(TAG, "PDF saved successfully to: " + uri.getPath());
                    } else {
                        Toast.makeText(this, String.format(Locale.getDefault(), getString(R.string.pdf_export_failed), "Failed to open output stream."), Toast.LENGTH_LONG).show();
                        Log.e(TAG, "FileOutputStream is null for URI: " + uri);
                    }
                } else {
                    Toast.makeText(this, String.format(Locale.getDefault(), getString(R.string.pdf_export_failed), "No data from file picker."), Toast.LENGTH_LONG).show();
                    Log.w(TAG, "No data or URI from file picker.");
                }
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "PDF export cancelled.", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "PDF export cancelled by user.");
            } else {
                Toast.makeText(this, String.format(Locale.getDefault(), getString(R.string.pdf_export_failed), "Unknown result code."), Toast.LENGTH_LONG).show();
                Log.e(TAG, "Unknown result code for PDF creation: " + resultCode);
            }
        } catch (FileNotFoundException e) {
            Toast.makeText(this, String.format(Locale.getDefault(), getString(R.string.pdf_export_failed), "File not found or permission denied."), Toast.LENGTH_LONG).show();
            Log.e(TAG, "FileNotFoundException: " + e.getMessage());
        } catch (IOException e) {
            Toast.makeText(this, String.format(Locale.getDefault(), getString(R.string.pdf_export_failed), "Error writing PDF: " + e.getMessage()), Toast.LENGTH_LONG).show();
            Log.e(TAG, "IOException: " + e.getMessage());
        } finally {
            // Ensure the document is closed even if an error occurs during writing
            if (document != null) {
                document.close();
            }
        }
    }

    // Helper method to draw content onto a PDF page
    private void drawPdfContent(Canvas canvas, Paint paint, Ride ride) {
        int startX = 40;
        int startY = 60;
        int lineHeight = 30; // Spacing between lines

        // Title
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(24);
        paint.setColor(Color.BLACK);
        canvas.drawText(getString(R.string.pdf_ride_summary_document_title), canvas.getWidth() / 2, startY, paint);
        paint.setTextAlign(Paint.Align.LEFT);

        startY += lineHeight * 2; // Move down for next section
        paint.setTextSize(18); // Section title size
        paint.setFakeBoldText(true); // Bold text

        // Total Fare
        canvas.drawText(getString(R.string.total_fare_label) + ": " + String.format(Locale.getDefault(), "₹ %.2f", ride.getTotalFare()), startX, startY, paint);
        startY += lineHeight;

        // Ride Stats
        canvas.drawText(getString(R.string.ride_stats_label), startX, startY, paint);
        startY += lineHeight;
        paint.setFakeBoldText(false); // Regular text
        paint.setTextSize(14); // Content text size

        canvas.drawText(getString(R.string.mode_label) + ": " + ride.getTransportMode(), startX, startY, paint);
        startY += lineHeight;
        canvas.drawText(getString(R.string.distance_covered_label) + ": " + DECIMAL_FORMAT.format(ride.getTotalDistanceKm()) + " km", startX, startY, paint);
        startY += lineHeight;
        canvas.drawText(getString(R.string.total_duration_label) + ": " + formatTime(ride.getTotalRideTimeSeconds()), startX, startY, paint);
        startY += lineHeight;
        canvas.drawText(getString(R.string.waiting_time_label) + ": " + formatTime(ride.getTotalWaitingDurationSeconds()), startX, startY, paint);
        startY += lineHeight;
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        canvas.drawText(getString(R.string.ride_date_time_label) + ": " + sdf.format(new Date(ride.getRideEndTimeMillis())), startX, startY, paint);
        startY += lineHeight * 2;

        // Fare Breakdown
        paint.setTextSize(18);
        paint.setFakeBoldText(true);
        canvas.drawText(getString(R.string.fare_breakdown_label), startX, startY, paint);
        startY += lineHeight;
        paint.setFakeBoldText(false);
        paint.setTextSize(14);

        canvas.drawText(getString(R.string.base_fare_label) + ": " + String.format(Locale.getDefault(), "₹ %.2f", ride.getBaseFareChargeRaw()), startX, startY, paint);
        startY += lineHeight;
        canvas.drawText(getString(R.string.distance_charge_label) + ": " + String.format(Locale.getDefault(), "₹ %.2f", ride.getDistanceChargeRaw() * ride.getFareMultiplierUsed()), startX, startY, paint);
        startY += lineHeight;
        canvas.drawText(getString(R.string.time_charge_label) + ": " + String.format(Locale.getDefault(), "₹ %.2f", ride.getTimeChargeRaw() * ride.getFareMultiplierUsed()), startX, startY, paint);
        startY += lineHeight;
        canvas.drawText(getString(R.string.waiting_charge_label) + ": " + String.format(Locale.getDefault(), "₹ %.2f", ride.getWaitingChargeRaw() * ride.getFareMultiplierUsed()), startX, startY, paint);
        startY += lineHeight;
        canvas.drawText(getString(R.string.multiplier_adjustment_label) + ": x " + String.format(Locale.getDefault(), "%.2f", ride.getFareMultiplierUsed()), startX, startY, paint);
    }
}
