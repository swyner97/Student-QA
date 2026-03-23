/**
 * The {@code Clarification} class represents a clarification message in a Q&A system.
 * A clarification is typically a request or suggestion for further information 
 * related to a specific question or answer.
 * 
 * <p>Each clarification is associated with:
 * <ul>
 *   <li>A unique ID</li>
 *   <li>A question ID (the question being clarified)</li>
 *   <li>An optional answer ID (if the clarification is in response to an answer)</li>
 *   <li>The ID and username of the author who submitted the clarification</li>
 *   <li>The recipient user ID (the intended recipient of the clarification)</li>
 *   <li>The content of the clarification</li>
 *   <li>A timestamp of when the clarification was created</li>
 *   <li>A flag indicating whether the clarification has been read</li>
 * </ul>
 * 
 * @author CSE360-Team11 Fall 2025
 *
 * @see databasePart1.DatabaseHelper
 * @see model.Question
 * @see model.Answer
 * 
 */

package model;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import logic.*;

import databasePart1.*;

public class Clarification {
	private int id, questionId, answerId, authorId, recipientId;
	private String author, content;
	private LocalDateTime timestamp;
	private boolean isRead;
	private boolean isPublic;
	private ContentType itemType;
	private int contentId;

	/**
	 * Constructs a {@code Clarification} with a content type and content ID,
	 * setting the timestamp to the current date and time.
	 *
	 * @param id          the unique identifier for this clarification
	 * @param itemType    the type of content this clarification targets (e.g., QUESTION or ANSWER)
	 * @param contentId   the ID of the specific content item being clarified
	 * @param questionId  the ID of the associated question
	 * @param answerId    the ID of the associated answer, or 0 if not applicable
	 * @param authorId    the ID of the user who authored this clarification
	 * @param recipientId the ID of the user intended to receive this clarification
	 * @param author      the display name of the author
	 * @param content     the text body of the clarification
	 * @param timestamp   ignored; the current time is used instead
	 * @param isRead      whether this clarification has been read by the recipient
	 * @param isPublic    whether this clarification is visible to all users
	 */
	public Clarification(int id, ContentType itemType, int contentId, int questionId, int answerId, int authorId, int recipientId, String author, String content, LocalDateTime timestamp, Boolean isRead, Boolean isPublic) {
		this.id = id;
		this.itemType = itemType;
	    this.contentId = contentId;
		this.questionId = questionId;
		this.answerId = answerId;
		this.authorId = authorId;
		this.recipientId = recipientId;
		this.author = author;
		this.content = content;
		this.timestamp = LocalDateTime.now();
		this.isRead = isRead;
		this.isPublic = isPublic;
	}

	/**
	 * Constructs a {@code Clarification} using the provided timestamp exactly as given,
	 * typically used when rehydrating records from the database.
	 *
	 * @param id          the unique identifier for this clarification
	 * @param questionId  the ID of the associated question
	 * @param answerId    the ID of the associated answer, or 0 if not applicable
	 * @param authorId    the ID of the user who authored this clarification
	 * @param recipientId the ID of the user intended to receive this clarification
	 * @param author      the display name of the author
	 * @param content     the text body of the clarification
	 * @param timestamp   the exact timestamp to assign to this clarification
	 * @param isRead      whether this clarification has been read by the recipient
	 * @param isPublic    whether this clarification is visible to all users
	 */
	public Clarification(int id, int questionId, int answerId, int authorId, int recipientId, String author, String content, LocalDateTime timestamp, boolean isRead, boolean isPublic) {
	    this.id = id;
	    this.questionId = questionId;
	    this.answerId = answerId;
	    this.authorId = authorId;
	    this.recipientId = recipientId;
	    this.author = author;
	    this.content = content;
	    this.timestamp = timestamp; // don't use LocalDateTime.now() here
	    this.isRead = isRead;
	    this.isPublic = isPublic;
	}

	/**
	 * Constructs a {@code Clarification} without an answer ID,
	 * setting the timestamp to the current date and time.
	 *
	 * @param id          the unique identifier for this clarification
	 * @param questionId  the ID of the associated question
	 * @param authorId    the ID of the user who authored this clarification
	 * @param recipientId the ID of the user intended to receive this clarification
	 * @param author      the display name of the author
	 * @param content     the text body of the clarification
	 * @param timestamp   ignored; the current time is used instead
	 * @param isRead      whether this clarification has been read by the recipient
	 * @param isPublic    whether this clarification is visible to all users
	 */
	public Clarification(int id, int questionId, int authorId, int recipientId, String author, String content, LocalDateTime timestamp, Boolean isRead, Boolean isPublic) {
		this.id = id;
		this.questionId = questionId;
		this.authorId = authorId;
		this.recipientId = recipientId;
		this.author = author;
		this.content = content;
		this.timestamp = LocalDateTime.now();
		this.isRead = isRead;
		this.isPublic = isPublic;
	}

	// GETTERS

	/**
	 * Returns the unique ID of this clarification.
	 *
	 * @return the clarification ID
	 */
	public int getId() {
		return id;
	}

	/**
	 * Returns the content type this clarification targets.
	 *
	 * @return the {@link ContentType} (e.g., QUESTION or ANSWER)
	 */
	public ContentType getItemType() {
	    return itemType;
	}

	/**
	 * Returns the ID of the specific content item being clarified.
	 *
	 * @return the content item ID
	 */
	public int getContentId() {
	    return contentId;
	}

	/**
	 * Returns the ID of the question associated with this clarification.
	 *
	 * @return the question ID
	 */
	public int getQuestionId() {
		return questionId;
	}

	/**
	 * Returns the ID of the answer associated with this clarification,
	 * or 0 if the clarification targets a question directly.
	 *
	 * @return the answer ID
	 */
	public int getAnswerId() {
		return answerId;
	}

	/**
	 * Returns the ID of the user who authored this clarification.
	 *
	 * @return the author's user ID
	 */
	public int getAuthorId() {
		return authorId;
	}

	/**
	 * Returns the ID of the user intended to receive this clarification.
	 *
	 * @return the recipient's user ID
	 */
	public int getRecipientId() {
		return recipientId;
	}

	/**
	 * Returns the display name of the author of this clarification.
	 *
	 * @return the author's name
	 */
	public String getAuthor() {
		return author;
	}

	/**
	 * Returns the text body of this clarification.
	 *
	 * @return the clarification content
	 */
	public String getContent() {
		return content;
	}

	/**
	 * Returns the timestamp of when this clarification was created.
	 *
	 * @return the creation timestamp as a {@link LocalDateTime}
	 */
	public LocalDateTime getTimestamp() {
		return timestamp;
	}

	/**
	 * Returns whether this clarification has been read by the recipient.
	 *
	 * @return {@code true} if the clarification has been read; {@code false} otherwise
	 */
	public boolean isRead() { 
		return isRead; 
	}

	/**
	 * Returns whether this clarification is publicly visible to all users.
	 *
	 * @return {@code true} if the clarification is public; {@code false} if private
	 */
	public boolean isPublic() { 
		return isPublic; 
	}

	// SETTERS

	/**
	 * Sets the unique ID of this clarification.
	 *
	 * @param id the new clarification ID
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * Sets the content type this clarification targets.
	 *
	 * @param itemType the {@link ContentType} to assign
	 */
	public void setItemType(ContentType itemType) {
	    this.itemType = itemType;
	}

	/**
	 * Sets the ID of the specific content item being clarified.
	 *
	 * @param contentId the content item ID to assign
	 */
	public void setContentId(int contentId) {
	    this.contentId = contentId;
	}

	/**
	 * Sets the ID of the question associated with this clarification.
	 *
	 * @param questionId the question ID to assign
	 */
	public void setQuestionId(int questionId) {
		this.questionId = questionId;
	}

	/**
	 * Sets the ID of the answer associated with this clarification.
	 *
	 * @param answerId the answer ID to assign
	 */
	public void setAnswerId(int answerId) {
		this.answerId = answerId;
	}

	/**
	 * Sets the ID of the user who authored this clarification.
	 *
	 * @param authorId the author's user ID to assign
	 */
	public void setAuthorId(int authorId) {
		this.authorId = authorId;
	}

	/**
	 * Sets the ID of the user intended to receive this clarification.
	 *
	 * @param recipientId the recipient's user ID to assign
	 */
	public void setRecipientId(int recipientId) {
		this.recipientId = recipientId;
	}

	/**
	 * Sets the display name of the author of this clarification.
	 *
	 * @param author the author name to assign
	 */
	public void setAuthor(String author) {
		this.author= author;
	}

	/**
	 * Sets the text body of this clarification.
	 *
	 * @param content the clarification content to assign
	 */
	public void setContent(String content) {
		this.content = content;
	}

	/**
	 * Sets the timestamp of this clarification to the current date and time.
	 * The provided string parameter is not used.
	 *
	 * @param timestamp unused; the current time is always applied
	 */
	public void setTimestamp(String timestamp) {
		this.timestamp = LocalDateTime.now();
	}

	/**
	 * Sets whether this clarification has been read by the recipient.
	 *
	 * @param read {@code true} to mark as read; {@code false} to mark as unread
	 */
	public void setIsRead(boolean read) { 
		isRead = read; 
	}

	/**
	 * Sets whether this clarification is publicly visible to all users.
	 *
	 * @param isPublic {@code true} to make public; {@code false} to make private
	 */
	public void setIsPublic(boolean isPublic) { 
		this.isPublic = isPublic; 
	}

	// Extended fields

	private String questionTitle;

	/**
	 * Returns the title of the question associated with this clarification.
	 * This is an extended field not set by the constructor.
	 *
	 * @return the question title, or {@code null} if not set
	 */
	public String getQuestionTitle() {
		return questionTitle;
	}

	/**
	 * Sets the title of the question associated with this clarification.
	 *
	 * @param questionTitle the question title to assign
	 */
	public void setQuestionTitle(String questionTitle) {
		this.questionTitle = questionTitle;
	}

	/**
	 * Retrieves the display title of the content item this clarification targets
	 * by querying the database. For questions, returns the question title directly.
	 * For answers, returns a string in the format {@code "Answer to: <question title>"}.
	 *
	 * @param db the {@link DatabaseHelper} used to look up content titles
	 * @return the display title of the targeted content, or an error string if lookup fails
	 */
	public String getContentTitle(DatabaseHelper db) {
		try {
			return switch (this.getItemType()) {
            case QUESTION -> db.getQuestionById(this.getContentId()).getTitle();
            case ANSWER -> {
                Answer a = db.getAnswerById(this.getContentId());
                Question q = db.getQuestionById(a.getQuestionId());
                yield "Answer to: " + q.getTitle();
            }
            default -> "[Unsupported type]";
        };
    } catch (SQLException e) {
        e.printStackTrace();
        return "Error fetching title";
	    }
	}
	
}