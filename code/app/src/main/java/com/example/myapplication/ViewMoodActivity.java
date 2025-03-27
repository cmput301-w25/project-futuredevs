package com.example.myapplication;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.TransitionInflater;

import com.futuredevs.database.Database;
import com.futuredevs.models.items.MoodComment;

import com.futuredevs.models.items.MoodPost;
import com.futuredevs.models.items.MoodPost.SocialSituation;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

/**
 * The {@code ViewMoodActivity} class displays a mood post's details.
 *
 * @author Daren Xu
 */
public class ViewMoodActivity extends AppCompatActivity {
    private MoodPost viewingPost;

    private TextView userNameTextView, postTimeTextView, situationTextView, reasonTextView;
    private ImageView moodImageView;
    private RecyclerView commentListView;
    private CommentAdapter commentsAdapter;
    private List<MoodComment> comments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.moodview);

        // Initialize UI elements
        userNameTextView = findViewById(R.id.text_mood_view_name);
        postTimeTextView = findViewById(R.id.text_mood_view_time);
        situationTextView = findViewById(R.id.text_mood_view_situation);
        reasonTextView = findViewById(R.id.text_mood_view_reason);
        moodImageView = findViewById(R.id.image_mood_view);
        commentListView = findViewById(R.id.list_mood_comments);
        commentListView.setLayoutManager(new LinearLayoutManager(this));

        // Retrieve MoodPost object from Intent
        viewingPost = getIntent().getParcelableExtra("viewingPost");
        ImageView optionMenu = this.findViewById(R.id.image_mood_view_options);

        this.comments = new ArrayList<>();
        this.commentsAdapter = new CommentAdapter(this, this.comments);
        this.commentListView.setAdapter(this.commentsAdapter);

        if (viewingPost != null) {
            if (!this.viewingPost.getUser().equals(Database.getInstance().getCurrentUser())) {
                optionMenu.setVisibility(View.GONE);
            }

             displayMoodPost();
         }
         else {
            userNameTextView.setText("Error: No Mood Data Found");
        }

        // Set up menu icon click to show popup menu


        ImageView optionsIcon = findViewById(R.id.image_mood_view);
        String currentUser = Database.getInstance().getCurrentUser();
        optionsIcon.setOnClickListener(view -> showPopupMenu(view));
        if (viewingPost != null && viewingPost.getUser().equals(currentUser)) {
            optionsIcon.setOnClickListener(view -> showPopupMenu(view));
        } else {
            optionsIcon.setVisibility(View.GONE);
        }

    }

    /**
     * Initializes all of the views for the post with their appropriate values.
     */
    private void displayMoodPost() {
        this.userNameTextView.setText(this.viewingPost.getUser());
        String datePosted = this.viewingPost.getDatePostedLocaleRepresentation();
        String timePosted = this.viewingPost.getTimePostedLocaleRepresentation();
        String timeDateLocation = String.format("Posted on %s at %s", datePosted, timePosted);

        if (this.viewingPost.hasValidLocation()) {
            timeDateLocation = String.format("Posted on %s at %s from %s",
                                             datePosted, timePosted,
                                             this.viewingPost.getCityLocation(this));
        }

        this.postTimeTextView.setText(timeDateLocation);
        String emotionStr = this.viewingPost.getEmotion().name().toLowerCase();

        if (this.viewingPost.getSocialSituation() != null) {
            SocialSituation situation = this.viewingPost.getSocialSituation();
            StringBuilder sitEmotionBuilder = new StringBuilder();
            sitEmotionBuilder.append("Was ");

            switch (situation) {
                case ALONE:
                    sitEmotionBuilder.append("alone");
                    break;
                case ONE_PERSON:
                    sitEmotionBuilder.append("with another person");
                    break;
                case MULTIPLE_PEOPLE:
                    sitEmotionBuilder.append("with multiple people");
                    break;
                case CROWD:
                    sitEmotionBuilder.append("with a crowd");
            }

            sitEmotionBuilder.append(" and felt %s.");
            String sitEmotionText = String.format(sitEmotionBuilder.toString(), emotionStr);
            this.situationTextView.setText(sitEmotionText);
        }
        else {
            String emotionText = String.format("Was feeling %s", emotionStr);
            this.situationTextView.setText(emotionText);
        }

        View situationReasonDiv = this.findViewById(R.id.divider_mood_sit_reason);

        if (this.viewingPost.getReason() != null && !this.viewingPost.getReason().isEmpty()) {
            situationReasonDiv.setVisibility(View.VISIBLE);
            this.reasonTextView.setVisibility(View.VISIBLE);
            this.reasonTextView.setText(this.viewingPost.getReason());
        }
        else {
            situationReasonDiv.setVisibility(View.GONE);
            this.reasonTextView.setVisibility(View.GONE);
        }

        String base64Image = this.viewingPost.getImageData();

        if (base64Image != null && !base64Image.isEmpty()) {
            byte[] imageBytes = Base64.decode(base64Image, Base64.NO_PADDING | Base64.NO_WRAP | Base64.URL_SAFE);
            Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            this.moodImageView.setImageBitmap(bitmap);
            this.moodImageView.setVisibility(View.VISIBLE);
        }
        else {
            this.moodImageView.setVisibility(View.GONE);
//            this.moodImageView.setImageResource(R.drawable.bird);
        }
    }

    /**
     * Displays the popup menu for editing and deleting of the mood
     * being viewed.
     *
     * @param view the view to attach the popup menu to
     */
    private void showPopupMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.getMenuInflater().inflate(R.menu.menu_view_mood, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();

            if (id == R.id.action_edit) {
                editMood();
                return true;
            }
            else if (id == R.id.action_delete) {
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
                    Intent intent = new Intent(ViewMoodActivity.this, MainActivity.class);
                    intent.putExtra("open_notifications", true);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .addOnFailureListener(e -> Toast.makeText(ViewMoodActivity.this, "Error deleting mood", Toast.LENGTH_SHORT).show());
    }
}






