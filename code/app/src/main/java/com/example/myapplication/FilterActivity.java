package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.AdapterView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;

public class FilterActivity extends AppCompatActivity {

    private Spinner spinnerMood, spinnerTimeRange;
    private EditText editFilterWord;
    private Button buttonReset, buttonApply;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);

        // Toolbar setup
        MaterialToolbar toolbar = findViewById(R.id.filterToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Find views
        spinnerMood = findViewById(R.id.spinner_mood);
        spinnerTimeRange = findViewById(R.id.spinner_time_range);
        editFilterWord = findViewById(R.id.edit_filter_word);
        buttonReset = findViewById(R.id.button_reset_filter);
        buttonApply = findViewById(R.id.button_apply_filter);

        // Populate mood spinner with "Select mood" and "ALL"
        String[] moods = {"Select mood", "ALL", "HAPPY", "SADNESS", "ANGER", "CONFUSED", "FEAR", "SURPRISED", "SHAME", "DISGUSTED"};
        ArrayAdapter<String> moodAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, moods);
        moodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMood.setAdapter(moodAdapter);
        spinnerMood.setSelection(0);  // Default: Select mood

        // Populate time range spinner with "Select time" and "All time"
        String[] timeRanges = {"Select time", "All time", "Last 24 hours", "Last 7 days", "Last 30 days"};
        ArrayAdapter<String> timeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, timeRanges);
        timeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTimeRange.setAdapter(timeAdapter);
        spinnerTimeRange.setSelection(0);  // Default: Select time

        // Disable Apply by default
        buttonApply.setEnabled(false);
        buttonApply.setAlpha(0.5f);

        setupFilterListeners();

        // Reset button logic
        buttonReset.setOnClickListener(v -> {
            spinnerMood.setSelection(1);  // "ALL"
            spinnerTimeRange.setSelection(1);  // "All time"
            editFilterWord.setText("");

            Intent resultIntent = new Intent();
            resultIntent.putExtra("FILTER_MOOD", "ALL");
            resultIntent.putExtra("FILTER_TIME", "All time");
            resultIntent.putExtra("FILTER_WORD", "");
            setResult(RESULT_OK, resultIntent);
            finish();  // Return to HomeActivity immediately
            Toast.makeText(this, "Filters reset", Toast.LENGTH_SHORT).show();
        });

        // Apply button logic
        buttonApply.setOnClickListener(v -> {
            String selectedMood = spinnerMood.getSelectedItem().toString();
            String timeRange = spinnerTimeRange.getSelectedItem().toString();
            String filterWord = editFilterWord.getText().toString().trim();

            Intent resultIntent = new Intent();
            resultIntent.putExtra("FILTER_MOOD", selectedMood);
            resultIntent.putExtra("FILTER_TIME", timeRange);
            resultIntent.putExtra("FILTER_WORD", filterWord);

            setResult(RESULT_OK, resultIntent);
            finish();
            Toast.makeText(this, "Filters applied", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupFilterListeners() {
        editFilterWord.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                updateApplyButtonState();
            }
        });

        spinnerMood.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateApplyButtonState();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        spinnerTimeRange.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateApplyButtonState();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void updateApplyButtonState() {
        String selectedMood = spinnerMood.getSelectedItem().toString();
        String selectedTime = spinnerTimeRange.getSelectedItem().toString();

        boolean moodValid = !selectedMood.equals("Select mood");
        boolean timeValid = !selectedTime.equals("Select time");

        boolean enableButton = moodValid && timeValid;

        buttonApply.setEnabled(enableButton);
        buttonApply.setAlpha(enableButton ? 1.0f : 0.5f);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
