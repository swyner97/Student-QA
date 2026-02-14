package application;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.*;
import java.util.*;

import model.*;
import databasePart1.DatabaseHelper;

public class QandAJUnitTests {
	private MockDatabaseHelper mockDb;


	@BeforeEach
	void setUp() {
		mockDb = new MockDatabaseHelper();
	}
	
	@Test
	void testInsertAndRetrieveQuestion() throws Exception {
		Question q = new Question(0, 1, "Annie", "Test Title", "Test Description");
		q.setTimestamp("2025-01-01T00:00:00");
	
	
		mockDb.insertQuestion(q);
		assertTrue(q.getQuestionId() > 0);
	
	
		Question loaded = mockDb.getQuestionById(q.getQuestionId());
		assertNotNull(loaded);
		assertEquals("Annie", loaded.getAuthor());
		assertEquals("Test Title", loaded.getTitle());
	}


	@Test
	void testSearchQuestionsByKeyword() throws Exception {
		mockDb.insertQuestion(new Question(0, 1, "Billy", "Java Basics", "How do I use JDBC?"));
		mockDb.insertQuestion(new Question(0, 1, "Chris", "Python Basics", "How do I use sqlite?"));
	
	
		List<Question> result = mockDb.searchQuestions("JDBC", null, null);
		assertEquals(1, result.size());
		assertEquals("Java Basics", result.get(0).getTitle());
	}


	@Test
	void testSearchAnswersByAuthor() throws Exception {
		Answer a1 = new Answer(0, 1, 1, "Diane", "Use try-with-resources", "2025-01-01T00:00:00", false);
		Answer a2 = new Answer(0, 2, 1, "Ellis", "Use connection pool", "2025-01-01T00:00:00", true);
	
	
		mockDb.insertAnswer(a1);
		mockDb.insertAnswer(a2);
	
	
		List<Answer> results = mockDb.searchAnswers(null, "Ellis", null);
		assertEquals(1, results.size());
		assertEquals("Ellis", results.get(0).getAuthor());
	}
	
	@Test
	void testUpdateQuestion() throws Exception {
		Question q = new Question(0, 1, "Finley", "Old Title", "Old Description");
		q.setTimestamp("2025-01-01T00:00:00");
		mockDb.insertQuestion(q);
	
	
		q.setTitle("New Title");
		q.setDescription("New Description");
		q.setResolved(true);
		mockDb.updateQuestion(q);
	
	
		Question updated = mockDb.getQuestionById(q.getQuestionId());
		assertEquals("New Title", updated.getTitle());
		assertEquals("New Description", updated.getDescription());
		assertTrue(updated.isResolved());
	}
	
	@Test
	void testDeleteQuestion() throws Exception {
		Question q = new Question(0, 1, "Garreth", "Title", "Desc");
		mockDb.insertQuestion(q);
	
	
		mockDb.deleteQuestion(q.getQuestionId());
		assertNull(mockDb.getQuestionById(q.getQuestionId()));
	}

	@Test
	void testFilterResolvedQuestions() {
		Question q1 = new Question(0, 1, "Hindley", "Resolved Q", "Desc");
		q1.setResolved(true);
		Question q2 = new Question(0, 1, "Ian", "Unresolved Q", "Desc");
		q2.setResolved(false);
	
	
		mockDb.insertQuestion(q1);
		mockDb.insertQuestion(q2);
	
	
		List<Question> resolved = mockDb.searchQuestions(null, null, true);
		assertEquals(1, resolved.size());
		assertTrue(resolved.get(0).isResolved());
	
	
		List<Question> unresolved = mockDb.searchQuestions(null, null, false);
		assertEquals(1, unresolved.size());
		assertFalse(unresolved.get(0).isResolved());
	}
	
	private static class MockDatabaseHelper extends DatabaseHelper {
		private int questionIdCounter = 1;
		private int answerIdCounter = 1;
		private final Map<Integer, Question> questionMap = new HashMap<>();
		private final Map<Integer, Answer> answerMap = new HashMap<>();
		
		@Override
		public void insertQuestion(Question question) {
			question.setQuestionId(questionIdCounter++);
			question.setResolved(question.isResolved());
			questionMap.put(question.getQuestionId(), question);
		}


		@Override
		public Question getQuestionById(int id) {
			return questionMap.get(id);
		}


		@Override
		public List<Question> searchQuestions(String keyword, String author, Boolean resolved) {
			List<Question> results = new ArrayList<>();
			for (Question q : questionMap.values()) {
				boolean matchesKeyword = keyword == null || q.getTitle().contains(keyword) || q.getDescription().contains(keyword);
				boolean matchesAuthor = author == null || q.getAuthor().equalsIgnoreCase(author);
				boolean matchesResolved = resolved == null || q.isResolved() == resolved;
				if (matchesKeyword && matchesAuthor && matchesResolved) {
					results.add(q);
				}
			}
			return results;
		}


		@Override
		public void insertAnswer(Answer answer) {
			answer.setAnswerId(answerIdCounter++);
			answerMap.put(answer.getAnswerId(), answer);
		}


		@Override
		public List<Answer> searchAnswers(String keyword, String author, Boolean isSolution) {
			List<Answer> results = new ArrayList<>();
			for (Answer a : answerMap.values()) {
				boolean matchesKeyword = keyword == null || a.getContent().toLowerCase().contains(keyword.toLowerCase());
				boolean matchesAuthor = author == null || a.getAuthor().equalsIgnoreCase(author);
				boolean matchesSolution = isSolution == null || a.isSolution() == isSolution;
				if (matchesKeyword && matchesAuthor && matchesSolution) {
					results.add(a);
				}
			}
			return results;
		}
		
		@Override
		public void updateQuestion(Question question) {
			questionMap.put(question.getQuestionId(), question);
		}


		@Override
		public void deleteQuestion(int questionId) {
			questionMap.remove(questionId);
		}
		
	}
}
