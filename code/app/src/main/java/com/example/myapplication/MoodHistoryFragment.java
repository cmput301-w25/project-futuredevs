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

    /**
     * Creates view for mood history fragment
     *
     * @param inflater inflates the view.
     * @param container The container to which the fragment's UI should be attached.
     * @param savedInstanceState A bundle that contains saved instance data.
     *
     * @return The view for the fragment
     */
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

        adapter = new MoodHistoryAdapter(requireContext(), moodHistoryList, true);
        recyclerView.setAdapter(adapter);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), LinearLayoutManager.VERTICAL);
        Drawable dividerDrawable = ContextCompat.getDrawable(getContext(), R.drawable.full_width_divider);
        if (dividerDrawable != null) {
            dividerItemDecoration.setDrawable(dividerDrawable);
        }
        recyclerView.addItemDecoration(dividerItemDecoration);

        return view;
    }

    /**
     * Applies an external emotion filter and refreshes the mood list accordingly.
     *
     * @param filter The filter criteria containing the selected emotion and time range.
     */
    public void applyEmotionFilter(FilterCriteria filter) {
        this.currentFilter = filter;
        applyCurrentFilter();
    }

    /**
     * Filters the allMoods list based on currentFilter and updates the UI.
     */
    private void applyCurrentFilter() {
        moodHistoryList.clear();

        long currentTime = System.currentTimeMillis();
        long cutoffTime = 0;

        if (currentFilter != null) {
            switch (currentFilter.timeRange) {
                case "Last 24 hours":
                    cutoffTime = currentTime - (24L * 60 * 60 * 1000);
                    break;
                case "Last 7 days":
                    cutoffTime = currentTime - (7L * 24 * 60 * 60 * 1000);
                    break;
                case "Last 30 days":
                    cutoffTime = currentTime - (30L * 24 * 60 * 60 * 1000);
                    break;
                case "All time":
                default:
                    cutoffTime = 0;
                    break;
            }
        }

        for (MoodPost post : allMoods) {
            boolean matchesEmotion = currentFilter == null || currentFilter.emotion.equalsIgnoreCase("ALL") || post.getEmotion().toString().equalsIgnoreCase(currentFilter.emotion);
            boolean matchesTime = cutoffTime == 0 || post.getTimePosted() >= cutoffTime;

            if (matchesEmotion && matchesTime) {
                moodHistoryList.add(post);
            }
        }

        adapter.notifyDataSetChanged();

        if (moodHistoryList.isEmpty()) {
            if (currentFilter != null && !currentFilter.emotion.equalsIgnoreCase("ALL")) {
                emptyFilterMessage.setText("You have no moods for this filter: " + currentFilter.emotion);
            } else {
                emptyFilterMessage.setText("You have no moods in your history! \n Press the compose button to \n compose a new mood.");
            }
            emptyFilterMessage.setVisibility(View.VISIBLE);
        } else {
            emptyFilterMessage.setVisibility(View.GONE);
        }
    }
}
