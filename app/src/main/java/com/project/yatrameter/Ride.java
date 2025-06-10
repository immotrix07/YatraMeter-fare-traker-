package com.project.yatrameter;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable; // NEW: Added import for Serializable

// Defines a table named 'rides'
@Entity(tableName = "rides")
public class Ride implements Serializable { // MODIFIED: Implements Serializable

    // Primary key for the table. autoGenerate = true makes Room generate unique IDs.
    @PrimaryKey(autoGenerate = true)
    public int id;

    // Columns for ride details
    @ColumnInfo(name = "transport_mode")
    public String transportMode;

    @ColumnInfo(name = "total_fare")
    public double totalFare;

    @ColumnInfo(name = "total_distance_km")
    public double totalDistanceKm;

    @ColumnInfo(name = "total_ride_time_seconds")
    public long totalRideTimeSeconds;

    @ColumnInfo(name = "total_waiting_duration_seconds")
    public long totalWaitingDurationSeconds;

    // Breakdown charges (raw, before multiplier)
    @ColumnInfo(name = "base_fare_charge_raw")
    public double baseFareChargeRaw;

    @ColumnInfo(name = "distance_charge_raw")
    public double distanceChargeRaw;

    @ColumnInfo(name = "time_charge_raw")
    public double timeChargeRaw;

    @ColumnInfo(name = "waiting_charge_raw")
    public double waitingChargeRaw;

    @ColumnInfo(name = "fare_multiplier_used")
    public double fareMultiplierUsed;

    @ColumnInfo(name = "ride_end_time_millis")
    public long rideEndTimeMillis; // Timestamp for when the ride ended

    // Constructor
    public Ride(String transportMode, double totalFare, double totalDistanceKm, long totalRideTimeSeconds,
                long totalWaitingDurationSeconds, double baseFareChargeRaw, double distanceChargeRaw,
                double timeChargeRaw, double waitingChargeRaw, double fareMultiplierUsed, long rideEndTimeMillis) {
        this.transportMode = transportMode;
        this.totalFare = totalFare;
        this.totalDistanceKm = totalDistanceKm;
        this.totalRideTimeSeconds = totalRideTimeSeconds;
        this.totalWaitingDurationSeconds = totalWaitingDurationSeconds;
        this.baseFareChargeRaw = baseFareChargeRaw;
        this.distanceChargeRaw = distanceChargeRaw;
        this.timeChargeRaw = timeChargeRaw;
        this.waitingChargeRaw = waitingChargeRaw;
        this.fareMultiplierUsed = fareMultiplierUsed;
        this.rideEndTimeMillis = rideEndTimeMillis;
    }

    // --- Getters (Room requires public getters for its fields) ---
    public int getId() { return id; }
    public String getTransportMode() { return transportMode; }
    public double getTotalFare() { return totalFare; }
    public double getTotalDistanceKm() { return totalDistanceKm; }
    public long getTotalRideTimeSeconds() { return totalRideTimeSeconds; }
    public long getTotalWaitingDurationSeconds() { return totalWaitingDurationSeconds; }
    public double getBaseFareChargeRaw() { return baseFareChargeRaw; }
    public double getDistanceChargeRaw() { return distanceChargeRaw; }
    public double getTimeChargeRaw() { return timeChargeRaw; }
    public double getWaitingChargeRaw() { return waitingChargeRaw; }
    public double getFareMultiplierUsed() { return fareMultiplierUsed; }
    public long getRideEndTimeMillis() { return rideEndTimeMillis; }

    // No setters needed for Room if using a constructor for insertion
}