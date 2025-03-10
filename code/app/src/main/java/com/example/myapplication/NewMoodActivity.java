package com.example.myapplication;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class NewMoodActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_PICK_IMAGE = 100;
    private static final int MAX_IMAGE_SIZE_BYTES = 64 * 1024; // 64 KB

    private MaterialToolbar topAppBar;
    private Button uploadPhotoButton;
    private Button postButton;

    // Holds the compressed image data if valid (< 64 KB).
    private byte[] selectedImageData = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.newmood);

        // Set up the top app bar
        topAppBar = findViewById(R.id.topAppBar);
        setSupportActionBar(topAppBar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        topAppBar.setNavigationOnClickListener(v -> onBackPressed());

        // Find your buttons
        uploadPhotoButton = findViewById(R.id.uploadPhotoButton);
        postButton = findViewById(R.id.button);

        // Click listener for "Upload Photo"
        uploadPhotoButton.setOnClickListener(v -> {
            // Launch a file chooser that can pick images from local or cloud sources
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_CODE_PICK_IMAGE);
        });

        // Click listener for "Post"
        postButton.setOnClickListener(v -> {
            // Here, you can do whatever you need with the selected image data
            // and other mood details (e.g., reason text, mood selection, etc.).

            if (selectedImageData != null) {
                // For example, you could upload it to Firebase or attach it to a Mood object.
                Toast.makeText(this, "Photo is ready to upload!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "No valid photo selected or photo was too large.", Toast.LENGTH_SHORT).show();
            }

            // ... the rest of your logic for creating and posting the mood event ...
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Check if the user picked an image
        if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            if (imageUri != null) {
                try {
                    // Convert the selected image to a Bitmap
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

                    // Check final size
                    if (baos.toByteArray().length > MAX_IMAGE_SIZE_BYTES) {
                        // Still too large, show an error message
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
}
