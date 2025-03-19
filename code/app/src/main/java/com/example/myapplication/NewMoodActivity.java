package com.example.myapplication;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.ActionMenuItemView;

import com.futuredevs.database.Database;
import com.futuredevs.models.items.MoodPost;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
	private Switch locationSwitch;
    private TextInputLayout reasonLayout;
    private TextInputEditText reasonTextView;
    private View dividerPhoto;
    private ImageView imageView;

    private MoodPost.Emotion selectedEmotion;
    private MoodPost.SocialSituation socialSituation;
    private Location postLocation;
    private String reasonText;
    // Holds the compressed image data if valid (< 64 KB).
    private byte[] selectedImageData = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.newmood);
        this.locationPerm = new LocationPerm(this);
        this.topAppBar = this.findViewById(R.id.topAppBar);
        this.setSupportActionBar(this.topAppBar);

        if (this.getSupportActionBar() != null)  {
            this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        this.topAppBar.setNavigationOnClickListener(v -> onBackPressed());
        this.locationSwitch = this.findViewById(R.id.switch_location_add);
        this.locationSwitch.setOnCheckedChangeListener((button, checked) -> {
            if (checked) {
                if (postLocation == null) {
                   getLocation();

//                   if (!locationPerm.hasLocationPermission()) {
//                       locationPerm.requestLocationPermission();
//
//                       if (locationPerm.hasLocationPermission()) {
//                           getLocation();
//                       }
//                   }
//                   else {
//                       getLocation();
//                   }
               }
               else {
                   postLocation = null;
                   locationSwitch.setChecked(false);
               }
            }
       });

        Spinner moodSpinner = this.findViewById(R.id.spinner_mood_select);
        List<String> emotions = new ArrayList<>();
        emotions.add("Select an emotion");
        Arrays.stream(MoodPost.Emotion.values())
              .map(MoodPost.Emotion::name)
              .forEach(emotions::add);
        ArrayAdapter<String> emotionsAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                emotions
        );
        // Specify the layout to use when the list of choices appears
        emotionsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        moodSpinner.setAdapter(emotionsAdapter);
        // Set an item selection listener
        moodSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                if (position == 0) {
                    selectedEmotion = null;
                }
                else {
                    selectedEmotion = MoodPost.Emotion.values()[position - 1];
                }

                validatePostDetails();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        Spinner situationSpinner = this.findViewById(R.id.spinner_social_situation);
        String[] socialSituations = new String[] {
                "Select an situation (optional)",
                "Alone",
                "One other person",
                "Several people",
                "A crowd"
        };
        ArrayAdapter<String> situationAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                socialSituations
        );
        // Specify the layout to use when the list of choices appears
        situationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        situationSpinner.setAdapter(situationAdapter);
        // Set an item selection listener
        situationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                if (position == 0) {
                    socialSituation = null;
                }
                else {
                    socialSituation = MoodPost.SocialSituation.values()[position - 1];
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        this.reasonLayout = this.findViewById(R.id.layout_text_input_reason);
        this.reasonTextView = this.findViewById(R.id.edittext_reason);
        this.reasonTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                validatePostDetails();
            }
        });
        this.dividerPhoto = this.findViewById(R.id.mood_divider_photo);
        this.imageView = this.findViewById(R.id.mood_view_image);

        this.postButton = this.findViewById(R.id.button_post_mood);
        this.postButton.setEnabled(false);
        this.postButton.setOnClickListener(v -> {
            Intent intent = new Intent(NewMoodActivity.this, HomeActivity.class);
            String name = Database.getInstance().getCurrentUser();
            MoodPost mood = new MoodPost(name, this.selectedEmotion);

            if (this.reasonTextView.getText() != null)
               mood.setReason(this.reasonTextView.getText().toString());

            mood.setSocialSituation(this.socialSituation);
            mood.setLocation(this.postLocation);
            mood.setImageData(this.selectedImageData);
            intent.putExtra("added_post", "");
            intent.putExtra("mood", mood);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        });

        this.uploadPhotoButton = this.findViewById(R.id.button_upload_photo);
        this.uploadPhotoButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(Intent.createChooser(intent, "Select Picture"),
                                   REQUEST_CODE_PICK_IMAGE);
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
                        new AlertDialog.Builder(this)
                                .setTitle("Photo too large!")
                                .setMessage("The selected photo must be under 64kB.")
                                .setNeutralButton("", (d, i) -> {
                                    selectedImageData = null;
                                })
                                .show();
                    }
                    else {
                        // Good to go—store the compressed image data
                        selectedImageData = baos.toByteArray();
                        imageView.setImageBitmap(bitmap);
                        imageView.setVisibility(View.VISIBLE);
                        dividerPhoto.setVisibility(View.VISIBLE);
                    }
                }
                catch (IOException e) {
                    Log.e("MOOD_ADD", "Failed to load the image", e);
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
            }
            else {
                Toast.makeText(this, "Location permission is required to add the location.", Toast.LENGTH_SHORT).show();
                this.locationSwitch.setChecked(false);
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
                Log.d("MainActivity", String.format(locationLog, l.getLatitude(), l.getLongitude()));
            }
            else {
                this.locationSwitch.setChecked(false);
                Log.d("MainActivity", "Location is null.");
            }
        });
    }

    /**
     * Checks to ensure that the reason text is not too long and that the
     * user has selected an emotion before enabling the post button.
     */
    private void validatePostDetails() {
        if (this.reasonTextView.getText().length() > this.reasonLayout.getCounterMaxLength()) {
            this.postButton.setEnabled(false);
        }
        else {
            this.postButton.setEnabled(this.selectedEmotion != null);
        }
    }
}