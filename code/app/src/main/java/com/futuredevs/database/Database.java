package com.futuredevs.database;

import android.util.Log;

import com.futuredevs.database.IAuthenticator.AuthenticationResult;

import com.futuredevs.database.queries.DatabaseQuery;
import com.futuredevs.database.queries.IQueryListener;
import com.futuredevs.models.items.MoodComment;
import com.futuredevs.models.items.MoodPost;
import com.futuredevs.models.items.Notification;
import com.futuredevs.models.items.UserProfile;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>The {@code Database} class is a class that acts as a wrapper class for
 * the <b>Firebase Firestore</b> database that handles the interactions with
 * the database including initializing the connection with the database and
 * modifying the database.</p>
 *
 * <p>All interactions with the database should be done through this class. The
 * database uses a Singleton pattern and as such no instances of it can created
 * nor can this class be extended.</p>
 *
 * <p>To access the database, see {@link #getInstance()}.</p>
 *
 * @author Spencer Schmidt
 */
public final class Database
{
	/** A Singleton instance for this database. */
	private static Database theDatabase;
	/** Tag used for logging database information. */
	private static final String DB_TAG = "Database";
	/** The instance of the database. */
	private final FirebaseFirestore db;
	/** The name of the user currently logged into the application. */
	private String currentUserName;

	/**
	 * Creates an instance of a {@code Database} object and initializes the
	 * default {@code FirebaseFirestore} database.
	 */
	private Database() {
		this.db = FirebaseFirestore.getInstance();
		FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
				.setPersistenceEnabled(true)
				.build();
		db.setFirestoreSettings(settings);
	}

	/**
	 * Sets the name of the currently logged in user to {@code user}.
	 *
	 * @param user the name of the user who is active
	 */
	public void setCurrentUser(String user) {
		this.currentUserName = user;
	}

	/**
	 * Returns the username of the user currently logged into the application.
	 *
	 * @return the name of the logged in user
	 */
	public String getCurrentUser() {
		return this.currentUserName;
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
	public void attemptSignup(UserDetails user, IAuthenticator auth) {
		DocumentReference ref = this.getUserDoc(user.getUsername());

		ref.get()
		   .addOnSuccessListener(snapshot -> {
			   if (snapshot.exists()) {
				   auth.onAuthenticationResult(AuthenticationResult.USERNAME_TAKEN);
			   }
			   else {
				   Map<String, Object> newUserData = new HashMap<>();
				   // Queries are a bit simpler to perform if we include the
				   // username as part of the document.
				   newUserData.put(DatabaseFields.USER_NAME_FLD, user.getUsername());
				   newUserData.put(DatabaseFields.USER_PWD_FLD, user.getPassword());

				   ref.set(newUserData).addOnCompleteListener(createTask -> {
					   if (createTask.isSuccessful()) {
						   auth.onAuthenticationResult(AuthenticationResult.SUCCEED);
						   Log.i(DB_TAG, "User account successful");
					   }
					   else {
						   Log.e(DB_TAG,
								 "Task to set the contents of the new user document failed",
								 createTask.getException());
					   }
				   });
			   }
		   })
		   .addOnFailureListener(e -> {
			   auth.onAuthenticationResult(AuthenticationResult.FAIL);
			   Log.e(DB_TAG, "Failed to retrieve the document", e);
		   });
	}

	/**
	 * Checks the details of the given {@code user} in the database for a
	 * matching user account.
	 *
	 * @param user the details to use for user login
	 * @param auth the authenticator callback for handling the results of the
	 *             login; see {@link IAuthenticator.AuthenticationResult}
	 *             for details on the possible results
	 */
	public void validateLogin(UserDetails user, IAuthenticator auth) {
		DocumentReference ref = this.db.collection(DatabaseFields.USER_COLLECTION)
									   .document(this.getUserDocumentName(user.getUsername()));

		ref.get()
			.addOnSuccessListener(snapshot -> {
				if (snapshot.exists()) {
					String password = snapshot.getString(DatabaseFields.USER_PWD_FLD);

					if (user.getPassword().equals(password)) {
						auth.onAuthenticationResult(AuthenticationResult.SUCCEED);
					}
					else {
						auth.onAuthenticationResult(AuthenticationResult.INVALID_DETAILS);
					}
				}
				else {
					auth.onAuthenticationResult(AuthenticationResult.INVALID_DETAILS);
				}
			})
		   .addOnFailureListener(e -> {
			   Log.e(DB_TAG, "Task to validate login failed!", e);
			   auth.onAuthenticationResult(AuthenticationResult.FAIL);
		   });
	}

	/**
	 * <p>Performs a query based on the details of the given {@code query} and,
	 * upon completion, returns the results to the {@code listener}.</p>
	 *
	 * <p>See also: {@link IQueryListener#onQueryResult(List, DatabaseResult)}</p>
	 *
	 * @param query    the query to perform, see {@link DatabaseQuery.QueryBuilder}
	 * @param listener the listener to return the results of the querty to
	 *
	 * @implNote as of right now, this method returns the results as a list of
	 * {@code DocumentSnapshot} and the queries are not optimized. This may
	 * change in the future.
	 */
	public void performQuery(DatabaseQuery query, IQueryListener listener) {
		DatabaseQuery.QueryType queryType = query.getQueryType();
		CollectionReference usersCollection = this.db.collection(DatabaseFields.USER_COLLECTION);
		List<DocumentSnapshot> snapshots = new ArrayList<>();

		switch (queryType) {
			case USER_POSTS:
				this.getUserDoc(query.getUsername())
					.collection(DatabaseFields.USER_MOODS_COLLECTION)
					.get()
					.addOnSuccessListener(task -> {
						task.getDocuments().forEach(snapshots::add);
						listener.onQueryResult(snapshots, DatabaseResult.SUCCESS);
						Log.i(DB_TAG, "Obtained the posts for the user: " + query.getUsername());
					})
					.addOnFailureListener(e -> {
						Log.e(DB_TAG, "Task to obtain user's posts failed!", e);
						listener.onQueryResult(Collections.emptyList(), DatabaseResult.FAILURE);
					});
				break;
			case FOLLOWING_POSTS:
				this.getUserDoc(query.getUsername())
					.get()
					.addOnSuccessListener(ds -> {
						if (ds.contains(DatabaseFields.USER_FOLLOWING_FLD)) {
							List<String> follow
									= (List<String>) ds.get(DatabaseFields.USER_FOLLOWING_FLD);
							List<Task<QuerySnapshot>> followerTasks = new ArrayList<>();

							for (String name : follow) {
								Task<QuerySnapshot> ft
										= getUserDoc(name)
											.collection(DatabaseFields.USER_MOODS_COLLECTION)
											.get();
								followerTasks.add(ft);
           				   }

							// In order to ensure that we obtain all user's
							// posts at once, we use a set of tasks that
							// should be waited on due to the asynchronous
							// nature of Firebase's queries.
							Tasks.whenAllComplete(followerTasks)
								 .addOnSuccessListener(ftask -> {
									 for (Task t : ftask) {
										 QuerySnapshot q = (QuerySnapshot) t.getResult();
										 snapshots.addAll(q.getDocuments());
									 }

									 listener.onQueryResult(snapshots, DatabaseResult.SUCCESS);
								 }).addOnFailureListener(e -> {
									 Log.e(DB_TAG, "Failed to run follower tasks", e);
									 listener.onQueryResult(Collections.emptyList(),
															DatabaseResult.FAILURE);
								 });
						}
					})
					.addOnFailureListener(e -> {
						Log.e(DB_TAG, "Failed to fetch follower's posts", e);
						listener.onQueryResult(Collections.emptyList(), DatabaseResult.FAILURE);
					});
				break;
			case USER_NOTIFICATIONS:
				this.getUserDoc(query.getUsername())
					.collection(DatabaseFields.USER_NOTIF_COLLECTION)
					.get()
					.addOnSuccessListener(task -> {
						task.getDocuments().forEach(snapshots::add);
						listener.onQueryResult(snapshots, DatabaseResult.SUCCESS);
					})
					.addOnFailureListener(e -> {
						Log.e(DB_TAG, "Failed to fetch user's notifications", e);
						listener.onQueryResult(Collections.emptyList(), DatabaseResult.FAILURE);
					});
				break;
			case USERS:
				usersCollection
					.orderBy(DatabaseFields.USER_NAME_FLD)
					.startAt(query.getSearchTerm())
					.endAt(query.getSearchTerm() + "~")
					.get()
					.addOnSuccessListener(task -> {
						task.getDocuments().forEach(snapshots::add);
						listener.onQueryResult(snapshots, DatabaseResult.SUCCESS);
					})
					.addOnFailureListener(e -> {
						Log.e(DB_TAG, "Searching users failed", e);
						listener.onQueryResult(Collections.emptyList(), DatabaseResult.FAILURE);
					});
		}
	}

	/**
	 * <p>Requests information about the user given by {@code username}
	 * including the users the are following, the users who follow them,
	 * and the users that they have pending follow requests for. If the
	 * query is successful, then the user will be the first result in the
	 * {@code listener}'s results.</p>
	 *
	 * @param username the user to obtain the data from
	 * @param listener the callback to give the results
	 */
	public void requestUserInformation(String username, IQueryResult<UserProfile> listener) {
		DocumentReference userRef = this.getUserDoc(username);
		userRef.get()
			   .addOnSuccessListener(snapshot -> {
				   UserProfile userProfile = new UserProfile(username);
				   List<String> pending = new ArrayList<>();
				   List<String> following = new ArrayList<>();
				   List<String> folowers = new ArrayList<>();

				   if (snapshot.contains(DatabaseFields.USER_PENDING_FOLLOWS_FLD)) {
					   List<String> pendingNames = (List<String>)
							   snapshot.get(DatabaseFields.USER_PENDING_FOLLOWS_FLD);
					   userProfile.getPending().addAll(pendingNames);
				   }

				   if (snapshot.contains(DatabaseFields.USER_FOLLOWING_FLD)) {
					   List<String> followingNames = (List<String>)
							   snapshot.get(DatabaseFields.USER_FOLLOWING_FLD);
					   userProfile.getFollowing().addAll(followingNames);
				   }

				   if (snapshot.contains(DatabaseFields.USER_FOLLOWERS_FLD)) {
					   List<String> followersNames = (List<String>)
							   snapshot.get(DatabaseFields.USER_FOLLOWERS_FLD);
					   userProfile.getFollowers().addAll(followersNames);
				   }

				   listener.onResult(DatabaseResult.SUCCESS, Arrays.asList(userProfile));
				   Log.i(DB_TAG, "Successfully retrieved pending and following names");
			   })
			   .addOnFailureListener(error -> {
				   listener.onResult(DatabaseResult.FAILURE, Collections.emptyList());
				   Log.e(DB_TAG, "Failed to retreive pending and following names", error);
			   });
	}

	/**
	 * <p>Adds the {@code post} to the list of moods of the user given by
	 * {@code username}. The success or failure of adding the mood to
	 * the user is sent to {@code listener}.</p>
	 *
	 * @param username the name of the user to add the mood to
	 * @param post     the post to add to the user
	 * @param listener the listener to listen for the success or failure
	 *                 of the addition operation
	 */
	public void addMood(String username, MoodPost post, IResultListener listener) {
		DocumentReference userDoc = this.getUserDoc(username);
		Map<String, Object> postFields = this.getMoodFields(post);

		userDoc.collection(DatabaseFields.USER_MOODS_COLLECTION)
			   .add(postFields)
			   .addOnSuccessListener(ds -> {
				   listener.onResult(DatabaseResult.SUCCESS);
				   Log.i(DB_TAG, "User's new mood successfully added");
			   })
			   .addOnFailureListener(e -> {
				   listener.onResult(DatabaseResult.FAILURE);
				   Log.e(DB_TAG, "Failed to add mood to user!", e);
			   });
	}

	/**
	 * Updates the post that corresponds to the document of the given
	 * {@code post} for the user given by {@code username}. The success
	 * or failure of this editing will be returned to the {@code listener}.
	 *
	 * @param username the name of the user to find the mood to edit
	 * @param post     the post to edit along with its new details
	 * @param listener the listener to listen for the success or failure
	 *                 of the edit operation
	 */
	public void editMood(String username, MoodPost post, IResultListener listener) {
		DocumentReference userDoc = this.getUserDoc(username);
		Map<String, Object> postFields = this.getMoodFields(post, true);

		userDoc.collection(DatabaseFields.USER_MOODS_COLLECTION)
			   .document(post.getDocumentId())
			   .update(postFields)
			   .addOnSuccessListener(ds -> {
					   listener.onResult(DatabaseResult.SUCCESS);
					   Log.i(DB_TAG, "User's existing mood was successfully updated");
			   })
			   .addOnFailureListener(e -> {
				   listener.onResult(DatabaseResult.FAILURE);
				   Log.e(DB_TAG, "Failed to update user's mood!", e);
			   });
	}

	/**
	 * Remoevs the post that corresponds to the document of the given
	 * {@code post} for the user given by {@code username}. The success
	 * or failure of this editing will be returned to the {@code listener}.
	 *
	 * @param username the name of the user to find the mood to remove
	 * @param post     the post to remove from the user
	 * @param listener the listener to listen for the success or failure
	 *                 of the remove operation
	 */
	public void removeMood(String username, MoodPost post, IResultListener listener) {
		DocumentReference userDoc = this.getUserDoc(username);

		// Only incur the cost of getting comment documents if there
		// is actually comments.
		if (post.getNumTopLevelComments() > 0) {
			userDoc.collection(DatabaseFields.USER_MOODS_COLLECTION)
				   .document(post.getDocumentId())
				   .collection(DatabaseFields.MOOD_COMMENT_FLD)
				   .get()
				   .addOnSuccessListener(snapshot -> {
					   // Firestore provides no way to delete a collection, so we
					   // must delete every comment document first before deleting
					   // the mood's document, otherwise there will be a "hanging"
					   // collection.
					   List<DocumentSnapshot> commentSnapshots = snapshot.getDocuments();
					   List<Task> deletionTasks = new ArrayList<>();
					   snapshot.getDocuments().forEach(s -> {
						   deletionTasks.add(s.getReference().delete());
					   });

					   // Only when we successfully delete all comments can we
					   // delete the mood post.
					   Tasks.whenAllSuccess(deletionTasks)
							.addOnSuccessListener(l -> {
								userDoc.collection(DatabaseFields.USER_MOODS_COLLECTION)
									   .document(post.getDocumentId())
									   .delete()
									   .addOnSuccessListener(ds -> {
										   listener.onResult(DatabaseResult.SUCCESS);
										   Log.i(DB_TAG, "User's mood was successfully deleted");
									   })
									   .addOnFailureListener(e -> {
										   listener.onResult(DatabaseResult.FAILURE);
										   Log.e(DB_TAG, "Failed to delete user's mood!", e);
									   });
							})
							.addOnFailureListener(error -> {
								listener.onResult(DatabaseResult.FAILURE);
								Log.e(DB_TAG, "Failed to delete all mood comments!", error);
							});
				   });
		}
		else {
			userDoc.collection(DatabaseFields.USER_MOODS_COLLECTION)
				   .document(post.getDocumentId())
				   .delete()
				   .addOnSuccessListener(ds -> {
					   listener.onResult(DatabaseResult.SUCCESS);
					   Log.i(DB_TAG, "User's mood was successfully deleted");
				   })
				   .addOnFailureListener(e -> {
					   listener.onResult(DatabaseResult.FAILURE);
					   Log.e(DB_TAG, "Failed to delete user's mood!", e);
				   });
		}
	}

	/**
	 * Posts the given {@code comment} as a top-level comment to the given
	 * {@code post}. The success or failure of this editing will be returned
	 * to the {@code listener}.
	 *
	 * @param post	    the post to add the comment to
	 * @param comment   the comment to add
	 * @param listener  the listener to notify of the action's result
	 */
	public void postComment(MoodPost post, MoodComment comment, IResultListener listener) {
		DocumentReference postUser = this.getUserDoc(post.getUser());
		Map<String, Object> commentFields = new HashMap<>();
		commentFields.put(DatabaseFields.CMT_PARENT_POST_FLD, post.getDocumentId());
		// We store the number of subcomments in order to not have to query
		// all comments to count them.
		commentFields.put(DatabaseFields.CMT_NUM_SUB, comment.getNumSubReplies());
		MoodComment parentComment = comment.getParentComment();

		if (parentComment != null) {
			commentFields.put(DatabaseFields.CMT_PARENT_CMT_FLD, parentComment.getDocumentId());
		}
		else {
			commentFields.put(DatabaseFields.CMT_PARENT_CMT_FLD, DatabaseFields.CMT_TOP_LEVEL);
		}

		commentFields.put(DatabaseFields.CMT_TIME_FLD, comment.getTimeCommented());
		commentFields.put(DatabaseFields.CMT_TEXT_FLD, comment.getCommentText());
		commentFields.put(DatabaseFields.CMT_POSTER_FLD, comment.getPosterName());

		if (comment.getParentComment() == null) {
			postUser.collection(DatabaseFields.USER_MOODS_COLLECTION)
					.document(post.getDocumentId())
					.update(DatabaseFields.MOOD_COMMENT_COUNT, FieldValue.increment(1));
		}
		else {
			postUser.collection(DatabaseFields.USER_MOODS_COLLECTION)
					.document(post.getDocumentId())
					.collection(DatabaseFields.MOOD_COMMENT_FLD)
					.document(comment.getParentComment().getDocumentId())
					.update(DatabaseFields.CMT_NUM_SUB, FieldValue.increment(1));
		}

		postUser.collection(DatabaseFields.USER_MOODS_COLLECTION)
				.document(post.getDocumentId())
				.collection(DatabaseFields.MOOD_COMMENT_FLD)
				.add(commentFields)
				.addOnSuccessListener(document -> {
					listener.onResult(DatabaseResult.SUCCESS);
				})
				.addOnFailureListener(error -> {
					listener.onResult(DatabaseResult.FAILURE);
				});
	}

	/***
	 * Requests the comments for the given {@code post} and returns the
	 * obtained comments to the given {@code listener}.
	 *
	 * @see IQueryResult#onResult(DatabaseResult, List)
	 *
	 * @param post		the post to request the comments from
	 * @param listener  the callback to return the results to
	 */
	public void requestPostComments(MoodPost post, IQueryResult<MoodComment> listener) {
		DocumentReference postUser = this.getUserDoc(post.getUser());
		postUser.collection(DatabaseFields.USER_MOODS_COLLECTION)
				.document(post.getDocumentId())
				.collection(DatabaseFields.MOOD_COMMENT_FLD)
				.whereEqualTo(DatabaseFields.CMT_PARENT_CMT_FLD, DatabaseFields.CMT_TOP_LEVEL)
				.get()
				.addOnSuccessListener(snapshot -> {
					List<MoodComment> comments = new ArrayList<>();

					for (DocumentSnapshot cs : snapshot.getDocuments()) {
						String commentText = cs.getString(DatabaseFields.CMT_TEXT_FLD);
						String posterName = cs.getString(DatabaseFields.CMT_POSTER_FLD);
						MoodComment comment = new MoodComment(post, cs.getId(), posterName, commentText);
						comment.setTimeCommented(cs.getLong(DatabaseFields.CMT_TIME_FLD));
						long numReplies = cs.getLong(DatabaseFields.CMT_NUM_SUB);
						comment.setNumSubReplies((int) numReplies);
						comments.add(comment);
					}

					listener.onResult(DatabaseResult.SUCCESS, comments);
				})
				.addOnFailureListener(error -> {
					listener.onResult(DatabaseResult.FAILURE, Collections.emptyList());
				});
	}

	/***
	 * Requests the subcomments to the given {@code parentComment} if there
	 * are any. The results of the request will be returned in the given
	 * {@code listener}.
	 *
	 * @see IQueryResult#onResult(DatabaseResult, List)
	 *
	 * @param parentComment the comment to retrieve the replies to
	 * @param listener 		the callback to return the results of the request
	 */
	public void requestCommentReplies(MoodComment parentComment, IQueryResult<MoodComment> listener) {
		DocumentReference postUser = this.getUserDoc(parentComment.getParentPost().getUser());
		postUser.collection(DatabaseFields.USER_MOODS_COLLECTION)
				.document(parentComment.getParentPost().getDocumentId())
				.collection(DatabaseFields.MOOD_COMMENT_FLD)
				.whereEqualTo(DatabaseFields.CMT_PARENT_CMT_FLD, parentComment.getDocumentId())
				.get()
				.addOnSuccessListener(snapshot -> {
					List<MoodComment> comments = new ArrayList<>();

					for (DocumentSnapshot cs : snapshot.getDocuments()) {
						String commentText = cs.getString(DatabaseFields.CMT_TEXT_FLD);
						String posterName = cs.getString(DatabaseFields.CMT_POSTER_FLD);
						MoodComment comment = new MoodComment(parentComment, cs.getId(), posterName, commentText);
						comment.setTimeCommented(cs.getLong(DatabaseFields.CMT_TIME_FLD));
						long numReplies = cs.getLong(DatabaseFields.CMT_NUM_SUB);
						comment.setNumSubReplies((int) numReplies);
						comments.add(comment);
					}

					listener.onResult(DatabaseResult.SUCCESS, comments);
				})
				.addOnFailureListener(error -> {
					listener.onResult(DatabaseResult.FAILURE, Collections.emptyList());
				});
	}

	/**
	 * Sends a following request notification to the user with the given
	 * {@code destUser} username.
	 *
	 * @param sourceUser the name of the user sending the request
	 * @param destUser   the name of the user to receive the request
	 */
	public void sendFollowRequest(String sourceUser, String destUser) {
		DocumentReference sourceRef = this.getUserDoc(sourceUser);
		DocumentReference destRef = this.getUserDoc(destUser);
		Map<String, Object> notificationValue = new HashMap<>();
		notificationValue.put(DatabaseFields.NOTIF_SENDER_FLD, sourceUser);
		notificationValue.put(DatabaseFields.NOTIF_RECEIVER_FLD, destUser);

		sourceRef.update(DatabaseFields.USER_PENDING_FOLLOWS_FLD, FieldValue.arrayUnion(destUser));
		destRef.collection(DatabaseFields.USER_NOTIF_COLLECTION)
			   .add(notificationValue);
	}

	/**
	 * <p>Accepts the follow request represented by {@code notification} by
	 * adding the sender to the receiver's list of followers and the receiver
	 * to the sender's following list. Also removes the {@code notification}
	 * from the receiver's notification list and removes the receiver from
	 * the sender's pending requests.</p>
	 *
	 * <p>If all of these actions are successful, then {@code SUCCESS} will
	 * be sent to the {@code listener}, if any fail, then a {@code FAILURE}
	 * will be sent instead.</p>
	 *
	 * @param notification the notification to handle the request from
	 * @param listener     the listener to notifiy
	 */
	public void acceptFollowRequest(Notification notification, IResultListener listener) {
		String sender = notification.getSourceUsername();
		String receiver = notification.getDestinationUsername();
		DocumentReference senderRef = this.getUserDoc(sender);
		DocumentReference receiverRef = this.getUserDoc(receiver);

		Task addToSenderFollowing = senderRef.update(DatabaseFields.USER_FOLLOWING_FLD,
													 FieldValue.arrayUnion(receiver));
		Task addFollowerToReceiver = receiverRef.update(DatabaseFields.USER_FOLLOWERS_FLD,
														FieldValue.arrayUnion(sender));
		Task removeFromSenderPending = senderRef.update(DatabaseFields.USER_PENDING_FOLLOWS_FLD,
														FieldValue.arrayRemove(receiver));
		Task removeNotification = receiverRef.collection(DatabaseFields.USER_NOTIF_COLLECTION)
											 .document(notification.getDocumentId())
											 .delete();

		Tasks.whenAllComplete(addToSenderFollowing, addFollowerToReceiver,
							  removeFromSenderPending, removeNotification)
			 .addOnSuccessListener(ftask -> {
				 listener.onResult(DatabaseResult.SUCCESS);
				 Log.i(DB_TAG, "Successfully accepted following request");
			 })
			 .addOnFailureListener(e -> {
				 listener.onResult(DatabaseResult.FAILURE);
				 Log.e(DB_TAG, "Failed to accept following request", e);
			 });
	}

	/**
	 * <p>Rejects the follow request represented by {@code notification}
	 * removing the {@code notification} from the receiver's notification
	 * list and removing the receiver from the sender's pending requests.</p>
	 *
	 * <p>If all of these actions are successful, then {@code SUCCESS} will
	 * be sent to the {@code listener}, if any fail, then a {@code FAILURE}
	 * will be sent instead.</p>
	 *
	 * @param notification the notification to handle the request from
	 * @param listener     the listener to notifiy
	 */
	public void rejectFollowingRequest(Notification notification, IResultListener listener) {
		String sender = notification.getSourceUsername();
		String receiver = notification.getDestinationUsername();
		DocumentReference senderRef = this.getUserDoc(sender);
		DocumentReference receiverRef = this.getUserDoc(receiver);

		Task removeFromSenderPending = senderRef.update(DatabaseFields.USER_PENDING_FOLLOWS_FLD,
														FieldValue.arrayRemove(receiver));
		Task removeNotification = receiverRef.collection(DatabaseFields.USER_NOTIF_COLLECTION)
											 .document(notification.getDocumentId())
											 .delete();

		Tasks.whenAllComplete(removeFromSenderPending, removeNotification)
			 .addOnSuccessListener(ftask -> {
				 listener.onResult(DatabaseResult.SUCCESS);
				 Log.i(DB_TAG, "Successfully rejected following request");
			 })
			 .addOnFailureListener(e -> {
				 listener.onResult(DatabaseResult.FAILURE);
				 Log.e(DB_TAG, "Failed to reject following request", e);
			 });
	}

	/**
	 * Attempts to remove {@code userToRemove} as a follower from the user
	 * given by {@code username}. The success or failure of this task will
	 * be given to {@code listener}.
	 *
	 * @param username     the user whose following list is to be updated
	 * @param userToRemove the user to remove as a follower
	 * @param listener     the listener for the result of the action
	 */
	public void removeFollower(String username, String userToRemove, IResultListener listener) {
		DocumentReference userDoc = this.getUserDoc(username);
		DocumentReference otherUserDoc = this.getUserDoc(userToRemove);
		Task removeFollowingTask = otherUserDoc.update(DatabaseFields.USER_FOLLOWING_FLD,
													  FieldValue.arrayRemove(username));
		Task removeFollowerTask = userDoc.update(DatabaseFields.USER_FOLLOWERS_FLD,
									   				FieldValue.arrayRemove(userToRemove));
		Tasks.whenAllComplete(removeFollowerTask, removeFollowingTask)
			 .addOnSuccessListener(task -> {
				 listener.onResult(DatabaseResult.SUCCESS);
				 String logMsg = "Successfully removed %s from %s's following";
				 Log.i(DB_TAG, String.format(logMsg, userToRemove, username));
			 })
			 .addOnFailureListener(error -> {
				 listener.onResult(DatabaseResult.FAILURE);
				 String logMsg = "Failed to remove %s from %s's following";
				 Log.e(DB_TAG, String.format(logMsg, userToRemove, username), error);
			 });
	}

	/**
	 * Returns the {@code DocumentReference} associated with the user
	 * given by {@code username}.
	 *
	 * @param username the name of the user to get the document from
	 *
	 * @return a {@code DocumentReference} for the given {@code username}
	 */
    public DocumentReference getUserDoc(String username) {
		CollectionReference usersCollection
				= this.db.collection(DatabaseFields.USER_COLLECTION);
		DocumentReference userDoc
				= usersCollection.document(this.getUserDocumentName(username));
		return userDoc;
	}

	/**
	 * Returns the name of the document for the given user.
	 *
	 * @param username the name of the user to retreive the document name
	 *
	 * @return the name of the document for the user
	 */
	private String getUserDocumentName(String username) {
		return "user_" + username;
	}

	/**
	 * Returns a mapping representation of the given {@code post} based
	 * on the fields that are available in the post. Certain fields must
	 * be handled specially when updating otherwise they will not be
	 * updated correctly thus {@code isUpdate} should be {@code true}
	 * when updating a post.
	 *
	 * @param post		the post to convert into a map object
	 * @param isUpdate  whether the obtaining of fields is done by an update
	 *
	 * @return a map representation of the fields in the {@code post}
	 */
	private Map<String, Object> getMoodFields(MoodPost post, boolean isUpdate) {
		Map<String, Object> postFields = new HashMap<>();
		postFields.put(DatabaseFields.USER_NAME_FLD, post.getUser());
		postFields.put(DatabaseFields.MOOD_TIME_FLD, post.getTimePosted());
		postFields.put(DatabaseFields.MOOD_EMOTION_FLD, post.getEmotion().name());
		postFields.put(DatabaseFields.MOOD_VIEW_STATUS_FLD, post.isPrivate());
		postFields.put(DatabaseFields.MOOD_COMMENT_COUNT, post.getNumTopLevelComments());

		if (post.getReason() != null && !post.getReason().isEmpty()) {
			postFields.put(DatabaseFields.MOOD_REASON_FLD, post.getReason());
		}
		else if (isUpdate) {
			postFields.put(DatabaseFields.MOOD_REASON_FLD, FieldValue.delete());
		}

		if (post.getSocialSituation() != null) {
			postFields.put(DatabaseFields.MOOD_SITUATION_FLD, post.getSocialSituation().name());
		}

		if (post.getLatitude() != MoodPost.INVALID_COORDINATE) {
			if (post.getLongitude() != MoodPost.INVALID_COORDINATE) {
				double lat = post.getLatitude();
				double lon = post.getLongitude();
				List<Double> coordinates = Arrays.asList(lat, lon);
				postFields.put(DatabaseFields.MOOD_LOCATION_FLD, coordinates);
			}
		}
		else if (isUpdate) {
			postFields.put(DatabaseFields.MOOD_LOCATION_FLD, FieldValue.delete());
		}

		if (post.getImageData() != null && !post.getImageData().isEmpty()) {
			postFields.put(DatabaseFields.MOOD_IMG_FLD, post.getImageData());
		}
		else if (isUpdate) {
			postFields.put(DatabaseFields.MOOD_IMG_FLD, FieldValue.delete());
		}

		if (post.hasBeenEdited()) {
			postFields.put(DatabaseFields.MOOD_EDITED_FLD, post.hasBeenEdited());
		}

		return postFields;
	}

	/**
	 * Returns a mapping representation of the given {@code post} based
	 * on the fields that are available in the post.
	 *
	 * @param post the post to convert into a map object
	 *
	 * @return a map representation of the fields in the {@code post}
	 */
	private Map<String, Object> getMoodFields(MoodPost post) {
		return this.getMoodFields(post, false);
	}

	/***
	 * Returns a {@code MoodPost} using the fields in the given
	 * {@code snapshot}.
	 *
	 * @param snapshot the snapshot to parse the {@code MoodPost} from
	 *
	 * @return a {@code MoodPost} based on the {@code snapshot}
	 */
	public MoodPost parseMood(DocumentSnapshot snapshot) {
		String documentId = snapshot.getId();
		String user = snapshot.getString(DatabaseFields.USER_NAME_FLD);
		String emotionStr = snapshot.getString(DatabaseFields.MOOD_EMOTION_FLD);
		MoodPost.Emotion emotion = MoodPost.Emotion.valueOf(emotionStr);
		MoodPost post = new MoodPost(documentId, user, emotion);

		if (snapshot.contains(DatabaseFields.MOOD_REASON_FLD)) {
			post.setReason(snapshot.getString(DatabaseFields.MOOD_REASON_FLD));
		}

		if (snapshot.contains(DatabaseFields.MOOD_SITUATION_FLD)) {
			String sitStr = snapshot.getString(DatabaseFields.MOOD_SITUATION_FLD);
			MoodPost.SocialSituation situation = MoodPost.SocialSituation.valueOf(sitStr);
			post.setSocialSituation(situation);
		}

		long timePosted = snapshot.getLong(DatabaseFields.MOOD_TIME_FLD);

		if (snapshot.contains(DatabaseFields.MOOD_EDITED_FLD)) {
			post.setEdited(true);
		}

		post.setTimePosted(timePosted);

		if (snapshot.contains(DatabaseFields.MOOD_LOCATION_FLD)) {
			List<Double> coordinates = (List<Double>)
					snapshot.get(DatabaseFields.MOOD_LOCATION_FLD);

			if (coordinates != null && coordinates.size() >= 2) {
				double latitude = coordinates.get(0);
				double longitude = coordinates.get(1);
				post.setLocation(latitude, longitude);
			}
		}

		if (snapshot.contains(DatabaseFields.MOOD_IMG_FLD)) {
			post.setImageData(snapshot.getString(DatabaseFields.MOOD_IMG_FLD));
		}

		if (snapshot.contains(DatabaseFields.MOOD_COMMENT_COUNT)) {
			long numComments = snapshot.getLong(DatabaseFields.MOOD_COMMENT_COUNT);
			post.setNumTopLevelComments((int) numComments);
		}

		return post;
	}

	/**
	 * Returns a Singleton instance of this database. If the database does
	 * not already exist, then this will also initialize it.
	 *
	 * @return the Singleton instance of this database.
	 */
	public static Database getInstance() {
		if (theDatabase == null) {
			theDatabase = new Database();
		}

		return theDatabase;
	}
}