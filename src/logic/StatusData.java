package logic;

import databasePart1.DatabaseHelper;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;
import model.Answers;
import model.Questions;
import model.User;

public class StatusData {
    public static DatabaseHelper databaseHelper;
    public static Stage primaryStage;
    public static Questions questions;
    public static Answers answers;

    public static User currUser;
    public static final boolean DEV_MODE = true;

    public static final int WINDOW_WIDTH = 1000;
    public static final int WINDOW_HEIGHT = 800;

    public static Runnable onFlagRefresh;

    public static double getScaledWidth() {
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        return Math.min(WINDOW_WIDTH, screenBounds.getWidth() * 0.9);
    }

    public static double getScaledHeight() {
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        return Math.min(WINDOW_HEIGHT, screenBounds.getHeight() * 0.9);
    }

    public static void setScene(Stage stage, Parent root) {
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();

        if (stage.getScene() != null) {
            stage.getScene().setRoot(root);
        } else {
            double width = Math.min(WINDOW_WIDTH, screenBounds.getWidth() * 0.9);
            double height = Math.min(WINDOW_HEIGHT, screenBounds.getHeight() * 0.9);
            stage.setScene(new Scene(root, width, height));
        }

        stage.sizeToScene();
        double w = Math.min(stage.getWidth(), screenBounds.getWidth() * 0.9);
        double h = Math.min(stage.getHeight(), screenBounds.getHeight() * 0.9);
        stage.setWidth(w);
        stage.setHeight(h);

        stage.setFullScreen(false);
        stage.setMaximized(false);
        stage.centerOnScreen();
        stage.setResizable(true);
    }
}