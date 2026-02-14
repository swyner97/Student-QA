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


import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

/**
 * RequestsPage - Provides interface for staff members to create requests
 * and for administrators to manage and respond to those requests.
 * 
 * <p>This page supports a complete request workflow including:
 * <ul>
 *   <li>Request creation by staff members</li>
 *   <li>Request filtering by status (open, approved, denied, completed)</li>
 *   <li>Administrative actions (approve, deny, complete)</li>
 *   <li>Detailed request viewing with admin responses</li>
 * </ul>
 * 
 * <p>Access is restricted to staff and admin roles only.
 * 
 * @author Your Name
 * @version 1.0
 */
public class RequestsPage {
    /** The primary stage for displaying the page */
    private Stage stage;
    
    /** The currently logged-in user */
    private User user;
    
    /** Table view displaying all staff requests */
    private TableView<StaffRequest> requestsTable;
    
    /** Text area for displaying detailed request information */
    private TextArea requestDetails;
    
    /**
     * Displays the staff requests page with appropriate permissions checking.
     * 
     * @param stage the stage on which to display the page
     * @param user the currently logged-in user
     */
    public void show(Stage stage, User user) {
        this.stage = stage;
        this.user = user;
        
        // Check permissions - staff and admins only
        if (!canAccessPage()) {
            showAlert(Alert.AlertType.ERROR, "Access Denied", 
                     "Only staff and admins can access this page.");
            return;
        }
        
        stage.setTitle("Staff Requests");
        
        BorderPane mainPane = new BorderPane();
        
        // Navigation bar
        NavigationBar navBar = new NavigationBar();
        mainPane.setTop(navBar);
        
        // Center content
        BorderPane content = new BorderPane();
        content.setPadding(new Insets(15));
        
        VBox mainContent = createMainContent();
        
        ScrollPane scrollPane = new ScrollPane(mainContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");
        
        content.setCenter(scrollPane);
        mainPane.setCenter(content);
        
        Scene scene = new Scene(mainPane, StatusData.WINDOW_WIDTH, StatusData.WINDOW_HEIGHT);
        stage.setScene(scene);
        stage.show();
    }
    
    /**
     * Creates the main content area including header, filter buttons,
     * requests table, and details section.
     * 
     * @return VBox containing all main page components
     */
    private VBox createMainContent() {
        VBox mainBox = new VBox(15);
        mainBox.setPadding(new Insets(15));
        
        // Header
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label title = new Label("📝 Staff Requests");
        title.setStyle(
            "-fx-font-size: 20px; " +
            "-fx-font-weight: bold; " +
            "-fx-text-fill: #2c3e50;"
        );
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Create Request button (staff only)
        Button createRequestBtn = new Button("+ New Request");
        createRequestBtn.setStyle(
            "-fx-background-color: #9b59b6; " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: 600; " +
            "-fx-padding: 10 20; " +
            "-fx-background-radius: 8; " +
            "-fx-font-size: 13px; " +
            "-fx-cursor: hand; " +
            "-fx-effect: dropshadow(gaussian, rgba(155,89,182,0.3), 6, 0, 0, 2);"
        );
        
        if (user.getRole() != User.Role.STAFF) {
            createRequestBtn.setDisable(true);
            createRequestBtn.setTooltip(new Tooltip("Only staff members can create requests"));
        }
        
        createRequestBtn.setOnAction(e -> showCreateRequestDialog());
        
        header.getChildren().addAll(title, spacer, createRequestBtn);
        
        // Filter buttons
        HBox filterBox = new HBox(10);
        filterBox.setAlignment(Pos.CENTER_LEFT);
        
        Button allBtn = new Button("All Requests");
        Button openBtn = new Button("Open");
        Button approvedBtn = new Button("Approved");
        Button deniedBtn = new Button("Denied");
        Button completedBtn = new Button("Completed");
        
        String defaultStyle = 
            "-fx-background-color: #ecf0f1; " +
            "-fx-text-fill: #2c3e50; " +
            "-fx-padding: 8 15; " +
            "-fx-background-radius: 5; " +
            "-fx-cursor: hand;";
        
        String activeStyle = 
            "-fx-background-color: #9b59b6; " +
            "-fx-text-fill: white; " +
            "-fx-padding: 8 15; " +
            "-fx-background-radius: 5; " +
            "-fx-font-weight: bold;";
        
        allBtn.setStyle(activeStyle);
        openBtn.setStyle(defaultStyle);
        approvedBtn.setStyle(defaultStyle);
        deniedBtn.setStyle(defaultStyle);
        completedBtn.setStyle(defaultStyle);
        
        allBtn.setOnAction(e -> {
            allBtn.setStyle(activeStyle);
            openBtn.setStyle(defaultStyle);
            approvedBtn.setStyle(defaultStyle);
            deniedBtn.setStyle(defaultStyle);
            completedBtn.setStyle(defaultStyle);
            loadAllRequests();
        });
        
        openBtn.setOnAction(e -> {
            allBtn.setStyle(defaultStyle);
            openBtn.setStyle(activeStyle);
            approvedBtn.setStyle(defaultStyle);
            deniedBtn.setStyle(defaultStyle);
            completedBtn.setStyle(defaultStyle);
            filterByStatus("open");
        });
        
        approvedBtn.setOnAction(e -> {
            allBtn.setStyle(defaultStyle);
            openBtn.setStyle(defaultStyle);
            approvedBtn.setStyle(activeStyle);
            deniedBtn.setStyle(defaultStyle);
            completedBtn.setStyle(defaultStyle);
            filterByStatus("approved");
        });
        
        deniedBtn.setOnAction(e -> {
            allBtn.setStyle(defaultStyle);
            openBtn.setStyle(defaultStyle);
            approvedBtn.setStyle(defaultStyle);
            deniedBtn.setStyle(activeStyle);
            completedBtn.setStyle(defaultStyle);
            filterByStatus("denied");
        });
        
        completedBtn.setOnAction(e -> {
            allBtn.setStyle(defaultStyle);
            openBtn.setStyle(defaultStyle);
            approvedBtn.setStyle(defaultStyle);
            deniedBtn.setStyle(defaultStyle);
            completedBtn.setStyle(activeStyle);
            filterByStatus("completed");
        });
        
        filterBox.getChildren().addAll(allBtn, openBtn, approvedBtn, deniedBtn, completedBtn);
        
        // Table
        requestsTable = new TableView<>();
        requestsTable.setPrefHeight(350);
        requestsTable.setFixedCellSize(50);
        requestsTable.setStyle(
            "-fx-background-color: #ffffff; " +
            "-fx-border-color: #dee2e6; " +
            "-fx-border-width: 1; " +
            "-fx-border-radius: 10; " +
            "-fx-background-radius: 10; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 12, 0, 0, 3);"
        );
        
        setupTable();
        
        // Details section
        VBox detailsBox = createDetailsSection();
        
        mainBox.getChildren().addAll(header, filterBox, requestsTable, detailsBox);
        
        loadAllRequests();
        
        return mainBox;
    }
    
    /**
     * Configures the requests table with all necessary columns and cell factories.
     * Sets up columns for ID, staff member, type, title, status, date, and actions.
     * Includes custom cell renderers for badges and admin action buttons.
     */
    private void setupTable() {
        // ID Column
        TableColumn<StaffRequest, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("requestId"));
        idCol.setCellFactory(column -> new TableCell<StaffRequest, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText("#" + item);
                    setStyle("-fx-font-weight: bold; -fx-text-fill: #495057;");
                }
            }
        });
        idCol.setPrefWidth(60);
        
        // Staff Member Column
        TableColumn<StaffRequest, String> staffCol = new TableColumn<>("Staff Member");
        staffCol.setCellValueFactory(new PropertyValueFactory<>("staffName"));
        staffCol.setCellFactory(column -> new TableCell<StaffRequest, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText("👤 " + item);
                    setStyle("-fx-text-fill: #495057;");
                }
            }
        });
        staffCol.setPrefWidth(150);
        
        // Type Column
        TableColumn<StaffRequest, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("requestType"));
        typeCol.setCellFactory(column -> new TableCell<StaffRequest, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label badge = new Label(capitalizeFirst(item));
                    badge.setPadding(new Insets(4, 12, 4, 12));
                    badge.setStyle(
                        "-fx-background-color: #e3f2fd; " +
                        "-fx-text-fill: #1976d2; " +
                        "-fx-background-radius: 15; " +
                        "-fx-font-size: 11px; " +
                        "-fx-font-weight: bold;"
                    );
                    setGraphic(badge);
                    setText(null);
                    setAlignment(Pos.CENTER);
                }
            }
        });
        typeCol.setPrefWidth(120);
        
        // Title Column
        TableColumn<StaffRequest, String> titleCol = new TableColumn<>("Title");
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        titleCol.setCellFactory(column -> new TableCell<StaffRequest, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    setStyle("-fx-font-weight: 600; -fx-text-fill: #212529;");
                }
            }
        });
        titleCol.setPrefWidth(250);
        
        // Status Column
        TableColumn<StaffRequest, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setCellFactory(column -> new TableCell<StaffRequest, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label badge = new Label(capitalizeFirst(item));
                    badge.setPadding(new Insets(4, 12, 4, 12));
                    badge.setStyle(
                        "-fx-background-radius: 15; " +
                        "-fx-font-size: 11px; " +
                        "-fx-font-weight: bold; " +
                        getStatusStyle(item.toLowerCase())
                    );
                    setGraphic(badge);
                    setText(null);
                    setAlignment(Pos.CENTER);
                }
            }
            
            /**
             * Returns CSS styling based on request status.
             * 
             * @param status the status string (open, approved, denied, completed)
             * @return CSS style string for the status badge
             */
            private String getStatusStyle(String status) {
                switch (status) {
                    case "open":
                        return "-fx-background-color: #cfe2ff; -fx-text-fill: #084298;";
                    case "approved":
                        return "-fx-background-color: #d1e7dd; -fx-text-fill: #0f5132;";
                    case "denied":
                        return "-fx-background-color: #f8d7da; -fx-text-fill: #842029;";
                    case "completed":
                        return "-fx-background-color: #e2e3e5; -fx-text-fill: #41464b;";
                    default:
                        return "-fx-background-color: #fff3cd; -fx-text-fill: #997404;";
                }
            }
        });
        statusCol.setPrefWidth(100);
        
        // Date Column
        TableColumn<StaffRequest, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
        dateCol.setCellFactory(column -> new TableCell<StaffRequest, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText("🕐 " + item);
                    setStyle("-fx-text-fill: #6c757d; -fx-font-size: 11px;");
                }
            }
        });
        dateCol.setPrefWidth(180);
        
        // Actions Column (for admins only)
        TableColumn<StaffRequest, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setPrefWidth(250);
        actionsCol.setCellFactory(param -> new TableCell<StaffRequest, Void>() {
            private final Button approveBtn = new Button("Approve");
            private final Button denyBtn = new Button("Deny");
            private final Button completeBtn = new Button("Complete");
            private final HBox actionBox = new HBox(5, approveBtn, denyBtn, completeBtn);
            
            {
                approveBtn.setStyle(
                    "-fx-background-color: #198754; " +
                    "-fx-text-fill: white; " +
                    "-fx-font-size: 11px; " +
                    "-fx-font-weight: 600; " +
                    "-fx-padding: 6 12; " +
                    "-fx-background-radius: 6;"
                );
                
                denyBtn.setStyle(
                    "-fx-background-color: #dc3545; " +
                    "-fx-text-fill: white; " +
                    "-fx-font-size: 11px; " +
                    "-fx-font-weight: 600; " +
                    "-fx-padding: 6 12; " +
                    "-fx-background-radius: 6;"
                );
                
                completeBtn.setStyle(
                    "-fx-background-color: #6c757d; " +
                    "-fx-text-fill: white; " +
                    "-fx-font-size: 11px; " +
                    "-fx-font-weight: 600; " +
                    "-fx-padding: 6 12; " +
                    "-fx-background-radius: 6;"
                );
                
                approveBtn.setOnAction(e -> {
                    StaffRequest request = getTableView().getItems().get(getIndex());
                    handleRequest(request, "approved");
                });
                
                denyBtn.setOnAction(e -> {
                    StaffRequest request = getTableView().getItems().get(getIndex());
                    handleRequest(request, "denied");
                });
                
                completeBtn.setOnAction(e -> {
                    StaffRequest request = getTableView().getItems().get(getIndex());
                    handleRequest(request, "completed");
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }
                
                StaffRequest request = getTableView().getItems().get(getIndex());
                
                // Only show actions for admins and for open/approved requests
                if (user.getRole() == User.Role.ADMIN) {
                    if (request.isOpen()) {
                        // Show approve/deny for open requests
                        actionBox.getChildren().clear();
                        actionBox.getChildren().addAll(approveBtn, denyBtn);
                        setGraphic(actionBox);
                    } else if (request.isApproved()) {
                        // Show complete button for approved requests
                        actionBox.getChildren().clear();
                        actionBox.getChildren().add(completeBtn);
                        setGraphic(actionBox);
                    } else {
                        setGraphic(null);
                    }
                } else {
                    setGraphic(null);
                }
            }
        });
        
        requestsTable.getColumns().addAll(idCol, staffCol, typeCol, titleCol, statusCol, dateCol, actionsCol);
        
        requestsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                displayRequestDetails(newVal);
            }
        });
        
        // Row hover effect
        requestsTable.setRowFactory(tv -> {
            TableRow<StaffRequest> row = new TableRow<>();
            row.setOnMouseEntered(e -> {
                if (!row.isEmpty()) {
                    row.setStyle("-fx-background-color: #e7f3ff; -fx-cursor: hand;");
                }
            });
            row.setOnMouseExited(e -> {
                if (!row.isEmpty()) {
                    row.setStyle("");
                }
            });
            return row;
        });
    }
    
    /**
     * Creates the details section displaying full request information.
     * 
     * @return VBox containing the details title and text area
     */
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
        
        detailsBox.getChildren().addAll(detailsTitle, requestDetails);
        
        return detailsBox;
    }
    
    /**
     * Displays a dialog for staff members to create a new request.
     * Includes fields for request type, title, and description.
     */
    private void showCreateRequestDialog() {
        Stage dialog = new Stage();
        dialog.setTitle("Create Staff Request");
        dialog.initOwner(stage);
        
        VBox layout = new VBox(15);
        layout.setPadding(new Insets(20));
        
        Label infoLabel = new Label("Submit a request to administrators");
        infoLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        
        Label typeLabel = new Label("Request Type:");
        typeLabel.setStyle("-fx-font-weight: bold;");
        
        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll(
            "Account Management",
            "Permission Change",
            "System Configuration",
            "User Support",
            "Other"
        );
        typeCombo.setValue("Account Management");
        typeCombo.setPrefWidth(400);
        
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
            "-fx-background-color: #9b59b6; " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: bold;"
        );
        
        buttonBox.getChildren().addAll(cancelBtn, submitBtn);
        
        layout.getChildren().addAll(infoLabel, typeLabel, typeCombo, titleLabel, 
                                    titleField, descLabel, descArea, buttonBox);
        
        cancelBtn.setOnAction(e -> dialog.close());
        
        submitBtn.setOnAction(e -> {
            String type = typeCombo.getValue();
            String title = titleField.getText().trim();
            String desc = descArea.getText().trim();
            
            if (title.isEmpty() || desc.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Missing Information", 
                         "Please provide both title and description.");
                return;
            }
            
            StaffRequest request = new StaffRequest();
            request.setRequestId(0); // will be generated by DB
            request.setStaffId(user.getId());
            request.setStaffName(user.getName());
            request.setRequestType(type.toLowerCase().replace(" ", "_"));
            request.setTitle(title);
            request.setDescription(desc);
            request.setStatus("open");
            // set timestamp in same format as model expects
            String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            request.setTimestamp(ts);

            if (StatusData.databaseHelper.insertStaffRequest(request)) {
                showAlert(Alert.AlertType.INFORMATION, "Success", 
                         "Your request has been submitted to administrators!");
                dialog.close();
                loadAllRequests();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", 
                         "Failed to submit request.");
            }
        });
        
        Scene scene = new Scene(layout);
        dialog.setScene(scene);
        dialog.show();
    }
    
    /**
     * Handles admin actions on a request (approve, deny, or complete).
     * Displays a dialog for the admin to provide a response.
     * 
     * @param request the request to handle
     * @param newStatus the new status to set (approved, denied, or completed)
     */
    private void handleRequest(StaffRequest request, String newStatus) {
        Stage dialog = new Stage();
        dialog.setTitle(capitalizeFirst(newStatus) + " Request");
        dialog.initOwner(stage);
        
        VBox layout = new VBox(15);
        layout.setPadding(new Insets(20));
        
        Label infoLabel = new Label("Request: " + request.getTitle());
        infoLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        
        Label responseLabel = new Label("Admin Response:");
        responseLabel.setStyle("-fx-font-weight: bold;");
        
        TextArea responseArea = new TextArea();
        responseArea.setPromptText("Enter your response to the staff member...");
        responseArea.setPrefRowCount(5);
        responseArea.setWrapText(true);
        responseArea.setPrefWidth(400);
        
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        
        Button cancelBtn = new Button("Cancel");
        Button confirmBtn = new Button("Confirm " + capitalizeFirst(newStatus));
        
        String btnColor = newStatus.equals("approved") ? "#198754" : 
                         newStatus.equals("denied") ? "#dc3545" : "#6c757d";
        
        confirmBtn.setStyle(
            "-fx-background-color: " + btnColor + "; " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: bold;"
        );
        
        buttonBox.getChildren().addAll(cancelBtn, confirmBtn);
        
        layout.getChildren().addAll(infoLabel, responseLabel, responseArea, buttonBox);
        
        cancelBtn.setOnAction(e -> dialog.close());
        
        confirmBtn.setOnAction(e -> {
            String response = responseArea.getText().trim();
            
            if (response.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Missing Response", 
                         "Please provide a response.");
                return;
            }
            
            if (StatusData.databaseHelper.updateStaffRequestStatus(
                    request.getRequestId(), newStatus, response, user.getId(), user.getName())) {
                showAlert(Alert.AlertType.INFORMATION, "Success", 
                         "Request has been " + newStatus + "!");
                dialog.close();
                loadAllRequests();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", 
                         "Failed to update request.");
            }
        });
        
        Scene scene = new Scene(layout);
        dialog.setScene(scene);
        dialog.show();
    }
    
    /**
     * Displays detailed information about a selected request in the details text area.
     * Includes request ID, staff member, type, title, status, date, description,
     * and admin response if available.
     * 
     * @param request the request to display
     */
    private void displayRequestDetails(StaffRequest request) {
        StringBuilder details = new StringBuilder();
        details.append("Request ID: #").append(request.getRequestId()).append("\n");
        details.append("Staff Member: ").append(request.getStaffName()).append("\n");
        details.append("Type: ").append(capitalizeFirst(request.getRequestType().replace("_", " "))).append("\n");
        details.append("Title: ").append(request.getTitle()).append("\n");
        details.append("Status: ").append(capitalizeFirst(request.getStatus())).append("\n");
        details.append("Date: ").append(request.getTimestamp()).append("\n\n");
        
        details.append("Description:\n");
        details.append(request.getDescription()).append("\n");
        
        if (request.getAdminResponse() != null && !request.getAdminResponse().isEmpty()) {
            details.append("\n--- Admin Response ---\n");
            details.append("By: ").append(request.getHandledByAdminName()).append("\n");
            details.append(request.getAdminResponse());
        }
        
        requestDetails.setText(details.toString());
    }
    
    /**
     * Loads all staff requests from the database and displays them in the table.
     */
    private void loadAllRequests() {
        List<StaffRequest> requests = StatusData.databaseHelper.loadAllStaffRequests();
        requestsTable.getItems().setAll(requests);
    }
    
    /**
     * Filters and displays requests by a specific status.
     * 
     * @param status the status to filter by (open, approved, denied, completed)
     */
    private void filterByStatus(String status) {
        List<StaffRequest> allRequests = StatusData.databaseHelper.loadAllStaffRequests();
        List<StaffRequest> filtered = allRequests.stream()
            .filter(r -> r.getStatus().equalsIgnoreCase(status))
            .collect(Collectors.toList());
        requestsTable.getItems().setAll(filtered);
    }
    
    /**
     * Checks if the current user has permission to access this page.
     * Only staff and admin roles are allowed.
     * 
     * @return true if user can access the page, false otherwise
     */
    private boolean canAccessPage() {
        User.Role role = user.getRole();
        return role == User.Role.STAFF || role == User.Role.ADMIN;
    }
    
    /**
     * Capitalizes the first letter of a string.
     * 
     * @param text the text to capitalize
     * @return the text with first letter capitalized, or original text if null/empty
     */
    private String capitalizeFirst(String text) {
        if (text == null || text.isEmpty()) return text;
        return text.substring(0, 1).toUpperCase() + text.substring(1);
    }
    
    /**
     * Displays an alert dialog with the specified type, title, and message.
     * 
     * @param type the type of alert (ERROR, WARNING, INFORMATION, etc.)
     * @param title the title of the alert dialog
     * @param message the message content to display
     */
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}