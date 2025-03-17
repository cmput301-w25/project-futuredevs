package com.futuredevs.models.items;

import android.location.Location;
import android.util.Base64;

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
	 * Since latitude and longitude can only take on values between +/-90
	 * and +/-180 respectively, we use a value outside of that range to
	 * represent an invalid/unset coordinate value.
	 */
	public static final double INVALID_COORDINATE = -1000.0D;
	/**
	 * The time at which this post was created. Once a post is created, the
	 * time at which it was created should not be modified.
	 */
	private Date postDate;
	/**
	 * <p>The id of the document associated with this mood post.</p>
	 *
	 * <p>We must keep a reference to this id in order to edit and delete
	 * a post.</p>
	 */
	private final String documentId;
	private final String userPosted;
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
	/** A Base64 representation of the image data. */
	private String imageData;
	/** The longitudinal coordinate of this post. */
	private double longitude = INVALID_COORDINATE;
	/** The latitudinal coordinate of this post. */
	private double latitude = INVALID_COORDINATE;

	/**
	 * Creates a {@code MoodPost} for the user with the given {@code username}
	 * having the emotion given by {@code emotion}.
	 *
	 * @param username   the username of the user associated with this post
	 * @param emotion    the {@code Emotion} for this post
	 */
	public MoodPost(@NonNull String username,
					@NonNull Emotion emotion) {
		this("", username, emotion);
	}

	/**
	 * <p>Creates a {@code MoodPost} with the given {@code documentId} and
	 * {@code emotion}. The {@code documentId} must be a valid Firestore id
	 * for the document associated with this post.</p>
	 *
	 * <p>Note: this constructor should not be directly used and should instead
	 * only be used by database/model methods when data is obtained.</p>
	 *
	 * @param documentId the Firestore document id for this post
	 * @param username   the username of the user associated with this post
	 * @param emotion    the {@code Emotion} for this post
	 */
	public MoodPost(@NonNull String documentId,
					@NonNull String username,
					@NonNull Emotion emotion) {
		this.documentId = documentId;
		this.userPosted = username;
		this.emotion = emotion;
		this.postDate = new Date();
	}

	/**
	 * <p>Returns the {@code Firestore} document id for this post.</p>
	 *
	 * <p>Useful for editing the details of this mood in the database
	 * along with being able to delete it.</p>
	 *
	 * @return the {@code Firestore} document id of this post
	 */
	public String getDocumentId() {
		return this.documentId;
	}

	/**
	 * Returns the username of the user associated with this post.
	 *
	 * @return the username of the user associated with this post
	 */
	public String getUser() {
		return this.userPosted;
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
	 * @return a {@code String} for this post's trigger word
	 */
	public String getTrigger() {
		return this.triggerWord;
	}

	/**
	 * Sets the reason for this mood. The reason must be both 20 characters
	 * or fewer and 3 words or fewer, if it is not then {@code reason} will
	 * be shortened to fit this restriction.
	 *
	 * @param reason the reason for this mood
	 */
	public void setReason(String reason) {
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
	 * @return a {@code String} for the reason sentence if it was set
	 */
	@Nullable
	public String getReason() {
		return this.reasonSentence;
	}

	/**
	 * Sets the emotion of this post to be the given {@code emotion}.
	 *
	 * @param emotion the new emotion to associate with this post
	 */
	public void setEmotion(Emotion emotion) {
		this.emotion = emotion;
	}

	/**
	 * Returns the {@code Emotion} associated with this post.
	 *
	 * @return the {@code Emotion} for this post
	 */
	public Emotion getEmotion() {
		return this.emotion;
	}

	/**
	 * Sets the situation of this post to be the given {@code situation}.
	 *
	 * @param situation the new situation to associate with this post
	 */
	public void setSocialSituation(SocialSituation situation) {
		this.situation = situation;
	}

	/**
	 * Returns the {@code SocialSituation} associated with this post.
	 *
	 * @return the {@code SocialSituation} for this post
	 */
	public SocialSituation getSocialSituation() {
		return this.situation;
	}

	/**
	 * <p>Sets the date/time at which this post was created using {@code} where
	 * {@code time} is the number of milliseconds since January 1, 1970
	 * 00:00:00 GMT.</p>
	 *
	 * <p><b>Note:</b> this should not be used to set the time of the post after
	 * the post has been created and should only be used when obtaining
	 * an existing post from the database.</p>
	 *
	 * @param time the number of milliseconds since January 1, 1970 00:00:00 GMT
	 *             at which the post was created.
	 */
	public void setTimePosted(long time) {
		this.postDate = new Date(time);
	}

	/**
	 * Returns the time this post was created in milliseconds since the
	 * Unix epoch. For a String-formatted representation of the post time
	 * see {@link #getTimePostedLocaleRepresentation()} and
	 * {@link #getDatePostedLocaleRepresentation()}.
	 *
	 * @return time in milliseconds since this post was created
	 */
	public long getTimePosted() {
		return this.postDate.getTime();
	}

	/**
	 * Returns a short-form format of the time at which this post was created,
	 * e.g., 10:15PM for US locale.
	 *
	 * @return a locale formatted version of the time this post was created
	 */
	public String getTimePostedLocaleRepresentation() {
		return TIME_FORMATTER.format(this.postDate);
	}

	/**
	 * Returns a medium-form format of the date at which this post was created,
	 * e.g., Jan 12, 2025 for US locale.
	 *
	 * @return a locale formatted version of the time this post was created
	 */
	public String getDatePostedLocaleRepresentation() {
		return DATE_FORMATTER.format(this.postDate);
	}

	/**
	 * Sets the location associated with this post to {@code location}.
	 *
	 * @param location the {@code Location} to associate with this post
	 */
	public void setLocation(@NonNull Location location) {
		this.latitude = location.getLatitude();
		this.longitude = location.getLongitude();
	}

	/**
	 * Sets the location coordinates of this post to the given {@code latitude}
	 * and {@code longitude}. {@code latitude} must be a valid latitude, that
	 * is, a value between -90.0 and +90.0 and {@code longitude} must be a
	 * valid longitude which is a value between -180.0 and 180.0. If a value
	 * outside of this range is given, then the value will be set to the
	 * nearest limit, e.g. if a latitude of -95.0 is given, then it will
	 * instead be given a value of -90.0.
	 *
	 * @param latitude  the latitudinal coordinate this post was created at
	 * @param longitude the longitudinal coordinate this post was created at
	 */
	public void setLocation(double latitude, double longitude) {
		this.latitude = this.clampCoordinate(latitude, -90.0D, 90.0D);
		this.longitude = this.clampCoordinate(longitude, -180.0D, 180.0D);
	}

	/**
	 * Returns a coordinate value between the given {@code lowerBound} and
	 * the {@code upperBound}, that is, returns value <i>x</i> such that
	 * {@code lowerBound} < <i>x</i> < {@code upperBound}.
	 *
	 * @param coordinate the coordinate value to limit
	 * @param lowerBound the lower bound on the coordinate value
	 * @param upperBound the upper bound on the coordinate value
	 *
	 * @return {@code coordinate} if it is between {@code lowerBound} and
	 *         {@code upperBound}, otherwise {@code lowerBound} if it is
	 *         below and {@code upperBound} if it is above.
	 */
	private double clampCoordinate(double coordinate,
								   double lowerBound,
								   double upperBound) {
		if (coordinate < lowerBound) {
			return lowerBound;
		}
		else if (coordinate > upperBound) {
			return upperBound;
		}
		else {
			return coordinate;
		}
	}

	/**
	 * Returns a {@code String} representation of the location associated with
	 * this post. The format of this location is "90.000N 90.000W".
	 *
	 * @return a {@code String} representation of this location
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
	 * Returns the latitudinal coordinate for this post.
	 *
	 * @return the latitude coordinate of this post
	 */
	public double getLatitude() {
		return this.latitude;
	}

	/**
	 * Returns the longitudinal coordinate for this post.
	 *
	 * @return the longitude coordinate of this post
	 */
	public double getLongitude() {
		return this.longitude;
	}

	/**
	 * <p>Sets the image data for this post to the given {@code base64Data}.
	 * It is expected that the given data is in <i>Base64</i> format and as
	 * such this method should only be used for reconstructing the image from
	 * a saved base64 string.</p>
	 *
	 * @param base64Data the base64 representation of an image
	 */
	public void setImageData(String base64Data) {
		this.imageData = base64Data;
	}

	/**
	 * Sets the image data for this post to the given {@code imageDate}.
	 *
	 * @see #setImageData(String)
	 *
	 * @param imageData the byte representation of the image
	 */
	public void setImageData(byte[] imageData) {
		int imageFlags = Base64.NO_PADDING | Base64.NO_WRAP | Base64.URL_SAFE;
		this.imageData = Base64.encodeToString(imageData, imageFlags);
	}

	/**
	 * <p>Returns the image data for this post. The data for the image is
	 * represented using a Base64 string that has no padding characters,
	 * no wrapping, and is URL safe.</p>
	 *
	 * <p>It is possible for this to return either {@code null} or an empty
	 * string if the image data is invalid or has not been set, thus both
	 * should be checked before using the image data.</p>
	 *
	 * @return a Base64 representation of this post's image if it has been set
	 *         and is a valid image, {@code null} if the image has not been set
	 */
	public String getImageData() {
		return this.imageData;
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
		SADNESS,

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