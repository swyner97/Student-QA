package model;

public enum ContentType {
	QUESTION, ANSWER, REVIEW, SUGGESTION, OTHER;
	
	public static ContentType fromString(String value) {
		return switch (value.toLowerCase()) {
		case "question" -> QUESTION;
		case "answer" -> ANSWER;
		case "review" -> REVIEW;
		case "suggestion" -> SUGGESTION;
		default -> OTHER;
		};
	}
	
	public String toDatabaseString() {
		return this.name().toLowerCase();
	}
}
