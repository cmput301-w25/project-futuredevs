package com.example.myapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.futuredevs.database.Database;
import com.futuredevs.models.IModelListener;
import com.futuredevs.models.ModelBase;
import com.futuredevs.models.ModelMoodsFollowing;
import com.futuredevs.models.items.MoodPost;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

/**
 * Fragment that displays the most recent mood event from each followed user.
 * It uses {@link ModelMoodsFollowing} to fetch mood posts and groups them by username,
 * retaining only the most recent post (with the highest timestamp) per user.
 */
public class FollowingHistoryFragment extends Fragment implements IModelListener<MoodPost> {

    private RecyclerView recyclerView;
    private FollowingHistoryAdapter adapter;
    private List<MoodPost> moodHistoryList = new ArrayList<>();
    private ModelMoodsFollowing followingModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the layout defined in following_mood_history_list.xml (which now uses a LinearLayout)
        View view = inflater.inflate(R.layout.following_mood_history_list, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new FollowingHistoryAdapter(moodHistoryList);
        recyclerView.setAdapter(adapter);

        // Initialize ModelMoodsFollowing with the current user from the Database
        String currentUser = Database.getInstance().getCurrentUser();
        followingModel = new ModelMoodsFollowing(currentUser);
        followingModel.addChangeListener(this);
        followingModel.requestData();

        return view;
    }

    /**
     * Callback method invoked when the model's data is updated.
     * Groups all fetched mood posts by username, retaining only the most recent post
     * (the one with the highest timePosted) per user, then sorts the list in descending order.
     *
     * @param model The model containing the fetched {@link MoodPost} data.
     */
    @Override
    public void onModelChanged(ModelBase<MoodPost> model) {
        moodHistoryList.clear();
        moodHistoryList.clear();

        // Map each username to a list containing at most 3 of their most recent posts.
        Map<String, List<MoodPost>> postsByUser = new HashMap<>();

        for (MoodPost post : model.getModelData()) {
            String user = post.getUser();
            List<MoodPost> userPosts = postsByUser.get(user);
            if (userPosts == null) {
                userPosts = new ArrayList<>();
                postsByUser.put(user, userPosts);
            }

            // If this user has fewer than 3 posts stored, add the current post.
            if (userPosts.size() < 3) {
                userPosts.add(post);
            } else {
                // Otherwise, find the oldest post in the user's list.
                int indexOfOldest = 0;
                long oldestTime = userPosts.get(0).getTimePosted();
                for (int i = 1; i < userPosts.size(); i++) {
                    if (userPosts.get(i).getTimePosted() < oldestTime) {
                        oldestTime = userPosts.get(i).getTimePosted();
                        indexOfOldest = i;
                    }
                }
                // If the current post is more recent than the oldest stored post, replace it.
                if (post.getTimePosted() > oldestTime) {
                    userPosts.set(indexOfOldest, post);
                }
            }
        }

        // Aggregate the posts from each user's list.
        List<MoodPost> aggregatedPosts = new ArrayList<>();
        for (List<MoodPost> userPosts : postsByUser.values()) {
            aggregatedPosts.addAll(userPosts);
        }

        // Sort the aggregated list in reverse chronological order (most recent first).
        Collections.sort(aggregatedPosts, new Comparator<MoodPost>() {
            @Override
            public int compare(MoodPost p1, MoodPost p2) {
                return Long.compare(p2.getTimePosted(), p1.getTimePosted());
            }
        });
        moodHistoryList.addAll(aggregatedPosts);

        adapter.notifyDataSetChanged();
    }
}
