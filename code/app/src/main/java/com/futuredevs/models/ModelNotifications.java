package com.futuredevs.models;

import com.futuredevs.database.Database;
import com.futuredevs.database.Notification;
import com.futuredevs.database.queries.DatabaseQuery;
import com.futuredevs.database.queries.IQueryListener;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.List;

public class ModelNotifications extends ModelBase<Notification> implements IQueryListener {
	private final String username;

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

	}
}