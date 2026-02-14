package pages;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.*;
import databasePart1.DatabaseHelper;
import logic.StatusData;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class ReviewerProfilePage {
	private ReviewerProfilePage() {}
	
	public static void showReviewerProfile(int reviewerId) {
		DatabaseHelper db = StatusData.databaseHelper;
		User currentUser = StatusData.currUser;
		User reviewer;
		int score = 0;
		
		try {
			reviewer = db.getUserById(reviewerId);
		} catch (Exception e) {
			showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load reviewer from database: " + e.getMessage());
			return;
		}

		if (reviewer == null) {
			showAlert(Alert.AlertType.ERROR, "Not Found", "Could not find a user with id: " + reviewerId);
			return;
		}
		
		try {
			score = db.calculateReviewerScore(reviewerId);
		} catch (SQLException e) {
			System.out.println("Could not load reviewer score: " + e.getMessage());
		}
		
		boolean canEdit = false;
		if (currentUser != null) {
			if (currentUser.getId() == reviewerId && (currentUser.getRole() == User.Role.REVIEWER || currentUser.getRole() == User.Role.ADMIN)) {
				canEdit = true;
			}
		}
		
		String experience = "";
		List<Review> reviews = new ArrayList<>();
		
		try {
			experience = db.getReviewerExperience(reviewerId);
		} catch (SQLException e) {
			showAlert(Alert.AlertType.ERROR, "Profile Load Warning", "Could not load experience text: " + e.getMessage());
			return;
		}
		
		try {
			reviews = db.getReviewsByReviewer(reviewerId);
		} catch (SQLException e) {
			showAlert(Alert.AlertType.WARNING, "Review Load Warning", "Could not load reviews for this reviewer: " + e.getMessage());
		}
		
		Stage stage = new Stage();
		stage.setTitle("Reviewer Profile - " + reviewer.getName());
		stage.initModality(Modality.APPLICATION_MODAL);
		
		BorderPane root = new BorderPane();
		root.setPadding(new Insets(15));
		
		// Header
		VBox header = new VBox(5);

		try {
			header.getChildren().addAll(
					new Label("Name: " + reviewer.getName()),
					new Label("Username: " + reviewer.getUserName()),
					new Label("Email: " + reviewer.getEmail()),
					new Label("Score: " + StatusData.databaseHelper.calculateReviewerScore(reviewer.getId()))
					);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		root.setTop(header);
		
		// Experience Section
		TextArea expArea = new TextArea(experience);
		expArea.setWrapText(true);
		
		Button saveButton = new Button("Save Experience");
		
		saveButton.setOnAction(e -> {
			try {
				db.updateReviewerExperience(reviewerId, expArea.getText());
				showAlert(Alert.AlertType.INFORMATION, "Saved", "Experience updated.");
			} catch (SQLException ex) {
				showAlert(Alert.AlertType.ERROR, "Database Error", ex.getMessage());
			}
		});
		
		expArea.setEditable(canEdit);
		saveButton.setDisable(!canEdit);
		
		VBox expBox = new VBox(8, new Label("Experience:"), expArea, saveButton);
		expBox.setPadding(new Insets(10));
		
		// Reviews Table
		TableView<Review> table = new TableView<>();
		table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		
		TableColumn<Review, Integer> idCol = new TableColumn<>("ID");
		idCol.setCellValueFactory(new PropertyValueFactory<>("reviewId"));
		
		TableColumn<Review, String> authorCol = new TableColumn<>("Author");
		authorCol.setCellValueFactory(new PropertyValueFactory<>("author"));
		
		TableColumn<Review, String> contentCol = new TableColumn<>("Review Content");
		contentCol.setCellValueFactory(new PropertyValueFactory<>("content"));
		
		table.getColumns().addAll(idCol, authorCol, contentCol);
		table.getItems().addAll(reviews);
		
		VBox reviewBox = new VBox(10, new Label("Reviews Written:"), table);
		
		VBox center = new VBox(20, expBox, reviewBox);
		ScrollPane scroll = new ScrollPane(center);
		scroll.setFitToWidth(true);
		
		root.setCenter(scroll);;
		Scene scene = new Scene(root, 700, 600);
		stage.setScene(scene);
		stage.showAndWait();
	}
	
	private static void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

	// Helper for logic testing
	public static boolean canEditProfile(User currentUser, User reviewer) {
		if (currentUser == null || reviewer == null) return false;
		if (currentUser.getId() != reviewer.getId()) return false;
		
		var role = currentUser.getRole();
		return role == User.Role.REVIEWER || role == User.Role.ADMIN;
	}
	
}
