package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
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
    private Context context;

    public MoodHistoryAdapter(Context context, List<MoodPost> moodHistoryList) {
        this.context = context;
        this.moodHistoryList = moodHistoryList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_mood_history, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        MoodPost moodHistory = moodHistoryList.get(position);
        holder.mood.setText(moodHistory.getEmotion().toString());
        holder.username.setText(moodHistory.getUser());
        holder.Time.setText(moodHistory.getTimePostedLocaleRepresentation());

        // Show image if exists
        String base64Image = moodHistory.getImageData();
        if (base64Image != null && !base64Image.isEmpty()) {
            byte[] imageBytes = Base64.decode(base64Image, Base64.NO_PADDING | Base64.NO_WRAP | Base64.URL_SAFE);
            Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            holder.moodImageView.setImageBitmap(bitmap);
            holder.moodImageView.setVisibility(View.VISIBLE);
        } else {
            holder.moodImageView.setVisibility(View.GONE);
        }

        // Open ViewMoodActivity on click
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ViewMoodActivity.class);
            intent.putExtra("viewingPost", moodHistory); // MoodPost implements Serializable
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return moodHistoryList.size();
    }

    private String formatTime(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView Time, mood, username;
        ImageView moodImageView;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            Time = itemView.findViewById(R.id.Time);
            mood = itemView.findViewById(R.id.moodDescription);
            username = itemView.findViewById(R.id.username);
            moodImageView = itemView.findViewById(R.id.moodImage);
        }
    }
}

