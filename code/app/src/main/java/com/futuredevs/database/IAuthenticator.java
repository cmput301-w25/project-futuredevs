package com.futuredevs.database;

/**
 *
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
	public void onAuthenticationResult(AuthenticationResult result);

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
	public enum AuthenticationResult {
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
		 * then this will be returned.
		 */
		INVALID_DETAILS,
		/**
		 * If any other type of error is encountered during authentication,
		 * then this will be returned.
		 */
		FAIL
	}
}