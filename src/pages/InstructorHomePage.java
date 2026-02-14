package pages;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;

import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.Objects;

import logic.*;
import model.*;
import databasePart1.DatabaseHelper;

/**********
 * The {@code InstructorHomePage} class represents the home page for instructors.
 * <p>
 * Instructors can view request from students who wants to become reviewer.
 * Instructors will accept or deny them based on questions and answers students has made.
 * This class manages requests views, questions and answer views through {@link DatabaseHelper}.
 * </p>
 * 
 * <p>
 * Key features:
 * <ul>
 *   <li>Display a list of users requesting reviewer roles.</li>
 *   <li>Accept or deny reviewer requests directly from a table.</li>
 *   <li>View questions and answers by selected users.</li>
 *   <li>Update account details via a separate page.</li>
 * </ul>
 * 
 */

public class InstructorHomePage {

    private TextArea questionDetails;

    private Stage stage;
    private User user;

    private final DatabaseHelper databaseHelper;
    private final User currentUser;
    
    private TableView<User> flaggedTable;
    private TableView<User> flaggedUserTable;
    
    /**********
     * Default constructor. Uses the currently logged-in user and database helper from {@link StatusData}.
     */
    public InstructorHomePage() {
        this(StatusData.databaseHelper, StatusData.currUser);
    }

    /**********
     * Constructor that specifies a database helper and the current user.
     *
     * @param databaseHelper the database helper for performing database operations
     * @param currentUser    the currently logged-in instructor
     */
    public InstructorHomePage(DatabaseHelper databaseHelper, User currentUser) {
        this.databaseHelper = databaseHelper;
        this.currentUser = currentUser;
    }


    /**********
     * Displays the instructor home page in stage.
     * Sets up reviewer request, and detailed tabs for questions and answers.
     *
     * @param primaryStage the primary stage to display the instructor page
     */
    public void show(Stage primaryStage) {
        this.stage = primaryStage;

        primaryStage.setTitle("Instructor Homepage");

        BorderPane borderPane = new BorderPane();
        borderPane.setTop(new NavigationBar());
        
        VBox sideBar = new VBox(15);
        sideBar.setPadding(new Insets(20, 15, 15, 15));
        sideBar.setStyle("-fx-background-color: #f4f4f4;");
        sideBar.setMinWidth(180);
        sideBar.setMaxWidth(180);
        sideBar.setPrefWidth(180);

        Button moderationBtn = new Button("🛠️ Moderation Center");
        moderationBtn.setWrapText(true);
        moderationBtn.setMaxWidth(Double.MAX_VALUE);
        moderationBtn.setOnAction(e -> new ModerationHandlingPage(databaseHelper, currentUser).show(stage)); 
        
        Button reviewerReqBtn = new Button("📝 Reviewer Requests");
        reviewerReqBtn.setWrapText(true);
        reviewerReqBtn.setMaxWidth(Double.MAX_VALUE);
        reviewerReqBtn.setOnAction(e -> new ReviewerRequestPage().show(stage)); 

        Button updateAccountButton = new Button("Update Account");
        updateAccountButton.setWrapText(true);
        updateAccountButton.setMaxWidth(Double.MAX_VALUE);
        updateAccountButton.setOnAction(e ->
            new UpdateAccountPage(databaseHelper, currentUser).show(stage)
        );
        
        sideBar.getChildren().addAll(moderationBtn, reviewerReqBtn, updateAccountButton);

        VBox headerArea = new VBox(10);
        headerArea.setAlignment(Pos.CENTER);
        
        Label instructorLabel = new Label("Hello, Instructor!");
        instructorLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        
        headerArea.getChildren().add(instructorLabel);

        Label sectionLabel = new Label("Users by Role");
        sectionLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        sectionLabel.setPadding(new Insets(10, 0, 5, 0));

        TabPane tabPane = new TabPane();
        tabPane.setStyle(
        	    "-fx-background-color: white;" +
        	    "-fx-border-color: #ccc;" +
        	    "-fx-border-radius: 5;" +
        	    "-fx-background-radius: 5;"
        );
        tabPane.setPrefSize(900, 600);
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        
        Tab allTab = new Tab("All Users");
        allTab.setContent(createUserTableFilteredBy(null));
        tabPane.getTabs().add(0, allTab);
        
        for (User.Role role : User.Role.values()) {
            Tab roleTab = new Tab(role.name());
            roleTab.setContent(createUserTableFilteredBy(role));
            tabPane.getTabs().add(roleTab);
        }

        flaggedTable = buildUserTableFilteredByFlags();
        Tab flaggedTab = new Tab("FLAGGED");
        flaggedTab.setContent(flaggedTable);
        tabPane.getTabs().add(flaggedTab);

        VBox tabContainer = new VBox(sectionLabel, tabPane);
        VBox.setVgrow(tabPane, Priority.ALWAYS);
        
        HBox.setHgrow(sideBar, Priority.NEVER);
        HBox.setHgrow(tabContainer, Priority.ALWAYS);
        
        HBox mainLayout = new HBox(15);
        mainLayout.setAlignment(Pos.TOP_LEFT);
        mainLayout.getChildren().addAll(sideBar, tabContainer);  // layout 

        VBox centerLayout = new VBox(10);
        centerLayout.setPadding(new Insets(20));
        centerLayout.getChildren().addAll(headerArea, mainLayout);
        
        borderPane.setCenter(centerLayout);

        StatusData.setScene(primaryStage, borderPane);
    }     
    
    /**********
     * Converts a list of {@link Question} objects into a string 
     *
     * @param questions the list of questions to display
     * @return a formatted string
     */
    //same thing from welcomelogin w/ small adjustments
    private String displayQuestionDetailsString(List<Question> questions) {
        if (questions == null || questions.isEmpty()) {
            return ("No questions made yet.");
        }
        StringBuilder sb = new StringBuilder();
        for (Question q: questions) {
        	sb.append("Question: ").append(q.getDescription()).append("\n");
        	sb.append("Author: ").append(q.getAuthor()).append("\n");
        	sb.append("Title: ").append(q.getTitle()).append("\n");
        	sb.append("Status: ").append(q.getStatusText()).append("\n");
        	sb.append("Timestamp: ").append(q.getTimestamp() != null ? q.getTimestamp() : "N/A").append("\n");
        }
        return sb.toString();
    }
    
    /**********
     * Converts a list of {@link Answer} objects into a string
     *
     * @param answers the list of answers to display
     * @return a formatted string 
     */
    //same thing from welcomelogin w/ small adjustments
    private String displayAnswersString(List<Answer> answers) {
        if (answers == null || answers.isEmpty()) {
            return "No answers yet.\n";
        }

        StringBuilder sb = new StringBuilder();
        for (Answer a : answers) {
            sb.append("Answer ID: ").append(a.getAnswerId()).append("\n");
            sb.append("  Author: ").append(a.getAuthor()).append("\n");
            sb.append("  Content: ").append(a.getContent()).append("\n");
            sb.append("  Timestamp: ").append(a.getTimestamp() != null ? a.getTimestamp() : "N/A").append("\n");
            sb.append("  ").append(a.isSolution() ? "[Solution]" : "[Answer]").append("\n");
            sb.append("\n");
        }
        return sb.toString();
    }
    
    private TableView<User> buildUserTableFilteredByFlags() {
    	TableView<User> flaggedUserTable = new TableView<>();
    	
    	  // ⚠️ Flag Column
        TableColumn<User, String> flagCol = new TableColumn<>("⚠️");
        flagCol.setCellValueFactory(data -> new SimpleStringProperty(""));
        flagCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (!empty) {
                    User user = getTableRow().getItem();
                    if (user != null && userHasModerationFlags(user.getId())) {
                        setText("⚠️");
                        setStyle("-fx-font-size: 16px;");
                    } else {
                        setText("");
                        setStyle("");
                    }
                } else {
                    setText(null);
                }
            }
        });

        // ID, Username, Email, Role, All Roles
        TableColumn<User, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<User, String> usernameCol = new TableColumn<>("Username");
        usernameCol.setCellValueFactory(new PropertyValueFactory<>("userName"));

        TableColumn<User, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));

        TableColumn<User, String> roleCol = new TableColumn<>("Current Role");
        roleCol.setCellValueFactory(new PropertyValueFactory<>("role"));

        TableColumn<User, String> allRolesCol = new TableColumn<>("All Roles");
        allRolesCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (!empty) {
                    User user = getTableRow().getItem();
                    if (user != null) {
                        try {
                            List<String> roles = databaseHelper.allUserRoles(user.getUserName());
                            setText(String.join(", ", roles));
                        } catch (SQLException e) {
                            setText("Error loading roles");
                        }
                    }
                } else {
                    setText(null);
                }
            }
        });

        // Get users who have flagged content
        List<User> allUsers = databaseHelper.getAllUsers();
        List<User> flaggedUsers = allUsers.stream()
            .filter(user -> userHasModerationFlags(user.getId()))
            .collect(Collectors.toList());

        ObservableList<User> users = FXCollections.observableArrayList(flaggedUsers);
        flaggedUserTable.setItems(users);
        flaggedUserTable.getColumns().addAll(flagCol, idCol, usernameCol, emailCol, roleCol, allRolesCol);

        // Optional double-click action
        flaggedUserTable.setRowFactory(tv -> {
            TableRow<User> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getClickCount() == 2) {
                    User user = row.getItem();
                    if (userHasModerationFlags(user.getId())) {
                        handleFlaggedUserClick(user); // If you want to reuse your flag popup
                    }
                }
            });
            return row;
        });

        return flaggedUserTable;
    }
    
    private TableView<User> createUserTableFilteredBy(User.Role filterRole) {
        TableView<User> userTable = new TableView<>();

        // ⚠️ Flag Indicator
        TableColumn<User, String> flagCol = new TableColumn<>("⚠️");
        flagCol.setCellValueFactory(data -> new SimpleStringProperty(""));
        flagCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (!empty) {
                    User user = getTableRow().getItem();
                    if (user != null && userHasModerationFlags(user.getId())) {
                        setText("⚠️");
                        setStyle("-fx-font-size: 16px; -fx-text-fill: red;");
                    } else {
                        setText("");
                        setStyle("");
                    }
                } else {
                    setText(null);
                }
            }
        });

        // ID Column
        TableColumn<User, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));

        // Username Column
        TableColumn<User, String> usernameCol = new TableColumn<>("Username");
        usernameCol.setCellValueFactory(new PropertyValueFactory<>("userName"));

        // Role Column
        TableColumn<User, String> roleCol = new TableColumn<>("Current Role");
        roleCol.setCellValueFactory(new PropertyValueFactory<>("role"));

        // All Roles Column
        TableColumn<User, String> allRolesCol = new TableColumn<>("All Roles");
        allRolesCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (!empty) {
                    User user = getTableRow().getItem();
                    if (user != null) {
                        try {
                            List<String> roles = databaseHelper.allUserRoles(user.getUserName());
                            setText(String.join(", ", roles));
                        } catch (SQLException e) {
                            setText("Error loading roles");
                        }
                    }
                } else {
                    setText(null);
                }
            }
        });

        // Highlight Row if flagged (light red background)
        userTable.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(User user, boolean empty) {
                super.updateItem(user, empty);
                if (user == null || empty) {
                    setStyle("");
                } else if (userHasModerationFlags(user.getId())) {
                    setStyle("-fx-background-color: #ffe6e6;"); // Light red for flagged users
                } else {
                    setStyle("");
                }
            }
        });

        // Populate data
        List<User> allUsers = databaseHelper.getAllUsers();
        ObservableList<User> users = FXCollections.observableArrayList(
            filterRole == null
                ? allUsers
                : allUsers.stream().filter(u -> u.getRole() == filterRole).collect(Collectors.toList())
        );
        userTable.setItems(users);

        userTable.getColumns().addAll(flagCol, idCol, usernameCol, roleCol, allRolesCol);

        return userTable;
    }

    public boolean userHasModerationFlags(int userId) {
        try {
            List<ModerationFlag> allFlags = databaseHelper.loadAllModerationFlags();
            for (ModerationFlag flag : allFlags) {
                int checkId = databaseHelper.getUserIdForFlag(flag);
                System.out.println("DEBUG flag: flagId=" + flag.getFlagId()
                + ", userId=" + userId
                + ", authorId=" + userId
                + ", itemType=" + flag.getItemType()
                + ", status=" + flag.getStatus());
                if (userId == checkId) {
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void handleFlaggedUserClick(User user) {
        try {
            List<ModerationFlag> allFlags = databaseHelper.loadAllModerationFlags();
            List<ModerationFlag> userFlags = allFlags.stream()
                .filter(flag -> {
                    Integer authorId = databaseHelper.getAuthorIdForFlag(flag);
                    return authorId != null && authorId == user.getId();
                })
                .toList();

            if (userFlags.isEmpty()) {
                showAlert(Alert.AlertType.INFORMATION, "No Active Flags", "This content has already been resolved or does not belong to this user.");
                return;
            }

            Stage popup = new Stage();
            popup.setTitle("Moderation Flags for: " + user.getUserName());

            VBox layout = new VBox(10);
            layout.setStyle("-fx-padding: 15;");

            Label info = new Label("Flags related to this user's content:");
            info.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

            StringBuilder flagSummary = new StringBuilder();
            for (ModerationFlag flag : userFlags) {
                flagSummary.append(String.format(
                    "Item: %s #%d\nReason: %s\nStatus: %s\nDate: %s\n\n",
                    flag.getItemType(),
                    flag.getItemId(),
                    flag.getReason(),
                    flag.getStatus(),
                    flag.getCreatedAt()
                ));
            }

            Label flagDetails = new Label(flagSummary.toString());
            flagDetails.setWrapText(true);
            
            Button resolveButton = new Button("Mark All Resolved");
            resolveButton.setStyle("-fx-background-color: red; -fx-text-fill: white; -fx-font-weight: bold;");
            resolveButton.setOnAction(e -> {
                try {
                    for (ModerationFlag flag : userFlags) {
                        databaseHelper.updateModerationFlagStatus(flag.getFlagId(), "resolved");
                    }
                    popup.close();
                    refreshFlaggedTable();
                    refreshUsers(); // refresh UI if needed
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to update flag status.");
                }
            });

            layout.getChildren().addAll(info, flagDetails, resolveButton);
            popup.setScene(new Scene(layout, 450, 350));
            popup.show();
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to fetch moderation flag details.");
        }
    }
    
    public void refreshUsers() {
   	 if (this.stage != null) {
   	        show(this.stage);
   	    }
    }
   
    public void refreshFlaggedTable() {
       if (flaggedTable != null) {
    	   try {
               List<ModerationFlag> allFlags = databaseHelper.loadAllModerationFlags();

               Set<Integer> flaggedUserIds = allFlags.stream()
                   .map(flag -> databaseHelper.getAuthorIdForFlag(flag))
                   .filter(Objects::nonNull)
                   .collect(Collectors.toSet());

               List<User> allUsers = databaseHelper.getAllUsers();
               List<User> flaggedUsers = allUsers.stream()
                   .filter(u -> flaggedUserIds.contains(u.getId()))
                   .collect(Collectors.toList());

               ObservableList<User> users = FXCollections.observableArrayList(flaggedUsers);
               flaggedTable.setItems(users);
               flaggedTable.refresh();
           } catch (SQLException e) {
               e.printStackTrace();
           }
       }
    }

	private void showAlert(AlertType error, String string, String string2) {
		// TODO Auto-generated method stub
		
	}
}
