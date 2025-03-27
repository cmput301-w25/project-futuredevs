package com.example.myapplication;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.futuredevs.models.ViewModelComments;
import com.futuredevs.models.items.MoodComment;

import java.util.ArrayList;
import java.util.List;

/**
 * The {@code ViewCommentFragment} class is a fragment that allows the display
 * of a list of comments along with a top level comment. The top level comment
 * will be displayed with all of its information at the top of the view, and
 * underneath it is a list of comments.
 *
 * @author Spencer Schmidt
 */
public class ViewCommentFragment extends Fragment {
	/** The tag used for logging activities within the fragment. */
	private static final String VIEW_COMMENT_TAG = "VIEW_COMMENT";
	private RecyclerView repliesListView;
	private CommentAdapter repliesAdapter;
	private List<MoodComment> replies;
	private ProgressBar loadingCommentsBar;
	private MoodComment topComment;
	private ViewModelComments commentsModel;

	/**
	 * Creates a new instance of a {@code ViewCommentFragment} where
	 * {@code comment} is the comment that will be used as the top
	 * comment in the view and from which the replies will be obtained
	 * from.
	 *
	 * @param comment the comment to display
	 *
	 * @return a {@code ViewCommentFragment} instance with the top comment
	 * 		   given by {@code comment}
	 */
	public static ViewCommentFragment newInstance(MoodComment comment) {
		ViewCommentFragment fragment = new ViewCommentFragment();
		Bundle args = new Bundle();
		args.putParcelable("comment", comment);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (this.getArguments() != null) {
			this.topComment = this.getArguments().getParcelable("comment");
		}
	}

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater,
							 @Nullable ViewGroup container,
							 @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_comments_view, container, false);

		TextView replyingToTextView = view.findViewById(R.id.text_comment_replying_to);
		String replyingToStr = "Replying to %s";
		String replyingToText;

		if (this.topComment.getParentComment() != null) {
			String replyingName = this.topComment.getParentComment().getPosterName();
			replyingToText = String.format(replyingToStr, replyingName);
		}
		else {
			replyingToText = String.format(replyingToStr, this.topComment.getParentPost().getUser());
		}

		replyingToTextView.setText(replyingToText);

		TextView userNameTextView = view.findViewById(R.id.text_comment_view_name);
		String userName = this.topComment.getPosterName();
		userNameTextView.setText(userName);

		userNameTextView.setOnClickListener(v -> {
			if (getActivity() instanceof ViewMoodUserActivity) {
				ViewProfileFragment profileFragment = ViewProfileFragment.newInstance(userName);
				((ViewMoodUserActivity) getActivity())
						.setFragment(ViewMoodUserActivity.PROFILE_FRAGMENT, profileFragment);
			}
		});

		TextView commentTextView = view.findViewById(R.id.text_comment_view_text);
		commentTextView.setText(this.topComment.getCommentText());

		TextView postTimeTextView = view.findViewById(R.id.text_comment_view_time);
		String datePosted = this.topComment.getDateCommentedLocaleRepresentation();
		String timePosted = this.topComment.getTimeCommentedLocaleRepresentation();
		String timeDateLocation = String.format("Posted on %s at %s", datePosted, timePosted);
		postTimeTextView.setText(timeDateLocation);

		this.replies = new ArrayList<>();
		this.repliesListView = view.findViewById(R.id.list_comment_view_replies);
		this.repliesListView.setLayoutManager(new LinearLayoutManager(this.getContext()));
		this.repliesAdapter = new CommentAdapter(this.getContext(), this.replies);
		this.repliesListView.setAdapter(this.repliesAdapter);

		this.commentsModel = new ViewModelProvider(this.requireActivity()).get(ViewModelComments.class);
		this.commentsModel.getData().observe(this.getViewLifecycleOwner(), o -> {
			String commentLog = "Replies for %s's comment obtained, populating view";
			Log.i(VIEW_COMMENT_TAG, String.format(commentLog, topComment.getPosterName()));
			replies.clear();
			replies.addAll(o);

			if (loadingCommentsBar.getVisibility() == View.VISIBLE) {
				loadingCommentsBar.setVisibility(View.GONE);
				repliesListView.setVisibility(View.VISIBLE);
			}

			repliesAdapter.notifyDataSetChanged();
		});

		ImageButton commentButton = view.findViewById(R.id.image_comment_view_comment);
		commentButton.setOnClickListener(v -> {
			CommentBottomSheet bottomSheet = CommentBottomSheet.newInstance(topComment);
			bottomSheet.show(getActivity().getSupportFragmentManager(), "COMMENT");
		});

		this.loadingCommentsBar = view.findViewById(R.id.loading_comment_view);
		this.loadingCommentsBar.setVisibility(View.GONE);
		this.repliesListView.setVisibility(View.GONE);

		if (this.topComment.getNumSubReplies() > 0) {
			this.loadingCommentsBar.setVisibility(View.VISIBLE);
			this.commentsModel.requestSubComments(this.topComment);
		}

		return view;
	}

	/**
	 * Reloads the correct comments for the view when the view is returned
	 * from the backstack.
	 */
	@Override
	public void onResume() {
		super.onResume();
		// Because we work with a single ViewModel that provides the comments,
		// we hide the comment list while re-loading the comments.
		this.repliesListView.setVisibility(View.GONE);

		if (this.topComment.getNumSubReplies() > 0) {
			String commentLog = "Re-requesting sub comments for %s's comment";
			Log.i(VIEW_COMMENT_TAG, String.format(commentLog, this.topComment.getPosterName()));
			this.loadingCommentsBar.setVisibility(View.VISIBLE);
			this.commentsModel.requestSubComments(this.topComment);
		}
	}
}