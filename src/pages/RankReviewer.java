package pages;

import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import logic.StatusData;
import model.*;
import model.User.Role;
import pages.ReviewerProfilePage;

import java.io.ObjectInputFilter.Status;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;


/**
 * JavaFX GUI page that allows students to see their reviewers in a ranking order.
 * Instructors have a special view where they can choose how reviewers are scored by.
 * Integrates with {@link databasePart1.DatabaseHelper}
 * for database operations and displays ratings and associated reviews.
 */
public class RankReviewer {
	
	private Stage stage;
	private User user;
	
	private TextField addUserField;
	private TableView<TrustedReviewerRow> allReviewersTable = new TableView<>();
	private TableView<TrustedReviewerRow>  trustedTable = new TableView<>();
	private List<Integer> trustedIds = new ArrayList<>();
	private TableView<TrustedReviewDisplayRow> trustedReviewsTable = new TableView<>();
	
	@SuppressWarnings("unchecked")
	public void show(Stage stage) {
		this.stage = stage;
		this.user = StatusData.currUser;

		stage.setTitle("Ranking of All Reviewers");
		
		BorderPane mainPane = new BorderPane();
		
		// Navigation Bar
		NavigationBar navBar = new NavigationBar();
		mainPane.setTop(navBar);
		
		// Center content
		BorderPane content = new BorderPane();
		content.setPadding(new Insets(15));
		
		// Left: Manage Trusted Reviewers
		VBox manageBox = new VBox(6);
		manageBox.setPadding(new Insets(6, 10, 6, 10));
		manageBox.setStyle(
				"-fx-background-color: #e3f2fd; " +
				"-fx-border-color: #2196f3; -fx-border-width: 1; " +
				"-fx-border-radius: 5; -fx-background-radius: 5;"
		);
		manageBox.setMaxWidth(Double.MAX_VALUE);
		
		Label manageTitle = new Label("Reviewer Rankings");
		manageTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
		
		Label addLabel = new Label("Search reviewer:");
		addUserField = new TextField();
		addUserField.setPromptText("Enter reviewer username...");
		addUserField.setPrefWidth(220);
		
		Button addButton = new Button("Search");
		addButton.setOnAction(_ -> searchReviewer());
		
		HBox addRow = new HBox(8, addLabel, addUserField, addButton);
		addRow.setAlignment(Pos.CENTER_LEFT);
		
		allReviewersTable.setPlaceholder(new Label("No reviewers found."));
		allReviewersTable.setPrefHeight(300);
		allReviewersTable.setMaxHeight(Region.USE_PREF_SIZE);
		allReviewersTable.setFixedCellSize(30);
		
		TableColumn<TrustedReviewerRow, String> tUserCol = new TableColumn<>("Username");
		tUserCol.setPrefWidth(100);
		tUserCol.setCellValueFactory(new PropertyValueFactory<>("userName"));
		
		TableColumn<TrustedReviewerRow, String> tNameCol = new TableColumn<>("Name");
		tNameCol.setPrefWidth(120);
		tNameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
		
		TableColumn<TrustedReviewerRow, Void> tProfileCol = new TableColumn<>("Reviewer Profile");
		tProfileCol.setPrefWidth(110);
		tProfileCol.setCellFactory(col -> new TableCell<>() {
			private final Button viewButton = new Button("View Profile");
			
			{
				viewButton.setOnAction(e -> {
					TrustedReviewerRow reviewer = getTableView().getItems().get(getIndex());
					ReviewerProfilePage.showReviewerProfile(reviewer.getReviewerId());
				});
				viewButton.setStyle("-fx-background-color: #1976d2; -fx-text-fill: white;");
			}
			
			@Override
			protected void updateItem(Void item, boolean empty) {
				super.updateItem(item, empty);
				if (empty) {
					setGraphic(null);
				} else {
					setGraphic(viewButton);
				}
			}
		});
		
		TableColumn<TrustedReviewerRow, String> scoreCol = new TableColumn<> ("Score");
		scoreCol.setCellValueFactory(new PropertyValueFactory<>("score"));
		
		allReviewersTable.getColumns().addAll(tUserCol, tNameCol, scoreCol, tProfileCol);
		manageBox.getChildren().addAll(manageTitle, addRow, allReviewersTable);
		
		
		// Layout
		HBox topRow = new HBox(15, manageBox);
		HBox.setHgrow(manageBox, Priority.ALWAYS);
		
		VBox centerContent = new VBox(12, topRow);
		centerContent.setPadding(new Insets(10));
		VBox.setVgrow(centerContent, Priority.NEVER);
		
		// instructor view 
				if (StatusData.currUser.getRole() == Role.INSTRUCTOR) {
					
					Label instructorLabel = new Label("How should reviewers score be calculated?");
					instructorLabel.setStyle(
						    "-fx-font-size: 15px;"
						  + "-fx-font-weight: bold;"
						  + "-fx-text-fill: #2c3e50;"
						  + "-fx-padding: 4 0 4 0;"
						);
					
				    ToggleGroup scoringToggleGroup = new ToggleGroup();

				    RadioButton averageRadio = new RadioButton("Average of Ratings");
				    averageRadio.setToggleGroup(scoringToggleGroup);
				    averageRadio.setStyle(
				    	    "-fx-font-size: 14px;"
				    	  + "-fx-text-fill: #2c3e50;"
				    	  + "-fx-padding: 5 0 5 0;"
				    	);
				    
				    RadioButton sumRadio = new RadioButton("Highest Sum of Ratings");
				    sumRadio.setToggleGroup(scoringToggleGroup);
				    sumRadio.setStyle(
				    	    "-fx-font-size: 14px;"
				    	  + "-fx-text-fill: #2c3e50;"
				    	  + "-fx-padding: 5 0 5 0;"
				    	);
				    String method;
					try {
						method = StatusData.databaseHelper.getInstructorScoringMethod().toUpperCase();
						if (method.equals("AVERAGE")) averageRadio.setSelected(true);
					    else sumRadio.setSelected(true);
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				    // update db for changes
				    scoringToggleGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
				        if (newToggle != null) {
				            RadioButton selected = (RadioButton) newToggle;
				            String newMethod = selected == averageRadio ? "AVERAGE" : "HIGHEST_SUM";
				            try {
				                StatusData.databaseHelper.updateInstructorScoringMethod(newMethod);
				                reloadTrustedDataSetting();
				            } catch (SQLException e) {
				                e.printStackTrace();
				            }
				        }
				    });
				    VBox instructorBox = new VBox(averageRadio, sumRadio);
				    instructorBox.setStyle(
							"-fx-background-color: #e3f2fd; " +
							"-fx-border-color: #2196f3; -fx-border-width: 1; " +
							"-fx-border-radius: 15; -fx-background-radius: 15;"
					);
				    instructorBox.setSpacing(6);
				    centerContent.getChildren().addAll(instructorLabel, instructorBox); 
				}
				
				reloadReviewerRankings(); //shows the scores in console for debugging 
				reloadTrustedDataSetting(); //shows reviewers 
		
		ScrollPane scrollPane = new ScrollPane(centerContent);
		scrollPane.setFitToWidth(true);
		scrollPane.setStyle("-fx-background-color: transparent;");
		
		content.setCenter(scrollPane);
		mainPane.setCenter(content);
//		
//		Scene scene = new Scene(mainPane, StatusData.WINDOW_WIDTH, StatusData.WINDOW_HEIGHT);
//		stage.setScene(scene);
//		stage.show();
		
		StatusData.setScene(stage, mainPane);
		
	}
	
	/**
     * Shows an alert dialog with the specified type, title, and message.
     *
     * @param type the type of the alert
     * @param title the title of the alert window
     * @param message the message content
     */
	private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
	
	/**
     * Searches for reviewers based on the input field.
     */
	//modified to search reviewer
	private void searchReviewer() {
		String searchText = addUserField.getText() == null ? "" : addUserField.getText().trim();
		if(searchText.isEmpty()) {
			showAlert(Alert.AlertType.WARNING, "Validation", "Please enter a reviewer username.");
			return;
		}
		try {
			List<User>reviewers = StatusData.databaseHelper.getUsersByRole(User.Role.REVIEWER);
			
			String lower = searchText.toLowerCase();
			List<User> matches = reviewers.stream()
					.filter(u -> {
						String un = u.getUserName() == null ? "" : u.getUserName().toLowerCase();
						String nm = u.getName() == null ? "" : u.getName().toLowerCase();
						return un.contains(lower) || nm.contains(lower);
					}).toList();
			
			if (matches.isEmpty()) {
				showAlert(Alert.AlertType.INFORMATION, "No Match", "No reviewers found matching: \"" + searchText + "\"");
				return;
			}
			
			matches = matches.stream()
					.filter(u -> u.getId() != user.getId()).toList();
			
			if (matches.isEmpty()) {
				showAlert(Alert.AlertType.ERROR, "Invalid", "You cannot add yourself as a trusted reviewer.");
				return;
			}
			
			if (matches.size() == 1) {
				User reviewer = matches.get(0);
				String namePart = reviewer.getName() != null && !reviewer.getName().isEmpty()
						? reviewer.getName() + " (" + reviewer.getUserName() + ")"
						: reviewer.getUserName();
				
				showAlert(Alert.AlertType.INFORMATION, "Reviewer Found", "Reviewer found: " + namePart);
			} else {
				//showReviewerSelectionPopup(matches);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			showAlert(Alert.AlertType.ERROR, "Database Error", ex.getMessage());
		}
	}
	
	/**
     * Reloads all reviewer rankings and prints them to console for debugging.
     */
	//this helps me keep track of the scores from diff users
	private void reloadReviewerRankings() {
	    try {
	        List<TrustedReviewerRow> reviewers = StatusData.databaseHelper.getAllReviewerScores();

	        // debug
	        System.out.println("=== Loading All Reviewer Scores ===");
	        for (TrustedReviewerRow r : reviewers) {
	           // System.out.println(r.getUserName() + " (" + r.getReviewerId() + "): " + r.getRating());
	            System.out.println(r.getUserName() + " (" + r.getReviewerId() + "): score = " + r.getScore());
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
	
	/**
     * Reloads the reviewer rankings according to the instructor's selected scoring method.
     */
	private void reloadTrustedDataSetting() {
	    try {
	        List<User> allReviewers = StatusData.databaseHelper.getUsersByRole(User.Role.REVIEWER);
	        List<TrustedReviewerRow> rows = new ArrayList<>();

	        // Get instructor scoring method
	        String method = StatusData.databaseHelper.getInstructorScoringMethod().toUpperCase();
	        System.out.println("=== Reloading Reviewer Scores using method: " + method + " ===");

	        for (User u : allReviewers) {
	            int score = StatusData.databaseHelper.calculateReviewerScore(u.getId());
	            rows.add(new TrustedReviewerRow(u.getId(), u.getUserName(), u.getName(), score));
	            System.out.println("Reviewer: " + u.getUserName() + " (" + u.getId() + "), Score = " + score);
	        }
	        rows.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));
	        // Update the table
	        allReviewersTable.getItems().setAll(rows);

	        System.out.println("=== Reload Complete ===");
	    } catch (Exception ex) {
	        ex.printStackTrace();
	        allReviewersTable.getItems().clear();
	        trustedTable.getItems().clear();
	    }
	}

	/**
     * Represents a row in the trusted reviews display table.
     */
	public static class ReviewRow {
		private final String reviewerUserName;
		private final int rating;
		private final int answerId;
		private final String answerText;
		
		public ReviewRow(String reviewerUserName, int rating, int answerId, String answerText) {
			this.reviewerUserName = reviewerUserName;
			this.rating = rating;
			this.answerId = answerId;
			this.answerText = answerText;
		}
		
		public String getReviewerUserName() { return reviewerUserName; }
		public int getRating() { return rating; }
		public int getAnswerId() { return answerId; }
		public String getAnswerText() { return answerText; }
	}
	
	/**
     * Represents a row in the trusted reviewer table including reviewer ID, username, name, rating, and score.
     */
	public static class TrustedReviewerRow {
		private final int reviewerId;
		private final String userName;
		private final String name;
		private int rating;
		private double score;
		
		public TrustedReviewerRow(int reviewerId, String userName, String name, double score) {
			this.reviewerId = reviewerId;
			this.userName = userName;
			this.name = name;
			this.score = score;
		}


		public int getReviewerId() { return reviewerId; }
		public String getUserName() { return userName; }
		public String getName() { return name; }
		public int getRating() { return rating; }
		public double getScore() {return score;}
		public void setRating(int rating) { this.rating = rating; }
		public void setScore() {this.score = score; }
	}
	
	/**
     * Represents a row for displaying detailed review information.
     */
	public static class TrustedReviewDisplayRow {
		private final int reviewId;
		private final String reviewerName;
		private final int rating;
		private final int answerId;
		private final String contentPreview;
		private final String fullContent;
		
		public TrustedReviewDisplayRow(int reviewId, String reviewerName, int rating, int answerId, String contentPreview, String fullContent) {
			this.reviewId = reviewId;
			this.reviewerName = reviewerName;
			this.rating = rating;
			this.answerId = answerId;
			this.contentPreview = contentPreview;
			this.fullContent = fullContent;
		}
		
		public int getReviewId() { return reviewId; }
		public String getReviewerName() { return reviewerName; }
		public int getRating() { return rating; }
		public int getAnswerId() { return answerId; }
		public String getContentPreview() { return contentPreview; }
		public String getFullContent() {return fullContent; }
	}
	
}
