package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents an action taken by an admin on an AdminRequest.
 */
public class AdminAction {
    private int actionId;
    private int requestId;
    private int adminId;
    private String adminName;
    private String actionDescription;
    private String timestamp;
    
    public AdminAction(int actionId, int requestId, int adminId, 
                      String adminName, String actionDescription) {
        this.actionId = actionId;
        this.requestId = requestId;
        this.adminId = adminId;
        this.adminName = adminName;
        this.actionDescription = actionDescription;
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
    
    public AdminAction(int actionId, int requestId, int adminId, 
                      String adminName, String actionDescription, String timestamp) {
        this.actionId = actionId;
        this.requestId = requestId;
        this.adminId = adminId;
        this.adminName = adminName;
        this.actionDescription = actionDescription;
        this.timestamp = timestamp;
    }
    
    // Getters and Setters
/**
 * getActionId method.
 *
 */
    public int getActionId() { return actionId; }
/**
 * setActionId method.
 *
 * @param actionId 
 */
    public void setActionId(int actionId) { this.actionId = actionId; }
/**
 * getRequestId method.
 *
 */
    
    public int getRequestId() { return requestId; }
/**
 * setRequestId method.
 *
 * @param requestId 
 */
    public void setRequestId(int requestId) { this.requestId = requestId; }
/**
 * getAdminId method.
 *
 */
    
    public int getAdminId() { return adminId; }
/**
 * setAdminId method.
 *
 * @param adminId 
 */
    public void setAdminId(int adminId) { this.adminId = adminId; }
/**
 * getAdminName method.
 *
 */
    
    public String getAdminName() { return adminName; }
/**
 * setAdminName method.
 *
 * @param adminName 
 */
    public void setAdminName(String adminName) { this.adminName = adminName; }
/**
 * getActionDescription method.
 *
 */
    
    public String getActionDescription() { return actionDescription; }
/**
 * setActionDescription method.
 *
 * @param actionDescription 
 */
    public void setActionDescription(String actionDescription) { 
        this.actionDescription = actionDescription; 
    }
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
    
    @Override
/**
 * toString method.
 *
 */
    public String toString() {
        return String.format("[%s] %s: %s", timestamp, adminName, actionDescription);
    }
}