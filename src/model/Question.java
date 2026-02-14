package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;


/**
 * Represents a user-posted question with metadata such as author, title,
 * description, timestamp, tags, and resolution status.
 * Each question can hold multiple {@link Answer} objects.
 */
public class Question {
    private int questionId;
    private int userId;   // ADDED USERID
    private String author;
    private String title;
    private String description;
    private String timestamp;
    private boolean resolved;
    private int followUp;
    private List<String> tags;
    private List<Answer> answers = new ArrayList<>();
    private List<Edits> editHistory = new ArrayList<>();
    
    public Question(int questionId, int userId, String author, String title, String description) {
        this(questionId, userId, author, title, description, 
             LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
             false, new ArrayList<>());
    }
    
 // CONSTRUCTOR 2
    public Question(int questionId, int userId, String author, String title, String description,
                       String timestamp, boolean resolved, List<String> tags) {
            this.questionId = questionId;
            this.userId = userId;
            this.author = author;
            this.title = title;
            this.description = description;
            this.timestamp = timestamp;
            this.resolved = resolved;
            this.tags = tags != null ? tags : new ArrayList<>();
        }
    
  
    public void markResolved() {
        this.resolved = true;
    }
    
    public void markUnresolved() {
        this.resolved = false;
    }
    
    public void addEditHistory(Edits edit) {
        editHistory.add(edit);
    }
    
    public void setEditHistory(List<Edits> editHistory) {
        this.editHistory = editHistory;
    }
    
    public List<Edits> getEditHistory() {
        return editHistory;
    }
    
 // GETTERS SETTERS
    public int getQuestionId() { return questionId; }
     public void setQuestionId(int questionId) { this.questionId = questionId; }
     
     public int getUserId() { return userId; } // GET SET USER ID					
     public void setUserId(int userId) { this.userId = userId; }
     
     public String getAuthor() { return author; }
     public void setAuthor(String author) { this.author = author; }
     
     public String getTitle() { return title; }
     public void setTitle(String title) { this.title = title; }
     
     public String getDescription() { return description; }
     public void setDescription(String description) { this.description = description; }
     
     public String getTimestamp() { return timestamp; }
     public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
     
     public boolean isResolved() { return resolved; }
     public void setResolved(boolean resolved) { this.resolved = resolved; }

     public int getFollowUp() { return followUp; }
     public void setFollowUp(int followUp) { this.followUp = followUp; }
     
     public List<String> getTags() { return tags; }
     public void setTags(List<String> tags) { this.tags = tags; }
     
     public List<Answer> getAnswers() { return answers; }
     public void setAnswers(List<Answer> answers) { 
    	 this.answers = answers != null ? answers : new ArrayList<>();
     }
    
     
    public String getStatusText() {
    	    return resolved ? "Resolved" : "Unresolved";  // or "open"/"Closed" if you prefer
	}
    
    public String getDisplayId(Map<Integer, List<Question>> followUpsGrouped) {
        if (followUp == 0) return String.valueOf(questionId);  // Main question

        int parentId = followUp;
        List<Question> siblings = followUpsGrouped.getOrDefault(parentId, new ArrayList<>());
        
        int index = 1;
        for (Question q : siblings) {
            if (q.getQuestionId() == this.questionId) break;
            index++;
        }
        return parentId + "." + index;
    }
     
    @Override
    public String toString() {
        return String.format("Q#%d: %s (%s)", questionId, title, resolved);
    }
}

