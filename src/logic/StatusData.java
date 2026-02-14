package logic;

import databasePart1.DatabaseHelper;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import model.Answers;
import model.Questions;
import model.User;
import pages.*;


/**
 * Holds shared application state and configuration for the JavaFX client.
 * Stores shared instances such as the {@link databasePart1.DatabaseHelper}, the primary
 * {@link javafx.stage.Stage}, and the {@link Questions} / {@link Answers} managers,
 * as well as window sizing and a utility method for setting scenes.
 */
public class StatusData {
    public static DatabaseHelper databaseHelper;
    public static Stage primaryStage;
    public static Questions questions;
    public static Answers answers;
    
    public static User currUser;
    public static final boolean DEV_MODE = false;
    // Window size constants
    public static final int WINDOW_WIDTH = 1000;
    public static final int WINDOW_HEIGHT = 800;
    
    public static InstructorHomePage instructorHomePageInstance;
    public static AdminHomePage adminHomePageInstance;
    public static StaffPage staffPageInstance;
    
    public static double getScaledWidth() {
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        return Math.min(WINDOW_WIDTH, screenBounds.getWidth() * 0.9);
    }
    
    public static double getScaledHeight() {
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        return Math.min(WINDOW_HEIGHT, screenBounds.getHeight() * 0.9);
    }
    
    // Utility method to set scene with consistent sizing
    public static void setScene(Stage stage, Parent root) {
        //stage.setScene(new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT));
    	 Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();

    	    double width = Math.min(WINDOW_WIDTH, screenBounds.getWidth() * 0.9);
    	    double height = Math.min(WINDOW_HEIGHT, screenBounds.getHeight() * 0.9);

    	    Scene scene = new Scene(root, width, height);

    	    stage.setScene(scene);
    	    stage.setFullScreen(false);
    	    stage.setMaximized(false);
    	    stage.setWidth(width);
    	    stage.setHeight(height);
    	    stage.centerOnScreen();
    	    stage.setResizable(true);
    	    
    	    //stage.show();
    }
}