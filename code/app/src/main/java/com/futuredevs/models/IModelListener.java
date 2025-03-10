package com.futuredevs.models;

import com.futuredevs.models.items.MoodPost;

import java.util.List;

/**
 * A class that implements the {@code IMoodListener} will listen for any
 * changes to the data that is being listened on such as data being modified,
 * data being added, etc. Provides a single method that will be invoked
 * when the data being listened to is request or modified.
 *
 * @author Spencer Schmidt
 */
public interface IModelListener<T> {
    /**
	 * When the underlying data is modified in any way or the most
	 * up-to-date data is requested, this callback method will be
	 * invoked where {@code model} is the model that was changed.
	 *
	 * @param theModel the model that was changed
	 */
	void onModelChanged(ModelBase<T> theModel);
}
