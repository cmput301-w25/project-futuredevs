package com.example.myapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;

public class NewMoodActivity extends AppCompatActivity {

    private MaterialToolbar topAppBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.newmood);
        Spinner moodSpinner = findViewById(R.id.selectMoodText);
        String[] moods = {"Happy", "Sad", "Excited", "Angry", "Relaxed", "Confused"};
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item, // Default layout for the closed spinner
                moods
        );
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        moodSpinner.setAdapter(adapter);
        // Set an item selection listener
        moodSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                String selectedMood = adapterView.getItemAtPosition(position).toString();
                Toast.makeText(getApplicationContext(), "Selected: " + selectedMood, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // Do nothing
            }

        });
        Spinner situationSpinner = findViewById(R.id.selectSituationText);
        String []social_situation = {"Alone","One other person","Several people","A crowd"};
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<String> situationadapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item, // Default layout for the closed spinner
                social_situation
        );

        // Specify the layout to use when the list of choices appears
        situationadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        situationSpinner.setAdapter(situationadapter);

        // Set an item selection listener
        situationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
        String selectSituationText = adapterView.getItemAtPosition(position).toString();
       Toast.makeText(getApplicationContext(), "Selected: " + selectSituationText, Toast.LENGTH_SHORT).show();
                                                       }
       @Override
       public void onNothingSelected(AdapterView<?> adapterView) {
        // Do nothing
        }});

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