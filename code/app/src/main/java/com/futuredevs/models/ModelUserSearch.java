package com.futuredevs.models;

import com.futuredevs.database.Database;
import com.futuredevs.database.DatabaseFields;
import com.futuredevs.database.DatabaseResult;
import com.futuredevs.database.IResultListener;
import com.futuredevs.database.queries.DatabaseQuery;
import com.futuredevs.database.queries.IQueryListener;
import com.futuredevs.models.items.UserSearchResult;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * The {@code ModelMoodsFollowing} class represents a model class in which
 * the search results for searching users are contained.
 *
 * @author Spencer Schmidt
 */
public class ModelUserSearch extends ModelBase<UserSearchResult> implements IQueryListener {
	private final String username;
	private String searchTerm;

	public ModelUserSearch(String username) {
		this.username = username;
	}

	/**
	 * Sets the search term for this model to be used when data is requested
	 * from this model.
	 *
	 * @param searchTerm the term to use in the next data request
	 */
	public void setSearchTerm(String searchTerm) {
		this.searchTerm = searchTerm;
	}

	/**
	 * The search term should be set before requesting data from this model as
	 * the current term set will be used when obtaining new data.
	 */
	@Override
	public void requestData() {
		DatabaseQuery.QueryBuilder builder = new DatabaseQuery.QueryBuilder();
		builder.setType(DatabaseQuery.QueryType.USERS)
			   .setSearchTerm(this.searchTerm);
		DatabaseQuery query = builder.build();
		Database.getInstance().performQuery(query, this);
	}

	@Override
	public void onQueryResult(List<DocumentSnapshot> documents, DatabaseResult result) {
		List<UserSearchResult> searchResults = new ArrayList<>();

		if (result != DatabaseResult.FAILURE) {
			List<String> pendingNames = new ArrayList<>();
			List<String> followingNames = new ArrayList<>();
			IResultListener listener = r -> {
				for (DocumentSnapshot snapshot : documents) {
					String name = snapshot.getString(DatabaseFields.USER_NAME_FLD);
					if (name.equalsIgnoreCase(username)) {
						continue; // Do not add your own username to the results
					}
					boolean hasPending = pendingNames.contains(name);
					boolean isFollowing = followingNames.contains(name);
					UserSearchResult searchResult = new UserSearchResult(name, hasPending, isFollowing);
					searchResults.add(searchResult);
				}

				this.setData(searchResults);
				this.notifyModelChanged();
			};

			Database.getInstance().getPendingAndFollowing(this.username,
														  pendingNames,
														  followingNames,
														  listener);
		}
	}
}