package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents a request from staff members for various actions
 * (e.g., account management, permissions, system changes)
 */
public class StaffRequest {
    private int requestId;
    private int staffId;
    private String staffName;
    private String requestType; // "account", "permission", "system", "other"
    private String title;
    private String description;
    private String status; // "open", "approved", "denied", "completed"
    private String timestamp;
    private String adminResponse;
    private Integer handledByAdminId;
    private String handledByAdminName;
    
    
    /** No-arg constructor for frameworks and builders */
    public StaffRequest() {
        // default constructor
    }

public StaffRequest(int requestId, int staffId, String staffName, 
                       String requestType, String title, String description) {
        this.requestId = requestId;
        this.staffId = staffId;
        this.staffName = staffName;
        this.requestType = requestType;
        this.title = title;
        this.description = description;
        this.status = "open";
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
    
    public StaffRequest(int requestId, int staffId, String staffName, 
                       String requestType, String title, String description,
                       String status, String timestamp, String adminResponse,
                       Integer handledByAdminId, String handledByAdminName) {
        this.requestId = requestId;
        this.staffId = staffId;
        this.staffName = staffName;
        this.requestType = requestType;
        this.title = title;
        this.description = description;
        this.status = status;
        this.timestamp = timestamp;
        this.adminResponse = adminResponse;
        this.handledByAdminId = handledByAdminId;
        this.handledByAdminName = handledByAdminName;
    }
    
    public boolean isOpen() { return "open".equalsIgnoreCase(status); }
    public boolean isApproved() { return "approved".equalsIgnoreCase(status); }
    public boolean isDenied() { return "denied".equalsIgnoreCase(status); }
    public boolean isCompleted() { return "completed".equalsIgnoreCase(status); }
    
    // Getters and Setters
    public int getRequestId() { return requestId; }
    public void setRequestId(int requestId) { this.requestId = requestId; }
    
    public int getStaffId() { return staffId; }
    public void setStaffId(int staffId) { this.staffId = staffId; }
    
    public String getStaffName() { return staffName; }
    public void setStaffName(String staffName) { this.staffName = staffName; }
    
    public String getRequestType() { return requestType; }
    public void setRequestType(String requestType) { this.requestType = requestType; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    
    public String getAdminResponse() { return adminResponse; }
    public void setAdminResponse(String adminResponse) { this.adminResponse = adminResponse; }
    
    public Integer getHandledByAdminId() { return handledByAdminId; }
    public void setHandledByAdminId(Integer handledByAdminId) { 
        this.handledByAdminId = handledByAdminId; 
    }
    
    public String getHandledByAdminName() { return handledByAdminName; }
    public void setHandledByAdminName(String handledByAdminName) { 
        this.handledByAdminName = handledByAdminName; 
    }
    
    @Override
    public String toString() {
        return String.format("Staff Request #%d: %s [%s]", requestId, title, status);
    }
}