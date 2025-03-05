package com.futuredevs.database;

import com.futuredevs.database.IAuthenticator.AuthenticationResult;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * <p>The {@code Database} class is a class that acts as a wrapper class for
 * the <b>Firebase Firestore</b> database that handles the interactions with
 * the database including initializing the connection with the database and
 * modifying the database. All interactions with the database should be done
 * through this class.</p>
 *
 * @author Spencer Schmidt
 */
public class Database
{
	/** Represents the name of the user's collection. */
	public static final String USER_COLLECTION = "users";
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
	public void createUser(UserDetails user, IAuthenticator auth) {
		DocumentReference ref = this.db.collection(USER_COLLECTION)
									   .document(user.getUsername());

		ref.get().addOnCompleteListener(task -> {
			if (task.isSuccessful()) {
				DocumentSnapshot snapshot = task.getResult();

				if (snapshot.exists()) {
					auth.onAuthenticationResult(AuthenticationResult.USERNAME_TAKEN);
				}
				else {

					auth.onAuthenticationResult(AuthenticationResult.SUCCEED);
				}
			}
			else {
				auth.onAuthenticationResult(AuthenticationResult.FAIL);
			}
		});
	}
}