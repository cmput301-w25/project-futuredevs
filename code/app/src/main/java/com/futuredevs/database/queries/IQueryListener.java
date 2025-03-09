package com.futuredevs.database.queries;

import com.futuredevs.database.DatabaseResult;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.List;

/**
 * <p>Classes that implement the {@code IQueryListener} and have registered
 * themselves to a query will be notified of the results of the query
 * using {@link #onQueryResult(List, DatabaseResult)}.</p>
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
	 * {@link DatabaseResult#FAILURE}, then {@code documents} will be an empty
	 * list, otherwise, the data obtained by the query will be given.
	 *
	 * @param documents the documents resulting from the query
	 * @param result    the status of the query that can be used to give
	 *                  information to the user
	 */
	void onQueryResult(List<DocumentSnapshot> documents, DatabaseResult result);
}