package com.example.myapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.futuredevs.models.ViewModelMoodsFollowing;
import com.futuredevs.models.items.MoodPost;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Fragment that displays the most recent mood event from each followed user.
 * It uses {@link ViewModelMoodsFollowing} to fetch mood posts and groups them by username,
 * retaining only the most recent post (with the highest timestamp) per user.
 */
public class FollowingHistoryFragment extends Fragment {

    private RecyclerView recyclerView;
    private MoodHistoryAdapter adapter;
    private List<MoodPost> moodHistoryList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the layout defined in following_mood_history_list.xml (which now uses a LinearLayout)
        View view = inflater.inflate(R.layout.following_mood_history_list, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new MoodHistoryAdapter(moodHistoryList,false);
        recyclerView.setAdapter(adapter);

        if (this.getActivity() != null) {
            ViewModelMoodsFollowing viewModelMoods = new ViewModelProvider(this.getActivity())
                                                            .get(ViewModelMoodsFollowing.class);
            viewModelMoods.getData().observe(this.getViewLifecycleOwner(), o -> {
//                moodHistoryList.clear();
//                moodHistoryList.addAll(o);
//                moodHistoryList.sort((p1, p2) -> -Long.compare(p1.getTimePosted(), p2.getTimePosted()));
//                adapter.notifyDataSetChanged();

                // Map each username to a list containing at most 3 of their most recent posts.
                Map<String, List<MoodPost>> postsByUser = new HashMap<>();

                for (MoodPost post : o) {
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
            });
        }

        return view;
    }
}
