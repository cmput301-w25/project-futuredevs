package com.example.myapplication;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;

public class NewMoodActivity extends AppCompatActivity {

    private MaterialToolbar topAppBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // This uses your new_mood.xml layout
        setContentView(R.layout.newmood);

        // Set up the top app bar in new_mood.xml
        topAppBar = findViewById(R.id.topAppBar);
        setSupportActionBar(topAppBar);

        // Optional: enable a back arrow to return to homepage
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Handle clicks on the back arrow
        topAppBar.setNavigationOnClickListener(v -> onBackPressed());

        // TODO: Implement your logic for "Post" button, reason text, mood selection, etc.
    }
}
