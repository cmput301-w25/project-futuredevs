package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Adapter that displays Mood History items in Recycler view
 */
public class MoodHistoryAdapter extends RecyclerView.Adapter<MoodHistoryAdapter.MyViewHolder> {
    private final List<MoodHistory> moodHistoryList;
    /**
     * Constructor for initializing the adapter with a list of mood history items.
     *
     * @param moodHistoryList List of Mood History items to be displayed in the RecyclerView.
     */
    public MoodHistoryAdapter(List<MoodHistory> moodHistoryList) {
        this.moodHistoryList = moodHistoryList;
    }
    /**
     * Called when RecyclerView needs a new ViewHolder to represent an item.
     * @param parent   The parent ViewGroup that new view is added to.
     * @param viewType View type of the new view.
     * @return A new instance of MyViewHolder.
     */
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_mood_history, parent, false);
        return new MyViewHolder(view);
    }
    /**
     * Displays the data at the specified position
     * @param  holder   The ViewHolder which should be updated with data.
     * @param position The position in the data set where data is to be displayed.
     */

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        MoodHistory moodHistory = moodHistoryList.get(position);
        holder.mood.setText(moodHistory.getMood());
        holder.username.setText(moodHistory.getUsername());

        // Convert timestamp to a readable format
        String formattedTime = formatTime(moodHistory.getTimestamp());
        holder.Time.setText(formattedTime); // Set the formatted time
    }
    /**
     * Returns total number of items in mood history list
     * @return size of moodHistoryList
     */
    @Override
    public int getItemCount() {
        return moodHistoryList.size();
    }

    /**
     * Helper method  that formats the timestamp into readable time format.
     *
     * @param timestamp The timestamp to be formatted.
     * @return The formatted time string.
     */
    private String formatTime(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.getDefault()); // Example: "9:57 PM"
        return sdf.format(new Date(timestamp));
    }

    /**
     *Adds new mood history to the beginning of the list and notifies the adapter
     */
    static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView Time, mood, username;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            Time = itemView.findViewById(R.id.Time); // Initialize 'Time' TextView
            mood = itemView.findViewById(R.id.moodDescription);
            username = itemView.findViewById(R.id.username);  // Initialize 'username' TextView
        }
    }

    /**
     *Adds new mood history to the beginning of the list and notifies the adapter
     * @param newItem The new MoodHistory item to be added.
     */
    public void addItem(MoodHistory newItem) {
        moodHistoryList.add(0, newItem); // Insert at the beginning
        notifyItemInserted(0); // Notify adapter
    }
}