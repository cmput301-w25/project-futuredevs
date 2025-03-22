package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MoodViewActivity extends AppCompatActivity {

    private TextView moodEmojiTextView;
    private TextView moodTextTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.moodview);

        moodEmojiTextView = findViewById(R.id.moodEmoji);
        moodTextTextView = findViewById(R.id.moodText);

        // gets  mood and emojis from NewMoodActivity
        Intent intent = getIntent();
        String mood = intent.getStringExtra("MOOD");
        String emoji = intent.getStringExtra("EMOJI");

        // sets emoji and moods text in TextViews
        if (mood != null && emoji != null) {
            moodEmojiTextView.setText(emoji);
            moodTextTextView.setText(mood);
        }
    }
}
