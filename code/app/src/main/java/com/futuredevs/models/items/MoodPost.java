package com.futuredevs.models.items;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * The {@code MoodPost} class represents a single post in a list of posts.
 * Each mood must have an associated user and emotion and provides methods
 * for attaching a social situation, a descriptive reason sentence, an image,
 * and location data.
 *
 * @author Spencer Schmidt
 */
public class MoodPost implements Parcelable {
	private static final DateFormat DATE_FORMATTER = DateFormat.getDateInstance(DateFormat.MEDIUM);
	private static final DateFormat TIME_FORMATTER = DateFormat.getTimeInstance(DateFormat.SHORT);
	/**
	 * Required attribute in order for a parcel to be able to reconstruct
	 * {@code MoodPost} objects.
	 */
	public static final Creator<MoodPost> CREATOR = new Creator<>() {
		@Override
		public MoodPost createFromParcel(Parcel in) {
			return new MoodPost(in);
		}

		@Override
		public MoodPost[] newArray(int size) {
			return new MoodPost[size];
		}
	};
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
	 * A sentence explaining this mood. This sentence should be restricted to
	 * 200 characters.
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
	 * Permission flag for posts that determines whether other uses can see it
	 * or not.
	 */
	private boolean isPostPrivated;
	/** The number of top-level comments that are associated with the post. */
	private int numTopLevelComments;
	/** A flag to be set when this mood post has been edited by its poster. */
	private boolean wasEdited;

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
	 * <p>Creates a {@code MoodPost} using the parameters from the given
	 * {@code in} {@code Parcel}. Used only for reconstructing a post when
	 * passed using parcels.</p>
	 *
	 * @param in the {@code Parcel} to use to construct the {@code MoodPost}
	 */
	private MoodPost(Parcel in) {
		this.documentId = in.readString();
		this.userPosted = in.readString();
		this.emotion = Emotion.values()[in.readInt()];
		this.setTimePosted(in.readLong());
		int sitIndex = in.readInt();

		if (sitIndex != -1) {
			this.situation = SocialSituation.values()[sitIndex];
		}

		this.reasonSentence = in.readString();
		this.imageData = in.readString();
		this.longitude = in.readDouble();
		this.latitude = in.readDouble();
		this.numTopLevelComments = in.readInt();
		this.isPostPrivated = (in.readInt() == 1);
		this.wasEdited = (in.readInt() == 1);
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(this.documentId);
		dest.writeString(this.userPosted);
		dest.writeInt(this.emotion.ordinal());
		dest.writeLong(this.postDate.getTime());

		if (this.situation != null) {
			dest.writeInt(this.situation.ordinal());
		}
		else {
			dest.writeInt(-1);
		}

		dest.writeString(Objects.requireNonNullElse(this.reasonSentence, ""));
		dest.writeString(Objects.requireNonNullElse(this.imageData, ""));
		dest.writeDouble(this.longitude);
		dest.writeDouble(this.latitude);
		dest.writeInt(this.numTopLevelComments);
		dest.writeInt(this.isPostPrivated ? 1 : 0);
		dest.writeInt(this.wasEdited ? 1 : 0);
	}

	@Override
	public int describeContents() {
		return 0;
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
	 * Sets the reason for this mood. The reason must be less than or equal to
	 * 200 characters and if it is not then {@code reason} will be shortened to
	 * fit this restriction.
	 *
	 * @param reason the reason for this mood
	 */
	public void setReason(String reason) {
		if (reason != null) {
			final int MAX_CHAR_LENGTH = 200;

			if (reason.length() > MAX_CHAR_LENGTH)
			{
				reason = reason.substring(0, MAX_CHAR_LENGTH);
			}

			this.reasonSentence = reason;
		}
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
	 * Sets the time of this post to {@code date} and marks this post as
	 * having been edited.
	 *
	 * @param date the time at which the post was edited
	 */
	public void setTimeEdited(Date date) {
		this.postDate = date;
		this.setEdited(true);
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
	 * <p>Returns as a {@code String} a representation of the amount of time
	 * since the post was created in time increments of seconds, minutes,
	 * hours, and days.</p>
	 *
	 * <p>If the amount of time passed is less than 1 second, then instead
	 * the string "just now" is returned, and if the time passed is greater
	 * than 1 week, the date of the post is returned instead.</p>
	 *
	 * @return a {@code String} representation of the time that has passed
	 * 		   since the post was created
	 */
	public String getTimeSincePostedStr() {
		long now = System.currentTimeMillis();
		long diffMillis = now - this.getTimePosted();
		final long SECOND = 1000L;
		final long MINUTE = 60L * SECOND;
		final long HOUR = 60L * MINUTE;
		final long DAY = 24L * HOUR;
		final long WEEK = 7L * DAY;

		if (diffMillis < SECOND) {
			return "just now";
		}
		if (diffMillis < MINUTE) {
			return (diffMillis / SECOND) + "s ago";
		}
		else if (diffMillis < HOUR) {
			long minutes = diffMillis / MINUTE;
			return minutes + "m ago";
		}
		else if (diffMillis < DAY) {
			long hours = diffMillis / HOUR;
			return hours + "h ago";
		}
		else if (diffMillis < WEEK) {
			long days = diffMillis / DAY;
			return days + "d ago";
		}
		else {
			return this.getDatePostedLocaleRepresentation();
		}
	}

	/**
	 * Sets the location associated with this post to {@code location}.
	 *
	 * @param location the {@code Location} to associate with this post
	 */
	public void setLocation(Location location) {
		if (location != null) {
			this.latitude = location.getLatitude();
			this.longitude = location.getLongitude();
		}
		else {
			this.latitude = INVALID_COORDINATE;
			this.longitude = INVALID_COORDINATE;
		}
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

		return coordinate;
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
		} else {
			builder.append("N ");
		}

		builder.append(lon);

		if (this.longitude < 0.0D) {
			builder.append("W");
		} else {
			builder.append("E");
		}

		return builder.toString();
	}

	/**
	 * Returns the name of the city based on the location of the post if it
	 * has a valid location.
	 *
	 * @param context the context from which to obtain the geographical map
	 *                location information from
	 *
	 * @return the name of the city from which the post was created if it can
	 * 		   be obtained; a string representation of the post's latitude and
	 * 		   longitude if the name of the city cannot be obtained, and an
	 * 		   empty string otherwise.
	 */
	public String getCityLocation(Context context) {
		if (!this.hasValidLocation()) {
			return "";
		}

		Geocoder geocoder = new Geocoder(context, Locale.getDefault());
		String cityName = this.getLocation();

		try {
			List<Address> addresses = geocoder.getFromLocation(this.latitude, this.longitude, 1);

			if (addresses != null && !addresses.isEmpty()) {
				Address address = addresses.get(0);

				if (address != null && address.getLocality() != null) {
					cityName = addresses.get(0).getLocality();
				}
			}
		}
		catch (Exception e)
		{
			Log.e("MOOD_POST", "Failed to obtain the city location", e);
		}

		return cityName;
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
	 * Returns if the coordinates of the post's location represent a valid
	 * location.
	 *
	 * @return {@code true} if the coordinates of the post are valid,
	 * 		   {@code false} otherwise.
	 */
	public boolean hasValidLocation() {
		boolean invalidLatitude = this.latitude == INVALID_COORDINATE;
		boolean invalidLongitude = this.longitude == INVALID_COORDINATE;

		if (invalidLatitude || invalidLongitude) {
			return false;
		}
		else {
			return Math.abs(this.latitude) <= 90.0D && Math.abs(this.latitude) <= 180.0D;
		}
	}


	/**
	 * <p>Sets the image data for this post to the given {@code base64Data}.
	 * It is expected that the given data is in <i>Base64</i> format and as
	 * such this method should only be used for reconstructing the image from
	 * a saved base64 string.</p>
	 * 
	 * @see #setImageData(byte[])
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
		if (imageData != null) {
			int imageFlags = Base64.NO_PADDING | Base64.NO_WRAP | Base64.URL_SAFE;
			this.imageData = Base64.encodeToString(imageData, imageFlags);
		}
		else {
			this.imageData = null;
		}
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
	 * <p>Sets the view status of this post. A post that is marked as private will
	 * be visible only to the user that posted it, while a post that is marked
	 * as non-private (public) will be viewable by other users.</p>
	 *
	 * <p>Note: this flag only takes effect after changes to the database, thus
	 * this method alone will not mark a post as private or public.</p>
	 *
	 * @param isPrivate if this post should be marked as private
	 */
	public void setPrivateStatus(boolean isPrivate) {
		this.isPostPrivated = isPrivate;
	}

	/**
	 * Returns whether or not this post is privated (only visible to the user
	 * that posted the post).
	 *
	 * @return {@code true} if this post should be visible only to the poster,
	 * 		   {@code false} otherwise
	 */
	public boolean isPrivate() {
		return this.isPostPrivated;
	}

	/**
	 * <p>Sets the number of top-level comments that are associated with the
	 * post.</p>
	 *
	 * <p>It is expect that this number matches the count of the number of top-
	 * level comments a post has within the database and as such this should
	 * only be used by the database to set the count.</p>
	 *
	 * @param numComments the number of top-level comments associated with
	 *                    the post
	 */
	public void setNumTopLevelComments(int numComments) {
		this.numTopLevelComments = numComments;
	}

	/**
	 * Returns the number of top-level comments associated with the post.
	 *
	 * @return the number of top-level comments
	 */
	public int getNumTopLevelComments() {
		return this.numTopLevelComments;
	}

	/**
	 * Sets the edited flag for this post to the given {@code wasEdited}.
	 *
	 * @param wasEdited if this post has been edited
	 */
	public void setEdited(boolean wasEdited) {
		this.wasEdited = wasEdited;
		this.postDate = new Date();
	}

	/**
	 * Returns whether or not this post has been edited.
	 *
	 * @return {@code true} if this post has been edited, {@code false}
	 * 		   otherwise
	 */
	public boolean hasBeenEdited() {
		return this.wasEdited;
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
		ANGER("Angry", "😡", "🔴"),
		CONFUSED("Confused", "😕", "🟠"),
		DISGUSTED("Disgusted", "🤢", "🟢"),
		FEAR("Fear", "😨", "⚫"),
		HAPPY("Happy", "😊", "🟡"),
		SHAME("Shame", "😳", "⚪️"),
		SADNESS("Sadness", "😭", "🔵"),
		SURPRISED("Surprised", "😮", "🟣");
		private String emotionStr;
		private String emoji;
		private String colour;

		/**
		 * Creates a {@code Emotion} enumerator with the given descriptor
		 * given by {@code emotionStr} which details the name of the emotion,
		 * and associates it with the given {@code emoji} and {@code colour}.
		 *
		 * @param emotionStr the description name for the emotion
		 * @param emoji		 the emoji to use for the emotion
		 * @param colour	 the colour emoji for the emotion
		 */
		Emotion(String emotionStr, String emoji, String colour) {
			this.emotionStr = emotionStr;
			this.emoji = emoji;
			this.colour = colour;
		}

		/**
		 * Returns as a {@code String} a representation of the name of the
		 * emotion.
		 *
		 * @return the name of the emotion
		 */
		public String getEmotionDescrption() {
			return this.emotionStr;
		}

		/**
		 * Returns the emoji corresponding to the emotion.
		 *
		 * @return the emotion's emoji
		 */
		public String getEmoji() {
			return this.emoji;
		}

		/**
		 * Returns the colour corresponding to the the emotion.
		 *
		 * @return the emotion's colour
		 */
		public String getColour() {
			return this.colour;
		}
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