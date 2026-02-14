package pages;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import logic.StatusData;
import model.*;

import java.sql.SQLException;
import java.util.List;

/**
 * NotificationsPage - Displays all notifications for the current user
 * Shows admin request updates and other system notifications
 */
public class NotificationsPage {
    
    private Stage stage;
    private User user;
    
    private TableView<Notification> notificationsTable;
    private TextArea notificationDetails;
    private Label unreadCountLabel;
    private Button viewRelatedBtn;
    
    @SuppressWarnings("exports")
	public void show(Stage stage, User user) {
        this.stage = stage;
        this.user = user;
        
        stage.setTitle("Notifications");
        
        BorderPane mainPane = new BorderPane();
        
        // Navigation bar
        NavigationBar navBar = new NavigationBar();
        mainPane.setTop(navBar);
        
        // Center content
        VBox content = new VBox(15);
        content.setPadding(new Insets(15));
        
        // Header
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label title = new Label("🔔 Notifications");
        title.setStyle(
            "-fx-font-size: 20px; " +
            "-fx-font-weight: bold; " +
            "-fx-text-fill: #2c3e50;"
        );
        
        unreadCountLabel = new Label();
        unreadCountLabel.setStyle(
            "-fx-background-color: #dc3545; " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: bold; " +
            "-fx-padding: 4 12; " +
            "-fx-background-radius: 15;"
        );
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button deleteBtn = new Button("Delete");
        deleteBtn.setStyle(
            "-fx-background-color: #dc3545; " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: 600; " +
            "-fx-padding: 8 16; " +
            "-fx-background-radius: 8;"
        );
        deleteBtn.setDisable(true);
        deleteBtn.setOnAction(_ -> deleteSelectedNotification());
        
        Button markAllReadBtn = new Button("Mark All as Read");
        markAllReadBtn.setStyle(
            "-fx-background-color: #198754; " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: 600; " +
            "-fx-padding: 8 16; " +
            "-fx-background-radius: 8;"
        );
        markAllReadBtn.setOnAction(_ -> markAllAsRead());
       
        Button refreshBtn = new Button("🔄 Refresh");
        refreshBtn.setStyle(
            "-fx-background-color: #0d6efd; " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: 600; " +
            "-fx-padding: 8 16; " +
            "-fx-background-radius: 8;"
        );
        refreshBtn.setOnAction(_ -> {
            // Visual feedback
            refreshBtn.setDisable(true);
            refreshBtn.setText("Refreshing...");
            
            loadNotifications();
            
            // Re-enable after a brief delay
            new Thread(() -> {
                try {
                    Thread.sleep(300);
                    javafx.application.Platform.runLater(() -> {
                        refreshBtn.setDisable(false);
                        refreshBtn.setText("🔄 Refresh");
                    });
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }).start();
        });
        
        header.getChildren().addAll(title, unreadCountLabel, spacer, deleteBtn, markAllReadBtn, refreshBtn);
        
        // Table
        notificationsTable = new TableView<>();
        notificationsTable.setPrefHeight(400);
        notificationsTable.setStyle(
            "-fx-background-color: #ffffff; " +
            "-fx-border-color: #dee2e6; " +
            "-fx-border-width: 1; " +
            "-fx-border-radius: 10; " +
            "-fx-background-radius: 10;"
        );
        
        setupTable();
        
        // Details section
        VBox detailsBox = new VBox(10);
        detailsBox.setPadding(new Insets(10, 0, 0, 0));
        
        Label detailsTitle = new Label("Notification Details");
        detailsTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        notificationDetails = new TextArea();
        notificationDetails.setEditable(false);
        notificationDetails.setWrapText(true);
        notificationDetails.setPrefRowCount(6);
        notificationDetails.setPromptText("Select a notification to view details...");
        
        viewRelatedBtn = new Button("View Related Request");
        viewRelatedBtn.setStyle(
            "-fx-background-color: #0d6efd; " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: bold;"
        );
        viewRelatedBtn.setOnAction(_ -> viewRelatedRequest());
        viewRelatedBtn.setDisable(true);
        
        // ⭐ FIX: Add null check in selection listener
        notificationsTable.getSelectionModel().selectedItemProperty().addListener((_, _, newVal) -> {
            if (newVal != null) {
                displayNotificationDetails(newVal);
                viewRelatedBtn.setDisable(false);
                deleteBtn.setDisable(false); // Enable delete button
                
                // Mark as read when selected
                if (!newVal.isRead()) {
                    try {
                        StatusData.databaseHelper.markNotificationAsRead(newVal.getNotificationId());
                        loadNotifications(); // Refresh to update styling
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                }
            } else {
                viewRelatedBtn.setDisable(true);
                deleteBtn.setDisable(true); // Disable delete button
                notificationDetails.clear();
            }
        });
        
        detailsBox.getChildren().addAll(detailsTitle, notificationDetails, viewRelatedBtn);
        
        content.getChildren().addAll(header, notificationsTable, detailsBox);
        
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");
        
        mainPane.setCenter(scrollPane);
        
//        Scene scene = new Scene(mainPane, StatusData.WINDOW_WIDTH, StatusData.WINDOW_HEIGHT);
//        stage.setScene(scene);
//        stage.show();
        
        StatusData.setScene(stage, mainPane);
        loadNotifications();
    }
    
    @SuppressWarnings("unchecked")
	private void setupTable() {
        // Status Column (icon)
        TableColumn<Notification, String> statusCol = new TableColumn<>("");
        statusCol.setPrefWidth(40);
        statusCol.setCellValueFactory(cellData -> {
            return new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().isRead() ? "" : "🔔"
            );
        });
        
        // Type Column
        TableColumn<Notification, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        typeCol.setCellFactory(_ -> new TableCell<Notification, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label badge = new Label(getTypeDisplayName(item));
                    badge.setPadding(new Insets(4, 12, 4, 12));
                    badge.setStyle(getTypeStyle(item));
                    setGraphic(badge);
                    setText(null);
                    setAlignment(Pos.CENTER);
                }
            }
        });
        typeCol.setPrefWidth(180);
        
        // Title Column
        TableColumn<Notification, String> titleCol = new TableColumn<>("Title");
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        titleCol.setCellFactory(_ -> new TableCell<Notification, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    // ⭐ FIX: Add bounds check
                    if (getIndex() >= 0 && getIndex() < getTableView().getItems().size()) {
                        Notification notif = getTableView().getItems().get(getIndex());
                        setText(item);
                        if (!notif.isRead()) {
                            setStyle("-fx-font-weight: bold;");
                        } else {
                            setStyle("");
                        }
                    } else {
                        setText(item);
                        setStyle("");
                    }
                }
            }
        });
        titleCol.setPrefWidth(350);
        
        // Date Column
        TableColumn<Notification, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
        dateCol.setPrefWidth(180);
        
        notificationsTable.getColumns().addAll(statusCol, typeCol, titleCol, dateCol);
        
        // Row styling for unread notifications
        notificationsTable.setRowFactory(_ -> new TableRow<Notification>() {
            @Override
            protected void updateItem(Notification item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setStyle("");
                } else if (!item.isRead()) {
                    setStyle("-fx-background-color: #e3f2fd;");
                } else {
                    setStyle("");
                }
            }
        });
    }
    
    private void loadNotifications() {
        try {
            List<Notification> notifications = StatusData.databaseHelper.getAllNotifications(user.getId());
            
            if (notifications != null) {
                notificationsTable.getItems().setAll(notifications);
                System.out.println("✅ Loaded " + notifications.size() + " notifications for user " + user.getId());
            } else {
                notificationsTable.getItems().clear();
                System.out.println("⚠️ No notifications found for user " + user.getId());
            }
            
            int unreadCount = StatusData.databaseHelper.getUnreadNotificationCount(user.getId());
            if (unreadCount > 0) {
                unreadCountLabel.setText(unreadCount + " unread");
                unreadCountLabel.setVisible(true);
            } else {
                unreadCountLabel.setVisible(false);
            }
            
            System.out.println(" Unread count: " + unreadCount);
            
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("❌ Error loading notifications: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load notifications: " + e.getMessage());
        }
    }
    
    private void displayNotificationDetails(Notification notification) {
        if (notification == null) {
            notificationDetails.clear();
            return;
        }
        
        StringBuilder details = new StringBuilder();
        details.append("Type: ").append(getTypeDisplayName(notification.getType())).append("\n");
        details.append("Date: ").append(notification.getTimestamp()).append("\n");
        details.append("Status: ").append(notification.isRead() ? "Read" : "Unread").append("\n\n");
        details.append("Title:\n").append(notification.getTitle()).append("\n\n");
        details.append("Message:\n").append(notification.getMessage());
        
        notificationDetails.setText(details.toString());
    }
    
    private void markAllAsRead() {
        try {
            int marked = StatusData.databaseHelper.markAllNotificationsAsRead(user.getId());
            loadNotifications();
            
            if (marked > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Success", 
                    marked + " notification(s) marked as read.");
            } else {
                showAlert(Alert.AlertType.INFORMATION, "Info", 
                    "No unread notifications to mark.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", 
                "Failed to mark notifications as read: " + e.getMessage());
        }
    }
    
    
    private void deleteSelectedNotification() {
        Notification selected = notificationsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", 
                "Please select a notification to delete.");
            return;
        }
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Notification");
        confirm.setHeaderText("Delete this notification?");
        confirm.setContentText("Are you sure you want to delete this notification? This action cannot be undone.");
        
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    StatusData.databaseHelper.deleteNotification(selected.getNotificationId());
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Notification deleted.");
                    loadNotifications();
                    notificationDetails.clear();
                } catch (SQLException e) {
                    e.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "Error", 
                        "Failed to delete notification: " + e.getMessage());
                }
            }
        });
    }
    
    private void viewRelatedRequest() {
        Notification selected = notificationsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", 
                "Please select a notification first.");
            return;
        }
        
        if (selected.getType().contains("ADMIN_REQUEST")) {
            // Open AdminRequestsPage
            AdminRequestsPage requestsPage = new AdminRequestsPage();
            requestsPage.show(stage, user);
        } else {
            showAlert(Alert.AlertType.INFORMATION, "Not Applicable", 
                "This notification is not linked to an admin request.");
        }
    }
    
    private String getTypeDisplayName(String type) {
        if (type == null) return "Unknown";
        
        return switch (type) {
            case "ADMIN_REQUEST_NEW" -> "New Request";
            case "ADMIN_REQUEST_UPDATE" -> "Request Updated";
            case "ADMIN_REQUEST_CLOSED" -> "Request Closed";
            case "ADMIN_REQUEST_REOPENED" -> "Request Reopened";
            default -> "System Alert";
        };
    }
    
    private String getTypeStyle(String type) {
        if (type == null) {
            return "-fx-background-color: #e2e3e5; -fx-text-fill: #41464b; " +
                   "-fx-background-radius: 15; -fx-font-size: 11px; -fx-font-weight: bold;";
        }
        
        return switch (type) {
            case "ADMIN_REQUEST_NEW" -> 
                "-fx-background-color: #cfe2ff; -fx-text-fill: #084298; " +
                "-fx-background-radius: 15; -fx-font-size: 11px; -fx-font-weight: bold;";
            case "ADMIN_REQUEST_UPDATE" -> 
                "-fx-background-color: #fff3cd; -fx-text-fill: #997404; " +
                "-fx-background-radius: 15; -fx-font-size: 11px; -fx-font-weight: bold;";
            case "ADMIN_REQUEST_CLOSED" -> 
                "-fx-background-color: #d1e7dd; -fx-text-fill: #0f5132; " +
                "-fx-background-radius: 15; -fx-font-size: 11px; -fx-font-weight: bold;";
            case "ADMIN_REQUEST_REOPENED" -> 
                "-fx-background-color: #f8d7da; -fx-text-fill: #842029; " +
                "-fx-background-radius: 15; -fx-font-size: 11px; -fx-font-weight: bold;";
            default -> 
                "-fx-background-color: #e2e3e5; -fx-text-fill: #41464b; " +
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