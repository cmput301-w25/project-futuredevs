package com.futuredevs.database;

import android.util.Log;

import com.futuredevs.database.IAuthenticator.AuthenticationResult;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>The {@code Database} class is a class that acts as a wrapper class for
 * the <b>Firebase Firestore</b> database that handles the interactions with
 * the database including initializing the connection with the database and
 * modifying the database.</p>
 *
 * <p>All interactions with the database should be done through this class. The
 * database uses a Singleton pattern and as such no instances of it can created
 * nor can this class be extended.</p>
 *
 * <p>To access the database, see {@link #getInstance()}.</p>
 *
 * @author Spencer Schmidt
 */
public final class Database
{
	/** A Singleton instance for this database. */
	private static Database theDatabase;
	/** Tag used for logging database information. */
	private static final String DB_TAG = "Database";
	private final List<IFollowingListener> followingListeners;
	/** The instance of the database */
	private final FirebaseFirestore db;

	/**
	 * Creates an instance of a {@code Database} object and initializes the
	 * default {@code FirebaseFirestore} database.
	 */
	private Database() {
		this.db = FirebaseFirestore.getInstance();
		this.followingListeners = new ArrayList<>();
	}

	/**
	 * Add a listener to listen for updates to attributes of users the main
	 * user follows such as new posts, posts being deleted, and posts being
	 * updated.
	 *
	 * @param listener the callback listener to send updates
	 */
	public void addFollowingUpdateListener(IFollowingListener listener) {
		if (!this.followingListeners.contains(listener)) {
			this.followingListeners.add(listener);
		}
	}

	/**
	 * Remove the given {@code listener} if it subscribed to updates from
	 * the following list.
	 *
	 * @param listener
	 */
	public void removeFollowingUpdateListener(IFollowingListener listener) {
		this.followingListeners.remove(listener);
	}

	/**
	 * Attempts to create a user in the database with the details specified by
	 * the {@code user}.
	 *
	 * @param user the details to use for user creation
	 * @param auth the authenticator callback for handling the results of the
	 *             user creation; see {@link IAuthenticator.AuthenticationResult}
	 *             for details on the possible results
	 */
	public void attemptSignup(UserDetails user, IAuthenticator auth) {
		DocumentReference ref = this.db.collection(DatabaseFields.USER_COLLECTION)
									   .document(user.getUsername());

		ref.get().addOnCompleteListener(task -> {
			if (task.isSuccessful()) {
				DocumentSnapshot snapshot = task.getResult();

				if (snapshot.exists()) {
					auth.onAuthenticationResult(AuthenticationResult.USERNAME_TAKEN);
				}
				else {
					Map<String, Object> newUserData = new HashMap<>();
					newUserData.put(DatabaseFields.USER_PWD_FLD, user.getPassword());

					ref.set(newUserData).addOnCompleteListener(createTask -> {
						if (createTask.isSuccessful()) {
							if (task.getResult() != null)  {
//								Database.this.registerSnapshotListeners(user.getUsername());
								auth.onAuthenticationResult(AuthenticationResult.SUCCEED);
								Log.i(DB_TAG, "User account successful");
							}
							else {
								auth.onAuthenticationResult(AuthenticationResult.FAIL);
								Log.e(DB_TAG,
									  "Failed to set the contents of the new user document",
									  createTask.getException());
							}
						}
					});
				}
			}
			else {
				auth.onAuthenticationResult(AuthenticationResult.FAIL);
				Log.e(DB_TAG, "Failed to retrieve the document", task.getException());
			}
		});
	}

	/**
	 * Checks the details of the given {@code user} in the database for a
	 * matching user account.
	 *
	 * @param user the details to use for user login
	 * @param auth the authenticator callback for handling the results of the
	 *             login; see {@link IAuthenticator.AuthenticationResult}
	 *             for details on the possible results
	 */
	public void validateLogin(UserDetails user, IAuthenticator auth) {
		DocumentReference ref = this.db.collection(DatabaseFields.USER_COLLECTION)
									   .document(user.getUsername());

		ref.get().addOnCompleteListener(task -> {
			if (task.isSuccessful()) {
				DocumentSnapshot snapshot = task.getResult();

				if (snapshot.exists()) {
					String password = snapshot.getString(DatabaseFields.USER_PWD_FLD);

					if (user.getPassword().equals(password)) {
//						Database.this.registerSnapshotListeners(user.getUsername());
						auth.onAuthenticationResult(AuthenticationResult.SUCCEED);
					}
					else {
						auth.onAuthenticationResult(AuthenticationResult.INVALID_DETAILS);
					}
				}
				else {
					auth.onAuthenticationResult(AuthenticationResult.INVALID_DETAILS);
				}
			}
			else {
				auth.onAuthenticationResult(AuthenticationResult.FAIL);
				Log.e(DB_TAG, "Failed to retrieve the document", task.getException());
			}
		});
	}

//	private void registerSnapshotListeners(String username) {
//		CollectionReference usersCollection = this.db.collection(DatabaseFields.USER_COLLECTION);
//		DocumentReference ref = usersCollection.document(username);
//
//		ref.get().addOnCompleteListener(task -> {
//			if (task.isSuccessful()) {
//				DocumentSnapshot snapshot = task.getResult();
//
//				if (snapshot.contains(DatabaseFields.USER_FOLLOWING_FLD)) {
//					List<String> followingNames =
//							(ArrayList<String>) snapshot.get(DatabaseFields.USER_FOLLOWING_FLD);
//
//					for (String name : followingNames) {
//						Database.this.registerFollowingSnapshotListener(name);
//					}
//				}
//			}
//		});
//	}

//	private void registerFollowingSnapshotListener(String username) {
//		CollectionReference usersCollection = this.db.collection(DatabaseFields.USER_COLLECTION);
//		CollectionReference userMoodsRef = usersCollection.document(username)
//														  .collection(DatabaseFields.USER_MOODS_COLLECTION);
//
//		userMoodsRef.addSnapshotListener((v, e) -> {
//			if (e != null)
//				return;
//
//			if (v != null) {
//				List<DocumentSnapshot> moods = v.getDocuments();
//				List<MoodPost> moodPosts = new ArrayList<>();
//
//				for (DocumentSnapshot moodSnapshot : moods) {
//					String emotionStr = moodSnapshot.getString("emotion");
//					long postTime = moodSnapshot.getLong("time_posted");
//					MoodPost.Emotion emotion = MoodPost.Emotion.valueOf(emotionStr);
//					MoodPost post = new MoodPost(moodSnapshot.getId(), emotion);
//
//					if (moodSnapshot.contains("trigger")) {
//						post.setTrigger(moodSnapshot.getString("trigger"));
//					}
//
//					if (moodSnapshot.contains("reason")) {
//						post.setReason(moodSnapshot.getString("reason"));
//					}
//
//					if (moodSnapshot.contains("social_situation")) {
//						String sitString = moodSnapshot.getString("social_situation");
//						MoodPost.SocialSituation situation
//								= MoodPost.SocialSituation.valueOf(sitString);
//						post.setSocialSituation(situation);
//					}
//
//					if (moodSnapshot.contains("location")) {
//						List<Double> coords = (List<Double>) moodSnapshot.get("location");
//						double lat = coords.get(0);
//						double lon = coords.get(1);
//						post.setLocation(lat, lon);
//					}
//
//					moodPosts.add(post);
//				}
//			}
//		});
//	}

	/**
	 * Returns a Singleton instance of this database. If the database does
	 * not already exist, then this will also initialize it.
	 *
	 * @return the Singleton instance of this database.
	 */
	public static Database getInstance() {
		if (theDatabase == null) {
			theDatabase = new Database();
		}

		return theDatabase;
	}
}