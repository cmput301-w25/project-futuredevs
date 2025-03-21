package com.example.myapplication;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.futuredevs.database.Database;
import com.futuredevs.database.DatabaseResult;
import com.futuredevs.database.IResultListener;
import com.futuredevs.models.items.MoodPost;

import java.util.List;

/**
 * Adapter that displays Mood History items in Recycler view
 */
public class MoodHistoryAdapter extends RecyclerView.Adapter<MoodHistoryAdapter.MyViewHolder> {

    private final List<MoodPost> moodHistoryList;
    private final Context context;
    private final boolean showOverflowMenu;

    public MoodHistoryAdapter(Context context, List<MoodPost> moodHistoryList, boolean showOverflowMenu) {
        this.context = context;
        this.moodHistoryList = moodHistoryList;
        this.showOverflowMenu = showOverflowMenu;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_mood_row, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        MoodPost mood = moodHistoryList.get(position);

        holder.username.setText(mood.getUser());
        holder.timeText.setText(mood.getTimePostedLocaleRepresentation());
        holder.moodText.setText("is feeling " + mood.getEmotion().toString().toLowerCase());

        // Load mood image
        String base64Image = mood.getImageData();
        if (base64Image != null && !base64Image.isEmpty()) {
            byte[] imageBytes = Base64.decode(base64Image, Base64.NO_PADDING | Base64.NO_WRAP | Base64.URL_SAFE);
            Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            holder.moodImageView.setImageBitmap(bitmap);
            holder.moodImageView.setVisibility(View.VISIBLE);
        } else {
            holder.moodImageView.setVisibility(View.GONE);
        }

        // Show or hide overflow menu
        if (showOverflowMenu) {
            holder.moreOptions.setVisibility(View.VISIBLE);
            holder.moreOptions.setOnClickListener(view -> {
                PopupMenu popup = new PopupMenu(view.getContext(), view);
                popup.getMenuInflater().inflate(R.menu.mood_item_menu, popup.getMenu());
                popup.setOnMenuItemClickListener(item -> {
                    int id = item.getItemId();
                    if (id == R.id.action_edit_mood) {
                        Toast.makeText(view.getContext(), "Edit mood clicked", Toast.LENGTH_SHORT).show();
                        // TODO: Launch edit activity here
                        return true;
                    } else if (id == R.id.action_delete_mood) {
                        new AlertDialog.Builder(view.getContext())
                                .setTitle("Delete Mood")
                                .setMessage("Are you sure you want to delete this mood? This action cannot be undone.")
                                .setPositiveButton("Delete", (dialog, which) -> {
                                    int pos = holder.getAdapterPosition();
                                    if (pos != RecyclerView.NO_POSITION) {
                                        String currentUser = Database.getInstance().getCurrentUser();
                                        Database.getInstance().removeMood(currentUser, mood, new IResultListener() {
                                            @Override
                                            public void onResult(DatabaseResult result) {
                                                if (result == DatabaseResult.SUCCESS) {
                                                    moodHistoryList.remove(pos);
                                                    notifyItemRemoved(pos);
                                                    Toast.makeText(view.getContext(), "Mood deleted", Toast.LENGTH_SHORT).show();
                                                } else {
                                                    Toast.makeText(view.getContext(), "Failed to delete mood", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                    }
                                })
                                .setNegativeButton("Cancel", null)
                                .show();
                        return true;
                    }
                    return false;
                });
                popup.show();
            });
        } else {
            holder.moreOptions.setVisibility(View.GONE);
        }

        // Open mood view page on click
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ViewMoodActivity.class);
            intent.putExtra("viewingPost", mood);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return moodHistoryList.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView username, timeText, moodText;
        ImageView moodImageView, moreOptions;

        MyViewHolder(@NonNull View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.username);
            timeText = itemView.findViewById(R.id.Time);
            moodText = itemView.findViewById(R.id.moodDescription);
            moodImageView = itemView.findViewById(R.id.moodImage);
            moreOptions = itemView.findViewById(R.id.moreOptions);
        }
    }
}
