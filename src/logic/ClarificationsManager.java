package logic;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.sql.SQLException;
import java.util.*;
import databasePart1.DatabaseHelper;
import model.Clarification;
import model.*;

public class ClarificationsManager {
	private final Map<Integer, Clarification> clarifications;
	private int nextId;
	private final DatabaseHelper db;
	
	public ClarificationsManager(DatabaseHelper db) {
		this.db = db;
		this.clarifications = new HashMap<>();
		this.nextId = 1;
		loadAllClarifications();
	}
	
	private int determineRecipientId(Question question, Answer answer) {
	    if (answer != null && answer.getUserId() > 0) {
	        return answer.getUserId(); 
	    } else if (question != null && question.getUserId() > 0) {
	        return question.getUserId();  
	    }
	    return -1; 
	}
	
	private void loadAllClarifications() {
		try {
			System.out.println("Clarifications will be loaded on demand.");
		}
		catch (Exception e) {
			System.err.println("Error loading clarifications: " + e.getMessage());
		}
	}
	
	//public Result create(int questionId, int authorId, int recipientId, String author, String content) throws SQLException {
	public Result create(ContentType type, int contentId, int authorId, int recipientId, String author, String content) throws SQLException {
		if (content == null || content.isBlank()) {
			return new Result(false, "Clarification content cannot be empty.", null);
		}
		
		int questionId = 0;
	    int answerId = 0;
	    String questionTitle = "";
	    
	    if (type == ContentType.QUESTION) {
	        Question question = db.getQuestionById(contentId);
	        if (question == null) return new Result(false, "Question not found", null);
	        questionId = question.getQuestionId();
	        recipientId = question.getUserId();
	        questionTitle = question.getTitle();
	    } else if (type == ContentType.ANSWER) {
	        Answer answer = db.getAnswerById(contentId);
	        if (answer == null) return new Result(false, "Answer not found", null);
	        answerId = answer.getAnswerId();
	        recipientId = answer.getUserId();
	        questionId = answer.getQuestionId(); // Still link to the question for context
	        Question question = db.getQuestionById(questionId);
	        questionTitle = (question != null) ? question.getTitle() : "";
	    } else {
	        return new Result(false, "Unsupported content type for clarification.", null);
	    }

	    LocalDateTime timestamp = LocalDateTime.now();
	    Clarification clarification = new Clarification(
	        nextId,
	        questionId,
	        answerId,
	        authorId,
	        recipientId,
	        author,
	        content,
	        timestamp,
	        false,
	        false
	    );

	    try {
	        db.insertClarification(clarification);
	        clarifications.put(nextId, clarification);
	        nextId++;
	        return new Result(true, "Clarification added successfully.", clarification);
	    } catch (SQLException e) {
	        e.printStackTrace();
	        return new Result(false, "Database error while saving clarification: " + e.getMessage(), null);
	    }
	}
		
//		
//		Question question = db.getQuestionById(questionId);
//		//Answer answer = (answerId > 0) ? db.getAnswerById(answerId) : null;
//
//		recipientId = question.getUserId();
//		String questionTitle = question.getTitle();
//				
//		LocalDateTime timestamp = LocalDateTime.now();
//		Boolean isRead = false;
//		Clarification clarification = new Clarification(nextId, questionId, authorId, recipientId, author, content, timestamp, false, false);
//		
//		try {
//			db.insertClarification(clarification);
//			clarifications.put(nextId, clarification);
//			nextId++;
//			return new Result(true, "Clarification added successfully.", clarification);
//		}
//		catch (SQLException e) {
//			e.printStackTrace();
//			return new Result(false, "Database error while saving clarification: " + e.getMessage(), null);
//		}

		
		List<Clarification> readByQuestion(int questionId) {
			try {
				return db.loadClarificationsforQ(questionId);
			}
			catch (SQLException e) {
				e.printStackTrace();
				return new ArrayList<>();
			}
		}
		
		public List<Clarification> readByAnswer(int answerId) {
			try {
				return db.loadClarificationsforA(answerId);
			}
			catch (SQLException e) {
				e.printStackTrace();
				return new ArrayList<>();
			}
				
		}
		
		public String questionTitleForSuggestion(Clarification c) {
			
			int qId = c.getQuestionId();
			Question q;
			try {
				q = db.getQuestionById(qId);
				return q.getTitle();
			} catch (SQLException e) {
				e.printStackTrace();
				return "";
			}
		}
		
	}

