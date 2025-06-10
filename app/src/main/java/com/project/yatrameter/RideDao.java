package com.project.yatrameter;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.concurrent.ExecutorService; // NEW
import java.util.concurrent.Executors;
import java.util.List;

// Data Access Object for the Ride entity
@Dao
public interface RideDao {

    // Inserts a single ride into the database
    @Insert
    void insertRide(Ride ride);

    // Retrieves all rides from the database, ordered by end time (newest first)
    @Query("SELECT * FROM rides ORDER BY ride_end_time_millis DESC")
    List<Ride> getAllRides();

    // You could add more queries here, e.g., getRideById, deleteAllRides, etc.
    // @Query("DELETE FROM rides")
    // void deleteAllRides();
}