package com.futuredevs.database;

/**
 * An class that implements the {@code IAuthenticator} interface is able to
 * subscribe to callbacks from the database to listen for the results of
 * account authentication.
 *
 * @author Spencer Schmidt
 */
public interface IAuthenticator  {
	/**
	 * A callback that is invoked after a user attempts to login or sign up
	 * for a new account.
	 *
	 * @param result the result of account authentication.
	 */
    void onAuthenticationResult(AuthenticationResult result);

	/**
	 * <p>The {@code AutheticationResult} enumeration represents the result of
	 * attempting to login to an existing account or signing up for a new
	 * account.</p>
	 *
	 * <p>The possible results of authentication are dependent on whether the
	 * user is logging in or signing up and are given by:
	 * <li>{@link #SUCCEED}</li>
	 * <li>{@link #USERNAME_TAKEN}</li>
	 * <li>{@link #INVALID_DETAILS}</li>
	 * <li>{@link #FAIL}</li></p>
	 */
    enum AuthenticationResult {
		/**
		 * If the account exists and the user gives the correct login details
		 * or an account is successfully created, then this will be returned.
		 */
		SUCCEED,
		/**
		 * If a user is signing up for an account and the given username is
		 * already taken, then this will be returned.
		 */
		USERNAME_TAKEN,
		/**
		 * If the user exists, but the user entered the incorrect details,
		 * then this will be returned. Will also be returned on signup if
		 * a user with the specified username does not exist.
		 */
		INVALID_DETAILS,
		/**
		 * If any other type of error is encountered during authentication,
		 * then this will be returned.
		 */
		FAIL
	}
}