package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.TransitionInflater;

import com.futuredevs.database.Database;
import com.futuredevs.database.DatabaseResult;
import com.futuredevs.database.IResultListener;
import com.futuredevs.models.ViewModelComments;
import com.futuredevs.models.items.MoodComment;
import com.futuredevs.models.items.MoodPost;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Daren Xu, Spencer Schmidt
 */
public class ViewMoodFragment extends Fragment {
	private TextView userNameTextView;
	private TextView postTimeTextView;
	private TextView situationTextView;
	private TextView reasonTextView;
	private ImageView moodImageView;
	private ImageView commentButton;
	private RecyclerView commentListView;
	private CommentAdapter commentsAdapter;
	private List<MoodComment> comments;
	private ProgressBar loadingCommentsBar;
	private MoodPost viewingPost;
	private ViewModelComments commentsModel;

	public static ViewMoodFragment newInstance(MoodPost post) {
		ViewMoodFragment fragment = new ViewMoodFragment();
		Bundle args = new Bundle();
		args.putParcelable("post", post);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (this.getArguments() != null) {
			this.viewingPost = this.getArguments().getParcelable("post");
		}
	}

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater,
							 @Nullable ViewGroup container,
							 @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.moodview, container, false);

		this.userNameTextView = view.findViewById(R.id.text_mood_view_name);
		this.postTimeTextView = view.findViewById(R.id.text_mood_view_time);
		this.situationTextView = view.findViewById(R.id.text_mood_view_situation);
		this.reasonTextView = view.findViewById(R.id.text_mood_view_reason);
		this.moodImageView = view.findViewById(R.id.image_mood_view);
		this.commentButton = view.findViewById(R.id.image_mood_comment);
		this.commentButton.setOnClickListener(v -> {
			CommentBottomSheet bottomSheet = CommentBottomSheet.newInstance(viewingPost);
			bottomSheet.show(getActivity().getSupportFragmentManager(), "COMMENT");
		});

		this.commentListView = view.findViewById(R.id.list_mood_comments);
		this.commentListView.setLayoutManager(new LinearLayoutManager(this.getContext()));
		this.comments = new ArrayList<>();
		this.commentsAdapter = new CommentAdapter(this.getContext(), this.comments);
		this.commentListView.setAdapter(this.commentsAdapter);
		this.commentsModel = new ViewModelProvider(this.requireActivity()).get(ViewModelComments.class);
		this.commentsModel.getData().observe(this.getViewLifecycleOwner(), o -> {
			comments.clear();
			comments.addAll(o);

			if (loadingCommentsBar.getVisibility() == View.VISIBLE) {
				loadingCommentsBar.setVisibility(View.GONE);
				commentListView.setVisibility(View.VISIBLE);
			}

			commentsAdapter.notifyDataSetChanged();
		});
		this.loadingCommentsBar = view.findViewById(R.id.loading_mood_view_comments);
		this.loadingCommentsBar.setVisibility(View.GONE);
		this.commentListView.setVisibility(View.GONE);

		if (this.viewingPost != null) {
			ImageView optionMenu = view.findViewById(R.id.image_mood_view_options);
			optionMenu.setOnClickListener(this::showPopupMenu);

			if (this.viewingPost.getNumTopLevelComments() > 0) {
				this.loadingCommentsBar.setVisibility(View.VISIBLE);
				this.commentsModel.requestTopLevelComments(this.viewingPost);
			}

			if (!this.viewingPost.getUser().equals(Database.getInstance().getCurrentUser())) {
				optionMenu.setVisibility(View.GONE);
			}

			this.displayMoodPost(view);
		}
		else {
			userNameTextView.setText("Error: No Mood Data Found");
		}

		return view;
	}

	@Override
	public void onResume() {
		super.onResume();
		this.commentListView.setVisibility(View.GONE);

		if (this.viewingPost.getNumTopLevelComments() > 0) {
			this.loadingCommentsBar.setVisibility(View.VISIBLE);
			this.commentsModel.requestTopLevelComments(this.viewingPost);
		}
	}

	/**
	 * Initializes all of the views for the post with their appropriate values.
	 */
	private void displayMoodPost(View parentView) {
		String userName = this.viewingPost.getUser();
		this.userNameTextView.setText(userName);

		this.userNameTextView.setOnClickListener(view -> {
			if (getActivity() instanceof ViewMoodUserActivity) {
				ViewProfileFragment profileFragment = ViewProfileFragment.newInstance(userName);
				((ViewMoodUserActivity) getActivity())
						.setFragment(ViewMoodUserActivity.PROFILE_FRAGMENT, profileFragment);
			}
		});

		String datePosted = this.viewingPost.getDatePostedLocaleRepresentation();
		String timePosted = this.viewingPost.getTimePostedLocaleRepresentation();
		String timeDateLocation = String.format("Posted on %s at %s", datePosted, timePosted);

		if (this.viewingPost.hasValidLocation()) {
			timeDateLocation = String.format("Posted on %s at %s from %s",
											 datePosted, timePosted,
											 this.viewingPost.getCityLocation(this.getContext()));
		}

		this.postTimeTextView.setText(timeDateLocation);
		String emotionStr = this.viewingPost.getEmotion().name().toLowerCase();

		if (this.viewingPost.getSocialSituation() != null) {
			MoodPost.SocialSituation situation = this.viewingPost.getSocialSituation();
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

		View situationReasonDiv = parentView.findViewById(R.id.divider_mood_sit_reason);

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
		}
	}

	/**
	 * Displays the popup menu for editing and deleting of the mood
	 * being viewed.
	 *
	 * @param view the view to attach the popup menu to
	 */
	private void showPopupMenu(View view) {
		PopupMenu popupMenu = new PopupMenu(this.getContext(), view);
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
		Intent intent = new Intent(this.getContext(), NewMoodActivity.class);
		intent.putExtra("edit_mode", true); // Signal that this is an edit
		intent.putExtra("mood", viewingPost); // Pass the mood object
		startActivity(intent);
	}

	private void confirmDeleteMood() {
		new AlertDialog.Builder(this.getContext())
				.setTitle("Delete Mood?")
				.setMessage("Are you sure you want to delete this mood post?")
				.setPositiveButton("Delete", (dialog, which) -> deleteMood())
				.setNegativeButton("Cancel", null)
				.show();
	}

//	private void deleteMood() {
//		IResultListener listener = r -> {
//			if (r == DatabaseResult.SUCCESS) {
//				Toast.makeText(this.getContext(), "Mood deleted successfully", Toast.LENGTH_SHORT).show();
//				Intent intent = new Intent(ViewMoodFragment.this.getActivity(), HomeActivity.class);
//				intent.putExtra("open_notifications", true);
//				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//				startActivity(intent);
//			}
//			else {
//				Toast.makeText(this.getContext(), "Error deleting mood", Toast.LENGTH_SHORT).show();
//			}
//		};
//
//		Database.getInstance().removeMood(this.viewingPost.getUser(), this.viewingPost, listener);
//	}

	private void deleteMood() {
		final Context context = getContext();
		if (context == null) {
			Log.e("ViewMoodFragment", "deleteMood: Context is null; cannot proceed.");
			return;
		}

		IResultListener listener = r -> {
			if (r == DatabaseResult.SUCCESS) {
				Toast.makeText(context, "Mood deleted successfully", Toast.LENGTH_SHORT).show();
				if (context instanceof android.app.Activity) {
					Intent intent = new Intent(context, HomeActivity.class);
					intent.putExtra("open_notifications", true);
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
					context.startActivity(intent);
				} else {
					Log.e("ViewMoodFragment", "Context is not an Activity, cannot start HomeActivity");
				}
			} else {
				Toast.makeText(context, "Error deleting mood", Toast.LENGTH_SHORT).show();
			}
		};

		Database.getInstance().removeMood(this.viewingPost.getUser(), this.viewingPost, listener);
	}

}
