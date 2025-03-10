package com.example.myapplication;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.Toast;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

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
    private static final int REQUEST_CODE_PICK_IMAGE = 100;

    /** Maximum allowed image size in bytes (64 KB). */
    private static final int MAX_IMAGE_SIZE_BYTES = 64 * 1024;

    /** The top app bar (toolbar) shown at the top of the screen. */
    private MaterialToolbar topAppBar;

    /** Button that allows the user to upload a photo from their device. */
    private Button uploadPhotoButton;

    /** Button that finalizes the mood creation (including photo, if selected). */
    private Button postButton;

    /**
     * Holds the compressed photo data if it’s successfully reduced below
     * the size limit. Will be null if no valid photo is selected.
     */
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
        );
        moodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        moodSpinner.setAdapter(moodAdapter);
        moodSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                String selectedMood = adapterView.getItemAtPosition(position).toString();
                Toast.makeText(getApplicationContext(), "Selected: " + selectedMood, Toast.LENGTH_SHORT).show();
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
        }
        topAppBar.setNavigationOnClickListener(v -> onBackPressed());

        // ----------------------------------------------------------
        // 3. Setup Buttons
        // ----------------------------------------------------------
        uploadPhotoButton = findViewById(R.id.uploadPhotoButton);
        postButton = findViewById(R.id.button);

        // Launches a file chooser when the user clicks "Upload Photo".
        uploadPhotoButton.setOnClickListener(v -> {
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

        // Check if this is our image pick request and the user has indeed picked an image.
        if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            if (imageUri != null) {
                try {
                    // Convert the selected image into a Bitmap.
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

                    // Final check: if the image is still too large, inform the user.
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
