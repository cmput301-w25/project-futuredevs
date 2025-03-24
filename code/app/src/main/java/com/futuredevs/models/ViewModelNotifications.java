package com.futuredevs.models;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.futuredevs.database.Database;
import com.futuredevs.database.DatabaseFields;
import com.futuredevs.database.DatabaseResult;
import com.futuredevs.database.queries.DatabaseQuery;
import com.futuredevs.database.queries.IQueryListener;
import com.futuredevs.models.items.Notification;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

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

	public void requestData() {
		DatabaseQuery.QueryBuilder builder = new DatabaseQuery.QueryBuilder();
		builder.setType(DatabaseQuery.QueryType.USER_NOTIFICATIONS)
			   .setSourceUser(this.username);
		DatabaseQuery query = builder.build();
		Database.getInstance().performQuery(query, this);
	}

	private void setModelData(List<Notification> posts) {
		this.notifData.setValue(posts);
	}

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

	public static class ViewModelNotificationsFactory implements ViewModelProvider.Factory {
		private final String username;

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