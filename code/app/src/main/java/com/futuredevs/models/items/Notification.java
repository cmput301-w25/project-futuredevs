package com.futuredevs.models.items;

/**
 * The {@code Notification} class represents a data-holding class intended to
 * contain the information about a single notification the active user has
 * received.
 *
 * @author Spencer Schmidt
 */
public class Notification {
	private final String documentId;
	private final String sourceUsername;
	private final String destinationUsername;

	/**
	 * Creates an {@code Notification} instance where {@code documentId}
	 * is the Firebase document id associated with this notification and
	 * {@code sourceName} is the name of the user that sent the notification
	 * to the active user.
	 *
	 * @param documentId the Firebase document id for this notification
	 * @param sourceName the name of the user who sent this notification
	 * @param destName   the name of the user receiving the notification
	 */
	public Notification(String documentId, String sourceName, String destName) {
		this.documentId = documentId;
		this.sourceUsername = sourceName;
		this.destinationUsername = destName;
	}

	/**
	 * Returns the Firebase document id associated with this notification. Used
	 * for deleting this notification when the user interacts with it.
	 *
	 * @return the Firebase document id for this notification
	 */
	public String getDocumentId() {
		return this.documentId;
	}

	/**
	 * Returns the name of the user that sent this notification.
	 *
	 * @return the username of the sending user
	 */
	public String getSourceUsername() {
		return this.sourceUsername;
	}

	/**
	 * Returns the name of the user receiving this notification
	 *
	 * @return the username of the receiving user
	 */
	public String getDestinationUsername() {
		return this.destinationUsername;
	}
}