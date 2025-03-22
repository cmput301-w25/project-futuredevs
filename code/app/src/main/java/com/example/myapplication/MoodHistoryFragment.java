package com.example.myapplication;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.futuredevs.models.ViewModelMoods;
import com.futuredevs.models.items.MoodPost;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment that displays Mood History items in Chronological order
 */
public class MoodHistoryFragment extends Fragment {
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

        if (this.getActivity() != null) {
            ViewModelMoods viewModelMoods = new ViewModelProvider(this.getActivity())
                    .get(ViewModelMoods.class);

            viewModelMoods.getData().observe(this.getViewLifecycleOwner(), o -> {
                allMoods.clear();
                allMoods.addAll(o);
                allMoods.sort((p1, p2) -> -Long.compare(p1.getTimePosted(), p2.getTimePosted()));
                applyCurrentFilter();
            });
        }

        adapter = new MoodHistoryAdapter(requireContext(), moodHistoryList, true, this);
        recyclerView.setAdapter(adapter);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), LinearLayoutManager.VERTICAL);
        Drawable dividerDrawable = ContextCompat.getDrawable(getContext(), R.drawable.full_width_divider);
        if (dividerDrawable != null) {
            dividerItemDecoration.setDrawable(dividerDrawable);
        }
        recyclerView.addItemDecoration(dividerItemDecoration);

        return view;
    }

    public void applyEmotionFilter(FilterCriteria filter) {
        this.currentFilter = filter;
        applyCurrentFilter();
    }

    private void applyCurrentFilter() {
        moodHistoryList.clear();
        long currentTime = System.currentTimeMillis();
        long filterTimeMillis = 0;

        switch (currentFilter != null ? currentFilter.timeRange : "All time") {
            case "Last 24 hours":
                filterTimeMillis = currentTime - 24 * 60 * 60 * 1000;
                break;
            case "Last 7 days":
                filterTimeMillis = currentTime - 7 * 24 * 60 * 60 * 1000;
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

        if (moodHistoryList.isEmpty()) {
            StringBuilder message = new StringBuilder("You have no moods");
            if (currentFilter != null) {
                if (!currentFilter.emotion.equalsIgnoreCase("ALL")) {
                    message.append(" for this filter: ").append(currentFilter.emotion);
                } else if (!currentFilter.filterWord.isEmpty()) {
                    message.append(" matching term: ").append(currentFilter.filterWord);
                } else if (!currentFilter.timeRange.equalsIgnoreCase("All time")) {
                    message.append(" for time range: ").append(currentFilter.timeRange);
                } else {
                    message.append(" in your history! \n Press the compose button to \n compose a new mood.");
                }
            }
            emptyFilterMessage.setText(message.toString());
            emptyFilterMessage.setVisibility(View.VISIBLE);
        } else {
            emptyFilterMessage.setVisibility(View.GONE);
        }
    }

    public void removeMood(MoodPost mood) {
        allMoods.remove(mood);
        applyCurrentFilter();
    }

    public void clearFilters() {
        this.currentFilter = null;
        applyCurrentFilter();
    }
}