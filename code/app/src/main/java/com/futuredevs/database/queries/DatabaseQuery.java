package com.futuredevs.database.queries;

/**
 * @author Spencer Schmidt
 */
public class DatabaseQuery
{
	private QueryType type;
	private String username;
	private String searchTerm;

	private DatabaseQuery() {}

	protected void setUser(String username) {
		this.username = username;
	}

	public String getUsername() {
		return this.username;
	}

	protected void setQueryType(QueryType type) {
		this.type = type;
	}

	public QueryType getQueryType() {
		return this.type;
	}

	protected void setSearchTerm(String term) {
		this.searchTerm = term;
	}

	public String getSearchTerm() {
		return this.searchTerm;
	}

	/**
	 * The {@code QueryBuilder} class follows a builder pattern for building
	 * queries to be sent to the database.
	 */
	public static class QueryBuilder {
		protected final DatabaseQuery theQuery;

		public QueryBuilder() {
			this.theQuery = new DatabaseQuery();
		}

		public QueryBuilder setSourceUser(String username) {
			this.theQuery.setUser(username);
			return this;
		}

		public QueryBuilder setType(QueryType type) {
			this.theQuery.setQueryType(type);
			return this;
		}

		public DatabaseQuery build() {
			return this.theQuery;
		}
	}

	public static class QueryBuilderUsers extends QueryBuilder {
		public QueryBuilderUsers setSearchTerm(String term) {
			this.theQuery.setSearchTerm(term);
			return this;
		}
	}

	/**
	 * The {@code QueryType} enumeration represents a type of query to be made
	 * on the database
	 */
	public enum QueryType {
		/**
		 * Queries of this type will return a list of {@code MoodPost}
		 * corresponding to the currently logged in user.
		 */
		USER_POSTS,
		/**
		 * Queries of this type will return a list of notifications
		 * corresponding to the currently logged in user.
		 */
		USER_NOTIFICATIONS,
		/**
		 * Queries of this type will return a list of {@code MoodPost}
		 * containing all mood posts for each user the currently logged
		 * in user follows.
		 */
		FOLLOWING_POSTS,
		/**
		 * Queries of this type will return a list of users that match
		 * a given string.
		 */
		USERS
	}
}