package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import java.util.ArrayList;
import java.util.List;

/**
 * Fragment that displays Mood History items in Chronological order
 */
public class MoodHistoryFragment extends Fragment {
    private RecyclerView recyclerView;
    private MoodHistoryAdapter adapter;
    private final List<MoodHistory> moodHistoryList = new ArrayList<>();
    /**
     *Creates view for mood history fragment
     ** @param inflater inflates the view.
     * @param container The container to which the fragment's UI should be attached.
     * @param savedInstanceState A bundle that contains saved instance data.
     * @return The view for the fragment
     */
    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_main, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        moodHistoryList.add(new MoodHistory("Happy", "User1", System.currentTimeMillis()));
        moodHistoryList.add(new MoodHistory("Sad", "User2", System.currentTimeMillis()));

        adapter = new MoodHistoryAdapter(moodHistoryList);
        recyclerView.setAdapter(adapter);

        return view;
    }
}
