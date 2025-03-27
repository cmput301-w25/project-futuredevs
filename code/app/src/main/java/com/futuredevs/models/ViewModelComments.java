package com.futuredevs.models;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.futuredevs.database.Database;
import com.futuredevs.database.DatabaseResult;
import com.futuredevs.database.IQueryResult;
import com.futuredevs.models.items.MoodComment;
import com.futuredevs.models.items.MoodPost;
import java.util.List;

/**
 * The {@code ViewModelComments} class represents a model that holds the
 * comments of a {@code MoodPost} or the replies of a {@code MoodComment}.
 * Provides methods to request specific types of comments such as top-level
 * comments (comments that are replies to a {@code MoodPost} and sub-level
 * comments (comments that are replies to another {@code MoodComment}). Also
 * provides methods for posting specific types of comments.
 *
 * @author Spencer Schmidt
 */
public class ViewModelComments extends ViewModel {
	private MutableLiveData<List<MoodComment>> comments = new MutableLiveData<>();
	private final IQueryResult<MoodComment> listener;

	/**
	 * Creates an instance of a {@code ViewModelComment} class that has no
	 * initial data.
	 */
	public ViewModelComments() {
		this.listener = (result, comments) -> {
			if (result == DatabaseResult.SUCCESS) {
				comments.sort((c1, c2) -> Long.compare(c2.getTimeCommented(), c1.getTimeCommented()));
				this.setModelData(comments);
			}
		};
	}

	/**
	 * Requests the comments that are designated as top-level comments for the
	 * given {@code post}.
	 *
	 * @param parentPost the post to obtain the comments from
	 */
	public void requestTopLevelComments(MoodPost parentPost) {
		Database.getInstance().requestPostComments(parentPost, this.listener);
	}

	/**
	 * Requests the comments that are designated as sub-level comments
	 * (replies) for the given {@code parentComment}.
	 *
	 * @param parentComment the comment to obtain the replies from
	 */
	public void requestSubComments(MoodComment parentComment) {
		Database.getInstance().requestCommentReplies(parentComment, this.listener);
	}

	/**
	 * Attempts to post the given {@code comment} to its parent
	 * {@code MoodPost}. If the comment is successfully posted,
	 * then the model will be updated with a new list of comments.
	 *
	 * @param comment the comment to post
	 */
	public void postTopLevelComment(MoodComment comment) {
		Database.getInstance().postComment(comment.getParentPost(), comment, r -> {
			if (r == DatabaseResult.SUCCESS) {
				requestTopLevelComments(comment.getParentPost());
				Log.i("MODEL_COMMENTS", "Successfully posted top level comment");
			}
		});
	}

	/**
	 * Attempts to post the given {@code comment} to its parent
	 * {@code MoodComment}. If the comment is successfully posted,
	 * then the model will be updated with a new list of comments.
	 *
	 * @param comment the comment to post
	 */
	public void postSubComment(MoodComment comment) {
		Database.getInstance().postComment(comment.getParentPost(), comment, r -> {
			requestSubComments(comment);
			Log.i("MODEL_COMMENTS", "Successfully posted sub comment");
		});
	}

	/**
	 * Updates the data held in this model with the given {@code posts}.
	 *
	 * @param posts the new data
	 */
	private void setModelData(List<MoodComment> posts) {
		this.comments.setValue(posts);
	}

	/**
	 * Returns a list of {@code MoodComments} objects associated with the
	 * model that are sorted by the time at which they were posted.
	 *
	 * @return the model's {@code MoodComment} objects
	 */
	public MutableLiveData<List<MoodComment>> getData() {
		return this.comments;
	}
}