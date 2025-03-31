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

/**
 * The {@code ViewModelMoodsFollow} class is a model class that is intended to
 * represent a model containing the mood posts of the users that a given user
 * follows. This model will retrieve all moods of those users except for those
 * that are marked as private.
 *
 * @author Spencer Schmidt
 */
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

	/**
	 * Requests the data for the model, attempting to obtain the posts of the
	 * users a user follows if there are any.
	 */
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

	/**
	 * Returns the data associated with the model (if any). Changes to the
	 * model data can be observed through this.
	 *
	 * @return the data for the model.
	 */
	public MutableLiveData<List<MoodPost>> getData() {
		return this.moodData;
	}

	@Override
	public void onQueryResult(List<DocumentSnapshot> documents, DatabaseResult result) {
		List<MoodPost> posts = new ArrayList<>();

		if (result != DatabaseResult.FAILURE) {
			for (DocumentSnapshot snapshot : documents) {
				if (snapshot.contains(DatabaseFields.MOOD_VIEW_STATUS_FLD)) {
					boolean isPrivated = snapshot.getBoolean(DatabaseFields.MOOD_VIEW_STATUS_FLD);

					if (isPrivated)
						continue;
				}

				posts.add(Database.getInstance().parseMood(snapshot));
			}

			this.setModelData(posts);
		}
	}

	/**
	 * The {@code ViewModelMoodsFollowingFactory} is a factory class intended
	 * to be used to contruct instances of {@code ViewModelMoodsFollowing}
	 * through the necessary {@code ViewModelProvider} constructors.
	 */
	public static class ViewModelMoodsFollowingFactory implements ViewModelProvider.Factory {
		private final String username;

		/**
		 * Creates a {@code ViewModelMoodsFollowingFactory} instances associated
		 * with the given {@code username}. The username is the name of the user
		 * of which their following will be used to obtain the posts.
		 *
		 * @param username the name of the user to obtain the following from
		 */
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
