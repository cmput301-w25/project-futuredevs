package com.futuredevs.models;

import com.futuredevs.database.Database;
import com.futuredevs.database.DatabaseFields;
import com.futuredevs.database.MoodPost;
import com.futuredevs.database.queries.DatabaseQuery;
import com.futuredevs.database.queries.IQueryListener;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * The {@code ModelMoods} class represents a model class in which the posts of
 * a user are contained.
 */
public class ModelMoods extends ModelBase<MoodPost> implements IQueryListener {
	/** The username of the user whose posts should be searched. */
	private final String username;

	/**
	 * Creates an instance of a {@code ModelMoods} where the given
	 * {@code username} is the name of the user whose posts will be
	 * used as the source for obtaining the data of this model.
	 *
	 * @param username the name of the user to obtain posts from
	 */
	public ModelMoods(String username) {
		this.username = username;
	}

	@Override
	protected void requestData()  {
		DatabaseQuery.QueryBuilder builder = new DatabaseQuery.QueryBuilder();
		builder.setType(DatabaseQuery.QueryType.USER_POSTS)
			   .setSourceUser(this.username);
		DatabaseQuery query = builder.build();
		Database.getInstance().performQuery(query, this);
	}

	@Override
	public void onQueryResult(List<DocumentSnapshot> documents, QueryResult result) {
		List<MoodPost> posts = new ArrayList<>();

		if (result != QueryResult.FAILURE) {
			for (DocumentSnapshot snapshot : documents) {
				String documentId = snapshot.getId();
				String emotionStr = snapshot.getString(DatabaseFields.MOOD_EMOTION_FLD);
				MoodPost.Emotion emotion = MoodPost.Emotion.valueOf(emotionStr);
				MoodPost post = new MoodPost(documentId, this.username, emotion);

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