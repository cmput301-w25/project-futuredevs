package com.example.myapplication;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
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

import java.io.Serializable;
import java.util.List;

/**
 * Adapter that displays Mood History items in RecyclerView
 */
public class MoodHistoryAdapter extends RecyclerView.Adapter<MoodHistoryAdapter.MyViewHolder> implements Serializable {

    private final List<MoodPost> moodHistoryList;
    private final Context context;
    private final boolean showOverflowMenu;
    private final MoodHistoryFragment fragment;  // Reference to fragment to call removeMood

    public MoodHistoryAdapter(Context context, List<MoodPost> moodHistoryList, boolean showOverflowMenu, MoodHistoryFragment fragment) {
        this.context = context;
        this.moodHistoryList = moodHistoryList;
        this.showOverflowMenu = showOverflowMenu;
        this.fragment = fragment;
    }

    // Overloaded constructor for use when fragment is not needed (e.g., ViewProfileFragment)
    public MoodHistoryAdapter(Context context, List<MoodPost> moodHistoryList, boolean showOverflowMenu) {
        this.context = context;
        this.moodHistoryList = moodHistoryList;
        this.showOverflowMenu = showOverflowMenu;
        this.fragment = null;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_mood_row, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        MoodPost moodHistory = moodHistoryList.get(position);
        String mood = moodHistory.getEmotion().toString();

        // Set emoji in TextView on the left
        holder.moodEmoji.setText(MoodUtils.getEmoji(mood));

        holder.moodText.setText("is feeling " + mood.toLowerCase());
        holder.username.setText(moodHistory.getUser());
        holder.timeText.setText("(" + moodHistory.getTimePostedLocaleRepresentation() + ")");

        // Handle overflow menu
        if (showOverflowMenu) {
            holder.moreOptions.setVisibility(View.VISIBLE);
            holder.moreOptions.setOnClickListener(view -> {
                PopupMenu popup = new PopupMenu(view.getContext(), view);
                popup.getMenuInflater().inflate(R.menu.mood_item_menu, popup.getMenu());
                popup.setOnMenuItemClickListener(item -> {
                    int id = item.getItemId();
                    if (id == R.id.action_edit_mood) {
                        Toast.makeText(view.getContext(), "Edit mood clicked", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(context, NewMoodActivity.class);
                        intent.putExtra("edit_mode", true); // Signal that this is an edit
                        intent.putExtra("mood", moodHistory); // Pass the mood object
                        context.startActivity(intent);

                        return true;
                    } else if (id == R.id.action_delete_mood) {
                        new AlertDialog.Builder(view.getContext())
                                .setTitle("Delete Mood?")
                                .setMessage("Are you sure you want to delete this mood? It will be deleted for everyone and once done this action cannot be undone!")
                                .setPositiveButton("Delete", (dialog, which) -> {
                                    int pos = holder.getAdapterPosition();
                                    if (pos != RecyclerView.NO_POSITION) {
                                        String currentUser = Database.getInstance().getCurrentUser();
                                        Database.getInstance().removeMood(currentUser, moodHistory, new IResultListener() {
                                            @Override
                                            public void onResult(DatabaseResult result) {
                                                if (result == DatabaseResult.SUCCESS) {
                                                    moodHistoryList.remove(pos);
                                                    notifyItemRemoved(pos);
                                                    if (fragment != null) {
                                                        fragment.removeMood(moodHistory); // Remove from allMoods and reapply filter
                                                    }
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

        // Open mood detail view on click
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ViewMoodActivity.class);
            intent.putExtra("viewingPost", moodHistory);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return moodHistoryList.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView username, timeText, moodText, moodEmoji;
        ImageView moreOptions;

        MyViewHolder(@NonNull View itemView) {
            super(itemView);
            moodEmoji = itemView.findViewById(R.id.moodEmoji);
            username = itemView.findViewById(R.id.username);
            timeText = itemView.findViewById(R.id.Time);
            moodText = itemView.findViewById(R.id.moodDescription);
            moreOptions = itemView.findViewById(R.id.moreOptions);
        }
    }
}
