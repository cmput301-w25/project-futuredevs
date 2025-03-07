package com.futuredevs.models;

import com.futuredevs.database.MoodPost;

import java.util.List;

/**
 * A class that implements the {@code IMoodRemoveListener} will listen for when
 * data is removed from the data being listened to.
 *
 * @author Spencer Schmidt
 */
public interface IMoodRemoveListener extends IMoodListener {
	/**
	 * When data is removed from the underlying data, then this callback will
	 * be invoked where {@code removed} is the data that was removed.
	 *
	 * @param removed the data that was removed
	 */
	void onMoodsRemoved(List<MoodPost> removed);
}