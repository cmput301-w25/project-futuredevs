package com.futuredevs.models;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.futuredevs.database.Database;
import com.futuredevs.database.DatabaseFields;
import com.futuredevs.database.DatabaseResult;
import com.futuredevs.database.IResultListener;
import com.futuredevs.database.queries.DatabaseQuery;
import com.futuredevs.database.queries.IQueryListener;
import com.futuredevs.models.items.MoodPost;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * The {@code ViewModelMoods} class is a model that holds a list of
 * {@code MoodPost} objects associated with a user. Provides methods
 * for requesting the mood posts for the user, updating moods, and
 * removing moods.
 *
 * @author Spencer Schmidt
 */
public class ViewModelMoods extends ViewModel implements IQueryListener {
	private final String username;
	private MutableLiveData<List<MoodPost>> moodData = new MutableLiveData<>();

	/**
	 * Creates a {@code ViewModelMoods} which acts as a representation of a
	 * model containing the mood posts of the user given by {@code username}.
	 * Also requests the initial data for the given user.
	 *
	 * @param username the name of the user to model the moods of
	 */
	public ViewModelMoods(String username) {
		this.username = username;
		this.requestData();
	}

	/**
	 * Requests the most up to date data for the model. On obtaining new data,
	 * the model data will notify observers of the new data.
	 *
	 * @see #getData()
	 */
	public void requestData() {
		DatabaseQuery.QueryBuilder builder = new DatabaseQuery.QueryBuilder();
		builder.setType(DatabaseQuery.QueryType.USER_POSTS)
			   .setSourceUser(this.username);
		DatabaseQuery query = builder.build();
		Database.getInstance().performQuery(query, this);
	}

	private void setModelData(List<MoodPost> posts) {
		this.moodData.setValue(posts);
	}

	/**
	 * Attempts to add the given {@code post} to the user's list of moods
	 * and returns the success or failure of the operation to the provided
	 * {@code listener}.
	 *
	 * @param post	   the post to attempt to add
	 * @param listener the listener to notify of operation success or failure
	 */
	public void addMood(MoodPost post, IResultListener listener) {
		Database.getInstance().addMood(this.username, post, r -> {
			if (r == DatabaseResult.SUCCESS) {
				ViewModelMoods.this.requestData();
			}

			listener.onResult(r);
		});
	}

	/**
	 * Attempts to update the post that is associated with the given
	 * {@code post} in the user's list of moods and returns the success
	 * or failure of the operation to the provided {@code listener}.
	 *
	 * @param post	   the post to attempt to update
	 * @param listener the listener to notify of operation success or failure
	 */
	public void updateMood(MoodPost post, IResultListener listener) {
		Database.getInstance().editMood(this.username, post, r -> {
			if (r == DatabaseResult.SUCCESS) {
				ViewModelMoods.this.requestData();
			}

			listener.onResult(r);
		});
	}

	/**
	 * Attempts to remove the given {@code post} to the user's list of moods
	 * and returns the success or failure of the operation to the provided
	 * {@code listener}.
	 *
	 * @param post	   the post to attempt to remove
	 * @param listener the listener to notify of operation success or failure
	 */
	public void removeMood(MoodPost post, IResultListener listener) {
		Database.getInstance().removeMood(this.username, post, r -> {
			if (r == DatabaseResult.SUCCESS) {
				ViewModelMoods.this.requestData();
			}

			listener.onResult(r);
		});
	}

	/**
	 * Returns an observable object which can be used to observe changes to the
	 * model's data.
	 *
	 * @return an observable object containing the model data
	 */
	public MutableLiveData<List<MoodPost>> getData() {
		return this.moodData;
	}

	@Override
	public void onQueryResult(List<DocumentSnapshot> documents, DatabaseResult result) {
		List<MoodPost> posts = new ArrayList<>();

		if (result != DatabaseResult.FAILURE) {
			for (DocumentSnapshot snapshot : documents) {
				MoodPost post = Database.getInstance().parseMood(snapshot);

				if (snapshot.contains(DatabaseFields.MOOD_VIEW_STATUS_FLD)) {
					boolean isPrivated = snapshot.getBoolean(DatabaseFields.MOOD_VIEW_STATUS_FLD);

					if (isPrivated && !this.username.equals(Database.getInstance().getCurrentUser())) {
						continue;
					}

					post.setPrivateStatus(isPrivated);
				}

				posts.add(post);
			}

			this.setModelData(posts);
		}
	}

	/**
	 * The {@code ViewModelMoodsFactory} class is a factory class intended to
	 * be used to construct instances of {@code ViewModelMoods} models. The
	 * factory is used to provide a username to the model to obtain the
	 * provided user's moods.
	 *
	 * @author Spencer Schmidt
	 */
	public static class ViewModelMoodsFactory implements ViewModelProvider.Factory {
		private final String username;

		/**
		 * Creates a {@code ViewModelMoodsFactory} instance to associate the
		 * given {@code username} with a {@code ViewModelMoods}.
		 *
		 * @param username the name of the user to obtain the moods from
		 */
		public ViewModelMoodsFactory(String username) {
			this.username = username;
		}

		@NonNull
		@Override
		public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
			return (T) new ViewModelMoods(this.username);
		}
	}
}