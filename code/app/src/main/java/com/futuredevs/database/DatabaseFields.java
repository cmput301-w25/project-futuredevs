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
	public static final String USER_NAME_FLD = "username";
	/** The field name for the user's password. */
	public static final String USER_PWD_FLD = "password";
	/** The field name for the user's followers list. */
	public static final String USER_FOLLOWERS_FLD = "followers";
	/** The field name for the user's following list. */
	public static final String USER_FOLLOWING_FLD = "following";
	/** The field name for the user's moods collection. */
	public static final String USER_MOODS_COLLECTION = "moods";
	/** The field name for the user's notifications. */
	public static final String USER_NOTIF_COLLECTION = "notifications";
	/** The field name for the list of users the user has sent requests to. */
	public static final String USER_PENDING_FOLLOWS_FLD = "pending_follows";

	// =================================================
	// Fields for the mood posts
	// =================================================

	/** The field name for the emotion of a mood post. */
	public static final String MOOD_EMOTION_FLD = "emotion";
	/** The field name for the trigger of a mood post. */
	public static final String MOOD_TRIGGER_FLD = "trigger";
	/** The field name for the reason of a mood post. */
	public static final String MOOD_REASON_FLD = "reason";
	/** The field name for the situation of a mood post. */
	public static final String MOOD_SITUATION_FLD = "situation";
	/** The field name for the time at which a post was created. */
	public static final String MOOD_TIME_FLD = "post_time";
	/** The field name for the location a post was created. */
	public static final String MOOD_LOCATION_FLD = "location";

	// =================================================
	// Fields for the notifications
	// =================================================

	public static final String NOTIF_SENDER_FLD = "sender";
	public static final String NOTIF_RECEIVER_FLD = "receiver";

	private DatabaseFields() {}
}