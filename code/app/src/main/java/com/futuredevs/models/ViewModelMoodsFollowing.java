package com.futuredevs.models;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.futuredevs.database.Database;
import com.futuredevs.database.DatabaseFields;
import com.futuredevs.database.DatabaseResult;
import com.futuredevs.database.queries.DatabaseQuery;
import com.futuredevs.database.queries.IQueryListener;
import com.futuredevs.models.items.MoodPost;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ViewModelMoodsFollowing extends ViewModel implements IQueryListener {
	private final String username;
	private MutableLiveData<List<MoodPost>> moodData = new MutableLiveData<>();

	/**
	 * Creates a {@code ViewModelMoodsFollowing} which acts as a representation
	 * of a model containing the mood posts of the users that the user given by
	 * {@code username} follows.
	 *
	 * @param username the name of the user to obtain a follow list from
	 */
	public ViewModelMoodsFollowing(String username) {
		this.username = username;
		this.requestData();
	}

	public void requestData() {
		DatabaseQuery.QueryBuilder builder = new DatabaseQuery.QueryBuilder();
		builder.setType(DatabaseQuery.QueryType.FOLLOWING_POSTS)
			   .setSourceUser(this.username);
		DatabaseQuery query = builder.build();
		Database.getInstance().performQuery(query, this);
	}

	private void setModelData(List<MoodPost> posts) {
		this.moodData.setValue(posts);
	}

	public MutableLiveData<List<MoodPost>> getData() {
		return this.moodData;
	}

	@Override
	public void onQueryResult(List<DocumentSnapshot> documents, DatabaseResult result) {
		List<MoodPost> posts = new ArrayList<>();

		if (result != DatabaseResult.FAILURE) {
			// Temporary way to not obtain private posts
			for (DocumentSnapshot snapshot : documents) {
				if (snapshot.contains(DatabaseFields.MOOD_VIEW_STATUS_FLD)) {
					boolean isPrivated = snapshot.getBoolean(DatabaseFields.MOOD_VIEW_STATUS_FLD);

					if (isPrivated)
						continue;
				}

				String documentId = snapshot.getId();
				String user = snapshot.getString(DatabaseFields.USER_NAME_FLD);
				String emotionStr = snapshot.getString(DatabaseFields.MOOD_EMOTION_FLD);
				MoodPost.Emotion emotion = MoodPost.Emotion.valueOf(emotionStr);
				MoodPost post = new MoodPost(documentId, user, emotion);

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

				if (snapshot.contains(DatabaseFields.MOOD_IMG_FLD)) {
					post.setImageData(snapshot.getString(DatabaseFields.MOOD_IMG_FLD));
				}

				posts.add(post);
			}

			this.setModelData(posts);
		}
	}

	public static class ViewModelMoodsFollowingFactory implements ViewModelProvider.Factory {
		private final String username;

		public ViewModelMoodsFollowingFactory(String username) {
			this.username = username;
		}

		@NonNull
		@Override
		public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
			return (T) new ViewModelMoodsFollowing(this.username);
		}
	}
}
