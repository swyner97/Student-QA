package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents a notification in the system.
 * Used for admin request updates and other system notifications.
 */
public class Notification {
    private int notificationId;
    private int userId;  // Recipient user ID
    private String type;  // "ADMIN_REQUEST_NEW", "ADMIN_REQUEST_UPDATE", "ADMIN_REQUEST_CLOSED"
    private int relatedId;  // ID of related entity (e.g., requestId)
    private String title;
    private String message;
    private String timestamp;
    private boolean isRead;
    
    /**
     * Notification types
     */
    public enum Type {
        ADMIN_REQUEST_NEW("New Admin Request"),
        ADMIN_REQUEST_UPDATE("Admin Request Updated"),
        ADMIN_REQUEST_CLOSED("Admin Request Closed"),
        ADMIN_REQUEST_REOPENED("Admin Request Reopened"),
        SYSTEM_ALERT("System Alert");
        
        private final String displayName;
        
        Type(String displayName) {
            this.displayName = displayName;
        }
/**
 * getDisplayName method.
 *
 */
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    public Notification() {
        // default constructor
    }
    
    // Constructor for new notifications
    public Notification(int userId, String type, int relatedId, String title, String message) {
        this.userId = userId;
        this.type = type;
        this.relatedId = relatedId;
        this.title = title;
        this.message = message;
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        this.isRead = false;
    }
    
    // Constructor for loading from database
    public Notification(int notificationId, int userId, String type, int relatedId, 
                       String title, String message, String timestamp, boolean isRead) {
        this.notificationId = notificationId;
        this.userId = userId;
        this.type = type;
        this.relatedId = relatedId;
        this.title = title;
        this.message = message;
        this.timestamp = timestamp;
        this.isRead = isRead;
    }
    
    // Getters and Setters
/**
 * getNotificationId method.
 *
 */
    public int getNotificationId() { return notificationId; }
/**
 * setNotificationId method.
 *
 * @param notificationId 
 */
    public void setNotificationId(int notificationId) { this.notificationId = notificationId; }
/**
 * getUserId method.
 *
 */
    
    public int getUserId() { return userId; }
/**
 * setUserId method.
 *
 * @param userId 
 */
    public void setUserId(int userId) { this.userId = userId; }
/**
 * getType method.
 *
 */
    
    public String getType() { return type; }
/**
 * setType method.
 *
 * @param type 
 */
    public void setType(String type) { this.type = type; }
/**
 * getRelatedId method.
 *
 */
    
    public int getRelatedId() { return relatedId; }
/**
 * setRelatedId method.
 *
 * @param relatedId 
 */
    public void setRelatedId(int relatedId) { this.relatedId = relatedId; }
/**
 * getTitle method.
 *
 */
    
    public String getTitle() { return title; }
/**
 * setTitle method.
 *
 * @param title 
 */
    public void setTitle(String title) { this.title = title; }
/**
 * getMessage method.
 *
 */
    
    public String getMessage() { return message; }
/**
 * setMessage method.
 *
 * @param message 
 */
    public void setMessage(String message) { this.message = message; }
/**
 * getTimestamp method.
 *
 */
    
    public String getTimestamp() { return timestamp; }
/**
 * setTimestamp method.
 *
 * @param timestamp 
 */
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
/**
 * isRead method.
 *
 */
    
    public boolean isRead() { return isRead; }
/**
 * setRead method.
 *
 * @param read 
 */
    public void setRead(boolean read) { isRead = read; }
    
    @Override
/**
 * toString method.
 *
 */
    public String toString() {
        return String.format("%s: %s [%s]", 
            isRead ? "" : "🔔", title, timestamp);
    }
}
