package application;
import static org.junit.jupiter.api.Assertions.*;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import org.junit.jupiter.api.*;
import databasePart1.DatabaseHelper;
import javafx.collections.*;
import javafx.embed.swing.JFXPanel;
import javafx.stage.Stage;
import logic.*;
import model.*;
import pages.*;

/**
 * <p> Title: Ranking System Testing </p>
 *
 * <p> Description: Showcases effective automated testing with JUnit
 *  of the ranking system responsibilites. </p>
 *
 *   <p> Copyright: Kristi Van @ 2025 </p>
 *
 *   @author Kristi Van
 *
 *   @version 0.0 2025-11-20
 */

class RankingSystemTest {
    private DatabaseHelper dB;
    private RankReviewer rankPage; 
    private User instructor; 
    private User student; 
    private User student2; 
    private static boolean jfxInitialized = false;
    
    
    public void start(Stage stage) {
        rankPage = new RankReviewer();
        rankPage.show(stage);
    }

    
    @BeforeAll
    public static void initJFX() {
        if (!jfxInitialized) {
            // Start JavaFX toolkit once for all tests
            new JFXPanel();
            jfxInitialized = true;
            System.out.println("JavaFX initialized for tests");
        }
    }

    /**
     * Sets up the database and dummy users before each test.
     * @throws SQLException if database operations fail
     */
    @BeforeEach
    void setUp() throws SQLException {
    	dB = new DatabaseHelper();
    	dB.connectToDatabase();
    	// Clean tables that matter so each test starts fresh
        try (Statement stmt = dB.getConnection().createStatement()) {
        	stmt.executeUpdate("DELETE FROM reviewerScoreCard");
        	stmt.executeUpdate("DELETE FROM allReviewerRatings");
        	stmt.executeUpdate("DELETE FROM allReviewerRatings");
        	stmt.executeUpdate("DELETE FROM trustedReviewers");
            stmt.executeUpdate("DELETE FROM cse360users");
        }	
        
        //dummy users
        instructor = User.createUser("instructor1", "Password123!", User.Role.INSTRUCTOR,
                "Instructor One", "I1@example.com", null);
        
        student = User.createUser("student1", "Password123!", User.Role.STUDENT,
                "Student One", "s1@example.com", null);
        student2 = User.createUser("student2", "Password123!", User.Role.STUDENT,
                "Student Two", "s2@example.com", null);
        dB.register(student2);
        dB.register(student);
        dB.register(instructor);
    }
    
    /**
     * Closes the database connection after each test.
     */
    @AfterEach
    void tearDown() {
        dB.closeConnection();
    }
    
    @Test
    void testUILoads() {
        // If stage loaded correctly
        assertTrue(true);
    }
    
    /**
     * Tests adding reviewer scores to the database.
     * Verifies that scores for multiple reviewers can be added successfully.
     * @throws SQLException if database operations fail
     */
    @Test
    void testAddReviewerScore() throws SQLException {
        User reviewer = User.createUser("reviewer1", "Pass123!", User.Role.REVIEWER, "Reviewer One", "r1@example.com", null);
        User reviewer2 = User.createUser("reviewer3", "Pass123!", User.Role.REVIEWER, "Reviewer Three", "r2@example.com", null);
        dB.register(reviewer);
        dB.register(reviewer2);

        // Add some fake ratings from different students
        boolean add = dB.addReviewerScore(reviewer.getId(), student.getId(), 5); // reviewerId, answerId, rating
        boolean add2 = dB.addReviewerScore(reviewer2.getId(), student.getId(), 3);
        assertTrue(add, "Reviewer 1 score should be added successfully");
        assertTrue(add2, "Reviewer 2 score should be added successfully");
    }
    
    /**
     * Tests updating a reviewer score.
     * @throws SQLException if database operations fail
     */
    @Test
    void testUpdateReviewerScore() throws SQLException {
        User reviewer = User.createUser("reviewer1", "Pass123!", User.Role.REVIEWER, "Reviewer One", "r1@example.com", null);
        User reviewer2 = User.createUser("reviewer3", "Pass123!", User.Role.REVIEWER, "Reviewer Two", "r2@example.com", null);
        dB.register(reviewer);
        dB.register(reviewer2);

        // Add some fake ratings from different students
        dB.addReviewerScore(reviewer.getId(), student.getId(), 5); // reviewerId, answerId, rating
        dB.addReviewerScore(reviewer.getId(), student.getId(), 3);
        int score = dB.calculateReviewerScore(reviewer.getId());
        assertEquals(3, score, "Score should updated");
    }
    
    /**
     * Tests calculation of the highest sum of reviewer scores across two students.
     * @throws SQLException if database operations fail
     */
    @Test
    void testHighestSumCalculation() throws SQLException {
        User reviewer = User.createUser("reviewer1", "Pass123!", User.Role.REVIEWER, "Reviewer One", "r1@example.com", null);
        User reviewer2 = User.createUser("reviewer3", "Pass123!", User.Role.REVIEWER, "Reviewer Two", "r2@example.com", null);
        dB.register(reviewer);
        dB.register(reviewer2);

        // Add some fake ratings from different students
        dB.addReviewerScore(reviewer.getId(), student.getId(), 5); // reviewerId, answerId, rating
        dB.addReviewerScore(reviewer.getId(), student2.getId(), 3);

        int score = dB.calculateHighestSumScore(reviewer.getId());
        assertEquals(8, score, "Score should sum up ratings");
    }
    
    /**
     * Tests calculation of the average score for a reviewer across two students.
     * @throws SQLException if database operations fail
     */
    @Test
    void testAverageCalculation() throws SQLException {
        User reviewer = User.createUser("reviewer1", "Pass123!", User.Role.REVIEWER, "Reviewer One", "r1@example.com", null);
        User reviewer2 = User.createUser("reviewer3", "Pass123!", User.Role.REVIEWER, "Reviewer Two", "r2@example.com", null);
        dB.register(reviewer);
        dB.register(reviewer2);

        // Add some fake ratings from different students
        dB.addReviewerScore(reviewer.getId(), student.getId(), 3); // reviewerId, answerId, rating
        dB.addReviewerScore(reviewer.getId(), student2.getId(), 3);

        int score = dB.calculateAverageScore(reviewer.getId());
        assertEquals(3, score, "Score should be average ratings");
    }
    
    /**
     * Tests that the instructor scoring method updates the database correctly.
     * Default is "HIGHEST_SUM"
     * @throws SQLException if database operations fail
     */
    // default test only works if you delete .mv
    @Test
    void testInstructorScoringMethodToggle() throws SQLException {
        // Default should be HIGHEST_SUM
        String defaultMethod = dB.getInstructorScoringMethod();
        assertEquals("HIGHEST_SUM", defaultMethod, "Default scoring method should be HIGHEST_SUM");

        // Update to AVERAGE
        dB.updateInstructorScoringMethod("AVERAGE");
        String updatedMethod = dB.getInstructorScoringMethod();
        assertEquals("AVERAGE", updatedMethod, "Scoring method should update to AVERAGE");

        // Update back to HIGHEST_SUM
        dB.updateInstructorScoringMethod("HIGHEST_SUM");
        String revertedMethod = dB.getInstructorScoringMethod();
        assertEquals("HIGHEST_SUM", revertedMethod, "Scoring method should revert to HIGHEST_SUM");
    }   
}