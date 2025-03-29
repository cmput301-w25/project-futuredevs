package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


import com.futuredevs.models.items.UserSearchResult;
import com.futuredevs.database.Database;

import java.util.ArrayList;

/**
 * SearchUserAdapter is a custom ArrayAdapter for displaying user search results.
 * It populates a list item view with a username and a follow button that updates its state
 * based on the follow status of each user.
 */
public class SearchUserAdapter extends ArrayAdapter<UserSearchResult> {
    private final Context context;
    private final String currentUsername;

    /**
     * Constructs a new SearchUserAdapter.
     *
     * @param context         The current context.
     * @param searchUsers     An ArrayList of user search results to display.
     * @param currentUsername The username of the current user, used for sending follow requests.
     */
    public SearchUserAdapter(Context context, ArrayList<UserSearchResult> searchUsers, String currentUsername) {
        super(context, 0, searchUsers);
        this.context = context;
        this.currentUsername = currentUsername;
    }

    /**
     * Provides a view for an AdapterView (ListView) for a given position in the data set.
     * This method inflates the view if needed and populates the views with the data from a UserSearchResult object.
     *
     * @param position    The position of the item within the adapter's data set.
     * @param convertView The old view to reuse, if possible.
     * @param parent      The parent view that this view will eventually be attached to.
     * @return A View corresponding to the data at the specified position.
     */
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent){
        UserSearchResult searchResult = this.getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.user_search_results, parent, false);
        }

        String currentUser = Database.getInstance().getCurrentUser();
        boolean isOwnProfile = searchResult.getUsername().equalsIgnoreCase(currentUser);
        TextView usernameTextView = convertView.findViewById(R.id.result_username_text);

        usernameTextView.setText(searchResult.getUsername());
        usernameTextView.setOnClickListener(view -> {
            Intent intent = new Intent(context, ViewMoodUserActivity.class);
            intent.putExtra("user_profile", true);
            intent.putExtra("name", searchResult.getUsername());
            context.startActivity(intent);
        });

        Button followButton = convertView.findViewById(R.id.result_user_follow_button);

        if (!isOwnProfile) {
            followButton.setVisibility(View.VISIBLE);
            followButton.setEnabled(true);
            followButton.setText("Follow");
            followButton.setClickable(true);

            if (searchResult.isFollowPending()) {
                followButton.setClickable(false);
                followButton.setEnabled(false);
                followButton.setText("Pending");
            }
            else if (searchResult.isUserFollowing()) {
                followButton.setClickable(false);
                followButton.setEnabled(false);
                followButton.setText("Following");
            }
            else {
                followButton.setOnClickListener(v -> {
                    // Use the Database instance's sendFollowRequest method.
                    Database.getInstance().sendFollowRequest(currentUsername, searchResult.getUsername());
                    Toast.makeText(context, "Follow request sent to " + searchResult.getUsername(), Toast.LENGTH_SHORT).show();
                    followButton.setEnabled(false);
                    followButton.setText("Pending");
                    followButton.setClickable(false);
                });
            }
        }
        else {
            followButton.setVisibility(View.GONE);
        }

         return convertView;
    }
}
