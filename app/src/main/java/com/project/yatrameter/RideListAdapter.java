package com.project.yatrameter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date; // NEW: Added import for Date
import java.util.List;
import java.util.Locale;

public class RideListAdapter extends RecyclerView.Adapter<RideListAdapter.RideViewHolder> {

    private List<Ride> rides = new ArrayList<>();
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.00");
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    // NEW INTERFACE: Define a click listener interface
    public interface OnItemClickListener {
        void onItemClick(Ride ride);
    }

    private OnItemClickListener listener; // NEW: Member variable for the listener

    // MODIFIED Constructor to accept the listener
    public RideListAdapter(List<Ride> rides, OnItemClickListener listener) {
        this.rides = rides;
        this.listener = listener; // Assign the listener
    }

    // Method to update the data in the adapter
    public void setRides(List<Ride> newRides) {
        this.rides = newRides;
        notifyDataSetChanged(); // Notifies the RecyclerView that data has changed
    }

    @NonNull
    @Override
    public RideViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the item_ride.xml layout for each list item
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ride, parent, false);
        return new RideViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RideViewHolder holder, int position) {
        // Get the Ride object for the current position
        Ride currentRide = rides.get(position);

        // Bind the data to the TextViews in the ViewHolder
        holder.rideDateTimeTextView.setText(DATE_FORMAT.format(new Date(currentRide.getRideEndTimeMillis())));
        holder.totalFareTextView.setText(String.format(Locale.getDefault(), "₹ %.2f", currentRide.getTotalFare()));
        holder.modeTextView.setText(currentRide.getTransportMode());
        holder.distanceTextView.setText(DECIMAL_FORMAT.format(currentRide.getTotalDistanceKm()) + " km");
        holder.durationTextView.setText(formatTime(currentRide.getTotalRideTimeSeconds()));

        // NEW: Set click listener for the entire item view
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(currentRide); // Call the onItemClick method of the listener
            }
        });
    }

    @Override
    public int getItemCount() {
        return rides.size(); // Returns the total number of items in the list
    }

    // ViewHolder class holds references to the UI elements for each item
    static class RideViewHolder extends RecyclerView.ViewHolder {
        private final TextView rideDateTimeTextView;
        private final TextView totalFareTextView;
        private final TextView modeTextView;
        private final TextView distanceTextView;
        private final TextView durationTextView;

        public RideViewHolder(@NonNull View itemView) {
            super(itemView);
            rideDateTimeTextView = itemView.findViewById(R.id.ride_item_date_time);
            totalFareTextView = itemView.findViewById(R.id.ride_item_total_fare);
            modeTextView = itemView.findViewById(R.id.ride_item_mode);
            distanceTextView = itemView.findViewById(R.id.ride_item_distance);
            durationTextView = itemView.findViewById(R.id.ride_item_duration);
        }
    }

    // Helper method to format seconds into HH:MM:SS
    private String formatTime(long totalSeconds) {
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
    }
}