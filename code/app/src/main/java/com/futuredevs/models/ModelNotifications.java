package com.futuredevs.models;

import com.futuredevs.database.Database;
import com.futuredevs.models.items.Notification;
import com.futuredevs.database.queries.DatabaseQuery;
import com.futuredevs.database.queries.IQueryListener;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * The {@code ModelNotifications} class represents a model class in which
 * the pending notifications the user has received are contained.
 *
 * @author Spencer Schmidt
 */
public class ModelNotifications extends ModelBase<Notification> implements IQueryListener {
	private final String username;

	/**
	 * Creates an instance of a {@code ModelNotifications} where the given
	 * {@code username} is the name of the user whose notifications will be
	 * used as the source for obtaining the data of this model.
	 *
	 * @param username the name of the user to obtain notifications from
	 */
	public ModelNotifications(String username) {
		this.username = username;
	}

	@Override
	protected void requestData() {
		DatabaseQuery.QueryBuilder builder = new DatabaseQuery.QueryBuilder();
		builder.setType(DatabaseQuery.QueryType.USER_NOTIFICATIONS)
			   .setSourceUser(this.username);
		DatabaseQuery query = builder.build();
		Database.getInstance().performQuery(query, this);
	}

	@Override
	public void onQueryResult(List<DocumentSnapshot> documents, QueryResult result) {
		List<Notification> notifications = new ArrayList<>();

		if (result != QueryResult.FAILURE) {
			for (DocumentSnapshot snapshot : documents) {
				String docId = snapshot.getId();
				String source = snapshot.getString("sender");
				Notification notification = new Notification(docId, source, this.username);
				notifications.add(notification);
			}

			this.setData(notifications);
			this.notifyModelChanged();
		}
	}
}