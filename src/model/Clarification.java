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
 *@author CSE360-Team11 Fall 2025
 *
 * @see databasePart1.DatabaseHelper
 * @see model.ClarificationsManager
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
	//GETTERS
	public int getId() {
		return id;
	}
	public ContentType getItemType() {
	    return itemType;
	}
	public int getContentId() {
	    return contentId;
	}
	public int getQuestionId() {
		return questionId;
	}
	public int getAnswerId() {
		return answerId;
	}
	public int getAuthorId() {
		return authorId;
	}
	public int getRecipientId() {
		return recipientId;
	}
	public String getAuthor() {
		return author;
	}
	public String getContent() {
		return content;
	}
	public LocalDateTime getTimestamp() {
		return timestamp;
	}
	public boolean isRead() { 
		return isRead; 
	}
	public boolean isPublic() { 
		return isPublic; 
	}
	
	//SETTERS
	public void setId(int id) {
		this.id = id;
	}
	public void setItemType(ContentType itemType) {
	    this.itemType = itemType;
	}
	public void setContentId(int contentId) {
	    this.contentId = contentId;
	}
	public void setQuestionId(int questionId) {
		this.questionId = questionId;
	}
	public void setAnswerId(int answerId) {
		this.answerId = answerId;
	}
	public void setAuthorId(int authorId) {
		this.authorId = authorId;
	}
	public void setRecipientId(int recipientId) {
		this.recipientId = recipientId;
	}
	public void setAuthor(String author) {
		this.author= author;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public void setTimestamp(String timestamp) {
		this.timestamp = LocalDateTime.now();
	}
	public void setIsRead(boolean read) { 
		isRead = read; 
	}
	public void setIsPublic(boolean isPublic) { 
		this.isPublic = isPublic; 
	}
	
	//extend
	private String questionTitle;
	
	public String getQuestionTitle() {
		return questionTitle;
	}
	public void setQuestionTitle(String questionTitle) {
		this.questionTitle = questionTitle;
	}
	
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

