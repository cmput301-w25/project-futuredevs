package com.futuredevs.models;

import com.futuredevs.database.MoodPost;

import java.util.List;

/**
 * A class that implements the {@code IMoodListener} will listen for any
 * changes to the data that is being listened on such as data being modified,
 * data being added, etc.
 *
 * @author Spencer Schmidt
 */
public interface IMoodListener {
	/**
	 * When the underlying data is modified in any way, this callback method
	 * will be invoked where {@code posts} will be all posts in the data.
	 *
	 * @param posts all posts that are contained in the underlying data
	 */
	void onMoodsChanged(List<MoodPost> posts);
}
