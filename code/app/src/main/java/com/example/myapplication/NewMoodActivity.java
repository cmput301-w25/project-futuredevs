package com.example.myapplication;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.futuredevs.models.items.MoodPost;
import com.google.android.material.appbar.MaterialToolbar;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * The {@code NewMoodActivity} class represents the screen for adding a new
 * mood post to the user's history.
 */
public class NewMoodActivity extends AppCompatActivity {
    private LocationPerm locationPerm;
    private static final int REQUEST_CODE_PICK_IMAGE = 100;
    private static final int MAX_IMAGE_SIZE_BYTES = 64 * 1024; // 64 KB

    private MaterialToolbar topAppBar;
    private Button uploadPhotoButton;
    private Button postButton;
    private Button locationButton;

    private MoodPost.Emotion selectedEmotion;
    private MoodPost.SocialSituation socialSituation;
    private Location postLocation;
    private String reasonText;
    private String triggerText;
    // Holds the compressed image data if valid (< 64 KB).
    private byte[] selectedImageData = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.newmood);
        Spinner moodSpinner = findViewById(R.id.selectMoodText);
        ArrayAdapter<MoodPost.Emotion> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                MoodPost.Emotion.values()
        );
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        moodSpinner.setAdapter(adapter);
        // Set an item selection listener
        moodSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                selectedEmotion = MoodPost.Emotion.values()[position];
                String selectedMood = "Selected: " + selectedEmotion.name();
                Toast.makeText(getApplicationContext(), selectedMood, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}

        });

        Spinner situationSpinner = findViewById(R.id.selectSituationText);
//      String []social_situation = {"Alone","One other person","Several people","A crowd"};
        ArrayAdapter<MoodPost.SocialSituation> situationadapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                MoodPost.SocialSituation.values()
        );
        // Specify the layout to use when the list of choices appears
        situationadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        situationSpinner.setAdapter(situationadapter);
        // Set an item selection listener
        situationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                socialSituation = MoodPost.SocialSituation.values()[position];
                String selectSituationText = "Selected: " + socialSituation.name();
                Toast.makeText(getApplicationContext(), "Selected: " + selectSituationText, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        this.topAppBar = findViewById(R.id.topAppBar);
//        this.locationButton = this.topAppBar.findViewById(R.id.action_location);
//        this.locationButton.setActivated(false);
//        this.locationButton.setOnClickListener(l -> {
//            if (postLocation == null) {
//                locationPerm = new LocationPerm(this);
//
//                if (!locationPerm.hasLocationPermission()) {
//                    locationPerm.requestLocationPermission();
//
//                    if (locationPerm.hasLocationPermission()) {
//                        getLocation();
//                    }
//                } else {
//                    getLocation();
//                }
//            }
//            else {
//                postLocation = null;
//                locationButton.setActivated(false);
//            }
//        });

        this.setSupportActionBar(this.topAppBar);

        if (this.getSupportActionBar() != null) {
            this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        this.topAppBar.setNavigationOnClickListener(v -> onBackPressed());
        this.uploadPhotoButton = findViewById(R.id.uploadPhotoButton);
        this.postButton = findViewById(R.id.button);

        this.uploadPhotoButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_CODE_PICK_IMAGE);
        });

        this.postButton.setOnClickListener(v -> {
            Intent intent = new Intent(NewMoodActivity.this, HomeActivity.class);
            intent.putExtra("added_post", "");
            intent.putExtra("post_emotion", selectedEmotion.name());

            if (socialSituation != null) {
                intent.putExtra("post_situation", socialSituation.name());
            }

            if (reasonText != null && !reasonText.isEmpty()) {
                intent.putExtra("post_reason", reasonText);
            }

            if (triggerText != null && !triggerText.isEmpty()) {
                intent.putExtra("post_trigger", triggerText);
            }

            if (postLocation != null) {
                intent.putExtra("post_location", postLocation);
            }

            startActivity(intent);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();

            if (imageUri != null) {
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);

                    // Compress the image in a loop until it’s under 64 KB or quality hits 10
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    int quality = 100;
                    bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);

                    while (baos.toByteArray().length > MAX_IMAGE_SIZE_BYTES && quality > 10) {
                        baos.reset();
                        quality -= 10;
                        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);
                    }

                    if (baos.toByteArray().length > MAX_IMAGE_SIZE_BYTES) {
                        Toast.makeText(this, "Photo too large, must be under 64 KB.", Toast.LENGTH_SHORT).show();
                        selectedImageData = null;
                    } else {
                        // Good to go—store the compressed image data
                        selectedImageData = baos.toByteArray();
                        Toast.makeText(this, "Photo selected and is under 64 KB!", Toast.LENGTH_SHORT).show();
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Failed to load image.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LocationPerm.LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                this.getLocation();
            } else {
                Toast.makeText(this, "Location permission is required to access the location.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Attempts to get the location from the user's device and if the user
     * gives permissions then sets the location of this post
     */
    private void getLocation() {
        this.locationPerm.getLastKnownLocation(l -> {
            if (l != null) {
                NewMoodActivity.this.postLocation = l;
                String locationLog = "Location: %f, %f";
//                this.locationButton.setActivated(true);
                Log.d("MainActivity", String.format(locationLog, l.getLatitude(), l.getLongitude()));
            } else {
                Log.d("MainActivity", "Location is null.");
            }
        });
    }
}