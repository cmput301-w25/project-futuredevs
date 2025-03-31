package com.example.moodmento;

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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.futuredevs.models.ViewModelMoodsFollowing;
import com.futuredevs.models.ViewModelUserProfile;
import com.futuredevs.models.items.MoodPost;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The {@code FollowingHistoryFragment} class is a fragment that acts as a
 * list of the moods of the user's a user follows.
 *
 *  @author Pranav Gupta
 */
public class FollowingHistoryFragment extends Fragment {
    private SwipeRefreshLayout refreshLayout;
    private ProgressBar loadingMoodsBar;
    private RecyclerView recyclerView;
    private MoodHistoryAdapter adapter;
    private List<MoodPost> moodHistoryList = new ArrayList<>();
    private List<MoodPost> allMoods = new ArrayList<>();
    private FilterCriteria currentFilter;
    private TextView emptyFollowingMessage;
    private ViewModelMoodsFollowing modelMoodsFollowing;
    private static final String TAG = "FollowingHistory";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.mood_list_fragment, container, false);

        this.refreshLayout = view.findViewById(R.id.refresh_mood_list);
        this.refreshLayout.setOnRefreshListener(() -> {
            Log.i("FOLLOWING_HISTORY", "Refreshing following content");
            modelMoodsFollowing.requestData();
        });
        this.recyclerView = view.findViewById(R.id.recyclerView);
        this.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        this.emptyFollowingMessage = view.findViewById(R.id.emptyFilterMessage);
        this.loadingMoodsBar = view.findViewById(R.id.loading_bar_moods);

        this.adapter = new MoodHistoryAdapter(getContext(), this.moodHistoryList, false);
        this.recyclerView.setAdapter(this.adapter);

        DividerItemDecoration divider = new DividerItemDecoration(this.recyclerView.getContext(), LinearLayoutManager.VERTICAL);
        Drawable dividerDrawable = ContextCompat.getDrawable(this.getContext(), R.drawable.full_width_divider);

        if (dividerDrawable != null) {
            divider.setDrawable(dividerDrawable);
        }

        this.recyclerView.addItemDecoration(divider);
        this.loadFollowingAndPosts();
        return view;
    }

    /**
     * Registers a listener on the current user's profile and obtains the list
     * of users they follow. If the current user follows other users then a
     * listener is registered for obtaining the posts of the users the current
     * user follows.
     */
    private void loadFollowingAndPosts() {
        ViewModelUserProfile profileModel = new ViewModelProvider(this.requireActivity()).get(ViewModelUserProfile.class);
        profileModel.requestData();
        profileModel.getData().observe(this.getViewLifecycleOwner(), profile -> {
            if (profile == null) {
                Log.e(TAG, "Failed to fetch following list");
                emptyFollowingMessage.setText("Failed to load following feed.");
                emptyFollowingMessage.setVisibility(View.VISIBLE);
                loadingMoodsBar.setVisibility(View.GONE);
            }
            else {
                List<String> followingNames = profile.getFollowing();

                if (followingNames.isEmpty()) {
                    allMoods.clear();
                    moodHistoryList.clear();
                    adapter.notifyDataSetChanged();
                    emptyFollowingMessage.setText("Your following feed is empty!\nFollow users or wait for them to post moods.");
                    emptyFollowingMessage.setVisibility(View.VISIBLE);
                    loadingMoodsBar.setVisibility(View.GONE);
                }
                else {
                    observeFollowingPosts();
                }
            }
        });
    }

    /**
     * Registers an observer for the changes in follow mood post data and
     * handles the updating of the moods to be displayed in the following
     * list.
     */
    private void observeFollowingPosts() {
        if (!isAdded() || getActivity() == null) {
            return;
        }

        this.modelMoodsFollowing = new ViewModelProvider(requireActivity()).get(ViewModelMoodsFollowing.class);
        this.modelMoodsFollowing.getData().observe(getViewLifecycleOwner(), posts -> {
            if (!isAdded() || getActivity() == null) {
                return;
            }

            if (refreshLayout.isRefreshing()) {
                refreshLayout.setRefreshing(false);
            }
            else {
                loadingMoodsBar.setVisibility(View.GONE);
                refreshLayout.setVisibility(View.VISIBLE);
            }

            if (posts.isEmpty()) {
                allMoods.clear();
                moodHistoryList.clear();
                adapter.notifyDataSetChanged();
                emptyFollowingMessage.setText("None of your followers have posted yet.\nCheck back later!");
                emptyFollowingMessage.setVisibility(View.VISIBLE);
                return;
            }
            else {
                emptyFollowingMessage.setVisibility(View.GONE);
            }

            Map<String, List<MoodPost>> postsByUser = new HashMap<>();

            // Obtain only the three most recent posts of each user
            for (MoodPost post : posts) {
                String user = post.getUser();
                postsByUser.computeIfAbsent(user, k -> new ArrayList<>());
                List<MoodPost> userPosts = postsByUser.get(user);

                if (userPosts.size() < 3) {
                    userPosts.add(post);
                }
                else {
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

    /**
     * Applies the given {@code filter} to the following post list.
     *
     * @param filter the filter to apply to the posts
     */
    public void applyEmotionFilter(FilterCriteria filter) {
        this.currentFilter = filter;
        this.applyCurrentFilter();
    }

    /**
     * Applies the currently set filter to the posts being displayed in
     * the list.
     */
    private void applyCurrentFilter() {
        this.moodHistoryList.clear();
        long currentTime = System.currentTimeMillis();
        String timeRange = (this.currentFilter != null) ? this.currentFilter.timeRange : "All time";
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
        }

        for (MoodPost post : this.allMoods) {
            boolean matchesEmotion = this.currentFilter == null
                    || this.currentFilter.emotion.equalsIgnoreCase("ALL")
                    || post.getEmotion().toString().equalsIgnoreCase(this.currentFilter.emotion);
            boolean matchesTime = filterTimeMillis == 0 || post.getTimePosted() >= filterTimeMillis;
            String reason = post.getReason() != null ? post.getReason().toLowerCase() : "";
            boolean matchesTerm = this.currentFilter == null
                    || this.currentFilter.filterWord.isEmpty()
                    || reason.contains(this.currentFilter.filterWord.toLowerCase());

            if (matchesEmotion && matchesTime && matchesTerm) {
                this.moodHistoryList.add(post);
            }
        }

        adapter.notifyDataSetChanged();

        Log.d(TAG, "All moods: " + this.allMoods.size() + ", Filtered moods: " + this.moodHistoryList.size());
        this.emptyFollowingMessage.setVisibility(this.moodHistoryList.isEmpty() ? View.VISIBLE : View.GONE);
    }

    /**
     * Clears the current filter and notifies the following post's list to
     * update with the cleared filter.
     */
    public void clearFilters() {
        this.currentFilter = null;
        this.applyCurrentFilter();
    }
}