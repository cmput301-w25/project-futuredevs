package com.futuredevs.database;

import android.util.Log;

import com.futuredevs.database.IAuthenticator.AuthenticationResult;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>The {@code Database} class is a class that acts as a wrapper class for
 * the <b>Firebase Firestore</b> database that handles the interactions with
 * the database including initializing the connection with the database and
 * modifying the database. All interactions with the database should be done
 * through this class.</p>
 *
 * @author Spencer Schmidt
 */
public final class Database
{
	/** Tag used for logging database information. */
	private static final String DB_TAG = "Database";
	/** The instance of the database */
	private final FirebaseFirestore db;

	/**
	 * Creates an instance of a {@code Database} object and initializes the
	 * default {@code FirebaseFirestore} database.
	 */
	public Database() {
		this.db = FirebaseFirestore.getInstance();
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
					Map<String, Object> newUserData = this.initializeNewUser(user);

					ref.set(newUserData).addOnCompleteListener(createTask -> {
						if (createTask.isSuccessful()) {
							if (task.getResult() != null)  {
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

	/**
	 * Initializes the data for a brand new user account having all fields
	 * that are required.
	 *
	 * @param user the user to initialize the data for
	 * @return a {@code Map<String, Object>} containing the initial data for
	 * 		   the given {@code user}
	 */
	private Map<String, Object> initializeNewUser(UserDetails user) {
		Map<String, Object> fields = new HashMap<>();
		fields.put(DatabaseFields.USER_PWD_FLD, user.getPassword());
		fields.put(DatabaseFields.USER_FOLLOWING_FLD, Collections.emptyList());
		fields.put(DatabaseFields.USER_FOLLOWERS_FLD, Collections.emptyList());
		fields.put(DatabaseFields.USER_NOTIF_FLD, Collections.emptyList());
		return fields;
	}
}