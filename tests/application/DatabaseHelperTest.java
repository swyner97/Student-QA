package application;

import model.Answer;
import model.Question;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import databasePart1.DatabaseHelper;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DatabaseHelperTest {

    private DatabaseHelper helper;
    private Connection conn;

    @BeforeEach
    void setUp() throws Exception {
        helper = new DatabaseHelper();
        conn = DriverManager.getConnection(
                "jdbc:h2:mem:testdb;MODE=MySQL;DB_CLOSE_DELAY=-1",
                "sa",
                ""
        );

        setPrivateField(helper, "connection", conn);
        setPrivateField(helper, "statement", conn.createStatement());

        createSchema(conn);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (conn != null && !conn.isClosed()) {
            conn.close();
        }
    }

    @Test
    void getQuestionById_returnsStoredQuestion() throws Exception {
        insertQuestionRow(1, "Alice", "Java help", "Need help with JDBC",
                Timestamp.valueOf("2026-03-23 10:38:12"), "Resolved", 0, 10);

        Question q = helper.getQuestionById(1);

        assertNotNull(q);
        assertEquals(1, q.getQuestionId());
        assertEquals("Alice", q.getAuthor());
        assertEquals("Java help", q.getTitle());
        assertEquals("Need help with JDBC", q.getDescription());
        assertEquals("Resolved", q.getStatusText());
    }

    @Test
    void getAnswersByQuestionId_returnsStoredAnswer() throws Exception {
        insertQuestionRow(1, "Alice", "Java help", "Need help with JDBC",
                Timestamp.valueOf("2026-03-23 10:38:12"), "Resolved", 0, 10);

        insertAnswerRow(7, 20, 1, "Bob", "Try using PreparedStatement",
                Timestamp.valueOf("2026-03-23 11:00:00"), true);

        List<Answer> answers = helper.getAnswersByQuestionId(1);

        assertEquals(1, answers.size());
        Answer a = answers.get(0);
        assertEquals(7, a.getAnswerId());
        assertEquals(1, a.getQuestionId());
        assertEquals("Bob", a.getAuthor());
        assertEquals("Try using PreparedStatement", a.getContent());

     
        assertEquals("2026-03-23T11:00:00", a.getTimestamp());
    }

    @Test
    void searchQuestions_findsKeywordMatches() throws Exception {
        insertQuestionRow(1, "Alice", "Java help", "Need help with JDBC",
                Timestamp.valueOf("2026-03-23 10:38:12"), "Resolved", 0, 10);

        insertQuestionRow(2, "Bob", "Python topic", "Different subject",
                Timestamp.valueOf("2026-03-23 12:00:00"), "Unresolved", 0, 11);

        List<Question> result = helper.searchQuestions("jdbc", "", null);

        assertEquals(1, result.size());
        assertEquals("Java help", result.get(0).getTitle());
    }

    @Test
    void searchAnswers_findsKeywordMatches() throws Exception {
        insertQuestionRow(1, "Alice", "Java help", "Need help with JDBC",
                Timestamp.valueOf("2026-03-23 10:38:12"), "Resolved", 0, 10);

        insertAnswerRow(7, 20, 1, "Bob", "Try using PreparedStatement",
                Timestamp.valueOf("2026-03-23 11:00:00"), true);

        List<Answer> result = helper.searchAnswers("preparedstatement", "", null);

        assertEquals(1, result.size());
        assertEquals("Bob", result.get(0).getAuthor());
    }

    private void createSchema(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement()) {
            st.execute("""
                CREATE TABLE cse360users (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    userName VARCHAR(255),
                    password VARCHAR(255),
                    role VARCHAR(20),
                    name VARCHAR(255),
                    email VARCHAR(255),
                    phone VARCHAR(20),
                    bio TEXT,
                    temp_password VARCHAR(255)
                )
            """);

            st.execute("""
                CREATE TABLE questions (
                    question_id INT PRIMARY KEY,
                    author VARCHAR(50),
                    title VARCHAR(200),
                    description VARCHAR(5000),
                    timestamp TIMESTAMP,
                    status VARCHAR(20),
                    follow_up INT,
                    user_id INT
                )
            """);

            st.execute("""
                CREATE TABLE answers (
                    answer_id INT PRIMARY KEY,
                    user_id INT,
                    question_id INT,
                    author VARCHAR(50),
                    content VARCHAR(2000),
                    timestamp TIMESTAMP,
                    is_solution BOOLEAN
                )
            """);

            st.execute("""
                CREATE TABLE edits (
                    edit_id INT AUTO_INCREMENT PRIMARY KEY,
                    question_id INT NOT NULL,
                    old_title TEXT NOT NULL,
                    old_description TEXT NOT NULL,
                    new_title TEXT NOT NULL,
                    new_description TEXT NOT NULL,
                    edited_by TEXT NOT NULL,
                    edit_time TEXT NOT NULL
                )
            """);
        }
    }

    private void insertQuestionRow(
            int questionId,
            String author,
            String title,
            String description,
            Timestamp timestamp,
            String status,
            int followUp,
            int userId
    ) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("""
            INSERT INTO questions
            (question_id, author, title, description, timestamp, status, follow_up, user_id)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """)) {
            ps.setInt(1, questionId);
            ps.setString(2, author);
            ps.setString(3, title);
            ps.setString(4, description);
            ps.setTimestamp(5, timestamp);
            ps.setString(6, status);
            ps.setInt(7, followUp);
            ps.setInt(8, userId);
            ps.executeUpdate();
        }
    }

    private void insertAnswerRow(
            int answerId,
            int userId,
            int questionId,
            String author,
            String content,
            Timestamp timestamp,
            boolean isSolution
    ) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("""
            INSERT INTO answers
            (answer_id, user_id, question_id, author, content, timestamp, is_solution)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """)) {
            ps.setInt(1, answerId);
            ps.setInt(2, userId);
            ps.setInt(3, questionId);
            ps.setString(4, author);
            ps.setString(5, content);
            ps.setTimestamp(6, timestamp);
            ps.setBoolean(7, isSolution);
            ps.executeUpdate();
        }
    }

    private void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field field = DatabaseHelper.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}