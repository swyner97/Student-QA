package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Edits {
	
	private String questionOldTitle;
    private String questionOldDescription;
    private String questionNewTitle;
    private String questionNewDescription;
    private String questionEditedBy;
    private LocalDateTime questionEditTime;
    
    public Edits(String questionOldTitle, String questionOldDesc, String questionNewTitle, String questionNewDesc, String questionEditedBy) {
        this.questionOldTitle = questionOldTitle;
        this.questionOldDescription = questionOldDesc;
        this.questionNewTitle = questionNewTitle;
        this.questionNewDescription = questionNewDesc;
        this.questionEditedBy = questionEditedBy;
        this.questionEditTime = LocalDateTime.now();
    }
    public String getQuestionOldTitle() {
        return questionOldTitle;
    }

    public void setQuestionOldTitle(String questionOldTitle) {
        this.questionOldTitle = questionOldTitle;
    }

    public String getQuestionOldDescription() {
        return questionOldDescription;
    }

    public void setQuestionOldDescription(String questionOldDescription) {
        this.questionOldDescription = questionOldDescription;
    }

    public String getQuestionNewTitle() {
        return questionNewTitle;
    }

    public void setQuestionNewTitle(String questionNewTitle) {
        this.questionNewTitle = questionNewTitle;
    }

    public String getQuestionNewDescription() {
        return questionNewDescription;
    }

    public void setQuestionNewDescription(String questionNewDescription) {
        this.questionNewDescription = questionNewDescription;
    }

    public String getQuestionEditedBy() {
        return questionEditedBy;
    }

    public void setQuestionEditedBy(String questionEditedBy) {
        this.questionEditedBy = questionEditedBy;
    }

    public LocalDateTime getQuestionEditTime() {
        return questionEditTime;
    }

    public void setQuestionEditTime(LocalDateTime equestionEditTime) {
        this.questionEditTime = equestionEditTime;
    }
    
    public String getFormattedSummary() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return String.format(
            "Edited by %s on %s\nOld Title: %s\nNew Title: %s\nOld Description: %s\nNew Description: %s",
            questionEditedBy,
            questionEditTime.format(formatter),
            questionOldTitle,
            questionNewTitle,
            questionOldDescription,
            questionNewDescription
        );
    }
}


