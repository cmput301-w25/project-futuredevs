package com.futuredevs.models;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.futuredevs.database.Database;
import com.futuredevs.database.DatabaseResult;
import com.futuredevs.models.items.UserProfile;

/**
 * <p>The {@code ViewModelUserProfile} class represents a model of the data necessary
 * for a user's profile page including the users they are following, the users
 * who follow them, and the users the currently have pending requests for.</p>
 *
 * <p>Instances of the {@code ViewModelUserProfile} should be created using
 * factory provided by the {@link ViewModelUserProfileFactory} class.</p>
 *
 * @author Spencer Schmidt
 */
public class ViewModelUserProfile extends ViewModel {
	private final String username;
	private final MutableLiveData<UserProfile> userProfile = new MutableLiveData<>();

	/**
	 * Creates an instance of a {@code ViewModelUserPage} for the user given
	 * by {@code username} and fills the model with the user's data.
	 *
	 * @param username the name of the user to model the data for
	 */
	public ViewModelUserProfile(String username) {
		this.username = username;
		this.requestData();
	}

	/**
	 * Requests the data for the user represented by the model. If the data can
	 * be successfully obtained, then the model's data will subsequently be
	 * updated.
	 */
	public void requestData() {
		Database.getInstance().requestUserInformation(this.username, (r, data) -> {
			if (r == DatabaseResult.SUCCESS) {
				UserProfile profile = data.get(0);
				this.setModelData(profile);
			}
			else {
				this.setModelData(null);
			}
		});
	}

	/**
	 * Sends a following request to the user represented by the model from
	 * the user given by {@code user}.
	 *
	 * @param user the user sending the follow request
	 */
	public void sendFollowing(String user) {
		Database.getInstance().sendFollowRequest(user, this.username);
	}

	/**
	 * Removes the given {@code user} from the user represented by the model's
	 * followers list and in turn removes the user represented by the model
	 * from the {@code user}'s following list.
	 *
	 * @param user the user to remove as a follower
	 */
	public void removeFollower(String user) {
		Database.getInstance().removeFollower(this.username, user, r -> {
			if (r == DatabaseResult.SUCCESS) {
				requestData();
			}
		});
	}

	private void setModelData(UserProfile profile) {
		this.userProfile.setValue(profile);
	}

	/**
	 * Returns the user profile data that is currently held by the model.
	 * Updates to the model data should be observed through this method.
	 *
	 * @return the {@code UserProfile} held by the model
	 */
	public MutableLiveData<UserProfile> getData() {
		return this.userProfile;
	}

	/**
	 * The {@code ViewModelUserProfileFactory} class is a factory class that is
	 * used to create instances of the {@code ViewModelUserProfile} class.
	 *
	 * @author Spencer Schmidt
	 */
	public static class ViewModelUserProfileFactory implements ViewModelProvider.Factory {
		private final String username;

		/**
		 * Creates a user page factory for the user given by {@code username}.
		 *
		 * @param username the name of the user to create the view model for
		 */
		public ViewModelUserProfileFactory(String username) {
			this.username = username;
		}

		@NonNull
		@Override
		public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
			return (T) new ViewModelUserProfile(this.username);
		}
	}
}
