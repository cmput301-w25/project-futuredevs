package com.example.myapplication;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private MoodEventAdapter adapter;
    private List<MoodEvent> moodEventList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Sample mood events (Replace with actual data retrieval later)
        moodEventList = new ArrayList<>();
        moodEventList.add(new MoodEvent("Happy", new Date(System.currentTimeMillis() - 1000000)));
        moodEventList.add(new MoodEvent("Sad", new Date(System.currentTimeMillis() - 500000)));
        moodEventList.add(new MoodEvent("Excited", new Date(System.currentTimeMillis())));

        // Sort mood events by timestamp (Most recent first)
        Collections.sort(moodEventList, (a, b) -> b.getTimestamp().compareTo(a.getTimestamp()));

        // Set adapter
        adapter = new MoodEventAdapter(moodEventList);
        recyclerView.setAdapter(adapter);
    }
}
