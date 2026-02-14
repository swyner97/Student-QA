package pages;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import java.util.List;
import logic.*;
import model.*;

/**
 * AdminRequestsPage - Enhanced with category selection and notifications
 * Manages admin requests from instructors with real-time notifications
 * Now includes reopen functionality for both instructors and admins
 */
public class AdminRequestsPage {
    private Stage stage;
    private User user;
    
    private TableView<AdminRequest> openRequestsTable;
    private TableView<AdminRequest> closedRequestsTable;
    private TextArea requestDetails;
    private ListView<String> actionsList;
    
    private AdminRequest selectedRequest;
    
    public void show(Stage stage, User user) {
        this.stage = stage;
        this.user = user;
        
        // Check permissions
        if (!canAccessPage()) {
            showAlert(Alert.AlertType.ERROR, "Access Denied", 
                     "Only instructors, staff, and admins can access admin requests.");
            return;
        }
        
        stage.setTitle("Admin Requests");
        
        BorderPane mainPane = new BorderPane();
        
        // Navigation bar
        NavigationBar navBar = new NavigationBar();
        mainPane.setTop(navBar);
        
        // Center content
        BorderPane content = new BorderPane();
        content.setPadding(new Insets(15));
        
        // Main content area with tabs
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        
        // Tab 1: Open Requests
        Tab openTab = new Tab("📋 Open Requests", createOpenRequestsTab());
        
        // Tab 2: Closed Requests
        Tab closedTab = new Tab("✅ Closed Requests", createClosedRequestsTab());
        
        tabPane.getTabs().addAll(openTab, closedTab);
        
        content.setCenter(tabPane);
        mainPane.setCenter(content);
        
//        Scene scene = new Scene(mainPane, StatusData.WINDOW_WIDTH, StatusData.WINDOW_HEIGHT);
//        stage.setScene(scene);
//        stage.show();
        StatusData.setScene(stage, mainPane);
    }
    
    private VBox createOpenRequestsTab() {
        VBox mainBox = new VBox(15);
        mainBox.setPadding(new Insets(15));
        
        // Header with Create Request button
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label title = new Label("Open Admin Requests");
        title.setStyle(
            "-fx-font-size: 20px; " +
            "-fx-font-weight: bold; " +
            "-fx-text-fill: #2c3e50;"
        );
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button createRequestBtn = new Button("+ New Request");
        createRequestBtn.setStyle(
            "-fx-background-color: #198754; " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: 600; " +
            "-fx-padding: 10 20; " +
            "-fx-background-radius: 8; " +
            "-fx-font-size: 13px; " +
            "-fx-cursor: hand; " +
            "-fx-effect: dropshadow(gaussian, rgba(25,135,84,0.3), 6, 0, 0, 2);"
        );
        
        // Only instructors can create requests
        if (user.getRole() != User.Role.INSTRUCTOR) {
            createRequestBtn.setDisable(true);
            createRequestBtn.setTooltip(new Tooltip("Only instructors can create requests"));
        }
        
        createRequestBtn.setOnAction(e -> showCreateRequestDialog());
        
        header.getChildren().addAll(title, spacer, createRequestBtn);
        
        // Table
        openRequestsTable = new TableView<>();
        openRequestsTable.setPrefHeight(300);
        openRequestsTable.setFixedCellSize(50);
        openRequestsTable.setStyle(
            "-fx-background-color: #ffffff; " +
            "-fx-border-color: #dee2e6; " +
            "-fx-border-width: 1; " +
            "-fx-border-radius: 10; " +
            "-fx-background-radius: 10; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 12, 0, 0, 3);"
        );
        
        setupOpenRequestsTable();
        
        // Details section
        VBox detailsBox = createDetailsSection();
        
        mainBox.getChildren().addAll(header, openRequestsTable, detailsBox);
        
        loadOpenRequests();
        
        return mainBox;
    }
    
    private VBox createClosedRequestsTab() {
        VBox mainBox = new VBox(15);
        mainBox.setPadding(new Insets(15));
        
        Label title = new Label("Closed Admin Requests");
        title.setStyle(
            "-fx-font-size: 20px; " +
            "-fx-font-weight: bold; " +
            "-fx-text-fill: #2c3e50;"
        );
        
        // Table
        closedRequestsTable = new TableView<>();
        closedRequestsTable.setPrefHeight(400);
        closedRequestsTable.setFixedCellSize(50);
        closedRequestsTable.setStyle(
            "-fx-background-color: #ffffff; " +
            "-fx-border-color: #dee2e6; " +
            "-fx-border-width: 1; " +
            "-fx-border-radius: 10; " +
            "-fx-background-radius: 10; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 12, 0, 0, 3);"
        );
        
        setupClosedRequestsTable();
        
        // Details section
        VBox detailsBox = createClosedDetailsSection();
        
        mainBox.getChildren().addAll(title, closedRequestsTable, detailsBox);
        
        loadClosedRequests();
        
        return mainBox;
    }
    
    private void setupOpenRequestsTable() {
        // ID Column
        TableColumn<AdminRequest, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("requestId"));
        idCol.setPrefWidth(60);
        
        // Requestor Column
        TableColumn<AdminRequest, String> requestorCol = new TableColumn<>("Requestor");
        requestorCol.setCellValueFactory(new PropertyValueFactory<>("requestorName"));
        requestorCol.setPrefWidth(120);
        
        // Category Column
        TableColumn<AdminRequest, String> categoryCol = new TableColumn<>("Category");
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("category"));
        categoryCol.setCellFactory(column -> new TableCell<AdminRequest, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label badge = new Label(item);
                    badge.setPadding(new Insets(4, 12, 4, 12));
                    badge.setStyle(getCategoryStyle(item));
                    setGraphic(badge);
                    setText(null);
                    setAlignment(Pos.CENTER);
                }
            }
        });
        categoryCol.setPrefWidth(150);
        
        // Title Column
        TableColumn<AdminRequest, String> titleCol = new TableColumn<>("Title");
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        titleCol.setPrefWidth(250);
        
        // Date Column
        TableColumn<AdminRequest, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
        dateCol.setPrefWidth(150);
        
     // Actions Column (for admins only)
        TableColumn<AdminRequest, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setPrefWidth(200);
        actionsCol.setCellFactory(param -> new TableCell<AdminRequest, Void>() {
            private final Button addActionBtn = new Button("Add Action");
            private final Button closeBtn = new Button("Close");
            private final HBox actionBox = new HBox(5, addActionBtn, closeBtn);
            
            {
                addActionBtn.setStyle(
                    "-fx-background-color: #0d6efd; " +
                    "-fx-text-fill: white; " +
                    "-fx-font-size: 11px; " +
                    "-fx-font-weight: 600; " +
                    "-fx-padding: 6 12; " +
                    "-fx-background-radius: 6;"
                );
                
                closeBtn.setStyle(
                    "-fx-background-color: #198754; " +
                    "-fx-text-fill: white; " +
                    "-fx-font-size: 11px; " +
                    "-fx-font-weight: 600; " +
                    "-fx-padding: 6 12; " +
                    "-fx-background-radius: 6;"
                );
                
                addActionBtn.setOnAction(e -> {
                    // ⭐ FIX: Add bounds check
                    if (getIndex() >= 0 && getIndex() < getTableView().getItems().size()) {
                        AdminRequest request = getTableView().getItems().get(getIndex());
                        if (request != null) {
                            showAddActionDialog(request);
                        }
                    }
                });
                
                closeBtn.setOnAction(e -> {
                    // ⭐ FIX: Add bounds check
                    if (getIndex() >= 0 && getIndex() < getTableView().getItems().size()) {
                        AdminRequest request = getTableView().getItems().get(getIndex());
                        if (request != null) {
                            closeRequest(request);
                        }
                    }
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || user.getRole() != User.Role.ADMIN) {
                    setGraphic(null);
                } else {
                    setGraphic(actionBox);
                }
            }
        });
        openRequestsTable.getColumns().addAll(idCol, requestorCol, categoryCol, titleCol, dateCol, actionsCol);
        
        openRequestsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectedRequest = newVal;
                displayRequestDetails(newVal);
            }
        });
    }
    
    private void setupClosedRequestsTable() {
        TableColumn<AdminRequest, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("requestId"));
        idCol.setPrefWidth(60);
        
        TableColumn<AdminRequest, String> requestorCol = new TableColumn<>("Requestor");
        requestorCol.setCellValueFactory(new PropertyValueFactory<>("requestorName"));
        requestorCol.setPrefWidth(120);
        
        // Category Column
        TableColumn<AdminRequest, String> categoryCol = new TableColumn<>("Category");
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("category"));
        categoryCol.setPrefWidth(150);
        
        TableColumn<AdminRequest, String> titleCol = new TableColumn<>("Title");
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        titleCol.setPrefWidth(250);
        
        TableColumn<AdminRequest, String> dateCol = new TableColumn<>("Closed Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
        dateCol.setPrefWidth(150);
        
     // Reopen button (for instructors and admins)
        TableColumn<AdminRequest, Void> reopenCol = new TableColumn<>("Reopen");
        reopenCol.setPrefWidth(100);
        reopenCol.setCellFactory(param -> new TableCell<AdminRequest, Void>() {
            private final Button reopenBtn = new Button("Reopen");
            
            {
                reopenBtn.setStyle(
                    "-fx-background-color: #ffc107; " +
                    "-fx-text-fill: #000; " +
                    "-fx-font-size: 11px; " +
                    "-fx-font-weight: 600; " +
                    "-fx-padding: 6 12; " +
                    "-fx-background-radius: 6;"
                );
                
                reopenBtn.setOnAction(e -> {
                    // ⭐ FIX: Add bounds check
                    if (getIndex() >= 0 && getIndex() < getTableView().getItems().size()) {
                        AdminRequest request = getTableView().getItems().get(getIndex());
                        if (request != null) {
                            showReopenDialog(request);
                        }
                    }
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                
                if (empty) {
                    setGraphic(null);
                } else {
                    User.Role role = user.getRole();
                    if (role == User.Role.INSTRUCTOR || role == User.Role.ADMIN) {
                        setGraphic(reopenBtn);
                    } else {
                        setGraphic(null);
                    }
                }
            }
        });
        
        closedRequestsTable.getColumns().addAll(idCol, requestorCol, categoryCol, titleCol, dateCol, reopenCol);
        
        closedRequestsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectedRequest = newVal;
                displayClosedRequestDetails(newVal);
            }
        });
    }
    
    private VBox createDetailsSection() {
        VBox detailsBox = new VBox(10);
        detailsBox.setPadding(new Insets(10));
        
        Label detailsTitle = new Label("Request Details");
        detailsTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        requestDetails = new TextArea();
        requestDetails.setEditable(false);
        requestDetails.setWrapText(true);
        requestDetails.setPrefRowCount(6);
        requestDetails.setPromptText("Select a request to view details...");
        
        Label actionsTitle = new Label("Actions Taken");
        actionsTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        
        actionsList = new ListView<>();
        actionsList.setPrefHeight(150);
        actionsList.setPlaceholder(new Label("No actions yet"));
        
        detailsBox.getChildren().addAll(detailsTitle, requestDetails, actionsTitle, actionsList);
        
        return detailsBox;
    }
    
    private VBox createClosedDetailsSection() {
        VBox detailsBox = new VBox(10);
        detailsBox.setPadding(new Insets(10));
        
        Label detailsTitle = new Label("Request Details");
        detailsTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        TextArea closedDetails = new TextArea();
        closedDetails.setEditable(false);
        closedDetails.setWrapText(true);
        closedDetails.setPrefRowCount(5);
        
        Label actionsTitle = new Label("Actions Taken");
        actionsTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        
        ListView<String> closedActionsList = new ListView<>();
        closedActionsList.setPrefHeight(150);
        
        closedRequestsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                StringBuilder details = new StringBuilder();
                details.append("Request ID: #").append(newVal.getRequestId()).append("\n");
                details.append("Requestor: ").append(newVal.getRequestorName()).append("\n");
                details.append("Category: ").append(newVal.getCategory()).append("\n");
                details.append("Title: ").append(newVal.getTitle()).append("\n");
                details.append("Date: ").append(newVal.getTimestamp()).append("\n\n");
                details.append("Description:\n").append(newVal.getDescription());
                
                closedDetails.setText(details.toString());
                
                closedActionsList.getItems().clear();
                for (AdminAction action : newVal.getActions()) {
                    closedActionsList.getItems().add(action.toString());
                }
            }
        });
        
        detailsBox.getChildren().addAll(detailsTitle, closedDetails, actionsTitle, closedActionsList);
        
        return detailsBox;
    }
    
    private void showCreateRequestDialog() {
        Stage dialog = new Stage();
        dialog.setTitle("Create Admin Request");
        dialog.initOwner(stage);
        
        VBox layout = new VBox(15);
        layout.setPadding(new Insets(20));
        
        Label infoLabel = new Label("Submit a request to administrators");
        infoLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        
        // Category Selection
        Label categoryLabel = new Label("Request Category:");
        categoryLabel.setStyle("-fx-font-weight: bold;");
        
        ComboBox<AdminRequest.Category> categoryCombo = new ComboBox<>();
        categoryCombo.getItems().addAll(AdminRequest.Category.values());
        categoryCombo.setValue(AdminRequest.Category.ACCOUNT_MANAGEMENT);
        categoryCombo.setPrefWidth(400);
        
        Label titleLabel = new Label("Title:");
        titleLabel.setStyle("-fx-font-weight: bold;");
        TextField titleField = new TextField();
        titleField.setPromptText("Brief title for your request...");
        titleField.setPrefWidth(400);
        
        Label descLabel = new Label("Description:");
        descLabel.setStyle("-fx-font-weight: bold;");
        TextArea descArea = new TextArea();
        descArea.setPromptText("Provide detailed information about your request...");
        descArea.setPrefRowCount(6);
        descArea.setWrapText(true);
        descArea.setPrefWidth(400);
        
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        
        Button cancelBtn = new Button("Cancel");
        Button submitBtn = new Button("Submit Request");
        submitBtn.setStyle(
            "-fx-background-color: #198754; " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: bold;"
        );
        
        buttonBox.getChildren().addAll(cancelBtn, submitBtn);
        
        layout.getChildren().addAll(
            infoLabel, 
            categoryLabel, categoryCombo,
            titleLabel, titleField, 
            descLabel, descArea, 
            buttonBox
        );
        
        cancelBtn.setOnAction(e -> dialog.close());
        
        submitBtn.setOnAction(e -> {
            AdminRequest.Category category = categoryCombo.getValue();
            String title = titleField.getText().trim();
            String desc = descArea.getText().trim();
            
            if (title.isEmpty() || desc.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Missing Information", 
                         "Please provide both title and description.");
                return;
            }
            
            AdminRequest request = new AdminRequest(
                0, // ID will be auto-generated
                user.getId(),
                user.getName(),
                category.name(),
                title,
                desc
            );
            
            if (StatusData.databaseHelper.insertAdminRequest(request)) {
                showAlert(Alert.AlertType.INFORMATION, "Success", 
                         "Your request has been submitted! Admins will be notified.");
                dialog.close();
                loadOpenRequests();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", 
                         "Failed to submit request.");
            }
        });
        
        Scene scene = new Scene(layout);
        dialog.setScene(scene);
        dialog.show();
    }
    
    private void showAddActionDialog(AdminRequest request) {
        Stage dialog = new Stage();
        dialog.setTitle("Add Action to Request #" + request.getRequestId());
        dialog.initOwner(stage);
        
        VBox layout = new VBox(15);
        layout.setPadding(new Insets(20));
        
        Label infoLabel = new Label("Request: " + request.getTitle());
        infoLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        
        Label actionLabel = new Label("Action Taken:");
        TextArea actionArea = new TextArea();
        actionArea.setPromptText("Describe what action you took...");
        actionArea.setPrefRowCount(5);
        actionArea.setWrapText(true);
        actionArea.setPrefWidth(400);
        
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        
        Button cancelBtn = new Button("Cancel");
        Button submitBtn = new Button("Add Action");
        submitBtn.setStyle(
            "-fx-background-color: #0d6efd; " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: bold;"
        );
        
        buttonBox.getChildren().addAll(cancelBtn, submitBtn);
        
        layout.getChildren().addAll(infoLabel, actionLabel, actionArea, buttonBox);
        
        cancelBtn.setOnAction(e -> dialog.close());
        
        submitBtn.setOnAction(e -> {
            String actionDesc = actionArea.getText().trim();
            
            if (actionDesc.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Empty Action", 
                         "Please describe the action taken.");
                return;
            }
            
            AdminAction action = new AdminAction(
                0,
                request.getRequestId(),
                user.getId(),
                user.getName(),
                actionDesc
            );
            
            if (StatusData.databaseHelper.insertAdminAction(action)) {
                showAlert(Alert.AlertType.INFORMATION, "Success", 
                         "Action recorded! Requestor has been notified.");
                dialog.close();
                loadOpenRequests();
                
                if (selectedRequest != null && selectedRequest.getRequestId() == request.getRequestId()) {
                    AdminRequest updated = StatusData.databaseHelper.getAdminRequestById(request.getRequestId());
                    if (updated != null) {
                        displayRequestDetails(updated);
                    }
                }
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", 
                         "Failed to record action.");
            }
        });
        
        Scene scene = new Scene(layout);
        dialog.setScene(scene);
        dialog.show();
    }
    
    private void closeRequest(AdminRequest request) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Close Request");
        confirm.setHeaderText("Close Request #" + request.getRequestId() + "?");
        confirm.setContentText("Are you sure this request has been completed?");
        
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (StatusData.databaseHelper.closeAdminRequest(request.getRequestId(), user.getName())) {
                    showAlert(Alert.AlertType.INFORMATION, "Success", 
                             "Request closed! Requestor has been notified.");
                    loadOpenRequests();
                    loadClosedRequests();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", 
                             "Failed to close request.");
                }
            }
        });
    }
    
    // ⭐ ENHANCED: Reopen dialog now works for both instructors and admins
    private void showReopenDialog(AdminRequest closedRequest) {
        Stage dialog = new Stage();
        dialog.setTitle("Reopen Request #" + closedRequest.getRequestId());
        dialog.initOwner(stage);
        
        VBox layout = new VBox(15);
        layout.setPadding(new Insets(20));
        
        Label infoLabel = new Label("Reopening: " + closedRequest.getTitle());
        infoLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        
        // Show who is reopening
        Label reopenByLabel = new Label("Reopened by: " + user.getName() + " (" + user.getRole() + ")");
        reopenByLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");
        
        Label titleLabel = new Label("New Title (optional):");
        TextField titleField = new TextField();
        titleField.setPromptText("Leave blank to keep original title");
        titleField.setPrefWidth(400);
        
        Label descLabel = new Label("Updated Description:");
        descLabel.setStyle("-fx-font-weight: bold;");
        TextArea descArea = new TextArea();
        descArea.setText(closedRequest.getDescription());
        descArea.setPrefRowCount(6);
        descArea.setWrapText(true);
        descArea.setPrefWidth(400);
        
        // ⭐ NEW: Reason for reopening
        Label reasonLabel = new Label("Reason for Reopening:");
        reasonLabel.setStyle("-fx-font-weight: bold;");
        TextArea reasonArea = new TextArea();
        reasonArea.setPromptText("Explain why this request is being reopened...");
        reasonArea.setPrefRowCount(3);
        reasonArea.setWrapText(true);
        reasonArea.setPrefWidth(400);
        
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        
        Button cancelBtn = new Button("Cancel");
        Button reopenBtn = new Button("Reopen Request");
        reopenBtn.setStyle(
            "-fx-background-color: #ffc107; " +
            "-fx-text-fill: #000; " +
            "-fx-font-weight: bold;"
        );
        
        buttonBox.getChildren().addAll(cancelBtn, reopenBtn);
        
        layout.getChildren().addAll(
            infoLabel, 
            reopenByLabel,
            titleLabel, titleField, 
            descLabel, descArea,
            reasonLabel, reasonArea,
            buttonBox
        );
        
        cancelBtn.setOnAction(e -> dialog.close());
        
        reopenBtn.setOnAction(e -> {
            String newTitle = titleField.getText().trim();
            String newDesc = descArea.getText().trim();
            String reason = reasonArea.getText().trim();
            
            if (newDesc.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Missing Description", 
                         "Please provide a description.");
                return;
            }
            
            if (reason.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Missing Reason", 
                         "Please explain why you're reopening this request.");
                return;
            }
            
            if (newTitle.isEmpty()) {
                newTitle = closedRequest.getTitle();
            }
            
            // Append reason to description
            String fullDescription = newDesc + "\n\n--- REOPENED ---\n" +
                                   "Reopened by: " + user.getName() + " (" + user.getRole() + ")\n" +
                                   "Reason: " + reason;
            
            AdminRequest reopenedRequest = new AdminRequest(
                0,
                user.getId(),
                user.getName(),
                closedRequest.getCategory(),
                newTitle,
                fullDescription,
                "open",
                null,
                closedRequest.getRequestId(),
                false
            );
            
            if (StatusData.databaseHelper.insertAdminRequest(reopenedRequest)) {
                showAlert(Alert.AlertType.INFORMATION, "Success", 
                         "Request reopened! Admins have been notified.");
                dialog.close();
                loadOpenRequests();
                loadClosedRequests();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", 
                         "Failed to reopen request.");
            }
        });
        
        Scene scene = new Scene(layout);
        dialog.setScene(scene);
        dialog.show();
    }
    
    private void displayRequestDetails(AdminRequest request) {
        StringBuilder details = new StringBuilder();
        details.append("Request ID: #").append(request.getRequestId()).append("\n");
        details.append("Requestor: ").append(request.getRequestorName()).append("\n");
        details.append("Category: ").append(request.getCategory()).append("\n");
        details.append("Title: ").append(request.getTitle()).append("\n");
        details.append("Date: ").append(request.getTimestamp()).append("\n");
        
        if (request.isReopened()) {
            details.append("\n⚠️ This is a reopened request\n");
            details.append("Original Request ID: #").append(request.getOriginalRequestId()).append("\n");
        }
        
        details.append("\nDescription:\n");
        details.append(request.getDescription());
        
        requestDetails.setText(details.toString());
        
        actionsList.getItems().clear();
        for (AdminAction action : request.getActions()) {
            actionsList.getItems().add(action.toString());
        }
    }
    
    private void displayClosedRequestDetails(AdminRequest request) {
        // Handled in createClosedDetailsSection()
    }
    
    private void loadOpenRequests() {
        List<AdminRequest> requests = StatusData.databaseHelper.loadOpenAdminRequests();
        openRequestsTable.getItems().setAll(requests);
    }
    
    private void loadClosedRequests() {
        List<AdminRequest> requests = StatusData.databaseHelper.loadClosedAdminRequests();
        closedRequestsTable.getItems().setAll(requests);
    }
    
    private boolean canAccessPage() {
        User.Role role = user.getRole();
        return role == User.Role.INSTRUCTOR || 
               role == User.Role.STAFF || 
               role == User.Role.ADMIN;
    }
    
    private String getCategoryStyle(String category) {
        return switch (category.toUpperCase()) {
            case "ACCOUNT_MANAGEMENT" -> 
                "-fx-background-color: #e3f2fd; -fx-text-fill: #1976d2; " +
                "-fx-background-radius: 15; -fx-font-size: 11px; -fx-font-weight: bold;";
            case "PERMISSION_CHANGE" -> 
                "-fx-background-color: #fff3e0; -fx-text-fill: #e65100; " +
                "-fx-background-radius: 15; -fx-font-size: 11px; -fx-font-weight: bold;";
            case "TECHNICAL_ISSUE" -> 
                "-fx-background-color: #fce4ec; -fx-text-fill: #c2185b; " +
                "-fx-background-radius: 15; -fx-font-size: 11px; -fx-font-weight: bold;";
            case "CONTENT_MODERATION" -> 
                "-fx-background-color: #f3e5f5; -fx-text-fill: #7b1fa2; " +
                "-fx-background-radius: 15; -fx-font-size: 11px; -fx-font-weight: bold;";
            case "SYSTEM_CONFIGURATION" -> 
                "-fx-background-color: #e8f5e9; -fx-text-fill: #2e7d32; " +
                "-fx-background-radius: 15; -fx-font-size: 11px; -fx-font-weight: bold;";
            case "USER_SUPPORT" -> 
                "-fx-background-color: #e0f2f1; -fx-text-fill: #00695c; " +
                "-fx-background-radius: 15; -fx-font-size: 11px; -fx-font-weight: bold;";
            default -> 
                "-fx-background-color: #eceff1; -fx-text-fill: #455a64; " +
                "-fx-background-radius: 15; -fx-font-size: 11px; -fx-font-weight: bold;";
        };
    }
    
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}