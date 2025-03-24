package com.futuredevs.models.items;

import androidx.annotation.Nullable;

import java.text.DateFormat;
import java.util.Date;

/**
 * <p>The {@code MoodComment} class represents a single comment which is to be
 * associated with a {@code MoodPost}.</p>
 *
 * <p>Comments can be classified into two different types, either top-level
 * comments or sub-level comments. A top-level comment is a comment that is a
 * direct reply to a {@code MoodPost} while a sub-level comment is a comment
 * that is a reply to another comment.</p>
 *
 * <p>Provides constructors for creating {@code MoodComments} with and without
 * knowing the Firestore document id for a given comment. The constructors
 * that include a document id should be reserved for use only with database
 * associated classes and instead the constructors without documents ids
 * should be used instead: {@link #MoodComment(MoodPost, String, String)} for
 * top-level comments and {@link #MoodComment(MoodComment, String, String)} for
 * sub-level comments.</p>
 *
 * @author Spencer Schmidt
 */
public class MoodComment {
	private static final DateFormat DATE_FORMATTER = DateFormat.getDateInstance(DateFormat.MEDIUM);
	private static final DateFormat TIME_FORMATTER = DateFormat.getTimeInstance(DateFormat.SHORT);
	private final MoodPost parentPost;
	private MoodComment parentComment;
	/**
	 * The document id of this comment from the Firestore database. This is
	 * necessary to keep track of for comment related queries.
	 */
	private String documentId;
	/** The username of the user that created the comment. */
	private final String posterName;
	/** The text to be associated with this comment. */
	private final String commentText;
	/** The date and time at which this comment was created. */
	private Date timeCommented;
	/** The number of sub-level comments that are associated with this comment. */
	private int numSubReplies;

	/**
	 * Creates a {@code MoodComment} which represents a reply to either the
	 * post given by {@code parentPost} or to the given {@code parentComment}
	 * in which case the comment represented by this {@code MoodPost} would
	 * be a nested comment.
	 *
	 * @param parentPost	the mood post to which the comment is a reply to
	 * @param parent		the comment to which the comment is a reply to
	 * @param documentId	the associated Firestore document id
	 * @param posterName	the name of the user creating the comment
	 * @param commentText   the text associated with the comment
	 */
	private MoodComment(MoodPost parentPost, MoodComment parent,
						String documentId, String posterName,
						String commentText) {
		this.parentPost = parentPost;
		this.parentComment = parent;
		this.documentId = documentId;
		this.posterName = posterName;
		this.commentText = commentText;
		this.timeCommented = new Date();
	}

	/**
	 * <p>Creates a {@code MoodComment} which represents a single top-level reply
	 * to the given {@code parentParent}.</p>
	 *
	 * <p>The intent of this constructor is to recreate a {@code MoodComment}
	 * from the database and as such it is not intended to be used outside of
	 * the database classes, {@link #MoodComment(MoodPost, String, String)}
	 * should be used instead.</p>
	 *
	 * @param parentPost	the mood post to which the comment is a reply to
	 * @param documentId	the associated Firestore document id
	 * @param posterName	the name of the user creating the comment
	 * @param commentText   the text associated with the comment
	 */
	public MoodComment(MoodPost parentPost, String documentId,
					   String posterName, String commentText) {
		this(parentPost, null, documentId, posterName, commentText);
	}

	/**
	 * Creates a {@code MoodComment} object which represents a reply to another
	 * {@code MoodComment}.
	 *
	 * <p>The intent of this constructor is to recreate a {@code MoodComment}
	 * from the database and as such it is not intended to be used outside of
	 * the database classes, {@link #MoodComment(MoodComment, String, String)}
	 * should be used instead.</p>
	 *
	 * @param parent		the comment to which the comment is a reply to
	 * @param documentId	the associated Firestore document id
	 * @param posterName	the name of the user creating the comment
	 * @param commentText   the text associated with the comment
	 */
	public MoodComment(MoodComment parent, String documentId,
					   String posterName, String commentText) {
		this(parent.parentPost, parent, documentId, posterName, commentText);
	}

	/**
	 * Creates a {@code MoodComment} which represents a single top-level reply
	 * to the given {@code parentParent}.
	 *
	 * @param parentPost	the mood post to which the comment is a reply to
	 * @param posterName	the name of the user creating the comment
	 * @param commentText   the text associated with the comment
	 */
	public MoodComment(MoodPost parentPost, String posterName,
					                        String commentText) {
		this(parentPost, null, "", posterName, commentText);
	}

	/**
	 * Creates a {@code MoodComment} object which represents a reply to another
	 * {@code MoodComment}.
	 *
	 * @param parent		the comment to which the comment is a reply to
	 * @param posterName	the name of the user creating the comment
	 * @param commentText   the text associated with the comment
	 */
	public MoodComment(MoodComment parent,String posterName,
					   							 String commentText) {
		this(parent.parentPost, parent, "", posterName, commentText);
	}

	/**
	 * Returns the Firestore document id for the comment. If this comment
	 * was not created by the database, then this will be an empty string.
	 *
	 * @return the Firestore document id if the comment was created by the
	 * 		   database, otherwise an empty string.
	 */
	public String getDocumentId() {
		return this.documentId;
	}

	/**
	 * Returns the name of the user that posted the comment.
	 *
	 * @return the username of the posting commenter
	 */
	public String getPosterName() {
		return this.posterName;
	}

	/**
	 * Returns the text contained in the comment.
	 *
	 * @return the text in the comment
	 */
	public String getCommentText() {
		return this.commentText;
	}

	/**
	 * <p>Returns the {@code MoodPost} of which this comment is associated
	 * with. If a comment is not a top-level comment, its parent post will be
	 * the parent post of the comment of which it is a sub-level comment. That
	 * is, all comments associated with a post whether they're top-level or
	 * sub-level share the same parent post.</p>
	 *
	 * @return the post that the comment is associated with
	 */
	public MoodPost getParentPost() {
		return this.parentPost;
	}

	/**
	 * <p>Returns the {@code MoodComment} that this comment is associated with.
	 * If the comment is a top-level comment, then its parent comment will be
	 * {@code null} as only sub-level comments can have parent comments.</p>
	 *
	 * @return the parent comment for this comment if this is a sub-level
	 * 		   comment, {@code null} otherwise
	 */
	@Nullable
	public MoodComment getParentComment() {
		return this.parentComment;
	}

	/**
	 * <p>Sets the date/time at which this post was created using {@code} where
	 * {@code time} is the number of milliseconds since January 1, 1970
	 * 00:00:00 GMT.</p>
	 *
	 * <p><b>Note:</b> this should only be used when obtaining an existing
	 * comment from the database to reconstruct the comment.</p>
	 *
	 * @param time the number of milliseconds since January 1, 1970 00:00:00 GMT
	 *             at which the comment was created.
	 */
	public void setTimeCommented(long time) {
		this.timeCommented = new Date(time);
	}

	/**
	 * Returns the date-time at which this comment was created.
	 *
	 * @see #getTimeCommentedLocaleRepresentation()
	 * @see #getDateCommentedLocaleRepresentation()
	 *
	 * @return the date-time at which this comment was created
	 */
	public long getTimeCommented() {
		return this.timeCommented.getTime();
	}

	/**
	 * Returns a short-form format of the time at which this comment was
	 * created, e.g., 10:15PM for US locale.
	 *
	 * @return a locale formatted version of the time this comment was created
	 */
	public String getTimeCommentedLocaleRepresentation() {
		return TIME_FORMATTER.format(this.timeCommented);
	}

	/**
	 * Returns a medium-form format of the date at which this comment was
	 * created, e.g., Jan 12, 2025 for US locale.
	 *
	 * @return a locale formatted version of the time this comment was created
	 */
	public String getDateCommentedLocaleRepresentation() {
		return DATE_FORMATTER.format(this.timeCommented);
	}

	/**
	 * Sets the number of replies that this comment currently has.
	 *
	 * @param numSubReplies the number of replies this post has
	 */
	public void setNumSubReplies(int numSubReplies) {
		this.numSubReplies = numSubReplies;
	}

	/**
	 * Returns the number of reply comments this comment has.
	 *
	 * @return the number of replies to this comment
	 */
	public int getNumSubReplies() {
		return this.numSubReplies;
	}
}