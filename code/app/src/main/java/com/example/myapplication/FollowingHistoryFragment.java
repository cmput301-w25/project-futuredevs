package com.example.myapplication;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
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
import com.futuredevs.models.ViewModelUserProfile;
import com.futuredevs.models.items.MoodPost;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FollowingHistoryFragment extends Fragment {

    private ProgressBar loadingMoodsBar;
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
        View view = inflater.inflate(R.layout.activity_main, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        emptyFollowingMessage = view.findViewById(R.id.emptyFilterMessage);
        loadingMoodsBar = view.findViewById(R.id.loading_bar_moods);

        adapter = new MoodHistoryAdapter(getContext(), moodHistoryList, false);
        recyclerView.setAdapter(adapter);

        DividerItemDecoration divider = new DividerItemDecoration(recyclerView.getContext(), LinearLayoutManager.VERTICAL);
        Drawable dividerDrawable = ContextCompat.getDrawable(getContext(), R.drawable.full_width_divider);
        if (dividerDrawable != null) {
            divider.setDrawable(dividerDrawable);
        }
        recyclerView.addItemDecoration(divider);

        loadFollowingAndPosts();

        return view;
    }

    private void loadFollowingAndPosts() {
        ViewModelUserProfile profileModel = new ViewModelProvider(this.requireActivity()).get(ViewModelUserProfile.class);
        profileModel.requestData();
        profileModel.getData().observe(this.getViewLifecycleOwner(), profile -> {
            if (profile == null) {
                Log.e(TAG, "Failed to fetch following list");
                emptyFollowingMessage.setText("Failed to load following feed.");
                emptyFollowingMessage.setVisibility(View.VISIBLE);
            }
            else {
                List<String> followingNames = profile.getFollowing();

                if (followingNames.isEmpty()) {
                    allMoods.clear();
                    moodHistoryList.clear();
                    adapter.notifyDataSetChanged();
                    emptyFollowingMessage.setText("Your following feed is empty!\nFollow users or wait for them to post moods.");
                    emptyFollowingMessage.setVisibility(View.VISIBLE);
                }
                else {
                    observeFollowingPosts();
                }
            }
        });

//        Database db = Database.getInstance();
//        String currentUser = db.getCurrentUser();

//        db.getUserDoc(currentUser).get().addOnSuccessListener(snapshot -> {
//            List<String> followingList = (List<String>) snapshot.get(DatabaseFields.USER_FOLLOWING_FLD);
//
//            if (followingList == null || followingList.isEmpty()) {
//                allMoods.clear();
//                moodHistoryList.clear();
//                adapter.notifyDataSetChanged();
//                emptyFollowingMessage.setText("Your following feed is empty!\nFollow users or wait for them to post moods.");
//                emptyFollowingMessage.setVisibility(View.VISIBLE);
//            } else {
//                observeFollowingPosts();
//            }
//        }).addOnFailureListener(e -> {
//            Log.e(TAG, "Failed to fetch following list", e);
//            emptyFollowingMessage.setText("Failed to load following feed.");
//            emptyFollowingMessage.setVisibility(View.VISIBLE);
//        });
    }

    private void observeFollowingPosts() {
        if (!isAdded() || getActivity() == null) {
            return;
        }
        ViewModelMoodsFollowing viewModelMoods = new ViewModelProvider(requireActivity()).get(ViewModelMoodsFollowing.class);
        viewModelMoods.getData().observe(getViewLifecycleOwner(), posts -> {
            loadingMoodsBar.setVisibility(View.GONE);

            if (!isAdded() || getActivity() == null) {
                return;
            }
            if (posts.isEmpty()) {
                allMoods.clear();
                moodHistoryList.clear();
                adapter.notifyDataSetChanged();
                emptyFollowingMessage.setText("None of your followers have posted yet.\nCheck back later!");
                emptyFollowingMessage.setVisibility(View.VISIBLE);
                return;
            }

            recyclerView.setVisibility(View.VISIBLE);
            Map<String, List<MoodPost>> postsByUser = new HashMap<>();

            for (MoodPost post : posts) {
                String user = post.getUser();
                postsByUser.computeIfAbsent(user, k -> new ArrayList<>());

                List<MoodPost> userPosts = postsByUser.get(user);
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

            aggregatedPosts.sort((p1, p2) -> Long.compare(p2.getTimePosted(), p1.getTimePosted()));

            allMoods.clear();
            allMoods.addAll(aggregatedPosts);
            applyCurrentFilter();
        });
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
        emptyFollowingMessage.setVisibility(moodHistoryList.isEmpty() ? View.VISIBLE : View.GONE);
    }

    public void clearFilters() {
        this.currentFilter = null;
        applyCurrentFilter();
    }
}
