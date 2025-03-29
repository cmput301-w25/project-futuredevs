package com.futuredevs.database;

import com.futuredevs.models.items.MoodComment;

import java.util.List;

/**
 * @author Spencer Schmidt
 */
public interface ICommentListener {
	/**
	 *
	 * @param result
	 * @param comments
	 */
	void onCommentsObtained(DatabaseResult result, List<MoodComment> comments);
}