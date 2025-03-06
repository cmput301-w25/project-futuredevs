package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class MoodEventAdapter extends RecyclerView.Adapter<MoodEventAdapter.ViewHolder> {
    private List<MoodEvent> moodEvents;

    public MoodEventAdapter(List<MoodEvent> moodEvents) {
        this.moodEvents = moodEvents;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.mood_event_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MoodEvent event = moodEvents.get(position);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

        holder.moodTextView.setText(event.getMood());
        holder.timestampTextView.setText(dateFormat.format(event.getTimestamp()));
    }

    @Override
    public int getItemCount() {
        return moodEvents.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView moodTextView, timestampTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            moodTextView = itemView.findViewById(R.id.moodTextView);
            timestampTextView = itemView.findViewById(R.id.timestampTextView);
        }
    }
}
