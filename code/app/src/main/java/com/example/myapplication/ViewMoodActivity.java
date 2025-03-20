package com.example.myapplication;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.futuredevs.models.items.MoodPost;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.DateFormat;

/**
 * The {@code ViewMoodActivity} class displays a mood post's details.
 *
 * @author [Daren Xu]
 */
public class ViewMoodActivity extends AppCompatActivity {
    private MoodPost viewingPost;

    private TextView userNameTextView, postTimeTextView, situationTextView, reasonTextView, locationTextView;
    private ImageView moodImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.moodview);

        // Initialize UI elements
        userNameTextView = findViewById(R.id.userName);
        postTimeTextView = findViewById(R.id.postTime);
        situationTextView = findViewById(R.id.situation);
        reasonTextView = findViewById(R.id.reason);
        locationTextView = findViewById(R.id.location);
        moodImageView = findViewById(R.id.birdImage);

        // Retrieve MoodPost object from Intent
        viewingPost = (MoodPost) getIntent().getSerializableExtra("viewingPost");

        if (viewingPost != null) {
            displayMoodPost();
        } else {
            userNameTextView.setText("Error: No Mood Data Found");
        }

        // Set up menu icon click to show popup menu
        ImageView optionsIcon = findViewById(R.id.imageView3);
        optionsIcon.setOnClickListener(view -> showPopupMenu(view));
    }

    private void displayMoodPost() {
        userNameTextView.setText(viewingPost.getUser());
        postTimeTextView.setText("Posted on " + DateFormat.getDateInstance().format(viewingPost.getTimePosted()));

        if (viewingPost.getSocialSituation() != null) {
            situationTextView.setText("Was " + viewingPost.getSocialSituation().name().toLowerCase() + " and felt " + viewingPost.getEmotion().name().toLowerCase() + ".");
        } else {
            situationTextView.setText("Felt " + viewingPost.getEmotion().name().toLowerCase() + ".");
        }

        reasonTextView.setText(viewingPost.getReason() != null ? viewingPost.getReason() : "[No reason provided]");
        locationTextView.setText("Posted from " + viewingPost.getLocation());

        // Convert byte[] to Bitmap and display if an image exists
        if (viewingPost.getImageData() != null && viewingPost.getImageData().length > 0) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(viewingPost.getImageData(), 0, viewingPost.getImageData().length);
            moodImageView.setImageBitmap(bitmap);
        } else {
            moodImageView.setImageResource(R.drawable.bird);
        }
    }

    private void showPopupMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.getMenuInflater().inflate(R.menu.menu_view_mood, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.action_edit) {
                editMood();
                return true;
            } else if (id == R.id.action_delete) {
                confirmDeleteMood();
                return true;
            }
            return false;
        });

        popupMenu.show();
    }

    private void editMood() {
        Intent intent = new Intent(ViewMoodActivity.this, NewMoodActivity.class);
        intent.putExtra("edit_mode", true); // Signal that this is an edit
        intent.putExtra("mood", viewingPost); // Pass the mood object
        startActivity(intent);
    }
    private void confirmDeleteMood() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Mood?")
                .setMessage("Are you sure you want to delete this mood post?")
                .setPositiveButton("Delete", (dialog, which) -> deleteMood())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteMood() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("moods").document(viewingPost.getDocumentId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(ViewMoodActivity.this, "Mood deleted successfully", Toast.LENGTH_SHORT).show();

                    // Redirect to MainActivity and tell it to open NotificationsFragment
                    Intent intent = new Intent(ViewMoodActivity.this, MainActivity.class);
                    intent.putExtra("open_notifications", true);  // Send flag to open NotificationsFragment
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);  // Clears back stack
                    startActivity(intent);
                })
                .addOnFailureListener(e -> Toast.makeText(ViewMoodActivity.this, "Error deleting mood", Toast.LENGTH_SHORT).show());
    }



