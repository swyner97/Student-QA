package application;
import javafx.application.Application;
import javafx.stage.Stage;
import logic.StatusData;
import model.Answers;
import model.Questions;
import model.Reviews;
import model.User;
import pages.FirstPage;
import pages.InitialAccessPage;

import java.sql.SQLException;

import java.util.*;

import databasePart1.DatabaseHelper;

/**
 * Main entry point for the Student QA JavaFX application.
 * <p>
 * This class initializes the database connection, sets up shared application
 * state via {@link logic.StatusData}, seeds default reviewer accounts and
 * sample reviews when none exist, and navigates to the appropriate first page.
 * </p>
 */
public class StartApp extends Application {

	/**
	 * Default constructor for StartApp.
	 * Instantiated automatically by the JavaFX runtime when launching the application.
	 */
	public StartApp() {
		// Default constructor required by JavaFX Application
	}

	/** Shared database helper used throughout the application lifecycle. */
	private static final DatabaseHelper databaseHelper = new DatabaseHelper();
	
	/** The width of the main application window in pixels. */
	public static final int WINDOW_WIDTH = 3000;

	/** The height of the main application window in pixels. */
	public static final int WINDOW_HEIGHT = 700;  
	
	/**
	 * Application entry point. Registers a shutdown hook to cleanly close the
	 * database connection, then launches the JavaFX runtime.
	 *
	 * @param args command-line arguments passed to the JavaFX launcher
	 */
	public static void main( String[] args ) 
	{
		StatusData.databaseHelper = databaseHelper;

		// Adding a catch to make sure the app shuts down before launching again
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			databaseHelper.closeConnection();
			System.out.println("Database connection closed (via shutdown hook).");
		}));
		
		launch(args);
	}
	
	/**
	 * Called by the JavaFX runtime after {@link #main(String[])} invokes
	 * {@code launch()}. Connects to the database, initializes shared model
	 * objects, seeds reviewer accounts and sample reviews if the database is
	 * not empty but has no reviewers, and shows either the
	 * {@link pages.FirstPage} (empty database) or the
	 * {@link pages.InitialAccessPage} (returning users).
	 *
	 * @param primaryStage the primary {@link Stage} provided by the JavaFX runtime
	 */
	@Override
    public void start(Stage primaryStage) { 

		StatusData.primaryStage = primaryStage;
        
        try {
            databaseHelper.connectToDatabase(); // Connect to the database
            StatusData.databaseHelper = databaseHelper;

            StatusData.questions = new Questions(databaseHelper);
            StatusData.answers = new Answers(databaseHelper);

            
            if (databaseHelper.isDatabaseEmpty()) {
            	
            	new FirstPage(databaseHelper).show(primaryStage);
            } else {
            	try {
            		List<User> existingReviewers = databaseHelper.getUsersByRole(User.Role.REVIEWER);
            		if (existingReviewers == null || existingReviewers.isEmpty()) {
            			System.out.println("No reviewers found, adding reviewers.");
            			
            			User r1 = User.createUser("Amy", "Password123!", User.Role.REVIEWER, "Reviewer Amy", "email@a.com", null);
            			User r2 = User.createUser("Bob", "Password123!", User.Role.REVIEWER, "Reviewer Bob", "email@a.com", null);
            			User r3 = User.createUser("Stanley", "Password123!", User.Role.REVIEWER, "Reviewer Stanley", "email@a.com", null);
            			User r4 = User.createUser("Claire", "Password123!", User.Role.REVIEWER, "Reviewer Claire", "email@a.com", null);
            			
            			if (!databaseHelper.doesUserExist(r1.getUserName())) databaseHelper.register(r1);
            			if (!databaseHelper.doesUserExist(r2.getUserName())) databaseHelper.register(r2);
            			if (!databaseHelper.doesUserExist(r3.getUserName())) databaseHelper.register(r3);
            			if (!databaseHelper.doesUserExist(r4.getUserName())) databaseHelper.register(r4);
            			
            			System.out.println("Reviewers created successfully.");
            			
            			//Create sample reviews
            			Reviews reviewsManager = new Reviews(databaseHelper);
            			
            			
            			if (reviewsManager.readAll().size() == 0)  {
                            System.out.println("No reviews found, adding sample reviews from auto-created reviewers.");

                            // Amy reviews Answer #1 and #2
                            reviewsManager.create(
                                    r1.getId(),
                                    1,  
                                    r1.getName(),
                                    "This answer gives a clear pointer to where Eclipse can be downloaded."
                            );
                            reviewsManager.create(
                                    r1.getId(),
                                    2,
                                    r1.getName(),
                                    "Good explanation of the homework extension timing."
                            );

                            // Bob reviews Answer #3
                            reviewsManager.create(
                                    r2.getId(),
                                    3,
                                    r2.getName(),
                                    "Nice suggestion about using Bing; might also mention official docs."
                            );

                            // Stanley reviews Answer #5
                            reviewsManager.create(
                                    r3.getId(),
                                    5,
                                    r3.getName(),
                                    "Very detailed steps, this should help confused students a lot."
                            );

                            // Claire reviews Answer #7
                            reviewsManager.create(
                                    r4.getId(),
                                    7,
                                    r4.getName(),
                                    "Accurate information about the extension; short but useful."
                            );

                            System.out.println("Sample reviews created successfully.");
                        } else {
                            System.out.println("Reviews already exist, skipping auto-creation of sample reviews.");
                        } 
            		} else {
            			System.out.println("Reviewers already exist. Skipping reviewer creation.");
            		}
            	} catch (Exception e) {
            		e.printStackTrace();
            	}
            	new InitialAccessPage(databaseHelper).show(primaryStage);
                
            }
            System.out.println("Users: " + databaseHelper.getAllUsers());
        } catch (SQLException e) {
        	System.out.println(e.getMessage());
        }
    }
	

}