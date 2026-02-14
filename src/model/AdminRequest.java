package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a request submitted by instructors to administrators for 
 * system-level actions or interventions. Supports request categorization, 
 * linked follow-up requests, action histories, and notification tracking.
 */
public class AdminRequest {

    /** Unique ID of the request. */
    private int requestId;

    /** ID of the user who submitted the request. */
    private int requestorId;

    /** Name of the user who submitted the request. */
    private String requestorName;

    /** Category describing the type of request. */
    private String category;

    /** Short title summarizing the request. */
    private String title;

    /** Detailed description of the request. */
    private String description;

    /** Current status of the request (e.g., "open", "closed"). */
    private String status;

    /** Timestamp when the request was created or recorded. */
    private String timestamp;

    /**
     * If this request is a reopened case or follow-up, points to the original 
     * request ID. Otherwise null.
     */
    private Integer originalRequestId;

    /** List of admin actions applied to this request. */
    private List<AdminAction> actions;

    /** Whether a notification has been sent to the requester. */
    private boolean notificationSent;

    /**
     * Available predefined request categories.
     */
    public enum Category {

        /** Account modification actions (create, disable, update). */
        ACCOUNT_MANAGEMENT("Account Management"),

        /** Changes to user roles, privileges, or permissions. */
        PERMISSION_CHANGE("Permission Change"),

        /** Technical problems requiring admin intervention. */
        TECHNICAL_ISSUE("Technical Issue"),

        /** Issues involving inappropriate or problematic content. */
        CONTENT_MODERATION("Content Moderation"),

        /** System configuration updates or settings changes. */
        SYSTEM_CONFIGURATION("System Configuration"),

        /** General user assistance or support-related issues. */
        USER_SUPPORT("User Support"),

        /** For requests not fitting a predefined category. */
        OTHER("Other");

        /** Display-friendly name of the category. */
        private final String displayName;

        /**
         * Constructs a Category with a user-friendly display name.
         *
         * @param displayName readable category label
         */
        Category(String displayName) {
            this.displayName = displayName;
        }

        /**
         * Returns the display-friendly name of the category.
         *
         * @return display name string
         */
        public String getDisplayName() {
            return displayName;
        }

        /**
         * Returns the display-friendly name of this category.
         *
         * @return category name string
         */
        @Override
        public String toString() {
            return displayName;
        }
    }

    /**
     * Creates a new administrator request with essential information.
     * Automatically sets status to "open" and generates a timestamp.
     *
     * @param requestId unique ID for the request
     * @param requestorId ID of the user submitting the request
     * @param requestorName name of the requester
     * @param category category of the request
     * @param title short title describing the request
     * @param description detailed explanation of the request
     */
    public AdminRequest(int requestId, int requestorId, String requestorName,
                        String category, String title, String description) {
        this.requestId = requestId;
        this.requestorId = requestorId;
        this.requestorName = requestorName;
        this.category = category;
        this.title = title;
        this.description = description;
        this.status = "open";
        this.timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        this.originalRequestId = null;
        this.actions = new ArrayList<>();
        this.notificationSent = false;
    }

    /**
     * Constructor used when loading request data from persistent storage.
     *
     * @param requestId unique request ID
     * @param requestorId requester user ID
     * @param requestorName requester name
     * @param category request category
     * @param title request title
     * @param description request description
     * @param status current status of the request
     * @param timestamp creation timestamp
     * @param originalRequestId ID of the original request if reopened
     * @param notificationSent whether a notification has been sent
     */
    public AdminRequest(int requestId, int requestorId, String requestorName,
                        String category, String title, String description, String status,
                        String timestamp, Integer originalRequestId, boolean notificationSent) {

        this.requestId = requestId;
        this.requestorId = requestorId;
        this.requestorName = requestorName;
        this.category = category;
        this.title = title;
        this.description = description;
        this.status = status;
        this.timestamp = timestamp;
        this.originalRequestId = originalRequestId;
        this.actions = new ArrayList<>();
        this.notificationSent = notificationSent;
    }

    /**
     * Legacy constructor for backward compatibility.
     * Uses category {@code OTHER} and marks notification as not sent.
     *
     * @param requestId request ID
     * @param requestorId user ID of requester
     * @param requestorName requester name
     * @param title request title
     * @param description request description
     * @param status request status
     * @param timestamp timestamp string
     * @param originalRequestId ID of original request if applicable
     */
    public AdminRequest(int requestId, int requestorId, String requestorName,
                        String title, String description, String status,
                        String timestamp, Integer originalRequestId) {
        this(requestId, requestorId, requestorName, "OTHER", title,
             description, status, timestamp, originalRequestId, false);
    }

    /**
     * Checks whether the request is currently open.
     *
     * @return true if status equals "open"
     */
    public boolean isOpen() {
        return "open".equalsIgnoreCase(status);
    }

    /**
     * Checks whether the request is currently closed.
     *
     * @return true if status equals "closed"
     */
    public boolean isClosed() {
        return "closed".equalsIgnoreCase(status);
    }

    /**
     * Determines whether this request is a reopened one.
     *
     * @return true if {@code originalRequestId} is not null
     */
    public boolean isReopened() {
        return originalRequestId != null;
    }

    /** @return request ID */
    public int getRequestId() { return requestId; }

    /**
     * @param requestId the unique request ID to set
     */
    public void setRequestId(int requestId) { this.requestId = requestId; }

    /** @return ID of the requester */
    public int getRequestorId() { return requestorId; }

    /**
     * @param requestorId ID of the user who submitted the request
     */
    public void setRequestorId(int requestorId) { this.requestorId = requestorId; }

    /** @return requester's name */
    public String getRequestorName() { return requestorName; }

    /**
     * @param requestorName name of the requester
     */
    public void setRequestorName(String requestorName) { this.requestorName = requestorName; }

    /** @return request category */
    public String getCategory() { return category; }

    /**
     * @param category category of the request
     */
    public void setCategory(String category) { this.category = category; }

    /** @return request title */
    public String getTitle() { return title; }

    /**
     * @param title request title
     */
    public void setTitle(String title) { this.title = title; }

    /** @return request description */
    public String getDescription() { return description; }

    /**
     * @param description detailed request description
     */
    public void setDescription(String description) { this.description = description; }

    /** @return current status */
    public String getStatus() { return status; }

    /**
     * @param status new status for the request
     */
    public void setStatus(String status) { this.status = status; }

    /** @return timestamp of creation */
    public String getTimestamp() { return timestamp; }

    /**
     * @param timestamp timestamp string when the request was made
     */
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    /** @return ID of original request if reopened */
    public Integer getOriginalRequestId() { return originalRequestId; }

    /**
     * @param originalRequestId ID of the first request in case of reopening
     */
    public void setOriginalRequestId(Integer originalRequestId) {
        this.originalRequestId = originalRequestId;
    }

    /** @return list of admin actions */
    public List<AdminAction> getActions() { return actions; }

    /**
     * @param actions list of admin actions to associate with request
     */
    public void setActions(List<AdminAction> actions) { this.actions = actions; }

    /** @return true if requester notification has been sent */
    public boolean isNotificationSent() { return notificationSent; }

    /**
     * @param notificationSent true if admin notification was sent
     */
    public void setNotificationSent(boolean notificationSent) {
        this.notificationSent = notificationSent;
    }

    /**
     * Returns a human-readable summary of the request.
     *
     * @return formatted string representation of the request
     */
    @Override
    public String toString() {
        return String.format("Request #%d: %s [%s - %s]",
                requestId, title, category, status);
    }
}
