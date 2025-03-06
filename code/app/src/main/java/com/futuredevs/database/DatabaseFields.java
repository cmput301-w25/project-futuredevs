package com.futuredevs.database;

/**
 * A helper class for storing the names of database collections and document
 * fields.
 *
 * @author Spencer Schmidt
 */
public final class DatabaseFields {
	// =================================================
	// Fields for the user entries
	// =================================================

	/** Represents the name of the user's collection. */
	public static final String USER_COLLECTION = "users";
	/** The field name for the user's password. */
	public static final String USER_PWD_FLD = "password";
	/** The field name for the user's followers list. */
	public static final String USER_FOLLOWERS_FLD = "followers";
	/** The field name for the user's following list. */
	public static final String USER_FOLLOWING_FLD = "following";
	/** The field name for the user's notifications. */
	public static final String USER_NOTIF_FLD = "notifications";

	private DatabaseFields() {}
}