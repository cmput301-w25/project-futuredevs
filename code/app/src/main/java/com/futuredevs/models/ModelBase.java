package com.futuredevs.models;

import java.util.ArrayList;
import java.util.List;

public abstract class ModelBase<T> {
	private final List<T> modelData;
	private final List<IModelListener<T>> listeners;

	public ModelBase() {
		this.modelData = new ArrayList<>();
		this.listeners = new ArrayList<>();
	}

	public void addChangeListener(IModelListener<T> listener) {
		if (!this.listeners.contains(listener)) {
			this.listeners.add(listener);
		}
	}

	public void removeChangeListener(IModelListener<T> listener) {
		this.listeners.remove(listener);
	}

	/**
	 * Initializes listeners on the data such as snapshot listeners on
	 * collections or documents
	 */
	protected abstract void initializeData();

	protected void notifyModelChanged(ModelChangeType typeChange,
									  		List<T> changed) {
		for (IModelListener<T> listener : this.listeners) {
			listener.onModelChanged(typeChange, changed);
		}
	}
}