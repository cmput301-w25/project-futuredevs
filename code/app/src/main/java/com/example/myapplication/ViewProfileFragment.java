package com.example.myapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.fragment.app.Fragment;

import com.futuredevs.database.Database;
import com.futuredevs.database.DatabaseFields;
import com.futuredevs.models.ViewModelMoods;
import com.futuredevs.models.ViewModelMoods.ViewModelMoodsFactory;
import com.futuredevs.models.ViewModelUserPage;
import com.futuredevs.models.ViewModelUserPage.ViewModelUserPageFactory;
import com.futuredevs.models.items.MoodPost;
import com.futuredevs.models.items.UserProfile;
import com.google.firebase.firestore.FieldValue;

import java.util.ArrayList;
import java.util.List;

public class ViewProfileFragment extends Fragment {
    private String username;
    private TextView usernameText;
    private TextView followingText;
    private TextView followersText;
    private Button followButton;
    private RecyclerView moodRecyclerView;
    private MoodHistoryAdapter moodHistoryAdapter;
    private List<MoodPost> moodHistoryList = new ArrayList<>();
    private ViewModelMoods viewModelMoods;
    private ViewModelUserPage profileModel;

    // Use newInstance to pass the username when creating this fragment
    public static ViewProfileFragment newInstance(String Username) {
        ViewProfileFragment fragment = new ViewProfileFragment();
        Bundle args = new Bundle();
        args.putString("username", Username);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            this.username = getArguments().getString("username");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.view_profile_page, container, false);

        this.usernameText = view.findViewById(R.id.profile_username_text);
        this.followingText = view.findViewById(R.id.followingCountTextView);
        this.followersText = view.findViewById(R.id.followersCountTextView);
        this.followButton = view.findViewById(R.id.profile_follow_button);
        // Don't allow the user to interact with the button until the user's
        // data has been loaded.
        this.followButton.setVisibility(View.GONE);

        this.usernameText.setText(this.username);
        this.followersText.setText("0");
        this.followingText.setText("0");

        Database db = Database.getInstance();
        String currentUser = db.getCurrentUser();
        boolean isOwnProfile = currentUser.equals(this.username);

        // Initialize RecyclerView for mood history
        this.moodRecyclerView = view.findViewById(R.id.profile_recycler_view); // Make sure this ID is in your layout
        this.moodRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        this.moodHistoryAdapter = new MoodHistoryAdapter(getContext(), this.moodHistoryList, isOwnProfile);
        this.moodRecyclerView.setAdapter(this.moodHistoryAdapter);

        // Get the ViewModel for mood data (scoped to this fragment)
        ViewModelMoodsFactory modelFactory = new ViewModelMoodsFactory(this.username);
        this.viewModelMoods = new ViewModelProvider(this, modelFactory).get(ViewModelMoods.class);
        this.viewModelMoods.getData().observe(this.getViewLifecycleOwner(), posts -> {
            moodHistoryList.clear();

            if (posts != null) {
                moodHistoryList.addAll(posts);
            }

            moodHistoryList.sort((p1, p2) -> Long.compare(p2.getTimePosted(), p1.getTimePosted()));
            moodHistoryAdapter.notifyDataSetChanged();
        });


        ViewModelUserPageFactory profileFactory = new ViewModelUserPageFactory(this.username);
        this.profileModel  = new ViewModelProvider(this, profileFactory).get(ViewModelUserPage.class);
        this.profileModel.getData().observe(this.getViewLifecycleOwner(), profile -> {
            int numFollowers = profile.getFollowers().size();
            String followingStr = "%d following";
            String followersStr = "%d followers";

            if (numFollowers == 1) {
                followersStr = "%d follower";
            }

            followersText.setText(String.format(followersStr, numFollowers));
            followingText.setText(String.format(followingStr, profile.getFollowing().size()));

            if (!currentUser.equalsIgnoreCase(profile.getUsername())) {
                followButton.setVisibility(View.VISIBLE);
            }

            if (profile.getFollowers().contains(currentUser)) {
                followButton.setText("Unfollow");
                followButton.setEnabled(true);
            }
            else if (profile.getPending().contains(currentUser)) {
                followButton.setText("Pending");
                followButton.setEnabled(false);
            }
            else {
                followButton.setText("Follow");
                followButton.setEnabled(true);
            }
        });

        this.followButton.setOnClickListener(v -> {
            String currentUserName = Database.getInstance().getCurrentUser();
            String buttonText = followButton.getText().toString();

            if (buttonText.equals("Follow")) {
                profileModel.sendFollowing(currentUserName);
                followButton.setText("Pending");
                followButton.setEnabled(false);
                Toast.makeText(getContext(), "Follow request sent to " + username, Toast.LENGTH_SHORT).show();
            }
            else if (buttonText.equals("Unfollow")) {
                profileModel.removeFollower(currentUserName);
                followButton.setText("Follow");
                followButton.setEnabled(true);
                Toast.makeText(getContext(), "Unfollowed " + username, Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }
}