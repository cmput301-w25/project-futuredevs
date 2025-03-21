package com.example.myapplication;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
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

        if (this.getActivity() != null) {
			ViewModelMoods viewModelMoods = new ViewModelProvider(this.getActivity())
                                                    .get(ViewModelMoods.class);
            viewModelMoods.getData().observe(this.getViewLifecycleOwner(), o -> {
                moodHistoryList.clear();
                moodHistoryList.addAll(o);
                moodHistoryList.sort((p1, p2) -> -Long.compare(p1.getTimePosted(), p2.getTimePosted()));
                adapter.notifyDataSetChanged();
            });
        }

        adapter = new MoodHistoryAdapter(requireContext(), moodHistoryList, true);
        recyclerView.setAdapter(adapter);

        return view;
    }
}