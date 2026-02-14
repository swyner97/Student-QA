package model;

import java.util.stream.Collectors;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import databasePart1.*;
import logic.Result;
import logic.StatusData;

/**
 * The Answers class handles all logic related to managing answers,
 * including creation, editing, deletion, and searching.
 * 
 * Permissions:
 *  - Students can edit/delete their own answers.
 *  - Admins, instructors, reviewers, and staff can edit/delete any answer.
 */
public class Answers {

    private Map<Integer, Answer> answers;
    private int nextId;
    private DatabaseHelper db;

    public Answers(DatabaseHelper db) {
        this.answers = new HashMap<>();
        this.nextId = 1;
        this.db = db;
        loadAnswersFromDatabase();
    }

    private void loadAnswersFromDatabase() {
        List<Answer> dbAnswers = db.loadAllAnswers();
        for (Answer ans : dbAnswers) {
            answers.put(ans.getAnswerId(), ans);
            if (ans.getAnswerId() >= nextId) {
                nextId = ans.getAnswerId() + 1;
            }
        }
        System.out.println("Loaded " + answers.size() + " answers from database.");
    }

    public Result create(int userId, int questionId, String author, String content) {
        if (author == null || author.isBlank()) {
            return new Result(false, "Author is required.", null);
        }
        if (content == null || content.isBlank()) {
            return new Result(false, "Content is required.", null);
        }

        try {	
        	 int newId = db.insertAnswer(userId, questionId, author, content);
        	 Answer answer = new Answer(newId, userId, questionId, author, content);
        	
        	 answers.put(newId, answer);
        	 
        	 return new Result(true, "Answer created successfully.", answer);
        } catch (SQLException e) {
            e.printStackTrace();
            return new Result(false, "Failed to save to database: " + e.getMessage(), null);
        }
    }

    public Answer read(int answerId) {
        return answers.get(answerId);
    }

    public List<Answer> readAll() {
        return new ArrayList<>(answers.values());
    }

    public List<Answer> readByQuestionId(int questionId) {
    	try {
			return db.getAnswersByQuestionId(questionId);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
    }

    /**
     * Updates an existing answer if permitted.
     * Students can update their own; staff/admins can update any.
     */
    public Result update(int answerId, int questionId, User currUser, String content, Boolean isSolution) {
        List<Question> qList = db.loadAllQs();
        Question question = qList.stream()
                .filter(q -> q.getQuestionId() == questionId)
                .findFirst()
                .orElse(null);

        Answer answer = answers.get(answerId);

        if (answer == null) {
            return new Result(false, "Answer not found.", null);
        }
        if (question == null) {
            return new Result(false, "Parent question not found.", null);
        }

      
        boolean isAuthor = answer.getUserId() == currUser.getId();
        boolean isPrivileged = currUser.isPrivileged();

        // Only author or privileged roles can edit
        if (!isAuthor && !isPrivileged) {
            return new Result(false, "Only the author or staff may update this answer.", null);
        }

        if (content != null) answer.setContent(content);
        if (isSolution != null) answer.setSolution(isSolution);

        // Save updates to the database
        try {
            db.updateAnswer(answer);
        } catch (SQLException e) {
            e.printStackTrace();
            return new Result(false, "Failed to update answer in database: " + e.getMessage(), null);
        }

        return new Result(true, "Answer updated successfully.", answer);
    }

    /**
     * Deletes an answer if permitted.
     * Students can delete their own; staff/admins can delete any.
     */
    public Result delete(int answerId, User user) {
        Answer answer = answers.get(answerId);

        if (answer == null) {
            return new Result(false, "Answer not found.", null);
        }

        boolean isAuthor = answer.getUserId() == user.getId();
        boolean isPrivileged = user.isPrivileged();
        
        // Only author or privileged roles can delete
        if (!isAuthor && !isPrivileged) {
            return new Result(false, "Only the author or staff can delete this answer.", null);
        }

        // Remove from memory and database
        answers.remove(answerId);
        try {
            db.deleteAnswer(answerId);
        } catch (SQLException e) {
            e.printStackTrace();
            return new Result(false, "Failed to delete answer from database: " + e.getMessage(), null);
        }

        return new Result(true, "Answer deleted successfully.", null);
    }

    /** Search helpers */
    public List<Answer> search(int questionId, String keyword, Boolean solutionOnly) {
        return answers.values().stream()
                .filter(a -> a.getQuestionId() == questionId)
                .filter(a -> (keyword == null || keyword.isEmpty()
                        || a.getContent().toLowerCase().contains(keyword.toLowerCase())
                        || a.getAuthor().toLowerCase().contains(keyword.toLowerCase())))
                .filter(a -> (solutionOnly == null || !solutionOnly || a.isSolution()))
                .collect(Collectors.toList());
    }

    public List<Answer> search(String keyword, String author, Boolean solutionOnly) {
        String kw = keyword == null ? "" : keyword.toLowerCase();
        String au = author == null ? "" : author.toLowerCase();

        return answers.values().stream()
                .filter(a -> kw.isEmpty()
                        || (a.getContent() != null && a.getContent().toLowerCase().contains(kw))
                        || (a.getAuthor() != null && a.getAuthor().toLowerCase().contains(kw)))
                .filter(a -> au.isEmpty()
                        || (a.getAuthor() != null && a.getAuthor().toLowerCase().contains(au)))
                .filter(a -> (solutionOnly == null || !solutionOnly || a.isSolution()))
                .collect(Collectors.toList());
    }
    
    public Result setSolution(int answerId, boolean isSolution) {
    	try {
	    	Answer answer;
			answer = StatusData.databaseHelper.getAnswerById(answerId);
			
			if (answer == null) {
	            return new Result(false, "Answer not found.", null);
	        }
			
//			int questionId = answer.getQuestionId();
//			System.out.println("Question id = " + questionId);
			
			// Load question fresh from DB
	        Question question = StatusData.databaseHelper.getQuestionById(answer.getQuestionId());
	        if (question == null) {
	            return new Result(false, "Question not found.", null);
	        }

	        User current = StatusData.currUser;
	        if (current == null) {
	            return new Result(false, "User not logged in.", null);
	        }
	        boolean isPrivileged =
	                current.getRole() == User.Role.ADMIN ||
	                current.getRole() == User.Role.INSTRUCTOR ||
	                current.getRole() == User.Role.REVIEWER ||
	                current.getRole() == User.Role.TA;

	        boolean isQuestionAuthor = current.getId() == question.getUserId();
	        
	        if (!isPrivileged && !isQuestionAuthor) {
	            return new Result(false,
	                    "You do not have permission to mark solutions.",
	                    null);
	        }
			//first update solution status for answer
			StatusData.databaseHelper.updateAnswerSolutionStatus(answerId, isSolution);
			answer.setSolution(isSolution);
			
			//sync inmemory cache in case we are still using it somewhere instead of database
			Answer cached = answers.get(answerId);
			if (cached != null) {
			    cached.setSolution(isSolution);
			}
	
			//check if there are any resolved solutions
	        List<Answer> allAnswers = StatusData.databaseHelper.getAnswersByQuestionId(question.getQuestionId());
	        boolean anyMarked = allAnswers.stream().anyMatch(Answer::isSolution);
			System.out.println("anyMarked = " + anyMarked + ", questionId = " + question.getQuestionId() + ", answerId = " + answerId);
	        //update question status based on if there are any solutions
//	        Question question = StatusData.databaseHelper.getQuestionById(question.getQuestionId());
//	        if (question != null) {
//	            if (anyMarked) {
//	            	question.setResolved(true);
//	                question.markResolved();
//	                System.out.println("question status = " + question.isResolved());
//	            } else {
//	            	question.setResolved(false);
//	                question.markUnresolved();
//	                System.out.println("question status = " + question.isResolved());
//	            }
//			
//	            StatusData.databaseHelper.updateQuestionResolved(questionId, question.isResolved());
//	        }
			question.setResolved(anyMarked);
	        StatusData.databaseHelper.updateQuestionResolved(question.getQuestionId(), anyMarked);
			return new Result(true, "Solution status updated.", null);

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return new Result(false, "Database error: " + e.getMessage(), null);
		}  
    }
    


    public int size() {
        return answers.size();
    }
}
