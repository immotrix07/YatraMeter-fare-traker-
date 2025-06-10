package com.project.yatrameter;

import android.content.Context; // Needed for attachBaseContext
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class FareChartsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fare_charts);
        // No additional Java logic needed here for simple display of static content.
    }

    // Override attachBaseContext to apply the selected locale for this Activity
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.setLocale(newBase, LocaleHelper.getLanguage(newBase)));
    }
}