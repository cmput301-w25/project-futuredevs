package com.example.myapplication;

import android.content.Context;
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

public class SearchUserAdapter extends ArrayAdapter<UserSearchResult> {
    private final ArrayList<UserSearchResult> searchUsers;
    private final Context context;
    private final String currentUsername;



    public SearchUserAdapter(Context context, ArrayList<UserSearchResult> searchUsers, String currentUsername) {
        super(context, 0, searchUsers);
        this.searchUsers = searchUsers;
        this.context = context;
        this.currentUsername = currentUsername;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent){
        UserSearchResult searchResult = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.user_search_results, parent, false);
        }

        TextView usernameTextView = convertView.findViewById(R.id.result_username_text);
        Button followButton = convertView.findViewById(R.id.result_user_follow_button);

        usernameTextView.setText(searchResult.getUsername());

        if (searchResult.isFollowPending()) {
            followButton.setEnabled(false);
            followButton.setText("Sent");
        } else if (searchResult.isUserFollowing()) {
            followButton.setEnabled(false);
            followButton.setText("Following");
        }
        else {

            followButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Use the Database instance's sendFollowRequest method.
                    Database.getInstance().sendFollowRequest(currentUsername, searchResult.getUsername());
                    Toast.makeText(context, "Follow request sent to " + searchResult.getUsername(), Toast.LENGTH_SHORT).show();
                }
            });
        }
         return convertView;
    }
}
