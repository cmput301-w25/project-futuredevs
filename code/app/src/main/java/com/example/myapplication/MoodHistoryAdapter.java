package com.example.myapplication;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.Gravity;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The {@code MoodHistoryAdapter} class is an adapter intended to be used for
 * a {@code RecyclerView} list
 */
public class MoodHistoryAdapter extends RecyclerView.Adapter<MoodHistoryAdapter.MyViewHolder> implements Serializable {
    private final List<MoodPost> moodHistoryList;
    private final Context context;
    private final boolean showOverflowMenu;
    private final MoodHistoryFragment fragment;

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
        MoodPost moodHistory = this.moodHistoryList.get(position);
        String mood = moodHistory.getEmotion().toString();

        MoodPost.Emotion emotion = moodHistory.getEmotion();
        String emoji = emotion.getEmoji();
        String colour = emotion.getColour();
        holder.moodEmoji.setText(emoji + colour);

        // Set the emoji and color separately in your views
        holder.moodEmoji.setText(emoji);
        holder.moodText.setText("Was feeling " + mood.toLowerCase() + " " + colour); // Add color beside the mood text

        holder.username.setText(moodHistory.getUser());
        holder.username.setOnClickListener(view -> {
            Intent intent = new Intent(context, ViewMoodUserActivity.class);
            intent.putExtra("user_profile", true);
            intent.putExtra("name", moodHistory.getUser());
            context.startActivity(intent);
        });
        holder.timeText.setText("(" + moodHistory.getTimeSincePostedStr() + ")");
        holder.privateIcon.setVisibility(moodHistory.isPrivate() ? View.VISIBLE : View.GONE);

        // Handle overflow menu
        if (this.showOverflowMenu) {
            holder.moreOptions.setVisibility(View.VISIBLE);
            holder.moreOptions.setOnClickListener(view -> {
                PopupMenu popup = new PopupMenu(view.getContext(), view, Gravity.NO_GRAVITY, androidx.appcompat.R.attr.actionOverflowMenuStyle, 0);
                popup.getMenuInflater().inflate(R.menu.mood_item_menu, popup.getMenu());
                popup.setOnMenuItemClickListener(item -> {
                    int id = item.getItemId();

                    if (id == R.id.action_edit_mood) {
                        Intent intent = new Intent(context, AddEditMoodActivity.class);
                        intent.putExtra("edit_mode", true); // Signal that this is an edit
                        intent.putExtra("mood", moodHistory); // Pass the mood object
                        context.startActivity(intent);
                        return true;
                    }
                    else if (id == R.id.action_delete_mood) {
                        holder.moreOptions.setEnabled(false);
                        new AlertDialog.Builder(view.getContext())
                                .setTitle("Delete Mood?")
                                .setMessage("Are you sure you want to delete this mood? It will be deleted for everyone and once done this action cannot be undone!")
                                .setPositiveButton("Delete", (dialog, which) -> {
                                    int pos = holder.getAdapterPosition();

                                    if (fragment != null) {
                                        fragment.removeMood(moodHistory); // Remove from allMoods and reapply filter
                                    }

                                    if (pos != RecyclerView.NO_POSITION) {
                                        String currentUser = Database.getInstance().getCurrentUser();
                                        Database.getInstance().removeMood(currentUser, moodHistory, r -> {
                                            if (r == DatabaseResult.SUCCESS) {
                                                Toast.makeText(view.getContext(), "Mood deleted", Toast.LENGTH_SHORT).show();
                                            }
                                            else {
                                                Toast.makeText(view.getContext(), "Failed to delete mood", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                })
                                .setNegativeButton("Cancel", (d, i) -> holder.moreOptions.setEnabled(true))
                                .show();

                        return true;
                    }

                    return false;
                });

                popup.show();
            });
        }
        else {
            holder.moreOptions.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (context instanceof ViewMoodUserActivity) {
                ViewMoodUserActivity viewActivity = (ViewMoodUserActivity) context;
                ViewMoodFragment viewFragment = ViewMoodFragment.newInstance(moodHistory);
                viewActivity.setFragment(ViewMoodUserActivity.MOOD_FRAGMENT, viewFragment);
            }
            else if (context instanceof HomeActivity) {
                Intent intent = new Intent(context, ViewMoodUserActivity.class);
                intent.putExtra("view_post", true);
                intent.putExtra("post", moodHistory);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return this.moodHistoryList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView username, timeText, moodText, moodEmoji;
        ImageView moreOptions, privateIcon;

        MyViewHolder(@NonNull View itemView) {
            super(itemView);
            moodEmoji = itemView.findViewById(R.id.moodEmoji);
            username = itemView.findViewById(R.id.username);
            timeText = itemView.findViewById(R.id.Time);
            moodText = itemView.findViewById(R.id.moodDescription);
            moreOptions = itemView.findViewById(R.id.moreOptions);
            privateIcon = itemView.findViewById(R.id.image_mood_private_icon);
        }
    }
}
