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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.futuredevs.models.items.MoodPost;
import com.google.android.material.appbar.MaterialToolbar;

import java.io.ByteArrayOutputStream;
import java.io.IOException;


/**
 * <p>
 * <strong>NewMoodActivity</strong> is responsible for creating a new Mood event.
 * It lets the user select an emotional state, provide a brief textual reason,
 * choose a social situation, and optionally attach a photo.
 * </p>
 *
 * <p>
 * This class particularly addresses:
 * <ul>
 *   <li><strong>US 02.02.01</strong>: "As a participant, I want to express the reason why for
 *       a mood event using a photograph."</li>
 *   <li><strong>US 02.03.01</strong>: "As a system administrator, I want the storage for each
 *       photographic image to be under 65536 bytes."</li>
 * </ul>
 * </p>
 *
 * <p>
 * The user can select a photo from their device, and the app will automatically
 * compress it to ensure it stays below 64 KB. If it’s still too large, the photo
 * will be rejected with a helpful error message.
 * </p>
 *
 * @author
 *   [Your Name / Team Name]
 * @version
 *   1.0
 */
public class NewMoodActivity extends AppCompatActivity {

    /** A request code to identify photo picking activity results. */

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

    /** Maximum allowed image size in bytes (64 KB). */
    private static final int MAX_IMAGE_SIZE_BYTES = 64 * 1024;

    /** The top app bar (toolbar) shown at the top of the screen. */
    private MaterialToolbar topAppBar;

    /** Button that allows the user to upload a photo from their device. */
    private Button uploadPhotoButton;

    /** Button that finalizes the mood creation (including photo, if selected). */
    private Button postButton;
    private Button locationButton;


    /**
     * Holds the compressed photo data if it’s successfully reduced below
     * the size limit. Will be null if no valid photo is selected.
     */

    private MoodPost.Emotion selectedEmotion;
    private MoodPost.SocialSituation socialSituation;
    private Location postLocation;
    private String reasonText;
    private String triggerText;
    // Holds the compressed image data if valid (< 64 KB).

    private byte[] selectedImageData = null;

    /**
     * Called when the activity is first created. Initializes the spinners for selecting
     * mood and social situation, sets up the toolbar, and configures the buttons for
     * photo upload and posting the new mood.
     *
     * @param savedInstanceState The previously saved instance state, if any.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.newmood);

        // ----------------------------------------------------------
        // 1. Setup Spinners for Mood and Social Situation
        // ----------------------------------------------------------
        Spinner moodSpinner = findViewById(R.id.selectMoodText);

        String[] moods = {"HAPPY","ANGER", "CONFUSED", "DISGUSTED", "FEAR", "SADNESS", "SHAME", "SURPRISED"};
        ArrayAdapter<String> moodAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                moods

        ArrayAdapter<MoodPost.Emotion> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                MoodPost.Emotion.values()

        );
        moodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        moodSpinner.setAdapter(moodAdapter);
        moodSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                selectedEmotion = MoodPost.Emotion.values()[position];
                String selectedMood = "Selected: " + selectedEmotion.name();
                Toast.makeText(getApplicationContext(), selectedMood, Toast.LENGTH_SHORT).show();
            }

            @Override

            public void onNothingSelected(AdapterView<?> adapterView) {
                // Do nothing if no mood is selected
            }
        });

        Spinner situationSpinner = findViewById(R.id.selectSituationText);
        String[] socialSituations = {"Alone", "One other person", "Several people", "A crowd"};
        ArrayAdapter<String> situationAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                socialSituations
        );
        situationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        situationSpinner.setAdapter(situationAdapter);
        situationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                String selectedSituation = adapterView.getItemAtPosition(position).toString();
                Toast.makeText(getApplicationContext(), "Selected: " + selectedSituation, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // Do nothing if no situation is selected
            }
        });

        // ----------------------------------------------------------
        // 2. Setup Top App Bar (Toolbar)
        // ----------------------------------------------------------
        topAppBar = findViewById(R.id.topAppBar);
        setSupportActionBar(topAppBar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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


        // ----------------------------------------------------------
        // 3. Setup Buttons
        // ----------------------------------------------------------
        uploadPhotoButton = findViewById(R.id.uploadPhotoButton);
        postButton = findViewById(R.id.button);

        // Launches a file chooser when the user clicks "Upload Photo".
        uploadPhotoButton.setOnClickListener(v -> {

        this.topAppBar.setNavigationOnClickListener(v -> onBackPressed());
        this.uploadPhotoButton = findViewById(R.id.uploadPhotoButton);
        this.postButton = findViewById(R.id.button);

        this.uploadPhotoButton.setOnClickListener(v -> {

            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_CODE_PICK_IMAGE);
        });


        // Attempts to "post" the mood, including the selected image data if any.
        postButton.setOnClickListener(v -> {
            // If we have a valid photo under 64 KB, proceed. Otherwise, show an error.
            if (selectedImageData != null) {
                Toast.makeText(this, "Photo is ready to upload!", Toast.LENGTH_SHORT).show();
                // Here, you might upload it to Firebase or attach it to a Mood object.
                // You would also gather the reason text and any other mood details.
            } else {
                Toast.makeText(this, "No valid photo selected or photo was too large.", Toast.LENGTH_SHORT).show();
            }

            // ... additional logic for creating and posting the mood event ...

        this.postButton.setOnClickListener(v -> {
            Intent intent = new Intent(NewMoodActivity.this, HomeActivity.class);
            intent.putExtra("added_post", "");
            intent.putExtra("post_emotion", selectedEmotion.name());

            if (socialSituation != null) {
                intent.putExtra("post_situation", socialSituation.name());
            }

            // TODO: Read text from text boxes to get these
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

    /**
     * Receives the result from the file chooser or other started activities.
     * <p>
     * Specifically, if the user has chosen an image, this method compresses the image
     * until it’s under 64 KB (or until quality cannot be reduced further). If the image
     * remains too large, it’s rejected.
     * </p>
     *
     * <p>
     * <strong>Why it matters:</strong> This fulfills US 02.03.01 by ensuring that all
     * photos stored are under 65536 bytes, and supports US 02.02.01 by letting users
     * express their mood reason via a photograph.
     * </p>
     *
     * @param requestCode identifies which request triggered this callback
     * @param resultCode  indicates success or failure of the result
     * @param data        the Intent carrying the returned data (e.g., the image Uri)
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();

            if (imageUri != null) {
                try {

                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);

                    // Prepare to compress the image.
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    int quality = 100;
                    bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);

                    // Gradually reduce quality until the image is under 64 KB or quality hits 10.
                    while (baos.toByteArray().length > MAX_IMAGE_SIZE_BYTES && quality > 10) {
                        baos.reset();
                        quality -= 10;
                        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);
                    }


                    if (baos.toByteArray().length > MAX_IMAGE_SIZE_BYTES) {
                        Toast.makeText(this, "Photo too large, must be under 64 KB.", Toast.LENGTH_SHORT).show();
                        selectedImageData = null;
                    } else {
                        // The image is within the allowed size – store it.
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

