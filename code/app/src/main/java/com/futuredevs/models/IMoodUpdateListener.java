package com.futuredevs.models;

import com.futuredevs.database.MoodPost;

import java.util.List;

/**
 * A class that implements the {@code IMoodRemoveListener} will listen for when
 * data is removed from the data being listened to.
 *
 * @author Spencer Schmidt
 */
public interface IMoodUpdateListener extends IMoodListener {
	void onMoodsAdded(List<MoodPost> addedMoods);

	void onMoodsUpdated(List<MoodPost> updatedMoods);
}
