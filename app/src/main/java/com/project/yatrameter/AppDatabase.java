package com.project.yatrameter;

import android.content.Context;
import java.util.concurrent.ExecutorService; // NEW
import java.util.concurrent.Executors;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

// Defines the database. Entities are the tables, version is for schema changes.
@Database(entities = {Ride.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    // Abstract method to get the DAO. Room generates the implementation.
    public abstract RideDao rideDao();

    // Singleton instance of the database to prevent multiple instances
    private static volatile AppDatabase INSTANCE;

    // Method to get the database instance
    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) { // Synchronized to prevent multiple threads from creating instance
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "ride_database") // Database name
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}