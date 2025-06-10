package com.project.yatrameter;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context; // Needed for attachBaseContext
import android.content.Intent; // NEW: Added import for Intent
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// NEW: Implement RideListAdapter.OnItemClickListener
public class HistoryActivity extends AppCompatActivity implements RideListAdapter.OnItemClickListener {

    private RecyclerView ridesRecyclerView;
    private TextView emptyHistoryTextView;
    private RideListAdapter rideListAdapter;
    private ExecutorService databaseReadExecutor; // For background database operations

    private static final String TAG = "HistoryActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        ridesRecyclerView = findViewById(R.id.rides_recycler_view);
        emptyHistoryTextView = findViewById(R.id.empty_history_text_view);

        // Set up the RecyclerView
        ridesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        // MODIFIED: Pass 'this' as the listener to RideListAdapter's constructor
        rideListAdapter = new RideListAdapter(new ArrayList<>(), this);
        ridesRecyclerView.setAdapter(rideListAdapter);

        // Initialize the ExecutorService for database reads
        databaseReadExecutor = Executors.newSingleThreadExecutor();

        // Load rides from the database
        loadRidesFromDatabase();
    }

    private void loadRidesFromDatabase() {
        databaseReadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    AppDatabase db = AppDatabase.getDatabase(HistoryActivity.this);
                    RideDao rideDao = db.rideDao();
                    final List<Ride> rides = rideDao.getAllRides(); // Fetch all rides

                    // Update UI on the main thread
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (rides != null && !rides.isEmpty()) {
                                rideListAdapter.setRides(rides); // Update adapter with fetched rides
                                ridesRecyclerView.setVisibility(View.VISIBLE);
                                emptyHistoryTextView.setVisibility(View.GONE);
                                Log.d(TAG, "Loaded " + rides.size() + " rides from database.");
                            } else {
                                ridesRecyclerView.setVisibility(View.GONE);
                                emptyHistoryTextView.setVisibility(View.VISIBLE);
                                Log.d(TAG, "No rides found in database.");
                            }
                        }
                    });
                } catch (Exception e) {
                    Log.e(TAG, "Error loading rides: " + e.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(HistoryActivity.this, "Failed to load rides: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (databaseReadExecutor != null) {
            databaseReadExecutor.shutdown(); // Properly shut down the executor
        }
    }

    // NEW: Implement the onItemClick method from RideListAdapter.OnItemClickListener
    @Override
    public void onItemClick(Ride ride) {
        // When a ride item is clicked, start the RideDetailActivity
        Intent intent = new Intent(HistoryActivity.this, RideDetailActivity.class);
        intent.putExtra("SELECTED_RIDE", ride); // Pass the entire Ride object
        startActivity(intent);
        Log.d(TAG, "Clicked on ride with ID: " + ride.getId());
    }

    // Override attachBaseContext to apply the selected locale for this Activity
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.setLocale(newBase, LocaleHelper.getLanguage(newBase)));
    }
}