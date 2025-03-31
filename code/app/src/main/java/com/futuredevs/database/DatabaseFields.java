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
	/** The field name for the reason of a mood post. */
	public static final String MOOD_REASON_FLD = "reason";
	/** The field name for the situation of a mood post. */
	public static final String MOOD_SITUATION_FLD = "situation";
	/** The field name for the time at which a post was created. */
	public static final String MOOD_TIME_FLD = "post_time";
	/** The field name for the location a post was created. */
	public static final String MOOD_LOCATION_FLD = "location";
	/** The field name for the image data of a post. */
	public static final String MOOD_IMG_FLD = "image";
	/** The field name for the view status of a post. */
	public static final String MOOD_VIEW_STATUS_FLD = "privated";
	/** The field name for the comment collection associated with a post. */
	public static final String MOOD_COMMENT_FLD = "comments";
	/** The field name for the edited status of a post. */
	public static final String MOOD_EDITED_FLD = "edited";
	/** The field name for the number of topo level comments for a post. */
	public static final String MOOD_COMMENT_COUNT = "num_comments";

	// =================================================
	// Fields for the mood comments
	// =================================================

	/** The field for a comment's parent post document id. */
	public static final String CMT_PARENT_POST_FLD = "parent_post";
	/** The field for a comment's parent comment document id. */
	public static final String CMT_PARENT_CMT_FLD = "parent_comment";
	/** Placeholder text to use for a top-level comment's parent. */
	public static final String CMT_TOP_LEVEL = "top_level";
	/** The field for a comment's date-time at which it was posted. */
	public static final String CMT_TIME_FLD = "time_posted";
	/** The field for a comment's text. */
	public static final String CMT_TEXT_FLD = "comment_text";
	/** The field for the username of the user who posted the comment. */
	public static final String CMT_POSTER_FLD = "poster";
	/** The field for the number of sub-comments of a comment. */
	public static final String CMT_NUM_SUB = "num_replies";

	// =================================================
	// Fields for the notifications
	// =================================================

	public static final String NOTIF_SENDER_FLD = "sender";
	public static final String NOTIF_RECEIVER_FLD = "receiver";

	private DatabaseFields() {}
}