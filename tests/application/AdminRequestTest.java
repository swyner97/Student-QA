package application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import model.*;


/**
 * JUnit test class for AdminRequest.
 * Tests all constructors, getters, setters, and business logic methods.
 */
class AdminRequestTest {
    
    private AdminRequest request;
    private static final int TEST_REQUEST_ID = 1;
    private static final int TEST_REQUESTOR_ID = 100;
    private static final String TEST_REQUESTOR_NAME = "John Doe";
    private static final String TEST_CATEGORY = "ACCOUNT_MANAGEMENT";
    private static final String TEST_TITLE = "Reset Password";
    private static final String TEST_DESCRIPTION = "User needs password reset";
    
    @BeforeEach
    void setUp() {
        request = new AdminRequest(TEST_REQUEST_ID, TEST_REQUESTOR_ID, 
                                   TEST_REQUESTOR_NAME, TEST_CATEGORY, 
                                   TEST_TITLE, TEST_DESCRIPTION);
    }
    
    @Test
    @DisplayName("Test new request constructor initializes correctly")
    void testNewRequestConstructor() {
        assertEquals(TEST_REQUEST_ID, request.getRequestId());
        assertEquals(TEST_REQUESTOR_ID, request.getRequestorId());
        assertEquals(TEST_REQUESTOR_NAME, request.getRequestorName());
        assertEquals(TEST_CATEGORY, request.getCategory());
        assertEquals(TEST_TITLE, request.getTitle());
        assertEquals(TEST_DESCRIPTION, request.getDescription());
        assertEquals("open", request.getStatus());
        assertNotNull(request.getTimestamp());
        assertNull(request.getOriginalRequestId());
        assertNotNull(request.getActions());
        assertTrue(request.getActions().isEmpty());
        assertFalse(request.isNotificationSent());
    }
    
    @Test
    @DisplayName("Test database constructor initializes all fields")
    void testDatabaseConstructor() {
        String timestamp = "2024-01-15 10:30:00";
        AdminRequest dbRequest = new AdminRequest(
            2, 101, "Jane Smith", "TECHNICAL_ISSUE", 
            "Bug Report", "System crashes", "closed", 
            timestamp, 1, true
        );
        
        assertEquals(2, dbRequest.getRequestId());
        assertEquals(101, dbRequest.getRequestorId());
        assertEquals("Jane Smith", dbRequest.getRequestorName());
        assertEquals("TECHNICAL_ISSUE", dbRequest.getCategory());
        assertEquals("Bug Report", dbRequest.getTitle());
        assertEquals("System crashes", dbRequest.getDescription());
        assertEquals("closed", dbRequest.getStatus());
        assertEquals(timestamp, dbRequest.getTimestamp());
        assertEquals(1, dbRequest.getOriginalRequestId());
        assertTrue(dbRequest.isNotificationSent());
    }
    
    @Test
    @DisplayName("Test legacy constructor sets default category")
    void testLegacyConstructor() {
        String timestamp = "2024-01-15 10:30:00";
        AdminRequest legacyRequest = new AdminRequest(
            3, 102, "Bob Johnson", "Old Request", 
            "Legacy description", "open", timestamp, null
        );
        
        assertEquals("OTHER", legacyRequest.getCategory());
        assertFalse(legacyRequest.isNotificationSent());
    }
    
    @Test
    @DisplayName("Test isOpen returns true for open status")
    void testIsOpenWithOpenStatus() {
        assertTrue(request.isOpen());
    }
    
    @Test
    @DisplayName("Test isOpen returns false for closed status")
    void testIsOpenWithClosedStatus() {
        request.setStatus("closed");
        assertFalse(request.isOpen());
    }
    
    @Test
    @DisplayName("Test isOpen is case insensitive")
    void testIsOpenCaseInsensitive() {
        request.setStatus("OPEN");
        assertTrue(request.isOpen());
        
        request.setStatus("Open");
        assertTrue(request.isOpen());
    }
    
    @Test
    @DisplayName("Test isClosed returns true for closed status")
    void testIsClosedWithClosedStatus() {
        request.setStatus("closed");
        assertTrue(request.isClosed());
    }
    
    @Test
    @DisplayName("Test isClosed returns false for open status")
    void testIsClosedWithOpenStatus() {
        assertFalse(request.isClosed());
    }
    
    @Test
    @DisplayName("Test isClosed is case insensitive")
    void testIsClosedCaseInsensitive() {
        request.setStatus("CLOSED");
        assertTrue(request.isClosed());
        
        request.setStatus("Closed");
        assertTrue(request.isClosed());
    }
    
    @Test
    @DisplayName("Test isReopened returns true when originalRequestId is set")
    void testIsReopenedWithOriginalId() {
        request.setOriginalRequestId(5);
        assertTrue(request.isReopened());
    }
    
    @Test
    @DisplayName("Test isReopened returns false when originalRequestId is null")
    void testIsReopenedWithoutOriginalId() {
        assertFalse(request.isReopened());
    }
    
    @Test
    @DisplayName("Test setRequestId updates request ID")
    void testSetRequestId() {
        request.setRequestId(999);
        assertEquals(999, request.getRequestId());
    }
    
    @Test
    @DisplayName("Test setRequestorId updates requestor ID")
    void testSetRequestorId() {
        request.setRequestorId(200);
        assertEquals(200, request.getRequestorId());
    }
    
    @Test
    @DisplayName("Test setRequestorName updates requestor name")
    void testSetRequestorName() {
        request.setRequestorName("Alice Brown");
        assertEquals("Alice Brown", request.getRequestorName());
    }
    
    @Test
    @DisplayName("Test setCategory updates category")
    void testSetCategory() {
        request.setCategory("PERMISSION_CHANGE");
        assertEquals("PERMISSION_CHANGE", request.getCategory());
    }
    
    @Test
    @DisplayName("Test setTitle updates title")
    void testSetTitle() {
        request.setTitle("New Title");
        assertEquals("New Title", request.getTitle());
    }
    
    @Test
    @DisplayName("Test setDescription updates description")
    void testSetDescription() {
        request.setDescription("New description");
        assertEquals("New description", request.getDescription());
    }
    
    @Test
    @DisplayName("Test setStatus updates status")
    void testSetStatus() {
        request.setStatus("in_progress");
        assertEquals("in_progress", request.getStatus());
    }
    
    @Test
    @DisplayName("Test setTimestamp updates timestamp")
    void testSetTimestamp() {
        String newTimestamp = "2024-12-01 15:45:00";
        request.setTimestamp(newTimestamp);
        assertEquals(newTimestamp, request.getTimestamp());
    }
    
    @Test
    @DisplayName("Test setOriginalRequestId updates original request ID")
    void testSetOriginalRequestId() {
        request.setOriginalRequestId(10);
        assertEquals(10, request.getOriginalRequestId());
    }
    
    @Test
    @DisplayName("Test setActions updates actions list")
    void testSetActions() {
        List<AdminAction> actions = new ArrayList<>();
        request.setActions(actions);
        assertSame(actions, request.getActions());
    }
    
    @Test
    @DisplayName("Test setNotificationSent updates notification status")
    void testSetNotificationSent() {
        request.setNotificationSent(true);
        assertTrue(request.isNotificationSent());
        
        request.setNotificationSent(false);
        assertFalse(request.isNotificationSent());
    }
    
    @Test
    @DisplayName("Test toString returns formatted string")
    void testToString() {
        String result = request.toString();
        assertTrue(result.contains("#" + TEST_REQUEST_ID));
        assertTrue(result.contains(TEST_TITLE));
        assertTrue(result.contains(TEST_CATEGORY));
        assertTrue(result.contains("open"));
    }
    
    @Test
    @DisplayName("Test Category enum display names")
    void testCategoryEnumDisplayNames() {
        assertEquals("Account Management", 
                     AdminRequest.Category.ACCOUNT_MANAGEMENT.getDisplayName());
        assertEquals("Permission Change", 
                     AdminRequest.Category.PERMISSION_CHANGE.getDisplayName());
        assertEquals("Technical Issue", 
                     AdminRequest.Category.TECHNICAL_ISSUE.getDisplayName());
        assertEquals("Content Moderation", 
                     AdminRequest.Category.CONTENT_MODERATION.getDisplayName());
        assertEquals("System Configuration", 
                     AdminRequest.Category.SYSTEM_CONFIGURATION.getDisplayName());
        assertEquals("User Support", 
                     AdminRequest.Category.USER_SUPPORT.getDisplayName());
        assertEquals("Other", 
                     AdminRequest.Category.OTHER.getDisplayName());
    }
    
    @Test
    @DisplayName("Test Category enum toString returns display name")
    void testCategoryEnumToString() {
        assertEquals("Account Management", 
                     AdminRequest.Category.ACCOUNT_MANAGEMENT.toString());
        assertEquals("Other", 
                     AdminRequest.Category.OTHER.toString());
    }
    
    @Test
    @DisplayName("Test timestamp format is correct")
    void testTimestampFormat() {
        String timestamp = request.getTimestamp();
        assertNotNull(timestamp);
        // Verify format matches yyyy-MM-dd HH:mm:ss
        assertTrue(timestamp.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}"));
    }
    
    @Test
    @DisplayName("Test actions list is mutable")
    void testActionsListIsMutable() {
        List<AdminAction> actions = request.getActions();
        assertNotNull(actions);
        // Should be able to modify the list without exceptions
        assertDoesNotThrow(() -> actions.add(null));
    }
}