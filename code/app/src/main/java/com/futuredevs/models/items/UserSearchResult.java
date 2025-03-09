package com.futuredevs.models.items;

/**
 * The {@code UserSearchResult} represents a result in the search screen and
 * contains information about the state of the user for the user performing
 * the search such as if the user has already requested to follow them and
 * if the user is already following the user.
 */
public class UserSearchResult {
	private final String username;
	private final boolean isFollowPending;
	private final boolean isUserFollowing;

	/**
	 * Creates an instance of a {@code UserSearchResult} where {@code name} is
	 * the name of a user that matches a given search term.
	 *
	 * @param name            the name of the user in the results
	 * @param isFollowPending whether the active user has requested to follow
	 *                        the user given by {@code name}
	 * @param isUserFollowing whether the active user is already following the
	 *                        user given by {@code name}
	 */
	public UserSearchResult(String name,
							boolean isFollowPending,
							boolean isUserFollowing) {
		this.username = name;
		this.isFollowPending = isFollowPending;
		this.isUserFollowing = isUserFollowing;
	}

	/**
	 * Returns the username of the user associated with this search result.
	 *
	 * @return the username of the user for this result
	 */
	public String getUsername() {
		return this.username;
	}

	/**
	 * Returns whether the active user has already requested to follow the user
	 * associated with this result.
	 *
	 * @return {@code true} is the active user has already requested to follow
	 *         the user, {@code false} otherwise
	 */
	public boolean isFollowPending() {
		return this.isFollowPending;
	}

	/**
	 * Returns whether the active user is following the user associated with
	 * this result.
	 *
	 * @return {@code true} is the active user is following the user,
	 *         {@code false} otherwise
	 */
	public boolean isUserFollowing() {
		return this.isUserFollowing;
	}
}