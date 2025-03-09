package com.futuredevs.database.queries;

/**
 * The {@code DatabaseQuery} class is a class intended to be used to perform
 * specific types of queries on the Firebase database. Provides builders for
 * creating
 *
 * @author Spencer Schmidt
 */
public class DatabaseQuery
{
	private QueryType type;
	private String username;
	private String searchTerm;

	private DatabaseQuery() {}

	/**
	 * Sets the username of this query to {@code username}.
	 *
	 * @param username the username to use for this query
	 */
	protected void setUser(String username) {
		this.username = username;
	}

	/**
	 * Returns the username to use as the source of data for this query.
	 *
	 * @return the username to query data from
	 */
	public String getUsername() {
		return this.username;
	}

	/**
	 * Sets the type of query to the given {@code type}.
	 *
	 * @param type the type of query to perform
	 */
	protected void setQueryType(QueryType type) {
		this.type = type;
	}

	/**
	 * Returns the type of query to perform.
	 *
	 * @return the query to perform
	 */
	public QueryType getQueryType() {
		return this.type;
	}

	/**
	 * Sets the search term of a search query to {@code term}.
	 *
	 * @param term the term to use for a search query
	 */
	protected void setSearchTerm(String term) {
		this.searchTerm = term;
	}

	/**
	 * Returns the term to use for search queries.
	 *
	 * @return the term to use to query data
	 */
	public String getSearchTerm() {
		return this.searchTerm;
	}

	/**
	 * The {@code QueryBuilder} class follows a builder pattern for building
	 * queries to be sent to the database.
	 */
	public static class QueryBuilder {
		private final DatabaseQuery theQuery;

		public QueryBuilder() {
			this.theQuery = new DatabaseQuery();
		}

		/**
		 * Sets the type of query to be built and also determines which
		 * attributes for the search are required and not required. To
		 * determine what attributes are required for each type of query,
		 * see {@link QueryType}.
		 *
		 * @param type the type of query to build
		 *
		 * @return the current builder
		 */
		public QueryBuilder setType(QueryType type) {
			this.theQuery.setQueryType(type);
			return this;
		}

		/**
		 * Sets the username of the user from which the data should be queried.
		 *
		 * @param username the name of the user to query from
		 *
		 * @return the current builder
		 */
		public QueryBuilder setSourceUser(String username) {
			this.theQuery.setUser(username);
			return this;
		}

		/**
		 * Sets the search term for this query to {@code term}.
		 *
		 * @param term the term to query for searches
		 *
		 * @return the current builder
		 */
		public QueryBuilder setSearchTerm(String term) {
			this.theQuery.setSearchTerm(term);
			return this;
		}

		/**
		 * Returns a {@code DatabaseQuery} with the attributes set by this
		 * builder object.
		 *
		 * @return a new query based on the set attributes of this builder
		 */
		public DatabaseQuery build() {
			return this.theQuery;
		}
	}

	/**
	 * The {@code QueryType} enumeration represents a type of query to be made
	 * on the database
	 */
	public enum QueryType {
		/**
		 * <p>Queries of this type will create a list of {@code MoodPost}
		 * corresponding to the a given user.</p>
		 *
		 * <p>Queries of this type require a source user.</p>
		 */
		USER_POSTS,
		/**
		 * <p>Queries of this type will create a list of notifications
		 * corresponding to a given user.</p>
		 *
		 * <p>Queries of this type require a source user.</p>
		 */
		USER_NOTIFICATIONS,
		/**
		 * <p>Queries of this type will create a list of {@code MoodPost}
		 * containing all mood posts for each user a given user follows.</p>
		 *
		 * <p>Queries of this type require a source user.</p>
		 */
		FOLLOWING_POSTS,
		/**
		 * <p>Queries of this type will create a list of users whose names
		 * begin with a given string.</p>
		 *
		 * <p>Queries of this type require a search term.</p>
		 */
		USERS
	}
}