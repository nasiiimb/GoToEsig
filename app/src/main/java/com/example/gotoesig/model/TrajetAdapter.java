package com.example.gotoesig.model;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gotoesig.R;

import java.io.IOException;
import java.util.List;

public class TrajetAdapter extends RecyclerView.Adapter<TrajetAdapter.TrajetViewHolder> {

    private Context context;
    private List<Trip> trips;
    private OnItemClickListener listener;

    // Interfaz para el listener
    public interface OnItemClickListener {
        void onItemClick(Trip trip) throws IOException;
    }

    // Configurar el listener
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public TrajetAdapter(Context context, List<Trip> trips) {
        this.context = context;
        this.trips = trips;
    }

    @Override
    public TrajetViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_trip, parent, false);
        return new TrajetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TrajetViewHolder holder, int position) {
        Trip trip = trips.get(position);

        // Set the departure point
        holder.departurePointTextView.setText(trip.getStartPoint());

        // Set the date and time
        holder.dateAndTimeTextView.setText(trip.getDate() + " " + trip.getTime());

        // Set the transport mode
        holder.transportTypeTextView.setText(trip.getTransportType());

        // Log to check data
        Log.d("TrajetAdapter", trip.getStartPoint() + ", " + trip.getTransportType());

        // On click listener for the item
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                try {
                    listener.onItemClick(trip);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return trips.size();
    }

    public static class TrajetViewHolder extends RecyclerView.ViewHolder {

        TextView departurePointTextView;
        TextView dateAndTimeTextView;
        TextView transportTypeTextView;

        public TrajetViewHolder(View itemView) {
            super(itemView);
            departurePointTextView = itemView.findViewById(R.id.start_point);
            dateAndTimeTextView = itemView.findViewById(R.id.date_and_time);
            transportTypeTextView = itemView.findViewById(R.id.transport_type);
        }
    }
}
