package com.futuredevs.database;

import android.location.Location;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.text.DateFormat;
import java.util.Date;

/**
 * The {@code MoodPost} class represents a single post in a list of posts.
 *
 * @author Spencer Schmidt
 */
public class MoodPost  {
	private static final DateFormat DATE_FORMATTER = DateFormat.getDateInstance(DateFormat.MEDIUM);
	private static final DateFormat TIME_FORMATTER = DateFormat.getTimeInstance(DateFormat.SHORT);
	/**
	 * The time at which this post was created. Once a post is created, the
	 * time at which it was created should not be modified.
	 */
	private final Date postDate;
	/**
	 * <p>The id of the document associated with this mood post.</p>
	 *
	 * <p>We must keep a reference to this id in order to edit and delete
	 * a post.</p>
	 */
	private final String documentId;
	private Emotion emotion;
	/**
	 * The trigger word for this post. Should be restricted to only a
	 * single word.
	 */
	private String triggerWord;
	/**
	 * A sentence explaining this mood. Should be restricted to 20 characters
	 * or 3 words.
	 */
	private String reasonSentence;
	private SocialSituation situation;
	/** The longitudinal coordinate of this post. */
	private double longitude;
	/** The latitudinal coordinate of this post. */
	private double latitude;

	/**
	 * Creates a {@code MoodPost} with the given {@code documentId} and
	 * {@code emotion}. The {@code documentId} must be a valid Firestore id
	 * for the document associated with this post.
	 *
	 * @param documentId the Firestore document id for this post.
	 * @param emotion the {@code Emotion} for this post.
	 */
	public MoodPost(@NonNull String documentId, @NonNull Emotion emotion) {
		this.documentId = documentId;
		this.emotion = emotion;
		this.postDate = new Date();
	}

	/**
	 * Returns the {@code Firestore} document id for this post. Useful for
	 * editing the details of this mood in the database along with being
	 * able to delete it.
	 *
	 * @return the {@code Firestore} document id of this post.
	 */
	public String getDocumentId() {
		return this.documentId;
	}

	/**
	 * Sets the trigger word for this post.
	 *
	 * @param trigger the trigger to associate with this mood
	 */
	public void setTrigger(String trigger) {
		this.triggerWord = trigger;
	}

	/**
	 * Returns the trigger word associated with this post.
	 *
	 * @return a {@code String} for this post's trigger word.
	 */
	public String getTrigger() {
		return this.triggerWord;
	}

	/**
	 * Sets the reason for this mood. The reason must be both 20 characters
	 * or fewer and 3 words or fewer, if it is not then {@code reason} will
	 * be shortened to fit this restriction.
	 *
	 * @param reason the reason for this mood.
	 */
	public void setReason(@NonNull String reason) {
		final int MAX_CHAR_LENGTH = 20;
		final int MAX_WORD_COUNT = 3;

		if (reason.length() > MAX_CHAR_LENGTH) {
			reason = reason.substring(0, MAX_CHAR_LENGTH);
		}

		String[] words = reason.split(" ");

		if (words.length > MAX_WORD_COUNT) {
			reason = words[0] + " " + words[1] + " " + words[2];
		}

		this.reasonSentence = reason;
	}

	/**
	 * Returns the reason sentence associated with this post if one was set.
	 *
	 * @return a {@code String} for the reason sentence if it was set.
	 */
	@Nullable
	public String getReason() {
		return this.reasonSentence;
	}

	/**
	 * Sets the emotion of this post to be the given {@code emotion}.
	 *
	 * @param emotion the new emotion to associate with this post.
	 */
	public void setEmotion(Emotion emotion) {
		this.emotion = emotion;
	}

	/**
	 * Returns the {@code Emotion} associated with this post.
	 *
	 * @return the {@code Emotion} for this post.
	 */
	public Emotion getEmotion() {
		return this.emotion;
	}

	/**
	 * Sets the situation of this post to be the given {@code situation}.
	 *
	 * @param situation the new situation to associate with this post.
	 */
	public void setSocialSituation(SocialSituation situation) {
		this.situation = situation;
	}

	/**
	 * Returns the {@code SocialSituation} associated with this post.
	 *
	 * @return the {@code SocialSituation} for this post.
	 */
	public SocialSituation getSocialSituation() {
		return this.situation;
	}

	/**
	 * Returns the time this post was created in milliseconds since the
	 * Unix epoch. For a String-formatted representation of the post time
	 * see {@link #getTimePostedLocaleRepresentation()} and
	 * {@link #getDatePostedLocaleRepresentation()}.
	 *
	 * @return time in milliseconds since this post was created.
	 */
	public long getTimePosted() {
		return this.postDate.getTime();
	}

	/**
	 * Returns a short-form format of the time at which this post was created,
	 * e.g., 10:15PM for US locale.
	 *
	 * @return a locale formatted version of the time this post was created.
	 */
	public String getTimePostedLocaleRepresentation() {
		return TIME_FORMATTER.format(this.postDate);
	}

	/**
	 * Returns a medium-form format of the date at which this post was created,
	 * e.g., Jan 12, 2025 for US locale.
	 *
	 * @return a locale formatted version of the time this post was created.
	 */
	public String getDatePostedLocaleRepresentation() {
		return DATE_FORMATTER.format(this.postDate);
	}

	/**
	 * Sets the location associated with this post to {@code location}.
	 *
	 * @param location the {@code Location} to associate with this post.
	 */
	public void setLocation(@NonNull Location location) {
		this.latitude = location.getLatitude();
		this.longitude = location.getLongitude();
	}

	/**
	 * Sets the location coordinates of this post to the given {@code latitude}
	 * and {@code longitude}.
	 *
	 * @param latitude  the latitudinal coordinate this post was created at.
	 * @param longitude the longitudinal coordinate this post was created at.
	 */
	public void setLocation(double latitude, double longitude) {
		this.latitude = latitude;
		this.longitude = longitude;
	}

	/**
	 * Returns a {@code String} representation of the location associated with
	 * this post. The format of this location is "90.000N 90.000W".
	 *
	 * @return a {@code String} representation of this location.
	 */
	public String getLocation() {
		StringBuilder builder = new StringBuilder();
		String lat = Location.convert(Math.abs(this.latitude), Location.FORMAT_DEGREES);
		String lon = Location.convert(Math.abs(this.longitude), Location.FORMAT_DEGREES);
		builder.append(lat);

		if (this.latitude < 0.0D) {
			builder.append("S ");
		}
		else {
			builder.append("N ");
		}

		builder.append(lon);

		if (this.longitude < 0.0D) {
			builder.append("W");
		}
		else {
			builder.append("E");
		}

		return builder.toString();
	}

	/**
	 * <p>The {@code Emotion} enumeration represents an emotion that is to
	 * be associated with a {@code MoodPost}. The values are:
	 * <li>{@link #ANGER}</li>
	 * <li>{@link #CONFUSED}</li>
	 * <li>{@link #DISGUSTED}</li>
	 * <li>{@link #FEAR}</li>
	 * <li>{@link #HAPPY}</li>
	 * <li>{@link #SHAME}</li>
	 * <li>{@link #SURPRISED}</li></p>
	 */
	public enum Emotion {
		ANGER,
		CONFUSED,
		DISGUSTED,
		FEAR,
		HAPPY,
		SHAME,
		SURPRISED
	}

	/**
	 * <p>The {@code SocialSituation} enumeration is a representation of the that is to
	 * be associated with a {@code MoodPost}. The values are:
	 * <li>{@link #ALONE}</li>
	 * <li>{@link #ONE_PERSON}</li>
	 * <li>{@link #MULTIPLE_PEOPLE}</li>
	 * <li>{@link #CROWD}</li></p>
	 */
	public enum SocialSituation {
		ALONE,
		ONE_PERSON,
		MULTIPLE_PEOPLE,
		CROWD
	}
}