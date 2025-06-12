package com.project.yatrameter;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context; // Needed for attachBaseContext
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast; // For language button's old Toast


public class MainActivity extends AppCompatActivity {

    // IMPORTANT: No BottomNavigationView declaration needed here if it's not in activity_main.xml

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Your main layout should be here

        // UI elements initialization (these should match your current activity_main.xml)
        Button startRideButton = findViewById(R.id.start_ride_button);
        Button viewHistoryButton = findViewById(R.id.view_history_button);
        Button settingsButton = findViewById(R.id.settings_button); // This was previously language_button

        // Listener for the "Start Ride" button
        startRideButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, FareSetupActivity.class);
                startActivity(intent);
            }
        });

        // Listener for the "View History" button
        viewHistoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
                startActivity(intent);
            }
        });

        // Listener for the "Settings" button (formerly "Change Language")
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });

        // IMPORTANT: Removed all BottomNavigationView related code as it's not in your current UI.
    }

    // Override attachBaseContext to apply the selected locale
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.setLocale(newBase, LocaleHelper.getLanguage(newBase)));
    }
}