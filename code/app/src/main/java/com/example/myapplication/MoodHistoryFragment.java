package com.example.myapplication;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
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

    private ProgressBar loadingMoodsBar;
    private RecyclerView recyclerView;
    private MoodHistoryAdapter adapter;
    private List<MoodPost> moodHistoryList = new ArrayList<>();
    private List<MoodPost> allMoods = new ArrayList<>();
    private FilterCriteria currentFilter;
    private TextView emptyFilterMessage;

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_main, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        emptyFilterMessage = view.findViewById(R.id.emptyFilterMessage);
        loadingMoodsBar = view.findViewById(R.id.loading_bar_moods);

        ViewModelMoods viewModelMoods = new ViewModelProvider(requireActivity()).get(ViewModelMoods.class);
        viewModelMoods.getData().observe(getViewLifecycleOwner(), moods -> {
            recyclerView.setVisibility(View.VISIBLE);
            loadingMoodsBar.setVisibility(View.GONE);
            allMoods.clear();
            allMoods.addAll(moods);
            allMoods.sort((p1, p2) -> Long.compare(p2.getTimePosted(), p1.getTimePosted()));
            applyCurrentFilter();
        });

        adapter = new MoodHistoryAdapter(requireContext(), moodHistoryList, true, this);
        recyclerView.setAdapter(adapter);

        DividerItemDecoration divider = new DividerItemDecoration(recyclerView.getContext(), LinearLayoutManager.VERTICAL);
        Drawable dividerDrawable = ContextCompat.getDrawable(getContext(), R.drawable.full_width_divider);
        if (dividerDrawable != null) {
            divider.setDrawable(dividerDrawable);
        }
        recyclerView.addItemDecoration(divider);

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
        updateEmptyMessage();
    }

    private void updateEmptyMessage() {
        if (moodHistoryList.isEmpty()) {
            StringBuilder message = new StringBuilder();
            if (allMoods.isEmpty()) {
                message.append("You have no moods in your history!\nPress the compose button to \ncompose a new mood.");
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
                message.append("You have no moods in your history!\nPress the compose button to compose a new mood.");
            }
            emptyFilterMessage.setText(message.toString().trim());
            emptyFilterMessage.setVisibility(View.VISIBLE);
        } else {
            emptyFilterMessage.setVisibility(View.GONE);
        }
    }

//    public void removeMood(MoodPost mood) {
//        allMoods.remove(mood);
//        applyCurrentFilter();
//    }
public void removeMood(MoodPost mood) {
    for (int i = 0; i < allMoods.size(); i++) {
        if (allMoods.get(i).getDocumentId().equals(mood.getDocumentId())) {
            allMoods.remove(i);
            break;
        }
    }
    applyCurrentFilter();
}

    public void clearFilters() {
        this.currentFilter = null;
        applyCurrentFilter();
    }
    public void updateMoodInList(MoodPost editedMood) {
        for (int i = 0; i < allMoods.size(); i++) {
            if (allMoods.get(i).getDocumentId().equals(editedMood.getDocumentId())) {
                allMoods.set(i, editedMood);
                break;
            }
        }
        applyCurrentFilter();
    }
}
