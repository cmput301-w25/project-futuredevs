package com.example.myapplication;

import android.app.AlertDialog;
import android.content.DialogInterface;
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
 * An Adapter that inflates a small row layout for each MoodPost,
 * producing a compact scrolling list. Includes a three-dot menu
 * for edit/delete.
 */
public class MoodHistoryAdapter extends RecyclerView.Adapter<MoodHistoryAdapter.MyViewHolder> {

    private final List<MoodPost> moodHistoryList;
    private final boolean showOverflowMenu;

    public MoodHistoryAdapter(List<MoodPost> moodHistoryList, boolean showOverflowMenu) {
        this.moodHistoryList = moodHistoryList;
        this.showOverflowMenu = showOverflowMenu;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the row layout (item_mood_row.xml)
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_mood_row, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        MoodPost mood = moodHistoryList.get(position);


        // Fill in text fields with mood info
        holder.username.setText(mood.getUser());
        holder.timeText.setText("("+mood.getTimePostedLocaleRepresentation()+")");
        holder.moodText.setText("is feeling " + mood.getEmotion().toString().toLowerCase());

        // Show or hide the three-dot menu based on the flag
        if (showOverflowMenu) {
            holder.moreOptions.setVisibility(View.VISIBLE);
            // Set up click listener to show popup menu
            holder.moreOptions.setOnClickListener(view -> {
                PopupMenu popup = new PopupMenu(view.getContext(), view);
                popup.getMenuInflater().inflate(R.menu.mood_item_menu, popup.getMenu());
                popup.setOnMenuItemClickListener(item -> {
                    int id = item.getItemId();
                    if (id == R.id.action_edit_mood) {
                        // TODO: handle edit action
                        Toast.makeText(view.getContext(), "Edit mood clicked", Toast.LENGTH_SHORT).show();
                        return true;
                    } else if (id == R.id.action_delete_mood) {
                        // Show a confirmation dialog before deletion
                        new AlertDialog.Builder(view.getContext())
                                .setTitle("Delete Mood")
                                .setMessage("Are you sure you want to delete this mood? It will be deleted for everyone and once you done this action cannot be undone!")
                                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
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
                                    }
                                })
                                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                                .create()
                                .show();
                        return true;
                    }
                    return false;
                });
                popup.show();
            });
        } else {
            // Hide the overflow icon if it's not your history
            holder.moreOptions.setVisibility(View.GONE);
        }
        holder.itemView.setOnClickListener(view -> {
            MoodPost mood2 = moodHistoryList.get(holder.getAdapterPosition());
            // TODO: Launch detail view or show mood details
            Toast.makeText(view.getContext(), "Clicked: " + mood2.getEmotion(), Toast.LENGTH_SHORT).show();
        });

    }

    @Override
    public int getItemCount() {
        return moodHistoryList.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView username, timeText, moodText;
        ImageView moreOptions; // The three-dot icon

        MyViewHolder(@NonNull View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.username);
            timeText = itemView.findViewById(R.id.Time);
            moodText = itemView.findViewById(R.id.moodDescription);
            moreOptions = itemView.findViewById(R.id.moreOptions); // Must exist in item_mood_row.xml
        }
    }
}
