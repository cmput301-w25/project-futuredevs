package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.futuredevs.models.items.MoodPost;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Adapter that displays a list of {@link MoodPost} items using the layout defined in
 * {@code fragment_mood_history.xml}. This adapter is used in {@link FollowingHistoryFragment}
 * to show the most recent mood post from each user you follow.
 */
public class FollowingHistoryAdapter extends RecyclerView.Adapter<FollowingHistoryAdapter.MyViewHolder> {

    private List<MoodPost> followHistoryList;

    /**
     * Constructs a new FollowingHistoryAdapter.
     *
     * @param moodHistoryList List of {@link MoodPost} items to be displayed.
     */
    public FollowingHistoryAdapter(List<MoodPost> moodHistoryList) {
        this.followHistoryList = moodHistoryList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the item layout (fragment_mood_history.xml)
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_mood_history, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        MoodPost moodPost = followHistoryList.get(position);
        String mood = moodPost.getEmotion().toString();

        String emoji = MoodUtils.getEmoji(mood);
        int color = MoodUtils.getColor(mood);
        //mashhoodadded new code
        // Apply emoji and color
        holder.mood.setText(emoji + " " + mood);
        holder.itemView.setBackgroundColor(color);
        holder.username.setText(moodPost.getUser());
        holder.Time.setText(moodPost.getTimePostedLocaleRepresentation());

        // old code below
        //holder.username.setText(moodPost.getUser());
        //holder.mood.setText(moodPost.getEmotion().toString());
        // Here we use a method that returns a locale-formatted time string.
        //holder.Time.setText(moodPost.getTimePostedLocaleRepresentation());
    }

    @Override
    public int getItemCount() {
        return followHistoryList.size();
    }

    /**
     * ViewHolder class that holds references to the views for each mood item.
     */
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView Time, mood, username;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            Time = itemView.findViewById(R.id.Time);
            mood = itemView.findViewById(R.id.moodDescription);
            username = itemView.findViewById(R.id.username);
        }
    }
}
