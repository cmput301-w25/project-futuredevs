package com.example.myapplication;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.futuredevs.models.items.MoodPost;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Adapter that displays Mood History items in Recycler view
 */
public class MoodHistoryAdapter extends RecyclerView.Adapter<MoodHistoryAdapter.MyViewHolder> {
    private List<MoodPost> moodHistoryList;
    /**
     * Constructor for initializing the adapter with a list of mood history items.
     *
     * @param moodHistoryList List of Mood History items to be displayed in the RecyclerView.
     */
    public MoodHistoryAdapter(List<MoodPost> moodHistoryList) {
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
        MoodPost moodHistory = moodHistoryList.get(position);
        String mood = moodHistory.getEmotion().toString();
        // Get emoji and color from MoodUtils
        String emoji = MoodUtils.getEmoji(mood);
        int color = MoodUtils.getColor(mood);
        holder.mood.setText(emoji + " " + mood);
        holder.username.setText(moodHistory.getUser());
        holder.itemView.setBackgroundColor(color);

        // Convert timestamp to a readable format using locale representation
        holder.Time.setText(moodHistory.getTimePostedLocaleRepresentation()); // Set the formatted time

        // Check if there is image data and display it
        String base64Image = moodHistory.getImageData();
        if (base64Image != null && !base64Image.isEmpty()) {
            // Decode the Base64 string back to byte array
            byte[] imageBytes = Base64.decode(base64Image,
                    Base64.NO_PADDING | Base64.NO_WRAP | Base64.URL_SAFE);
            Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            holder.moodImageView.setImageBitmap(bitmap);
            holder.moodImageView.setVisibility(View.VISIBLE);
        } else {
            holder.moodImageView.setVisibility(View.GONE);
        }
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
     * ViewHolder class that holds references to the views for each mood item.
     */
    static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView Time, mood, username;
        ImageView moodImageView;  // New ImageView for the mood image

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            Time = itemView.findViewById(R.id.Time); // Initialize 'Time' TextView
            mood = itemView.findViewById(R.id.moodDescription);
            username = itemView.findViewById(R.id.username);  // Initialize 'username' TextView
            moodImageView = itemView.findViewById(R.id.moodImage);
        }
    }
}
