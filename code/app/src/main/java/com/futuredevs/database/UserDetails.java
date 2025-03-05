package com.futuredevs.database;

/**
 * <p>The {@code UserDetails} class serves as a POJO (Plain Old Java Object)
 * representation of a user's details for login and sign-up to associate a
 * username and password. The details of instances of this class are not able
 * to be changed once instantiated.</p>
 *
 * @author Spencer Schmidt
 */
public final class UserDetails  {
	private String username;
	private String password;

	/**
	 * Creates an instance of a {@code UserDetails} class with the given
	 * {@code username} and {@code password}.
	 *
	 * @param username the name to give the user
	 * @param password the password to associate with the account
	 */
	public UserDetails(String username, String password) {
		this.username = username;
		this.password = password;
	}

	/**
	 * Returns the username of this user.
	 *
	 * @return this user's username
	 */
	public String getUsername() {
		return this.username;
	}

	/**
	 * Returns the password of this user.
	 *
	 * @return this user's password
	 */
	public String getPassword() {
		return this.password;
	}
}