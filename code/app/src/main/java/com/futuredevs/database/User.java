package com.futuredevs.database;

public class User {
	private final String username;
	private List<MoodPost> moodPosts;

	public User(String username) {
		this.username = username;
	}
}