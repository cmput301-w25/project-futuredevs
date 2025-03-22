package com.example.myapplication;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
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

public class FollowingHistoryFragment extends Fragment {

    private RecyclerView recyclerView;
    private MoodHistoryAdapter adapter;
    private List<MoodPost> moodHistoryList = new ArrayList<>();
    private List<MoodPost> allMoods = new ArrayList<>();
    private FilterCriteria currentFilter;
    private TextView emptyFollowingMessage;
    private static final String TAG = "FollowingHistory";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.following_mood_history_list, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        emptyFollowingMessage = view.findViewById(R.id.emptyFollowingMessage);

        adapter = new MoodHistoryAdapter(getContext(), moodHistoryList, false);
        recyclerView.setAdapter(adapter);

        DividerItemDecoration divider = new DividerItemDecoration(recyclerView.getContext(), LinearLayoutManager.VERTICAL);
        Drawable dividerDrawable = ContextCompat.getDrawable(getContext(), R.drawable.full_width_divider);
        if (dividerDrawable != null) {
            divider.setDrawable(dividerDrawable);
        }
        recyclerView.addItemDecoration(divider);

        ViewModelMoodsFollowing viewModelMoods = new ViewModelProvider(requireActivity()).get(ViewModelMoodsFollowing.class);
        viewModelMoods.getData().observe(getViewLifecycleOwner(), posts -> {
            // Process posts first
            Map<String, List<MoodPost>> postsByUser = new HashMap<>();

            for (MoodPost post : posts) {
                String user = post.getUser();
                List<MoodPost> userPosts = postsByUser.get(user);
                if (userPosts == null) {
                    userPosts = new ArrayList<>();
                    postsByUser.put(user, userPosts);
                }

                if (userPosts.size() < 3) {
                    userPosts.add(post);
                } else {
                    int indexOfOldest = 0;
                    long oldestTime = userPosts.get(0).getTimePosted();
                    for (int i = 1; i < userPosts.size(); i++) {
                        if (userPosts.get(i).getTimePosted() < oldestTime) {
                            oldestTime = userPosts.get(i).getTimePosted();
                            indexOfOldest = i;
                        }
                    }
                    if (post.getTimePosted() > oldestTime) {
                        userPosts.set(indexOfOldest, post);
                    }
                }
            }

            List<MoodPost> aggregatedPosts = new ArrayList<>();
            for (List<MoodPost> userPosts : postsByUser.values()) {
                aggregatedPosts.addAll(userPosts);
            }

            Collections.sort(aggregatedPosts, new Comparator<MoodPost>() {
                @Override
                public int compare(MoodPost p1, MoodPost p2) {
                    return Long.compare(p2.getTimePosted(), p1.getTimePosted());
                }
            });

            allMoods.clear();
            allMoods.addAll(aggregatedPosts);
            applyCurrentFilter();

            if (posts.isEmpty()) {
                updateEmptyMessage();
            }
        });

        return view;
    }

    public void applyEmotionFilter(FilterCriteria filter) {
        this.currentFilter = filter;
        applyCurrentFilter();
    }

    private void applyCurrentFilter() {
        moodHistoryList.clear();

        long currentTime = System.currentTimeMillis();
        String timeRange = (currentFilter != null) ? currentFilter.timeRange : "All time";

        long filterTimeMillis = 0;
        switch (timeRange) {
            case "Last 24 hours":
                filterTimeMillis = currentTime - 24L * 60 * 60 * 1000;
                break;
            case "Last 7 days":
                filterTimeMillis = currentTime - 7L * 24 * 60 * 60 * 1000;
                break;
            case "Last 30 days":
                filterTimeMillis = currentTime - 30L * 24 * 60 * 60 * 1000;
                break;
            default:
                filterTimeMillis = 0;
        }

        for (MoodPost post : allMoods) {
            boolean matchesEmotion = currentFilter == null || currentFilter.emotion.equalsIgnoreCase("ALL") || post.getEmotion().toString().equalsIgnoreCase(currentFilter.emotion);
            boolean matchesTime = filterTimeMillis == 0 || post.getTimePosted() >= filterTimeMillis;
            String reason = post.getReason() != null ? post.getReason().toLowerCase() : "";
            boolean matchesTerm = currentFilter == null || currentFilter.filterWord.isEmpty() || reason.contains(currentFilter.filterWord.toLowerCase());

            if (matchesEmotion && matchesTime && matchesTerm) {
                moodHistoryList.add(post);
            }
        }

        adapter.notifyDataSetChanged();

        Log.d(TAG, "All moods: " + allMoods.size() + ", Filtered moods: " + moodHistoryList.size());
        updateEmptyMessage();
    }

    private void updateEmptyMessage() {
        if (moodHistoryList.isEmpty()) {
            StringBuilder message = new StringBuilder();
            if (allMoods.isEmpty()) {
                message.append("Your following feed is empty!\nFollow users or wait for them to post moods.");
            } else if (currentFilter != null) {
                message.append("No moods match your filter:\n");
                if (!currentFilter.emotion.equalsIgnoreCase("ALL")) {
                    message.append("Emotion: ").append(currentFilter.emotion).append("\n");
                }
                if (!currentFilter.filterWord.isEmpty()) {
                    message.append("Keyword: ").append(currentFilter.filterWord).append("\n");
                }
                if (!currentFilter.timeRange.equalsIgnoreCase("All time")) {
                    message.append("Time: ").append(currentFilter.timeRange);
                }
            } else {
                message.append("Your following feed is empty!\nFollow users or wait for them to post moods.");
            }
            emptyFollowingMessage.setText(message.toString().trim());
            emptyFollowingMessage.setVisibility(View.VISIBLE);
        } else {
            emptyFollowingMessage.setVisibility(View.GONE);
        }
    }

    public void clearFilters() {
        this.currentFilter = null;
        applyCurrentFilter();
    }
}
