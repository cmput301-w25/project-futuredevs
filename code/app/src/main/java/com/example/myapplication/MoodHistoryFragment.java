package com.example.myapplication;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.futuredevs.models.ViewModelMoods;
import com.futuredevs.models.items.MoodPost;

import java.util.ArrayList;
import java.util.List;

public class MoodHistoryFragment extends Fragment {
    private SwipeRefreshLayout refreshLayout;
    private ProgressBar loadingMoodsBar;
    private RecyclerView recyclerView;
    private MoodHistoryAdapter adapter;
    private List<MoodPost> moodHistoryList = new ArrayList<>();
    private List<MoodPost> allMoods = new ArrayList<>();
    private FilterCriteria currentFilter;
    private TextView emptyFilterMessage;
    private ViewModelMoods viewModelMoods;

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.mood_list_fragment, container, false);

        this.refreshLayout = view.findViewById(R.id.refresh_mood_list);
        this.refreshLayout.setVisibility(View.GONE);
        this.refreshLayout.setOnRefreshListener(() -> {
            Log.i("HISTORY", "Refreshing mood list");
            viewModelMoods.requestData();
        });

        this.recyclerView = view.findViewById(R.id.recyclerView);
        this.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        this.emptyFilterMessage = view.findViewById(R.id.emptyFilterMessage);
        this.loadingMoodsBar = view.findViewById(R.id.loading_bar_moods);

        this.viewModelMoods = new ViewModelProvider(requireActivity()).get(ViewModelMoods.class);
        this.viewModelMoods.getData().observe(getViewLifecycleOwner(), moods -> {
            if (refreshLayout.isRefreshing()) {
                refreshLayout.setRefreshing(false);
            }
            else {
                loadingMoodsBar.setVisibility(View.GONE);
                refreshLayout.setVisibility(View.VISIBLE);
            }

            allMoods.clear();
            allMoods.addAll(moods);
            allMoods.sort((p1, p2) -> Long.compare(p2.getTimePosted(), p1.getTimePosted()));
            applyCurrentFilter();
        });

        this.adapter = new MoodHistoryAdapter(requireContext(), this.moodHistoryList, true, this);
        this.recyclerView.setAdapter(this.adapter);

        DividerItemDecoration divider = new DividerItemDecoration(this.recyclerView.getContext(), LinearLayoutManager.VERTICAL);
        Drawable dividerDrawable = ContextCompat.getDrawable(this.getContext(), R.drawable.full_width_divider);

        if (dividerDrawable != null) {
            divider.setDrawable(dividerDrawable);
        }

        this.recyclerView.addItemDecoration(divider);
        return view;
    }

    /**
     * Applies the given {@code filter} to the following post list.
     *
     * @param filter the filter to apply to the posts
     */
    public void applyEmotionFilter(FilterCriteria filter) {
        this.currentFilter = filter;
        applyCurrentFilter();
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
            boolean matchesEmotion = this.currentFilter == null || this.currentFilter.emotion.equalsIgnoreCase("ALL") || post.getEmotion().toString().equalsIgnoreCase(currentFilter.emotion);
            boolean matchesTime = filterTimeMillis == 0 || post.getTimePosted() >= filterTimeMillis;
            String reason = post.getReason() != null ? post.getReason().toLowerCase() : "";
            boolean matchesTerm = this.currentFilter == null || this.currentFilter.filterWord.isEmpty() || reason.contains(this.currentFilter.filterWord.toLowerCase());

            if (matchesEmotion && matchesTime && matchesTerm) {
                this.moodHistoryList.add(post);
            }
        }

        this.adapter.notifyDataSetChanged();
        updateEmptyMessage();
    }

    /**
     * Sets the empty message to be displayed when no moods match the currently
     * set filter.
     */
    private void updateEmptyMessage() {
        if (this.moodHistoryList.isEmpty()) {
            StringBuilder message = new StringBuilder();

            if (this.allMoods.isEmpty()) {
                message.append("You have no moods in your history!\nPress the compose button to \ncompose a new mood.");
            }
            else if (this.currentFilter != null) {
                message.append("No moods match your filter:\n");

                if (!this.currentFilter.emotion.equalsIgnoreCase("ALL")) {
                    message.append("Emotion: ").append(this.currentFilter.emotion).append("\n");
                }

                if (!this.currentFilter.filterWord.isEmpty()) {
                    message.append("Keyword: ").append(this.currentFilter.filterWord).append("\n");
                }

                if (!this.currentFilter.timeRange.equalsIgnoreCase("All time")) {
                    message.append("Time: ").append(this.currentFilter.timeRange);
                }
            }
            else {
                message.append("You have no moods in your history!\nPress the compose button to compose a new mood.");
            }

            this.emptyFilterMessage.setText(message.toString().trim());
            this.emptyFilterMessage.setVisibility(View.VISIBLE);
        }
        else {
            this.emptyFilterMessage.setVisibility(View.GONE);
        }
    }

    /**
     * Clears the current filter and notifies the following post's list to
     * update with the cleared filter.
     */
    public void clearFilters() {
        this.currentFilter = null;
        this.applyCurrentFilter();
    }

    /**
     * Removes the given {@code mood} from the history list.
     *
     * @param mood the mood to remove
     */
    public void removeMood(MoodPost mood) {
        for (int i = 0; i < this.allMoods.size(); i++) {
            if (this.allMoods.get(i).getDocumentId().equals(mood.getDocumentId())) {
                this.allMoods.remove(i);
                break;
            }
        }

        this.applyCurrentFilter();
    }

    /**
     * Updates the post that corresponds to the given {@code editedMood} in
     * the list using the new fields from {@code editedMood}.
     *
     * @param editedMood the post to update
     */
    public void updateMoodInList(MoodPost editedMood) {
        for (int i = 0; i < this.allMoods.size(); i++) {
            if (this.allMoods.get(i).getDocumentId().equals(editedMood.getDocumentId())) {
                this.allMoods.set(i, editedMood);
                break;
            }
        }

        this.applyCurrentFilter();
    }
}
