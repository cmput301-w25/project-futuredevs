package com.example.myapplication;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ListView listView;
    private List<MoodEvent> moodEventList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

                //  ListView
        listView = findViewById(R.id.moodEventListView);


                // this Sorts the mood events for timestamp in reverse chronological order with most recent first, like in user story 5.03.01
        Collections.sort(moodEventList, (a, b) -> b.getTimestamp().compareTo(a.getTimestamp()));

                    // this gets data ready for the ListView
        List<String> displayList = new ArrayList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

            // this makes  list of strings to show in ListView, including mood and timestamp
        for (MoodEvent event : moodEventList) {
            String displayText = event.getMood() + " - " + dateFormat.format(event.getTimestamp());
            displayList.add(displayText);
        }

            // this makes an ArrayAdapter to join  string list to  ListView
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, displayList);

                        // this set  adapter to the ListView
        listView.setAdapter(adapter);
    }
}

