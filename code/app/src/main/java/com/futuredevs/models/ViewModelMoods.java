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

public class ViewModelMoods extends ViewModel implements IQueryListener {
	private final String username;
	private MutableLiveData<List<MoodPost>> moodData = new MutableLiveData<>();

	/**
	 * Creates a {@code ViewModelMoods} which acts as a representation of a
	 * model containing the mood posts of the user given by {@code username}.
	 *
	 * @param username the name of the user to model the moods of
	 */
	public ViewModelMoods(String username) {
		this.username = username;
		this.requestData();
	}

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

	public void addMood(MoodPost post) {
		Database.getInstance().addMood(this.username, post, r -> {
			if (r == DatabaseResult.SUCCESS) {
				ViewModelMoods.this.requestData();
			}
		});
	}

	public void updateMood(MoodPost item) {
		Database.getInstance().editMood(this.username, item, r -> {
			if (r == DatabaseResult.SUCCESS) {
				ViewModelMoods.this.requestData();
			}
		});
	}

	public void removeMood(MoodPost post) {
		Database.getInstance().removeMood(this.username, post, r -> {
			if (r == DatabaseResult.SUCCESS) {
				ViewModelMoods.this.requestData();
			}
		});
	}

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

	public static class ViewModelMoodsFactory implements ViewModelProvider.Factory {
		private final String username;

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