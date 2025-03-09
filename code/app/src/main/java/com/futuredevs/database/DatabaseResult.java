package com.futuredevs.database;

/**
 * A {@code QueryResult} represents the status of an interaction with the
 * database after performing some action such as requesting data or modifying
 * data. The possible values are:
 * <li>{@link #SUCCESS}</li>
 * <li>{@link #FAILURE}</li>
 */
public enum DatabaseResult {
	/**
	 * Interaction with the database (such as adding, retrieving, or editing)
	 * that succeed will give this value.
	 */
	SUCCESS,
	/**
	 * An interaction that fails for any reason (e.g., failure to reach the
	 * database, improper query, etc.) will result in this value.
	 */
	FAILURE
}