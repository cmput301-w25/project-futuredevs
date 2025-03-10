package com.futuredevs.models;

import com.futuredevs.database.Database;
import com.futuredevs.database.DatabaseFields;
import com.futuredevs.database.DatabaseResult;
import com.futuredevs.models.items.MoodPost;
import com.futuredevs.database.queries.DatabaseQuery;
import com.futuredevs.database.queries.IQueryListener;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * The {@code ModelMoodsFollowing} class represents a model class in which
 * the posts of all users a user follows are contained.
 *
 * @author Spencer Schmidt
 */
public class ModelMoodsFollowing extends ModelBase<MoodPost> implements IQueryListener {
	/** The username of the user whose following should be searched. */
	private final String username;

	/**
	 * Creates an instance of a {@code ModelMoodsFollowing} where the given
	 * {@code username} is the name of the user whose following will be used
	 * as the source for obtaining the data of this model.
	 *
	 * @param username the name of the user to obtain following posts from
	 */
	public ModelMoodsFollowing(String username) {
		this.username = username;
	}

	@Override
	public void requestData() {
		DatabaseQuery.QueryBuilder builder = new DatabaseQuery.QueryBuilder();
		builder.setType(DatabaseQuery.QueryType.FOLLOWING_POSTS)
			   .setSourceUser(this.username);
		DatabaseQuery query = builder.build();
		Database.getInstance().performQuery(query, this);
	}

	@Override
	public void onQueryResult(List<DocumentSnapshot> documents, DatabaseResult result) {
		List<MoodPost> posts = new ArrayList<>();

		if (result != DatabaseResult.FAILURE) {
			for (DocumentSnapshot snapshot : documents) {
				String documentId = snapshot.getId();
				String user = snapshot.getString(DatabaseFields.USER_NAME_FLD);
				String emotionStr = snapshot.getString(DatabaseFields.MOOD_EMOTION_FLD);
				MoodPost.Emotion emotion = MoodPost.Emotion.valueOf(emotionStr);
				MoodPost post = new MoodPost(documentId, user, emotion);

				if (snapshot.contains(DatabaseFields.MOOD_TRIGGER_FLD)) {
					post.setTrigger(snapshot.getString(DatabaseFields.MOOD_TRIGGER_FLD));
				}

				if (snapshot.contains(DatabaseFields.MOOD_REASON_FLD)) {
					post.setReason(snapshot.getString(DatabaseFields.MOOD_REASON_FLD));
				}

				if (snapshot.contains(DatabaseFields.MOOD_SITUATION_FLD)) {
					String sitStr = snapshot.getString(DatabaseFields.MOOD_SITUATION_FLD);
					MoodPost.SocialSituation situation = MoodPost.SocialSituation.valueOf(sitStr);
					post.setSocialSituation(situation);
				}

				long timePosted = snapshot.getLong(DatabaseFields.MOOD_TIME_FLD);
				post.setTimePosted(timePosted);

				if (snapshot.contains(DatabaseFields.MOOD_LOCATION_FLD)) {
					List<Double> coordinates = (List<Double>)
							snapshot.get(DatabaseFields.MOOD_LOCATION_FLD);
					double latitude = coordinates.get(0);
					double longitude = coordinates.get(1);
					post.setLocation(latitude, longitude);
				}

				posts.add(post);
			}

			this.setData(posts);
			this.notifyModelChanged();
		}
	}
}