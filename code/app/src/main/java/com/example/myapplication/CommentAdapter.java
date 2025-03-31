package com.example.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.futuredevs.models.items.MoodComment;

import java.util.List;

/**
 * The {@code CommentAdapter} class is a {@code RecyclerView} adapter
 * containing a list of {@code MoodComment} instances.
 *
 * @author Spencer Schmidt
 */
public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentHolder> {
	private final Context context;
	private List<MoodComment> comments;

	public CommentAdapter(Context context, List<MoodComment> comments) {
		this.context = context;
		this.comments = comments;
	}

	@NonNull
	@Override
	public CommentAdapter.CommentHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.mood_view_comment, parent, false);
		return new CommentHolder(view);
	}

	@Override
	public void onBindViewHolder(@NonNull CommentHolder holder, int position) {
		MoodComment comment = this.comments.get(position);
		TextView usernameView = holder.usernameView;
		usernameView.setText(comment.getPosterName());
		usernameView.setOnClickListener(view -> {
			if (this.context instanceof ViewMoodUserActivity) {
				ViewProfileFragment profileFragment = ViewProfileFragment.newInstance(comment.getPosterName());

				((ViewMoodUserActivity) this.context)
						.setFragment(ViewMoodUserActivity.PROFILE_FRAGMENT, profileFragment);
			}
		});

		TextView timeView = holder.timeView;
		timeView.setText(" | (" + comment.getTimeSincePostedStr() + ")");
		TextView commentTextView = holder.commentTextView;
		commentTextView.setText(comment.getCommentText());
		TextView repliesView = holder.repliesView;
		repliesView.setText(String.format("%s replies", comment.getNumSubReplies()));
		holder.itemView.setOnClickListener(view -> {
			if (this.context instanceof ViewMoodUserActivity) {
				ViewCommentFragment commentFragment = ViewCommentFragment.newInstance(comment);

				((ViewMoodUserActivity) this.context)
						.setFragment(ViewMoodUserActivity.COMMENT_FRAGMENT, commentFragment);
			}
		});
	}

	@Override
	public int getItemCount() {
		return this.comments.size();
	}

	public static class CommentHolder extends RecyclerView.ViewHolder {
		private TextView usernameView;
		private TextView timeView;
		private TextView commentTextView;
		private TextView repliesView;

		CommentHolder(@NonNull View itemView) {
			super(itemView);
			this.usernameView = itemView.findViewById(R.id.text_comment_name);
			this.timeView = itemView.findViewById(R.id.text_comment_time);
			this.commentTextView = itemView.findViewById(R.id.text_comment_text);
			this.repliesView = itemView.findViewById(R.id.text_comment_replies);
		}
	}
}