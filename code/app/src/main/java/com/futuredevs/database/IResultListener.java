package com.futuredevs.database;

/**
 * Classes the implement the {@code IResultListener} interface are able to
 * listen for whether the result of some operation on the database was a
 * success or if it failed. Implementations of this interface are not expected
 * to listen for the results of the query, only for its success or failure.
 *
 * @see IQueryResult
 *
 * @author Spencer Schmidt
 */
public interface IResultListener {
	/**
	 * When an interaction with the database finishes, then this method will
	 * be called regardless of if the interaction succeeded or failed. The
	 * actual result of the interaction will be given by {@code result}.
	 *
	 * @param result {@link DatabaseResult#SUCCESS} if the operation on the
	 *               database was a success, {@link DatabaseResult#FAILURE}
	 *               otherwise
	 */
    void onResult(DatabaseResult result);
}