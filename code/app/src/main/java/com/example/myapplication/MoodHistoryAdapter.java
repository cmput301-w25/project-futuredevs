package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.futuredevs.models.items.MoodPost;

import java.util.List;

/**
 * An Adapter that inflates a small row layout for each MoodPost,
 * producing a compact scrolling list.
 */
public class MoodHistoryAdapter extends RecyclerView.Adapter<MoodHistoryAdapter.MyViewHolder> {

    private final List<MoodPost> moodHistoryList;

    public MoodHistoryAdapter(List<MoodPost> moodHistoryList) {
        this.moodHistoryList = moodHistoryList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the *row* layout (item_mood_row.xml), not the big fragment layout
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_mood_row, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        MoodPost mood = moodHistoryList.get(position);

        // Fill in text fields with mood info
        holder.username.setText(mood.getUser());
        holder.timeText.setText(mood.getTimePostedLocaleRepresentation());
        holder.moodText.setText("is feeling " + mood.getEmotion().toString());
    }

    @Override
    public int getItemCount() {
        return moodHistoryList.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView username, timeText, moodText;

        MyViewHolder(@NonNull View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.username);
            timeText = itemView.findViewById(R.id.Time);
            moodText = itemView.findViewById(R.id.moodDescription);
        }
    }
}
