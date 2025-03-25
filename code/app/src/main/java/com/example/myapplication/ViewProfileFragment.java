package com.example.myapplication;

//import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.fragment.app.Fragment;


import com.futuredevs.database.Database;
import com.futuredevs.database.DatabaseFields;
import com.futuredevs.models.IModelListener;
//import com.futuredevs.models.ModelBase;
//import com.futuredevs.models.ModelMoods;
import com.futuredevs.models.ViewModelMoods;
import com.futuredevs.models.items.MoodPost;
import com.google.firebase.firestore.FieldValue;

import java.util.ArrayList;
import java.util.List;

public class ViewProfileFragment extends Fragment {
    private String Username;
    private TextView UsernameText;
    private TextView followingText;
    private TextView followersText;
    private Button followButton;
    private RecyclerView moodRecyclerView;
    private MoodHistoryAdapter moodHistoryAdapter;
    private List<MoodPost> moodHistoryList = new ArrayList<>();
    private ViewModelMoods viewModelMoods;
//    private ModelMoods modelMoods;
//    private ViewModelUserMoods viewModelUserMoods;


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
        // Retrieve username from arguments
        if (getArguments() != null) {
            Username = getArguments().getString("username");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.view_profile_page, container, false);

        UsernameText = view.findViewById(R.id.profile_username_text);
        followingText = view.findViewById(R.id.followingCountTextView);
        followersText = view.findViewById(R.id.followersCountTextView);
        followButton = view.findViewById(R.id.profile_follow_button);

        // Set the username text
        UsernameText.setText(Username);

        Database db = Database.getInstance();
        String currentUser = db.getCurrentUser();
        boolean isOwnProfile = currentUser.equals(Username);

        // Load followers/following counts (dummy values for demonstration)
//        followingText.setText(getFollowingCount(username) + " Following");
//        followersText.setText(getFollowersCount(username) + " Followers");
        followersText.setText("0");
        followingText.setText("0");

        // Optionally, set follow button state here if needed.

        // Initialize RecyclerView for mood history
        moodRecyclerView = view.findViewById(R.id.profile_recycler_view); // Make sure this ID is in your layout
        moodRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        moodHistoryAdapter = new MoodHistoryAdapter(getContext(), moodHistoryList, isOwnProfile);
        moodRecyclerView.setAdapter(moodHistoryAdapter);

        // Get the ViewModel for mood data (scoped to this fragment)
//        viewModelUserMoods = new ViewModelProvider(this).get(ViewModelUserMoods.class);
        ViewModelMoods.ViewModelMoodsFactory modelFactory = new ViewModelMoods.ViewModelMoodsFactory(Username);
        this.viewModelMoods = new ViewModelProvider(this, modelFactory).get(ViewModelMoods.class);
        this.viewModelMoods.getData().observe(this.getViewLifecycleOwner(), posts -> {
            moodHistoryList.clear();

            if (posts != null)
                moodHistoryList.addAll(posts);

            moodHistoryAdapter.notifyDataSetChanged();
        });


        db.getUserDoc(currentUser).get().addOnSuccessListener(snapshot -> {
            List<String> followingList = (List<String>) snapshot.get(DatabaseFields.USER_FOLLOWING_FLD);
            List<String> pendingList = (List<String>) snapshot.get(DatabaseFields.USER_PENDING_FOLLOWS_FLD);

            boolean isFollowing = followingList != null && followingList.contains(Username);
            boolean isPending = pendingList != null && pendingList.contains(Username);

            if (isFollowing) {
                followButton.setText("Unfollow");
                followButton.setEnabled(true);
            } else if (isPending) {
                followButton.setText("Pending");
                followButton.setEnabled(false);
            } else {
                followButton.setText("Follow");
                followButton.setEnabled(true);
            }
        }).addOnFailureListener(e -> {
            followButton.setText("Follow");
            followButton.setEnabled(true);
        });

        followButton.setOnClickListener(v -> {
            String currentUserName = db.getCurrentUser();
            String buttonText = followButton.getText().toString();

            if (buttonText.equals("Follow")) {
                db.sendFollowRequest(currentUserName, Username);
                followButton.setText("Pending");
                followButton.setEnabled(false);
                Toast.makeText(getContext(), "Follow request sent to " + Username, Toast.LENGTH_SHORT).show();

            } else if (buttonText.equals("Unfollow")) {
                db.getUserDoc(currentUserName).update(DatabaseFields.USER_FOLLOWING_FLD,
                        FieldValue.arrayRemove(Username));
                db.getUserDoc(Username).update(DatabaseFields.USER_FOLLOWERS_FLD,
                        FieldValue.arrayRemove(currentUserName));

                followButton.setText("Follow");
                followButton.setEnabled(true);
                Toast.makeText(getContext(), "Unfollowed " + Username, Toast.LENGTH_SHORT).show();
            }
        });


        // Initialize ModelMoods to load mood posts for the profile's user
//        modelMoods = new ModelMoods(Username);
//        modelMoods.addChangeListener(new IModelListener<MoodPost>() {
//            @Override
//            public void onModelChanged(ModelBase<MoodPost> model) {
//                // Update LiveData in the ViewModel
//                viewModelUserMoods.setMoodData(model.getModelData());
//            }
//        });
//        modelMoods.requestData();

        // Observe LiveData changes and update the adapter
//        viewModelUserMoods.getData().observe(getViewLifecycleOwner(), posts -> {
//            moodHistoryList.clear();
//            if (posts != null) {
//                moodHistoryList.addAll(posts);
//            }
//            moodHistoryAdapter.notifyDataSetChanged();
//        });


        return view;

    }



}
