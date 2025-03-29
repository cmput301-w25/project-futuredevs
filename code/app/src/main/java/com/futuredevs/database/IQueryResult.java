package com.futuredevs.database;

import java.util.List;

/**
 * <p>The {@code IQueryResult<T>} interface provides an interface method for
 * listening for the result of a database query that is expected to provide
 * a list of data as a result.</p>
 *
 * @see IResultListener
 *
 * @param <T> the type of objects expected in the result
 *
 * @author Spencer Schmidt
 */
public interface IQueryResult<T> {
	/**
	 * Called upon obtaining the results of a query regardless of if the
	 * operation was successful or not. If {@code result} is successful,
	 * then {@code results} will have the data obtained from the query,
	 * otherwise, the data returned may be empty or {@code null}.
	 *
	 * @param result  the result of the query operation
	 * @param data the data obtained from the query
	 */
	void onResult(DatabaseResult result, List<T> data);
}