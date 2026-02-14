package application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.*;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import databasePart1.DatabaseHelper;
import model.Question;

/**
 * These jUnit tests are for validating follow-up question functionality in the Q&A platform.
 *  <p>
 * These tests ensure that:
 * <ul>
 <li>Follow-up {@link model.Question} instances are correctly linked to their parent question</li>
 *   <li>{@link DatabaseHelper#getFollowUps(int)} returns only the appropriate child questions</li>
 *   <li>Empty results are correctly returned when no follow-ups exist</li>
 * </ul>
 *
  * <p>These tests use a custom {@link FollowUpTests.MockDatabaseHelper} to replace the
 * live {@link databasePart1.DatabaseHelper} class. This mock stores all questions in an
 * in-memory {@code Map<Integer, Question>} and filters them when retrieving follow-ups.
 *
 * <p>Example usage pattern:
 * <pre>{@code
 * Question parent = new Question(0, 1, "User", "Parent", "Text");
 * mockDb.insertQuestion(parent);
 * 
 * Question child = new Question(0, 2, "Other", "Child", "Reply");
 * child.setFollowUp(parent.getQuestionId());
 * mockDb.insertQuestion(child);
 * 
 * List<Question> children = mockDb.getFollowUps(parent.getQuestionId());
 * assertEquals(1, children.size());
 * 
 * List<Question> children = mockDb.getFollowUps(parent.getQuestionId());
 * assertEquals(1, children.size());
 * }</pre>
 *
 * <p>This ensures follow-up relationships are testable and UI-friendly.
 *
 * @author Tirzha Rhys
 * @version 1.0
 * @since 2025-11-29
 *
 * @see model.Edits
 * @see model.Question
 * @see databasePart1.DatabaseHelper
 * @see application.EditTests.MockDatabaseHelper
 */

public class FollowUpTests {
	private MockDatabaseHelper mockDb;

	 /**
     * This setup creates a fresh mock database before each test so we don't have to
     * keep altering the actual database.
     * 
     * This ensures all tests start in a clean, isolated state and do not
     * interfere with one another.
     */
	@BeforeEach
	void setUp() {
		mockDb = new MockDatabaseHelper();
	}
	
	 /**
     * This get follow up test ensures that the {@link MockDatabaseHelper#getFollowUps(int)} method returns
     * all follow-up {@link model.Question} instances that reference a given parent question.
     *
     * <p>This test ensures this by creating a parent question and two follow-up questions with
     * {@code setFollowUp(parentId)} set, and asserting that both follow-ups
     * are returned and correctly linked to the original parent.</p>
     *
     * <p>Validates correct data retrieval and linkage by author names.</p>
     */
	@Test
    void testGetFollowUpsReturnsCorrectResults() {
        // Create parent question
        Question parent = new Question(0, 1, "Alex", "Main Q", "Main desc");
        mockDb.insertQuestion(parent);

        // Create follow-ups
        Question follow1 = new Question(0, 2, "Jamie", "Follow Q1", "Follow desc 1");
        follow1.setFollowUp(parent.getQuestionId());
        mockDb.insertQuestion(follow1);

        Question follow2 = new Question(0, 3, "Taylor", "Follow Q2", "Follow desc 2");
        follow2.setFollowUp(parent.getQuestionId());
        mockDb.insertQuestion(follow2);

        List<Question> followUps = mockDb.getFollowUps(parent.getQuestionId());

        assertEquals(2, followUps.size());
        assertTrue(followUps.stream().anyMatch(q -> q.getAuthor().equals("Jamie")));
        assertTrue(followUps.stream().anyMatch(q -> q.getAuthor().equals("Taylor")));
    }
	
	 /**
     * This test is for testing that the system does not crash if it tries to get a non-existent followup.
     * It does that by calling {@link MockDatabaseHelper#getFollowUps(int)} for a parent
     * question with no associated follow-up and sees that it returns a non-null but empty {@link List}.
     *
     * <p>This ensures the system behaves predictably and does not throw a {@code NullPointerException}
     * or return null when no follow-ups exist.</p>
     *
     * <p>Useful for verifying default UI behavior and guarding against edge cases.</p>
     */
	 @Test
	    void testGetFollowUpsWithNoMatchesReturnsEmptyList() {
	        Question parent = new Question(0, 1, "Jordan", "Lonely Q", "No replies");
	        mockDb.insertQuestion(parent);

	        List<Question> followUps = mockDb.getFollowUps(parent.getQuestionId());
	        assertNotNull(followUps);
	        assertTrue(followUps.isEmpty());
	    }
	 
	private static class MockDatabaseHelper extends DatabaseHelper {
		private final Map<Integer, Question> questionMap = new HashMap<>();
		private int questionIdCounter = 1;
		
		@Override
		public void insertQuestion(Question q) {
		    q.setQuestionId(questionIdCounter++);
		    questionMap.put(q.getQuestionId(), q);
		}
		
		@Override
		public List<Question> getFollowUps(int parentId) {
		    List<Question> result = new ArrayList<>();
		    for (Question q : questionMap.values()) {
		        if (q.getFollowUp() == parentId) {
		            result.add(q);
		        }
		    }
		    return result;
		}
	}
}
