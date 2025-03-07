package com.futuredevs.models;

import com.futuredevs.database.MoodPost;

import java.util.ArrayList;
import java.util.List;

public abstract class ModelMoods {
	private final List<MoodPost> moods;
	private final List<IMoodListener> listeners;

	public ModelMoods() {
		this.moods = new ArrayList<>();
		this.listeners = new ArrayList<>();
	}

	public void addChangeListener(IMoodListener listener) {
		if (!this.listeners.contains(listener)) {
			this.listeners.add(listener);
		}
	}

	public void removeChangeListener(IMoodListener listener) {
		this.listeners.remove(listener);
	}

	protected abstract void initializeData();

	protected void notifyModelChanged(ModelChangeType typeChange, List<MoodPost> changed) {
		for (IMoodListener listener : this.listeners) {
			if (listener instanceof IMoodUpdateListener) {
				if (typeChange == ModelChangeType.ADDED) {
					((IMoodUpdateListener) listener).onMoodsAdded(changed);
				}
				else if (typeChange == ModelChangeType.UPDATED) {
					((IMoodUpdateListener) listener).onMoodsUpdated(changed);
				}
			}
			else if (listener instanceof IMoodRemoveListener) {
				((IMoodRemoveListener) listener).onMoodsRemoved(changed);
			}

			listener.onMoodsChanged(this.moods);
		}
	}

	protected enum ModelChangeType {
		ADDED, UPDATED, REMOVED
	}
}