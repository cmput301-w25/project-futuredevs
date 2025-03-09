package com.futuredevs.database.queries;

import com.google.firebase.firestore.DocumentSnapshot;

import java.util.List;

/**
 * <p>Classes that implement the {@code IQueryListener} and have registered
 * themselves to a query will be notified of the results of the query
 * using {@link #onQueryResult(List, QueryResult)}.</p>
 *
 * <p>See {@link com.futuredevs.database.Database#performQuery} for performing
 * queries.</p>
 *
 * @author Spencer Schmidt
 */
public interface IQueryListener {
	/**
	 * If a query is performed the result will be returned in {@code documents}
	 * and the status of the query in {@code result}. If {@code result} is a
	 * {@link QueryResult#FAILURE}, then {@code documents} will be an empty
	 * list, otherwise, the data obtained by the query will be given.
	 *
	 * @param documents the documents resulting from the query
	 * @param result    the status of the query that can be used to give
	 *                  information to the user
	 */
	void onQueryResult(List<DocumentSnapshot> documents, QueryResult result);

	/**
	 * A {@code QueryResult} represents the status of a query after the query
	 * finishes and the values are:
	 * <li>{@link #SUCCESS}</li>
	 * <li>{@link #FAILURE}</li>
	 */
	public enum QueryResult {
		/**
		 * A successful query will contain the results of whatever was
		 * performed.
		 */
		SUCCESS,
		/**
		 * A query that fails for any reason (e.g., failure to reach the database,
		 * improper query, etc.) will result in an empty set.
		 */
		FAILURE
	}
}