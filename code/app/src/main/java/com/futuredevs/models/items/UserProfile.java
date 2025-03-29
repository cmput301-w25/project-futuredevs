package com.futuredevs.models.items;

import java.util.ArrayList;
import java.util.List;

/**
 * The {@code UserProfile} class is a model class used to represent the data
 * of a user including their name and all types of follows they currently have
 * such as pending sent follow requests, users they are following, and users
 * that currently follow them.
 *
 * @author Spencer Schmidt
 */
public class UserProfile {
	private String username;
	private List<String> followers;
	private List<String> following;
	private List<String> pending;

	/**
	 * Creates a {@code UserProfile} instance that represents the user given by
	 * the name {@code username}.
	 *
	 * @param username the name of the user to represent
	 */
	public UserProfile(String username) {
		this.username = username;
		this.followers = new ArrayList<>();
		this.following = new ArrayList<>();
		this.pending = new ArrayList<>();
	}

	/**
	 * Returns the name of the user represented by the profile.
	 * 
	 * @return the user's name
	 */
	public String getUsername() {
		return this.username;
	}

	/**
	 * Returns as a list the names of the users that follow the user/
	 * 
	 * @return the user's followers
	 */
	public List<String> getFollowers() {
		return this.followers;
	}

	/**
	 * Returns as a list the names of the users the user has sent follow
	 * requests to.
	 *
	 * @return users that have been sent follow requests
	 */
	public List<String> getPending() {
		return this.pending;
	}

	/**
	 * Returns as a list the names of the users the user follows.
	 *
	 * @return the users the user follows
	 */
	public List<String> getFollowing() {
		return this.following;
	}
}