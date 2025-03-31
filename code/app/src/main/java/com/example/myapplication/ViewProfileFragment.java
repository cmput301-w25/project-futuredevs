package com.example.myapplication;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.fragment.app.Fragment;

import com.futuredevs.database.Database;
import com.futuredevs.models.ViewModelMoods;
import com.futuredevs.models.ViewModelMoods.ViewModelMoodsFactory;
import com.futuredevs.models.ViewModelUserProfile;
import com.futuredevs.models.ViewModelUserProfile.ViewModelUserProfileFactory;
import com.futuredevs.models.items.MoodPost;

import java.util.ArrayList;
import java.util.List;

/**
 * A fragment to display a user's profile and mood history.
 * <p>
 * This fragment retrieves and displays profile information such as follower counts,
 * following counts, and a list of mood posts. It also enables the current user to follow
 * or unfollow the displayed profile.
 * </p>
 *
 * @author pranav gupta
 */

public class ViewProfileFragment extends Fragment {
    private String username;
    private TextView usernameText;
    private TextView followingText;
    private TextView followersText;
    private TextView profileLoadText;
    private TextView emptyListText;
    private ProgressBar loadingMoodsBar;
    private Button followButton;
    private RecyclerView moodRecyclerView;
    private MoodHistoryAdapter moodHistoryAdapter;
    private List<MoodPost> moodHistoryList = new ArrayList<>();
    private ViewModelMoods viewModelMoods;
    private ViewModelUserProfile profileModel;

    // Use newInstance to pass the username when creating this fragment
    public static ViewProfileFragment newInstance(String Username) {
        ViewProfileFragment fragment = new ViewProfileFragment();
        Bundle args = new Bundle();
        args.putString("username", Username);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Called when the fragment is created.
     * Retrieves the username from the provided arguments.
     *
     * @param savedInstanceState the saved instance state bundle
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            this.username = getArguments().getString("username");
        }
    }

    /**
     * Called to have the fragment instantiate its user interface view.
     * Initializes UI components, ViewModels, and data observers for the profile and mood data.
     *
     * @param inflater           the LayoutInflater object that can be used to inflate views in the fragment
     * @param container          the parent view that the fragment's UI should be attached to
     * @param savedInstanceState the saved instance state bundle
     * @return the View for the fragment's UI
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.view_profile_page, container, false);

        this.usernameText = view.findViewById(R.id.profile_username_text);
        this.followingText = view.findViewById(R.id.followingCountTextView);
        this.followersText = view.findViewById(R.id.followersCountTextView);
        this.followButton = view.findViewById(R.id.profile_follow_button);
        this.profileLoadText = view.findViewById(R.id.text_profile_failed_load);
        this.emptyListText = view.findViewById(R.id.text_profile_empty_list);
        this.loadingMoodsBar = view.findViewById(R.id.loading_profile_moods);
        // Don't allow the user to interact with the button until the user's
        // data has been loaded.
        this.followButton.setVisibility(View.GONE);

        this.usernameText.setText(this.username);
        this.followersText.setText("0 followers");
        this.followingText.setText("0 following");

        Database db = Database.getInstance();
        String currentUser = db.getCurrentUser();
        boolean isOwnProfile = currentUser.equals(this.username);

        this.moodRecyclerView = view.findViewById(R.id.profile_recycler_view);
        this.moodRecyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        this.moodHistoryAdapter = new MoodHistoryAdapter(this.getContext(), this.moodHistoryList, isOwnProfile);
        this.moodRecyclerView.setAdapter(this.moodHistoryAdapter);

        DividerItemDecoration divider = new DividerItemDecoration(moodRecyclerView.getContext(), LinearLayoutManager.VERTICAL);
        Drawable dividerDrawable = ContextCompat.getDrawable(this.getContext(), R.drawable.full_width_divider);

        if (dividerDrawable != null) {
            divider.setDrawable(dividerDrawable);
        }

        this.moodRecyclerView.addItemDecoration(divider);

        // Get the ViewModel for mood data (scoped to this fragment)
        ViewModelMoodsFactory modelFactory = new ViewModelMoodsFactory(this.username);
        this.viewModelMoods = new ViewModelProvider(this, modelFactory).get(ViewModelMoods.class);
        this.viewModelMoods.getData().observe(this.getViewLifecycleOwner(), posts -> {
            loadingMoodsBar.setVisibility(View.GONE);

            if (posts.isEmpty()) {
                emptyListText.setVisibility(View.VISIBLE);

                if (isOwnProfile) {
                    emptyListText.setText("You have not made any posts!\nWhen you create posts they will appear here!");
                }
                else {
                    String emptyMsg = "%s has not made any posts.\nCome back later!";
                    emptyListText.setText(String.format(emptyMsg, username));
                }
            }
            else {
                emptyListText.setVisibility(View.GONE);
                moodRecyclerView.setVisibility(View.VISIBLE);
                moodHistoryList.clear();

                if (posts != null) {
                    moodHistoryList.addAll(posts);
                }

                moodHistoryList.sort((p1, p2) -> Long.compare(p2.getTimePosted(), p1.getTimePosted()));
                moodHistoryAdapter.notifyDataSetChanged();
            }
        });

        ViewModelUserProfileFactory profileFactory = new ViewModelUserProfileFactory(this.username);
        this.profileModel  = new ViewModelProvider(this, profileFactory).get(ViewModelUserProfile.class);
        this.profileModel.getData().observe(this.getViewLifecycleOwner(), profile -> {
            if (profile == null) {
                profileLoadText.setVisibility(View.VISIBLE);
                return;
            }

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
            String buttonText = followButton.getText().toString();

            if (buttonText.equals("Follow")) {
                profileModel.sendFollowing(currentUser);
                followButton.setText("Pending");
                followButton.setEnabled(false);
                Toast.makeText(getContext(), "Follow request sent to " + username, Toast.LENGTH_SHORT).show();
            }
            else if (buttonText.equals("Unfollow")) {
                profileModel.removeFollower(currentUser);
                followButton.setText("Follow");
                followButton.setEnabled(true);
                Toast.makeText(getContext(), "Unfollowed " + username, Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }
}