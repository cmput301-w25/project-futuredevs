package com.futuredevs.database;

import java.util.List;
import java.util.Map;

/**
 * @author Spencer Schmidt
 */
public interface IFollowingListener  {
	/**
	 *
	 */
	public void onFollowingUpdate(String username, List<MoodPost> followingPosts);
}