package com.project.yatrameter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

// Imports for Google Maps
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Cap;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;
import com.google.android.gms.maps.model.Marker;        // NEW: Import for Marker
import com.google.android.gms.maps.model.MarkerOptions; // NEW: Import for MarkerOptions

// Imports for Fused Location Provider
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;

public class RideTrackingActivity extends AppCompatActivity implements OnMapReadyCallback {

    // UI elements
    private TextView liveFareTextView;
    private TextView distanceTextView;
    private TextView timeTextView;
    private Button pauseResumeButton;
    private Button stopFinishButton;

    // Fare variables (received from FareSetupActivity)
    private String transportMode;
    private double baseFare;
    private double perKmRate;
    private double perMinuteRate;
    private double waitingCharge;
    private double fareMultiplier;

    // Location variables
    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private Location lastLocation;

    // Map variables
    private GoogleMap googleMap;
    private PolylineOptions polylineOptions;
    private Polyline ridePolyline;
    private Marker startMarker; // NEW: Marker for the ride start
    private Marker endMarker;   // NEW: Marker for the ride end

    // Ride tracking stats
    private double totalDistanceKm = 0.0;
    private long startTimeMillis;
    private long totalPausedTimeMillis = 0;
    private long pauseStartTimeMillis = 0;
    private boolean isRidePaused = false;
    private boolean isRideActive = false;

    // Fare calculation
    private double currentFare = 0.0;
    private long totalRideTimeSeconds = 0;
    private long totalWaitingDurationSeconds = 0;

    // Constants
    private static final String TAG = "RideTrackingActivity";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.00");
    private static final double MIN_SPEED_FOR_WAITING_KMH = 5.0;
    private static final double MIN_SPEED_FOR_WAITING_MPS = MIN_SPEED_FOR_WAITING_KMH / 3.6;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride_tracking);

        // Initialize UI elements
        liveFareTextView = findViewById(R.id.live_fare_text_view);
        distanceTextView = findViewById(R.id.distance_text_view);
        timeTextView = findViewById(R.id.time_text_view);
        pauseResumeButton = findViewById(R.id.pause_resume_button);
        stopFinishButton = findViewById(R.id.stop_finish_button);

        // Get the SupportMapFragment and request notification when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Retrieve fare data from Intent
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            transportMode = extras.getString("TRANSPORT_MODE", "N/A");
            baseFare = extras.getDouble("BASE_FARE", 0.0);
            perKmRate = extras.getDouble("PER_KM_RATE", 0.0);
            perMinuteRate = extras.getDouble("PER_MINUTE_RATE", 0.0);
            waitingCharge = extras.getDouble("WAITING_CHARGE", 0.0);
            fareMultiplier = extras.getDouble("FARE_MULTIPLIER", 1.0);

            Log.d(TAG, "Received Fare Data: " +
                    "\nMode: " + transportMode +
                    "\nBase: " + baseFare +
                    "\nPer Km: " + perKmRate +
                    "\nPer Min: " + perMinuteRate +
                    "\nWaiting: " + waitingCharge +
                    "\nMultiplier: " + fareMultiplier);

            currentFare = baseFare;
            liveFareTextView.setText(String.format(Locale.getDefault(), "₹ %.2f", currentFare));

        } else {
            Log.e(TAG, "No fare data received!");
            Toast.makeText(this, "No fare data received! Returning to previous screen.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Initialize FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Configure LocationRequest
        createLocationRequest();

        // Configure LocationCallback
        createLocationCallback();

        // Setup button listeners
        pauseResumeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                togglePauseResume();
            }
        });

        stopFinishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopRideAndFinish();
            }
        });

        // Start permission check
        checkLocationPermissions();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;

        // NEW: Enable traffic layer
        googleMap.setTrafficEnabled(true);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);
            googleMap.getUiSettings().setMyLocationButtonEnabled(true);

            if (lastLocation != null) {
                LatLng currentLatLng = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 16f));
            } else {
                LatLng defaultLatLng = new LatLng(30.7333, 76.7794); // Chandigarh
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLatLng, 12f));
            }
        } else {
            Log.w(TAG, "Location permission not granted. Cannot enable My Location layer on map.");
        }

        polylineOptions = new PolylineOptions()
                .color(ContextCompat.getColor(this, R.color.purple_700))
                .width(12f)
                .jointType(JointType.ROUND)
                .startCap(new RoundCap())
                .endCap(new RoundCap());

        ridePolyline = null;
    }


    private void createLocationRequest() {
        locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
                .setWaitForAccurateLocation(false)
                .setMinUpdateIntervalMillis(2000)
                .build();
    }

    private void createLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (isRidePaused) {
                    return;
                }

                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        Log.d(TAG, "New Location: " + location.getLatitude() + ", " + location.getLongitude() + " Speed: " + location.getSpeed() + "m/s");

                        LatLng newLatLng = new LatLng(location.getLatitude(), location.getLongitude());

                        if (!isRideActive) {
                            isRideActive = true;
                            startTimeMillis = System.currentTimeMillis();
                            currentFare = baseFare;
                            lastLocation = location;
                            Log.d(TAG, "Ride started at: " + startTimeMillis);

                            // --- NEW: Add start marker ---
                            if (googleMap != null) {
                                startMarker = googleMap.addMarker(new MarkerOptions()
                                        .position(newLatLng)
                                        .title(getString(R.string.ride_start_point_title)));
                            }
                            // --- END NEW ---

                            if (googleMap != null && polylineOptions != null) {
                                polylineOptions.add(newLatLng);
                                ridePolyline = googleMap.addPolyline(polylineOptions);
                                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newLatLng, 16f));
                            }

                        } else {
                            if (lastLocation != null) {
                                float distance = lastLocation.distanceTo(location);
                                totalDistanceKm += (distance / 1000.0);

                                distanceTextView.setText(DECIMAL_FORMAT.format(totalDistanceKm) + " km");
                                updateTime();
                                calculateLiveFare(location);
                            }

                            if (googleMap != null && polylineOptions != null && ridePolyline != null) {
                                List<LatLng> points = ridePolyline.getPoints();
                                points.add(newLatLng);
                                ridePolyline.setPoints(points);
                            }
                        }

                        lastLocation = location;

                        if (googleMap != null) {
                            googleMap.animateCamera(CameraUpdateFactory.newLatLng(newLatLng));
                        }
                    }
                }
            }
        };
    }

    private void startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest,
                    locationCallback,
                    Looper.getMainLooper());
            Log.d(TAG, "Location updates started.");
        } else {
            Log.e(TAG, "Cannot start location updates: permission not granted.");
            Toast.makeText(this, "Location permission not granted, cannot start tracking.", Toast.LENGTH_LONG).show();
        }
    }

    private void stopLocationUpdates() {
        if (fusedLocationClient != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
            Log.d(TAG, "Location updates stopped.");
        }
    }

    private void updateTime() {
        if (isRideActive && !isRidePaused) {
            totalRideTimeSeconds = (System.currentTimeMillis() - startTimeMillis - totalPausedTimeMillis) / 1000;
            long hours = totalRideTimeSeconds / 3600;
            long minutes = (totalRideTimeSeconds % 3600) / 60;
            long seconds = totalRideTimeSeconds % 60;
            timeTextView.setText(String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds));
        }
    }

    private void calculateLiveFare(Location currentLocation) {
        double distanceFare = totalDistanceKm * perKmRate;

        double timeBasedFare = 0.0;
        if (perMinuteRate > 0) {
            timeBasedFare = (totalRideTimeSeconds / 60.0) * perMinuteRate;
        }

        if (currentLocation.hasSpeed() && currentLocation.getSpeed() < MIN_SPEED_FOR_WAITING_MPS) {
            totalWaitingDurationSeconds++;
        }
        double waitingChargeCalculated = (totalWaitingDurationSeconds / 60.0) * waitingCharge;

        currentFare = baseFare + distanceFare + timeBasedFare + waitingChargeCalculated;
        currentFare *= fareMultiplier;

        liveFareTextView.setText(String.format(Locale.getDefault(), "₹ %.2f", currentFare));
        Log.d(TAG, "Current Fare: " + DECIMAL_FORMAT.format(currentFare) +
                ", Distance: " + DECIMAL_FORMAT.format(totalDistanceKm) +
                ", Time: " + totalRideTimeSeconds + "s" +
                ", Waiting: " + totalWaitingDurationSeconds + "s");
    }

    private void togglePauseResume() {
        if (isRideActive) {
            if (isRidePaused) {
                isRidePaused = false;
                pauseResumeButton.setText("Pause");
                totalPausedTimeMillis += (System.currentTimeMillis() - pauseStartTimeMillis);
                startLocationUpdates();
                Toast.makeText(this, "Ride Resumed", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Ride Resumed. Total paused time: " + (totalPausedTimeMillis / 1000) + "s");
            } else {
                isRidePaused = true;
                pauseResumeButton.setText("Resume");
                pauseStartTimeMillis = System.currentTimeMillis();
                stopLocationUpdates();
                Toast.makeText(this, "Ride Paused", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Ride Paused.");
            }
        } else {
            Toast.makeText(this, "Ride hasn't started yet to pause.", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopRideAndFinish() {
        if (!isRideActive) {
            Toast.makeText(this, "No active ride to stop.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        isRideActive = false;
        stopLocationUpdates();

        // NEW: Add end marker
        if (googleMap != null && lastLocation != null) {
            endMarker = googleMap.addMarker(new MarkerOptions()
                    .position(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()))
                    .title(getString(R.string.ride_end_point_title)));
            // Optional: Animate camera to show both start and end points
            // LatLngBounds.Builder builder = new LatLngBounds.Builder();
            // if (startMarker != null) builder.include(startMarker.getPosition());
            // builder.include(endMarker.getPosition());
            // LatLngBounds bounds = builder.build();
            // int padding = 100; // offset from edges of the map in pixels
            // CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
            // googleMap.animateCamera(cu);
        }

        // NEW: Clear start marker once ride ends (optional, can keep both)
        if (startMarker != null) {
            startMarker.remove();
            startMarker = null; // Clear reference
        }

        double distanceChargeWithoutMultiplier = totalDistanceKm * perKmRate;
        double timeChargeWithoutMultiplier = (totalRideTimeSeconds / 60.0) * perMinuteRate;
        double waitingChargeWithoutMultiplier = (totalWaitingDurationSeconds / 60.0) * waitingCharge;

        currentFare = baseFare + distanceChargeWithoutMultiplier + timeChargeWithoutMultiplier + waitingChargeWithoutMultiplier;
        currentFare *= fareMultiplier;

        Toast.makeText(this, "Ride Finished! Total Fare: ₹" + DECIMAL_FORMAT.format(currentFare), Toast.LENGTH_LONG).show();
        Log.d(TAG, "Ride Finished. Final Fare: " + DECIMAL_FORMAT.format(currentFare) +
                ", Total Distance: " + DECIMAL_FORMAT.format(totalDistanceKm) +
                ", Total Active Time: " + totalRideTimeSeconds + "s" +
                ", Total Waiting Time: " + totalWaitingDurationSeconds + "s");

        Intent intent = new Intent(RideTrackingActivity.this, FareSummaryActivity.class);
        intent.putExtra("TOTAL_FARE", currentFare);
        intent.putExtra("TOTAL_DISTANCE_KM", totalDistanceKm);
        intent.putExtra("TOTAL_RIDE_TIME_SECONDS", totalRideTimeSeconds);
        intent.putExtra("TOTAL_WAITING_DURATION_SECONDS", totalWaitingDurationSeconds);
        intent.putExtra("BASE_FARE_CHARGE_RAW", baseFare);
        intent.putExtra("DISTANCE_CHARGE_RAW", distanceChargeWithoutMultiplier);
        intent.putExtra("TIME_CHARGE_RAW", timeChargeWithoutMultiplier);
        intent.putExtra("WAITING_CHARGE_RAW", waitingChargeWithoutMultiplier);
        intent.putExtra("FARE_MULTIPLIER_USED", fareMultiplier);
        intent.putExtra("TRANSPORT_MODE_USED", transportMode);

        long rideEndTimeMillis = System.currentTimeMillis();
        intent.putExtra("RIDE_END_TIME_MILLIS", rideEndTimeMillis);

        startActivity(intent);
        finish();
    }

    private void checkLocationPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestLocationPermissions();
        } else {
            Log.d(TAG, "Location permissions already granted.");
            startLocationUpdates();
        }
    }

    private void requestLocationPermissions() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                LOCATION_PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Location permission granted by user.");
                Toast.makeText(this, "Location permission granted!", Toast.LENGTH_SHORT).show();
                startLocationUpdates();
            } else {
                Log.w(TAG, "Location permission denied by user.");
                Toast.makeText(this, "Location permission denied. Cannot track ride.", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isRideActive && !isRidePaused && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isRideActive && !isRidePaused) {
            stopLocationUpdates();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopLocationUpdates();
    }

    // Override attachBaseContext to apply the selected locale for this Activity
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.setLocale(newBase, LocaleHelper.getLanguage(newBase)));
    }
}