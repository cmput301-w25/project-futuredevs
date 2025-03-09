package com.futuredevs.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * <p>The {@code ModelBase} represents an abstract model that holds a set of
 * data which may be listened to for updates and in which data can be added,
 * removed, or updated.</p>
 *
 * <p>Any class that extends {@code ModelBase} must implement the
 * {@code requestData()} method in such a way that listeners will be notified
 * of the data being obtained after request.</p>
 *
 * @param <T> the type of data to hold in this model
 */
public abstract class ModelBase<T> {
	private final List<T> modelData;
	private final List<IModelListener<T>> listeners;

	public ModelBase() {
		this.modelData = new ArrayList<>();
		this.listeners = new ArrayList<>();
	}

	/**
	 * Adds the {@code listener} to the listeners of this model that will
	 * be notified when this model's data is modified as long as it is not
	 * already a listener. If {@code listener} is already listening to this
	 * model then this method will ignore the addition.
	 *
	 * @param listener the listener to be notified on data modifications
	 */
	public void addChangeListener(IModelListener<T> listener) {
		if (!this.listeners.contains(listener)) {
			this.listeners.add(listener);
		}
	}

	/**
	 * Removes the {@code listener} from the listeners of this model so that
	 * it will no longer be notified when this model's data is modified. If
	 * {@code listener} is not listening to this model, then the remove will
	 * simply be ignored.
	 *
	 * @param listener the listener to remove from this model
	 */
	public void removeChangeListener(IModelListener<T> listener) {
		this.listeners.remove(listener);
	}

	/**
	 * <p>Requests the most update-to-date data from the underlying data. Once
	 * data has been requested, listeners will be notified of the data being
	 * obtained.</p>
	 *
	 * <p>See also: {@link IModelListener#onModelChanged(ModelBase)}</p>
	 */
	protected abstract void requestData();

	public void addItem(T item) {}
	public void removeItem(T item) {}
	public void updateItem(T item) {}

	/**
	 * Returns an unmodifiable instance of this models data. Any
	 * attempts to modify the model through this will result in an
	 * {@link UnsupportedOperationException} being thrown.
	 *
	 * @return an unmodifiable copy of this model's data
	 */
	public List<T> getModelData() {
		return Collections.unmodifiableList(this.modelData);
	}

	/**
	 * Clears the data within this model and replaces it with the given
	 * {@code newData}. Intended to only be used within extending model
	 * classes to replace the existing data.
	 *
	 * @param newData the data to replace the existing data with
	 */
	protected void setData(List<T> newData) {
		this.modelData.clear();
		this.modelData.addAll(newData);
	}

	/**
	 * Notifies all listeners of this model that the data within this model
	 * has been modified. All implementations of {@link #addItem(Object)},
	 * {@link #removeItem(Object)} and {@link #updateItem(Object)} should call
	 * this method to properly notify users that the data has been changed.
	 */
	protected void notifyModelChanged() {
		for (IModelListener<T> listener : this.listeners) {
			listener.onModelChanged(this);
		}
	}
}