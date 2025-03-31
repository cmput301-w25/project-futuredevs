package com.futuredevs.models;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.futuredevs.database.Database;
import com.futuredevs.database.DatabaseFields;
import com.futuredevs.database.DatabaseResult;
import com.futuredevs.database.IResultListener;
import com.futuredevs.database.queries.DatabaseQuery;
import com.futuredevs.database.queries.IQueryListener;
import com.futuredevs.models.items.Notification;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * The {@code ViewModelNotifications} class represents a model that holds
 * the information about all notifications a user has. Provides methods
 *
 * @author Spencer Schmidt
 */
public class ViewModelNotifications extends ViewModel implements IQueryListener {
	private final String username;
	private MutableLiveData<List<Notification>> notifData = new MutableLiveData<>();

	/**
	 * Creates an instance of a {@code ModelNotifications} where the given
	 * {@code username} is the name of the user whose notifications will be
	 * used as the source for obtaining the data of this model.
	 *
	 * @param username the name of the user to obtain notifications from
	 */
	public ViewModelNotifications(String username) {
		this.username = username;
		this.requestData();
	}

	/**
	 * Requests the current notifications the user represented by the model
	 * currently has pending.
	 */
	public void requestData() {
		DatabaseQuery.QueryBuilder builder = new DatabaseQuery.QueryBuilder();
		builder.setType(DatabaseQuery.QueryType.USER_NOTIFICATIONS)
			   .setSourceUser(this.username);
		DatabaseQuery query = builder.build();
		Database.getInstance().performQuery(query, this);
	}

	/**
	 * Attempts to accept the follow request represented by the given
	 * {@code notification} and if successful updates the data of the
	 * model.
	 *
	 * @param notification the notification to attempt to accept
	 * @param listener     the callback to notify of the result
	 */
	public void acceptFollowRequest(Notification notification, IResultListener listener) {
		Database.getInstance().acceptFollowRequest(notification, r -> {
			if (r == DatabaseResult.SUCCESS) {
				requestData();
			}

			listener.onResult(r);
		});
	}

	/**
	 * Attempts to reject the follow request represented by the given
	 * {@code notification} and if successful updates the data of the
	 * model.
	 *
	 * @param notification the notification to attempt to reject
	 * @param listener     the callback to notify of the result
	 */
	public void rejectFollowRequest(Notification notification, IResultListener listener) {
		Database.getInstance().rejectFollowingRequest(notification, r -> {
			if (r == DatabaseResult.SUCCESS) {
				requestData();
			}

			listener.onResult(r);
		});
	}

	private void setModelData(List<Notification> posts) {
		this.notifData.setValue(posts);
	}

	/**
	 * Returns the notification data held in the model. Any changes to the data
	 * should be observed through this.
	 *
	 * @return the notification data for the model
	 */
	public MutableLiveData<List<Notification>> getData() {
		return this.notifData;
	}

	@Override
	public void onQueryResult(List<DocumentSnapshot> documents, DatabaseResult result) {
		List<Notification> notifications = new ArrayList<>();

		if (result != DatabaseResult.FAILURE) {
			for (DocumentSnapshot snapshot : documents) {
				String docId = snapshot.getId();
				String source = snapshot.getString(DatabaseFields.NOTIF_SENDER_FLD);
				Notification notification = new Notification(docId, source, this.username);
				notifications.add(notification);
			}

			this.setModelData(notifications);
		}
	}

	/**
	 * The {@code ViewModelNotificationsFactory} class is a factory class to be
	 * used when creating new instances of a {@code ViewModelNotifications} in
	 * order to pass the name of a user into the view model.
	 *
	 * @author Spencer Schmidt
	 */
	public static class ViewModelNotificationsFactory implements ViewModelProvider.Factory {
		private final String username;

		/**
		 * Creates a {@code ViewModelNotificationsFactory} instance for the
		 * user given by {@code username}
		 *
		 * @param username the user to obtain the notifications from
		 */
		public ViewModelNotificationsFactory(String username) {
			this.username = username;
		}

		@NonNull
		@Override
		public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
			return (T) new ViewModelNotifications(this.username);
		}
	}
}