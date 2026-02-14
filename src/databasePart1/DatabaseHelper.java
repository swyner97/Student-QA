package databasePart1;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import model.*;
import model.User.Role;
import pages.RankReviewer.TrustedReviewerRow;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import java.sql.Connection;
import java.util.stream.Collectors;

/**
 * The DatabaseHelper class is responsible for managing the connection to the database,
 * performing operations such as user registration, login validation, and handling invitation codes.
 *
 * NOTE: Rating-related tables and methods were removed per request (only reviews themselves are stored).
 */
public class DatabaseHelper {

    // JDBC driver name and database URL
    static final String JDBC_DRIVER = "org.h2.Driver";
    static final String DB_URL = "jdbc:h2:~/FoundationDatabase";

    // Database credentials
    static final String USER = "sa";
    static final String PASS = "";

    private Connection connection = null;
    private Statement statement = null;

    public void connectToDatabase() throws SQLException {
        try {
            Class.forName(JDBC_DRIVER);
            System.out.println("Connecting to database...");
            connection = DriverManager.getConnection(DB_URL, USER, PASS);
            statement = connection.createStatement();

            ensureMissingTablesExist();
            createTables();

        } catch (ClassNotFoundException e) {
            System.err.println("JDBC Driver not found: " + e.getMessage());
            throw new SQLException("JDBC Driver not found", e);
        } catch (SQLException e) {
            System.err.println("Database connection failed: " + e.getMessage());
            throw e;
        }
    }
    
    public Connection getConnection() {
        return this.connection;
    }

    public int executeUpdate(String sql) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            return stmt.executeUpdate(sql);
        }
    }

    /**
     * Returns a PreparedStatement from the underlying Connection.
     * Caller is responsible for closing the PreparedStatement when finished.
     */
    public PreparedStatement prepareStatement(String sql) throws SQLException {
        return connection.prepareStatement(sql);
    }

    private void createTables() throws SQLException {
        String userTable = "CREATE TABLE IF NOT EXISTS cse360users ("
                + "id INT AUTO_INCREMENT PRIMARY KEY, "
                + "userName VARCHAR(255) UNIQUE, "
                + "password VARCHAR(255), "
                + "role VARCHAR(20),"
                + "name VARCHAR(255), "
                + "email VARCHAR(255),"
                + "phone VARCHAR(20),"
                + "bio TEXT,"
                + "temp_password VARCHAR(255))";

        // Private messaging
        String messagesTable = "CREATE TABLE IF NOT EXISTS privateMessages ("
                + "id INT AUTO_INCREMENT PRIMARY KEY, "
                + "sender_id INT, "
                + "recipient_id INT, "
                + "message TEXT, "
                + "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                + "is_read BOOLEAN DEFAULT FALSE, "
                + "FOREIGN KEY (sender_id) REFERENCES cse360users(id), "
                + "FOREIGN KEY (recipient_id) REFERENCES cse360users(id))";

        // Questions and Answers
        String questionsTable = "CREATE TABLE IF NOT EXISTS questions ("
                + "question_id INT AUTO_INCREMENT PRIMARY KEY,"
                + "author VARCHAR(50),"
                + "title VARCHAR(200),"
                + "description VARCHAR(5000),"
                + "timestamp TIMESTAMP,"
                + "status VARCHAR(20),"
                + "follow_up INT NULL,"
                + "user_id INT,"
                + "FOREIGN KEY (follow_up) REFERENCES questions(question_id))";

        String answersTable = "CREATE TABLE IF NOT EXISTS answers ("
                + "answer_id INT AUTO_INCREMENT PRIMARY KEY,"
                + "user_id INT,"
                + "question_id INT,"
                + "author VARCHAR(50),"
                + "content VARCHAR(2000),"
                + "timestamp TIMESTAMP,"
                + "is_solution BOOLEAN,"
                + "FOREIGN KEY (question_id) REFERENCES questions(question_id))";

        // Clarifications
        String clarificationsTable = "CREATE TABLE IF NOT EXISTS clarifications ("
                + "clarification_id INT AUTO_INCREMENT PRIMARY KEY,"
        		+ "item_type VARCHAR(20) NOT NULL,"
                + "content_id INT NOT NULL,"
                + "question_id INT,"
                + "answer_id INT,"
                + "author_id INT,"
                + "recipient_id INT,"
                + "author VARCHAR(50),"
                + "content VARCHAR(2000),"
                + "timestamp TIMESTAMP,"
                + "is_read BOOLEAN DEFAULT FALSE,"
                + "is_public BOOLEAN DEFAULT FALSE)";

        statement.execute(userTable);
        statement.execute(messagesTable);
        statement.execute(questionsTable);
        statement.execute(answersTable);
        statement.execute(clarificationsTable);

        // UserRoles
        String userRolesTable = "CREATE TABLE IF NOT EXISTS UserRoles("
                + "id INT AUTO_INCREMENT PRIMARY KEY, "
                + "userName VARCHAR(255) NOT NULL, "
                + "role VARCHAR(255) NOT NULL)";
        statement.execute(userRolesTable);

        // Invitation codes and code roles
        String invitationCodesTable = "CREATE TABLE IF NOT EXISTS InvitationCodes ("
                + "code VARCHAR(50) PRIMARY KEY, "
                + "isUsed BOOLEAN DEFAULT FALSE)";
        statement.execute(invitationCodesTable);

        String codeRolesTable = "CREATE TABLE IF NOT EXISTS CodeRoles("
                + "id INT AUTO_INCREMENT PRIMARY KEY, "
                + "code VARCHAR(50),"
                + "initialRole VARCHAR(255))";
        statement.execute(codeRolesTable);

        // Trusted reviewers
        String trustedReviewersTable = "CREATE TABLE IF NOT EXISTS trustedReviewers ("
                + "student_id INT NOT NULL, "
                + "reviewer_id INT NOT NULL, "
                + "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                + "rating INT DEFAULT 0, "
                + "PRIMARY KEY (student_id, reviewer_id), "
                + "FOREIGN KEY (student_id) REFERENCES cse360users(id), "
                + "FOREIGN KEY (reviewer_id) REFERENCES cse360users(id))";
        statement.execute(trustedReviewersTable);

        // Index for lookup optimization
        String trustedIndex = "CREATE INDEX IF NOT EXISTS trustedStudentIndex "
                + "ON trustedReviewers(student_id)";
        statement.execute(trustedIndex);

        // Reviews
        String reviewsTable = "CREATE TABLE IF NOT EXISTS reviews ("
                + "review_id INT AUTO_INCREMENT PRIMARY KEY,"
                + "user_id INT,"
                + "answer_id INT,"
                + "author VARCHAR(50),"
                + "content VARCHAR(2000),"
                + "timestamp VARCHAR(30),"
                + "FOREIGN KEY (answer_id) REFERENCES answers(answer_id))";
        statement.execute(reviewsTable);

        // Request reviewer role
        String requestReviewerRoleTable = "CREATE TABLE IF NOT EXISTS requestReviewerRole("
                + "id INT AUTO_INCREMENT PRIMARY KEY, "
                + "userName VARCHAR(255),"
                + "role VARCHAR(255))";
        statement.execute(requestReviewerRoleTable);

        // sum of all reviewer ratings. 
        String allReviewerRatingsTable = "CREATE TABLE IF NOT EXISTS allReviewerRatings ("
                + "reviewer_id INT, "
                + "student_id INT,"
                + "rating DOUBLE DEFAULT 0, "
                + "PRIMARY KEY (reviewer_id, student_id), "
                + "FOREIGN KEY (reviewer_id) REFERENCES cse360users(id), "
                + "FOREIGN KEY (student_id) REFERENCES cse360users(id))";
        statement.execute(allReviewerRatingsTable);

        // reviewer score depending on method 
        String reviewerScoreCardTable = "CREATE TABLE IF NOT EXISTS reviewerScoreCard("
                + "id INT AUTO_INCREMENT PRIMARY KEY, "
                + "reviewer_id INT NOT NULL, "
                + "score INT DEFAULT 0, "
                + "scoring_method VARCHAR(50) DEFAULT 'average', "
                + "FOREIGN KEY (reviewer_id) REFERENCES cse360users(id),"
                + "UNIQUE(reviewer_id)"
                + ")";
        statement.execute(reviewerScoreCardTable);

        // Instructor settings
        String instructorSettingsTable = "CREATE TABLE IF NOT EXISTS instructorSettings("
                + "id INT AUTO_INCREMENT PRIMARY KEY,"
                + "scoring_method VARCHAR(50) DEFAULT 'average'"
                + ")";
        statement.execute(instructorSettingsTable);

        // Moderation flags and notes
        String moderationFlagsTable = "CREATE TABLE IF NOT EXISTS moderationFlags ("
                + "flag_id INT AUTO_INCREMENT PRIMARY KEY,"
                + "item_type VARCHAR(20) NOT NULL,"
                + "item_id INT NOT NULL,"
                + "staff_id INT NOT NULL,"
                + "reason VARCHAR(1000),"
                + "status VARCHAR(20) DEFAULT 'OPEN',"
                + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                + "FOREIGN KEY (staff_id) REFERENCES cse360users(id)"
                + ")";
        statement.execute(moderationFlagsTable);

        String moderationNotesTable = "CREATE TABLE IF NOT EXISTS moderationNotes ("
                + "note_id INT AUTO_INCREMENT PRIMARY KEY,"
                + "flag_id INT NOT NULL,"
                + "staff_id INT NOT NULL,"
                + "note_text VARCHAR(1000) NOT NULL,"
                + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                + "FOREIGN KEY (flag_id) REFERENCES moderationFlags(flag_id),"
                + "FOREIGN KEY (staff_id) REFERENCES cse360users(id)"
                + ")";
        statement.execute(moderationNotesTable);

        // Admin actions (admin_actions references admin_requests - admin_requests created by ensureMissingTablesExist)
        String adminActionsTable = "CREATE TABLE IF NOT EXISTS admin_actions ("
                + "action_id INT AUTO_INCREMENT PRIMARY KEY,"
                + "request_id INT NOT NULL,"
                + "admin_id INT NOT NULL,"
                + "admin_name VARCHAR(255),"
                + "action_description VARCHAR(2000),"
                + "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                + "FOREIGN KEY (request_id) REFERENCES admin_requests(request_id)"
                + ")";
        statement.execute(adminActionsTable);

        // Reviewer profile
        String reviewerProfileTable = "CREATE TABLE IF NOT EXISTS reviewerProfile ("
                + "reviewer_id INT PRIMARY KEY, "
                + "experience CLOB, "
                + "FOREIGN KEY (reviewer_id) REFERENCES cse360users(id)"
                + ")";
        statement.execute(reviewerProfileTable);
        
        //edits
        String editsTable = "CREATE TABLE IF NOT EXISTS edits ("
        		+ "edit_id INT AUTO_INCREMENT PRIMARY KEY,"
        		+ "question_id INT NOT NULL,"
        		+ "old_title TEXT NOT NULL,"
        		+ "old_description TEXT NOT NULL,"
        		+ "new_title TEXT NOT NULL,"
        		+ "new_description TEXT NOT NULL,"
        		+ "edited_by TEXT NOT NULL,"
        		+ "edit_time TEXT NOT NULL,"
        		+ "FOREIGN KEY (question_id) REFERENCES questions(question_id)"
        		+ ")";
        statement.execute(editsTable);
    }

    public boolean isDatabaseEmpty() throws SQLException {
        String query = "SELECT COUNT(*) AS count FROM cse360users";
        try (ResultSet resultSet = statement.executeQuery(query)) {
            if (resultSet.next()) {
                return resultSet.getInt("count") == 0;
            }
        }
        return true;
    }

    public ContentType getContentTypeById(int itemId) throws SQLException {
        // for question content
        String questionSQL = "SELECT 1 FROM questions WHERE question_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(questionSQL)) {
            ps.setInt(1, itemId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return ContentType.QUESTION;
            }
        }

        // for answer content
        String answerSQL = "SELECT 1 FROM answers WHERE answer_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(answerSQL)) {
            ps.setInt(1, itemId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return ContentType.ANSWER;
            }
        }

        // for review content
        String reviewsSQL = "SELECT 1 FROM reviews WHERE review_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(reviewsSQL)) {
            ps.setInt(1, itemId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return ContentType.REVIEW;
            }
        }

        // for suggestion / clarification content
        String clarificationsSQL = "SELECT 1 FROM clarifications WHERE clarification_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(clarificationsSQL)) {
            ps.setInt(1, itemId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return ContentType.SUGGESTION;
            }
        }

        return ContentType.OTHER;
    }

    public void register(User user) throws SQLException {
        String insertUser = "INSERT INTO cse360users (userName, password, role, name, email) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(insertUser, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, user.getUserName());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getRoleName()); // store enum as string
            pstmt.setString(4, user.getName());
            pstmt.setString(5, user.getEmail());

            int affected = pstmt.executeUpdate();

            if (affected > 0) {
                try (ResultSet keys = pstmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        int newId = keys.getInt(1);
                        user.setId(newId);
                    }
                }
            }
        }
    }

    public String loginWithOTPcheck(String userName, String enteredPw, String role) throws SQLException {
        String query = "SELECT password, temp_password FROM cse360users WHERE userName = ? AND role = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, userName);
            pstmt.setString(2, role);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String realPassword = rs.getString("password");
                    String tempPassword = rs.getString("temp_password");

                    if (tempPassword != null) {
                        if (enteredPw.equals(tempPassword)) {
                            clearTempPassword(userName);
                            return "temp";
                        } else {
                            return null; // OTP incorrect
                        }
                    } else if (enteredPw.equals(realPassword)) {
                        return "normal";
                    }
                }
            }
        }
        return null;
    }

    public boolean doesUserExist(String userName) {
        String query = "SELECT COUNT(*) FROM cse360users WHERE userName = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, userName);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public String getUserRole(String userName) {
        String query = "SELECT role FROM cse360users WHERE userName = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, userName);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("role");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Updates an existing user in the database
    public void updateUser(User user, String oldUserName) throws SQLException {
        String updateUser = "UPDATE cse360users SET userName=?, password=?, role=?, name=?, email=? WHERE userName=?";
        try (PreparedStatement pstmt = connection.prepareStatement(updateUser)) {
            pstmt.setString(1, user.getUserName());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getRoleName()); // store enum as string
            pstmt.setString(4, user.getName());
            pstmt.setString(5, user.getEmail());
            pstmt.setString(6, oldUserName);
            pstmt.executeUpdate();
        }
    }

    public void loadUserDetails(User user) {
        String query = "SELECT id, name, email, phone, bio FROM cse360users WHERE userName = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, user.getUserName());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    user.setId(rs.getInt("id"));
                    user.setName(rs.getString("name"));
                    user.setEmail(rs.getString("email"));
                    user.setPhone(rs.getString("phone"));
                    user.setBio(rs.getString("bio"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateFullProfile(User user) throws SQLException {
        String sql = "UPDATE cse360users SET userName=?, password=?, role=?, name=?, email=?, phone=?, bio=? WHERE id=?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, user.getUserName());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getRoleName()); // store enum as string
            pstmt.setString(4, user.getName());
            pstmt.setString(5, user.getEmail());
            pstmt.setString(6, user.getPhone());
            pstmt.setString(7, user.getBio());
            pstmt.setInt(8, user.getId());
            pstmt.executeUpdate();
        }
    }

    public void updateUserProfile(int userId, String name, String email, String phone, String bio) throws SQLException {
        String sql = "UPDATE cse360users SET email = ?, phone = ?, bio = ? WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, email);
            pstmt.setString(2, phone);
            pstmt.setString(3, bio);
            pstmt.setInt(4, userId);
            pstmt.executeUpdate();
        }
    }

    public Map<String, String> getUserProfile(int userId) throws SQLException {
        String sql = "SELECT userName, role, name, email, phone, bio FROM cse360users WHERE id = ?";
        Map<String, String> profile = new HashMap<>();

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    profile.put("username", rs.getString("userName"));
                    profile.put("role", rs.getString("role"));
                    profile.put("name", rs.getString("name"));
                    profile.put("email", rs.getString("email"));
                    profile.put("phone", rs.getString("phone"));
                    profile.put("bio", rs.getString("bio"));
                }
            }
        }
        return profile;
    }

    public String generateInvitationCode(List<String> selectedRoles) {
        List<String> priority = List.of("admin", "instructor", "staff", "reviewer", "student");

        // sort roles according to priority
        List<String> sortedRoles = selectedRoles.stream()
                .sorted(Comparator.comparingInt(priority::indexOf))
                .collect(Collectors.toList());

        // build prefix from selected roles
        StringBuilder prefix = new StringBuilder();
        for (String role : sortedRoles) {
            if (role != null && !role.isEmpty()) {
                prefix.append(role.charAt(0));
            }
        }

        // add random 4 digit suffix
        String suffix = UUID.randomUUID().toString().substring(0, 4);
        String code = prefix + "-" + suffix;

        // Insert into invitation codes table
        String query = "INSERT INTO InvitationCodes (code) VALUES (?)";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, code);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // insert into coderoles table
        String insertRoleQuery = "INSERT INTO CodeRoles (code, initialRole) VALUES (?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(insertRoleQuery)) {
            for (String role : selectedRoles) {
                pstmt.setString(1, code);
                pstmt.setString(2, role);
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return code;
    }

    public List<String> getRolesForInvitationCode(String code) throws SQLException {
        List<String> roles = new ArrayList<>();
        String query = "SELECT initialRole FROM CodeRoles WHERE code = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, code);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    roles.add(rs.getString("initialRole"));
                }
            }
        }
        return roles;
    }

    public boolean validateInvitationCode(String code) {
        String query = "SELECT * FROM InvitationCodes WHERE code = ? AND isUsed = FALSE";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, code);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    markInvitationCodeAsUsed(code);
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void markInvitationCodeAsUsed(String code) {
        String query = "UPDATE InvitationCodes SET isUsed = TRUE WHERE code = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, code);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String generatePassword(String userName) {
        String otp = UUID.randomUUID().toString().substring(0, 8);
        String sql = "UPDATE cse360users SET temp_password = ? WHERE userName = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, otp);
            pstmt.setString(2, userName);
            int updated = pstmt.executeUpdate();
            if (updated > 0) return otp;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // add marker to Admin's table for user's who need OTP
    public boolean requestedPw(String userName, String email) {
        String sql = "UPDATE cse360users SET temp_password = 'PENDING' WHERE userName = ? AND email = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, userName);
            pstmt.setString(2, email);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean validateOTP(String otp) {
        String query = "SELECT userName FROM cse360users WHERE temp_password = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, otp);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String userName = rs.getString("userName");
                    clearTempPassword(userName);
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void clearTempPassword(String userName) {
        String query = "UPDATE cse360users SET temp_password = NULL WHERE userName = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, userName);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateUserRole(int id, String newRole) throws SQLException {
        String query = "UPDATE cse360users SET role = ? WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, newRole);
            pstmt.setInt(2, id);
            pstmt.executeUpdate();
        }
    }

    public void addUserRoles(String userName, User.Role role) throws SQLException {
        if (userName == null || userName.trim().isEmpty()) {
            throw new IllegalArgumentException("userName must not be null/empty");
        }
        if (role == null || role == User.Role.UNKNOWN) {
            throw new IllegalArgumentException("role must be a valid Role enum");
        }

        String roleName = role.name(); // store enum name in DB

        String check = "SELECT COUNT(*) FROM UserRoles WHERE userName = ? AND role = ?";
        try (PreparedStatement checkStmt = connection.prepareStatement(check)) {
            checkStmt.setString(1, userName);
            checkStmt.setString(2, roleName);
            try (ResultSet rs = checkStmt.executeQuery()) {
                rs.next();
                if (rs.getInt(1) == 0) { // only insert if it doesn't exist
                    String insertRole = "INSERT INTO UserRoles (userName, role) VALUES (?, ?)";
                    try (PreparedStatement pstmt = connection.prepareStatement(insertRole)) {
                        pstmt.setString(1, userName);
                        pstmt.setString(2, roleName);
                        pstmt.executeUpdate();
                    }
                }
            }
        }
    }

    // where invitation codes will be assigned
    public void addRoleVIACode(String code, Role role) throws SQLException {
        String query = "INSERT INTO CodeRoles (code, initialRole) VALUES (?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, code);
            pstmt.setString(2, role.name());
            pstmt.executeUpdate();
            System.out.println("Inserting: " + role);
        }
    }

    // list all invitation codes with roles assigned
    public List<String> allCodeRoles(String code) throws SQLException {
        List<String> roles = new ArrayList<>();
        String query = "SELECT initialRole FROM CodeRoles WHERE code =?";

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, code);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    roles.add(rs.getString("initialRole"));
                }
            }
        }
        return roles;
    }

    public void deleteUserRole(String userName, Role role) throws SQLException {
        String query = "DELETE FROM UserRoles WHERE userName =? and role = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, userName);
            pstmt.setString(2, role.name());
            pstmt.executeUpdate();
        }
    }

    // List all roles for user
    public List<String> allUserRoles(String userName) throws SQLException {
        List<String> roles = new ArrayList<>();
        String query = "SELECT role FROM UserRoles WHERE userName =?";

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, userName);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    roles.add(rs.getString("role"));
                }
            }
        }
        return roles;
    }

    public List<User.Role> getAllRolesForUser(String userName) throws SQLException {
        List<User.Role> roles = new ArrayList<>();
        String sql = "SELECT role FROM UserRoles WHERE userName = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, userName);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    roles.add(User.Role.fromString(rs.getString("role")));
                }
            }
        }
        return roles;
    }

    public ObservableList<User> getAllUsers() {
        ObservableList<User> users = FXCollections.observableArrayList();
        String query = "SELECT * FROM cse360users";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String userName = rs.getString("userName");
                String password = rs.getString("password");
                String role = rs.getString("role");
                String name = rs.getString("name");
                String email = rs.getString("email");
                String phone = rs.getString("phone");
                String bio = rs.getString("bio");
                String tempPw = rs.getString("temp_password");

                // debugging print
                //System.out.println("Loaded User ID: " + id + " | Role: " + role + " | Username: " + userName);
                users.add(User.createUser(id, userName, password, role, name, email, phone, bio, tempPw));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    public List<User> getAllUsersExcept(int excludeUserId) {
        List<User> users = new ArrayList<>();
        String query = "SELECT * FROM cse360users WHERE id != ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, excludeUserId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String userName = rs.getString("userName");
                    String password = rs.getString("password");
                    String role = rs.getString("role");
                    String name = rs.getString("name");
                    String email = rs.getString("email");
                    String phone = rs.getString("phone");
                    String bio = rs.getString("bio");
                    String tempPw = rs.getString("temp_password");

                    users.add(User.createUser(id, userName, password, role, name, email, phone, bio, tempPw));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    // where request will be
    public void reviewerRequest(String userName) throws SQLException {
        String query = "INSERT INTO requestReviewerRole (userName) VALUES (?)";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, userName);
            pstmt.executeUpdate();
            System.out.println("Inserting: " + userName);
        }
    }

    public void deleteReviewerRequest(String userName) throws SQLException {
        String query = "DELETE FROM requestReviewerRole WHERE userName = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, userName);
            pstmt.executeUpdate();
            System.out.println("Deleted reviewer request for user: " + userName);
        }
    }

    public ObservableList<User> getAllReviewerRequest() {
        ObservableList<User> users = FXCollections.observableArrayList();
        String query = "SELECT * FROM requestReviewerRole";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String userName = rs.getString("userName");
                String role = rs.getString("role");
                users.add(User.createUser(id, userName, (String) null, role, (String) null, (String) null, (String) null));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    public List<User> getUsersByRole(Role role) throws SQLException {
        String sql = "SELECT id, userName, password, role, name, email, temp_password "
                + "FROM cse360users WHERE LOWER(role) = LOWER(?)";

        List<User> users = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, role.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    User u = User.createUser(
                            rs.getInt("id"),
                            rs.getString("userName"),
                            rs.getString("password"),
                            rs.getString("role"),
                            rs.getString("name"),
                            rs.getString("email"),
                            rs.getString("temp_password")
                    );
                    users.add(u);
                }
            }
        }
        return users;
    }

    public User getUserById(int id) throws SQLException {
        String sql = "SELECT id, userName, password, role, name, email, temp_password FROM cse360users WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return User.createUser(
                            rs.getInt("id"),
                            rs.getString("userName"),
                            rs.getString("password"),
                            rs.getString("role"),
                            rs.getString("name"),
                            rs.getString("email"),
                            rs.getString("temp_password")
                    );
                }
            }
        }
        return null;
    }

    // Searches for a User by both name and username
    public User getUserByName(String name) throws SQLException {
        String sql = "SELECT id, userName, password, role, name, email, temp_password "
                + "FROM cse360users "
                + "WHERE LOWER(userName) = LOWER(?) "
                + "OR LOWER(name) = LOWER(?) "
                + "OR LOWER(userName) LIKE LOWER(?) "
                + "OR LOWER(name) LIKE LOWER(?) "
                + "LIMIT 1";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            // Exact match
            ps.setString(1, name);
            ps.setString(2, name);
            // Partial match
            ps.setString(3, "%" + name + "%");
            ps.setString(4, "%" + name + "%");

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return User.createUser(
                            rs.getInt("id"),
                            rs.getString("userName"),
                            rs.getString("password"),
                            rs.getString("role"),
                            rs.getString("name"),
                            rs.getString("email"),
                            rs.getString("temp_password")
                    );
                }
            }
        }
        return null;
    }

    // Start: Questions and Answers

    // Store questions and answers
    public void insertQuestion(Question question) throws SQLException {
        String sql = "INSERT INTO questions (author, title, description, timestamp, status, follow_up, user_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, question.getAuthor());
            pstmt.setString(2, question.getTitle());
            pstmt.setString(3, question.getDescription());

            try {
                pstmt.setTimestamp(4, Timestamp.valueOf(question.getTimestamp()));
            } catch (Exception e) {
                // If timestamp is invalid, use current timestamp
                pstmt.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
            }

            pstmt.setBoolean(5, question.isResolved());
            if (question.getFollowUp() > 0) {
                pstmt.setInt(6, question.getFollowUp());
            } else {
                pstmt.setNull(6, Types.INTEGER);
            }
            pstmt.setInt(7, question.getUserId());

            int rowsInserted = pstmt.executeUpdate();
            System.out.println("Rows inserted: " + rowsInserted);

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int generateId = generatedKeys.getInt(1);
                    question.setQuestionId(generateId);
                }
            }
            System.out.println("Question author: " + question.getAuthor() + ", userId: " + question.getUserId());
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("insertQuestion failed: " + e.getMessage());
        }
    }

    public List<Question> getFollowUps(int parentId) throws SQLException{
    	List<Question> followUps = new ArrayList<>();
    	
    	String sql = "SELECT * FROM questions WHERE follow_up = ?";
    	try (PreparedStatement ps = connection.prepareStatement(sql)) {
    		ps.setInt(1, parentId);
    		ResultSet rs = ps.executeQuery();
    		
    		while (rs.next()) {
                Question q = new Question(
                    rs.getInt("question_id"),
                    rs.getInt("user_id"),
                    rs.getString("author"),
                    rs.getString("title"),
                    rs.getString("description"),
                    rs.getTimestamp("timestamp").toString(),
                    "Resolved".equalsIgnoreCase(rs.getString("status")),
                    null
                );
                
                q.setFollowUp(rs.getInt("follow_up"));
                followUps.add(q);
    		}
    	}
    	return followUps;
    }
    
    public int insertAnswer(int userId, int questionId, String author, String content) throws SQLException {
        String sql = "INSERT INTO answers (user_id, question_id, author, content, is_solution) VALUES (?, ?, ?, ?, false)";
        PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

        stmt.setInt(1, userId);
        stmt.setInt(2, questionId);
        stmt.setString(3, author);
        stmt.setString(4, content);

        stmt.executeUpdate();

        ResultSet keys = stmt.getGeneratedKeys();
        if (keys.next()) {
            return keys.getInt(1);
        }
        throw new SQLException("No generated key for answer insert");
    }
    
    public void insertAnswer(Answer answer) throws SQLException {
        String sql = "INSERT INTO answers (user_id, question_id, author, content, timestamp, is_solution) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, answer.getUserId());
            pstmt.setInt(2, answer.getQuestionId());
            pstmt.setString(3, answer.getAuthor());
            pstmt.setString(4, answer.getContent());

            try {
                pstmt.setTimestamp(5, Timestamp.valueOf(answer.getTimestamp()));
            } catch (Exception e) {
                pstmt.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
            }

            pstmt.setBoolean(6, answer.isSolution());
            pstmt.executeUpdate();
        }
    }

    public Question getQuestionById(int questionId) throws SQLException {
        String sql = "SELECT question_id, user_id, author, title, description, timestamp, status, follow_up FROM questions WHERE question_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, questionId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt("question_id");
                    int userId = rs.getInt("user_id");
                    String author = rs.getString("author");
                    String title = rs.getString("title");
                    String description = rs.getString("description");
                    Timestamp ts = rs.getTimestamp("timestamp");
                    String timestamp = ts != null ? ts.toLocalDateTime().toString() : null;
                    String statusStr = rs.getString("status");
                    boolean resolved = "Resolved".equalsIgnoreCase(statusStr); 
                    int followUp = rs.getInt("follow_up");

                    Question q = new Question(id, userId, author, title, description);
                    q.setTimestamp(timestamp);
                    q.setResolved(resolved);
                    q.setFollowUp(followUp);
                    return q;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("getQuestionById failed: " + e.getMessage());
        }

        return null;
    }

    public Question getQuestionByUser(String userName) throws SQLException {
        String sql = "SELECT question_id, user_id, author, title, description, timestamp, status, follow_up FROM questions WHERE author = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, userName);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt("question_id");
                    int userId = rs.getInt("user_id");
                    String author = rs.getString("author");
                    String title = rs.getString("title");
                    String description = rs.getString("description");
                    Timestamp ts = rs.getTimestamp("timestamp");
                    String timestamp = ts != null ? ts.toLocalDateTime().toString() : null;
                    String statusStr = rs.getString("status");
                    boolean resolved = "Resolved".equalsIgnoreCase(statusStr) || "Unresolved".equalsIgnoreCase(statusStr);
                    int followUp = rs.getInt("follow_up");

                    Question q = new Question(id, userId, author, title, description);
                    q.setTimestamp(timestamp);
                    q.setResolved(resolved);
                    q.setFollowUp(followUp);

                    return q;
                }
            }
        }

        return null;
    }

    public Answer getAnswerById(int answerId) throws SQLException {
        String sql = "SELECT answer_id, user_id, question_id, author, content, timestamp, is_solution FROM answers WHERE answer_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, answerId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt("answer_id");
                    int userId = rs.getInt("user_id");
                    int questionId = rs.getInt("question_id");
                    String author = rs.getString("author");
                    String content = rs.getString("content");
                    Timestamp ts = rs.getTimestamp("timestamp");
                    boolean isSolution = rs.getBoolean("is_solution");

                    Answer a = new Answer(id, userId, questionId, author, content, ts != null ? ts.toLocalDateTime().toString() : null, isSolution);
                    return a;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Question> loadAllQs() {

        List<Question> questions = new ArrayList<>();
        String sql = "SELECT * FROM questions";
        try (PreparedStatement pstmt = connection.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                int id = rs.getInt("question_id");
                String author = rs.getString("author");
                String title = rs.getString("title");
                String description = rs.getString("description");
                Timestamp ts = rs.getTimestamp("timestamp");
                String timestamp = ts != null ? ts.toLocalDateTime().toString() : null;
                String statusStr = rs.getString("status");
                boolean resolved = "Resolved".equalsIgnoreCase(statusStr) || "Unresolved".equalsIgnoreCase(statusStr);
                int followUp = rs.getInt("follow_up");
                int userId = rs.getInt("user_id");
                //System.out.println("LOADED QUESTION: ID=" + id + " | Author=" + author + " | Title=" + title);
                if (rs.wasNull()) {
                    followUp = 0;
                }

                Question q = new Question(id, userId, author, title, description, timestamp, resolved, new ArrayList<>());
                q.setTimestamp(timestamp);
                q.setResolved(resolved);
                q.setFollowUp(followUp);

                // Attach the answers and load them
                List<Answer> answers = loadAnswersForQs(id);
                q.setAnswers(answers);

                questions.add(q);
                List<Edits> edits = loadEditHistoryForQuestion(q.getQuestionId());
                q.setEditHistory(edits);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return questions;
    }

    // Load all answers
    public List<Answer> loadAllAnswers() {
        List<Answer> answers = new ArrayList<>();
        String sql = "SELECT * FROM answers";
        try (PreparedStatement pstmt = connection.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                int answerId = rs.getInt("answer_id");
                int userId = rs.getInt("user_id");
                int questionId = rs.getInt("question_id");
                String author = rs.getString("author");
                String content = rs.getString("content");
                Timestamp ts = rs.getTimestamp("timestamp");
                String timestamp = ts != null ? ts.toLocalDateTime().toString() : null;
                boolean isSolution = rs.getBoolean("is_solution");

                Answer a = new Answer(answerId, userId, questionId, author, content, timestamp, isSolution);
                answers.add(a);

            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error loading answers: " + e.getMessage());
        }
        return answers;
    }

    // Load answers for a question
    public List<Answer> loadAnswersForQs(int questionId) {
        List<Answer> answers = new ArrayList<>();
        String sql = "SELECT * FROM answers WHERE question_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, questionId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int answerId = rs.getInt("answer_id");
                    int userId = rs.getInt("user_id");
                    String author = rs.getString("author");
                    String content = rs.getString("content");
                    Timestamp ts = rs.getTimestamp("timestamp");
                    String timestamp = ts != null ? ts.toLocalDateTime().toString() : null;
                    boolean isSolution = rs.getBoolean("is_solution");

                    Answer answer = new Answer(answerId, userId, questionId, author, content, timestamp, isSolution);

                    answers.add(answer);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return answers;
    }

    public List<Question> searchQuestions(String keyword, String author, Boolean resolved) {
        List<Question> result = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM questions WHERE 1=1");

        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (title LIKE ? OR description LIKE ?)");
        }
        if (author != null && !author.isBlank()) {
            sql.append(" AND LOWER(author) LIKE ?");
        }
        if (resolved != null) {
            sql.append(" AND LOWER(status) = ?");
        }
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql.toString())) {
            int index = 1;

            if (keyword != null && !keyword.isBlank()) {
                pstmt.setString(index++, "%" + keyword + "%");
                pstmt.setString(index++, "%" + keyword + "%");
            }
            if (author != null && !author.isBlank()) {
                pstmt.setString(index++, "%" + author.toLowerCase() + "%");
            }
            if (resolved != null) {
                pstmt.setString(index++, resolved ? "resolved" : "false");
            }
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("question_id");
                    String title = rs.getString("title");
                    String description = rs.getString("description");
                    String auth = rs.getString("author");
                    Timestamp ts = rs.getTimestamp("timestamp");
                    String time = ts != null ? ts.toLocalDateTime().toString() : null;
                    String statusStr = rs.getString("status");
                    boolean res = "Resolved".equalsIgnoreCase(statusStr);
                    int followUp = rs.getInt("follow_up");
                    if (rs.wasNull()) {
                        followUp = 0;
                    }

                    Question q = new Question(id, -1, auth, title, description);
                    q.setTimestamp(time);
                    q.setResolved(res);
                    q.setFollowUp(followUp);
                    q.setAnswers(loadAnswersForQs(id));
                    result.add(q);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    // Search answers
    public List<Answer> searchAnswers(String keyword, String author, Boolean isSolution) {
        List<Answer> results = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM answers WHERE 1=1");

        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND LOWER(content) LIKE ?");
        }
        if (author != null && !author.isBlank()) {
            sql.append(" AND LOWER(author) LIKE ?");
        }
        if (isSolution != null) {
            sql.append(" AND is_solution = ?");
        }

        try (PreparedStatement pstmt = connection.prepareStatement(sql.toString())) {
            int paramIndex = 1;

            if (keyword != null && !keyword.isBlank()) {
                pstmt.setString(paramIndex++, "%" + keyword.toLowerCase() + "%");
            }
            if (author != null && !author.isBlank()) {
                pstmt.setString(paramIndex++, "%" + author.toLowerCase() + "%");
            }
            if (isSolution != null) {
                pstmt.setBoolean(paramIndex++, isSolution);
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int answerId = rs.getInt("answer_id");
                    int userId = rs.getInt("user_id");
                    int questionId = rs.getInt("question_id");
                    String ansAuthor = rs.getString("author");
                    String content = rs.getString("content");
                    Timestamp ts = rs.getTimestamp("timestamp");
                    String timestamp = ts != null ? ts.toLocalDateTime().toString() : null;
                    Boolean solution = rs.getBoolean("is_solution");

                    Answer a = new Answer(answerId, -1, questionId, ansAuthor, content, timestamp, solution);
                    results.add(a);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error searching answers: " + e.getMessage());
        }
        return results;

    }

    // Update existing question (title, description, status)
    public void updateQuestion(Question question) throws SQLException {
        String sql = "UPDATE questions SET title = ?, description = ?, status = ?, timestamp = ? WHERE question_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, question.getTitle());
            pstmt.setString(2, question.getDescription());
            pstmt.setBoolean(3, question.isResolved());

            try {
                pstmt.setTimestamp(4, Timestamp.valueOf(question.getTimestamp()));
            } catch (Exception e) {
                pstmt.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
            }

            pstmt.setInt(5, question.getQuestionId());

            int rowsUpdated = pstmt.executeUpdate();
            if (rowsUpdated == 0) {
                System.err.println("⚠️ No question found with ID: " + question.getQuestionId());
            } else {
                System.out.println(" Question " + question.getQuestionId() + " updated successfully!");
            }
        } catch (SQLException e) {
            System.err.println(" updateQuestion failed: " + e.getMessage());
            throw e;
        }
    }

    // Delete a question by ID
    public void deleteQuestion(int questionId) throws SQLException {
        // First delete all answers associated with this question
        String deleteAnswers = "DELETE FROM answers WHERE question_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(deleteAnswers)) {
            pstmt.setInt(1, questionId);
            pstmt.executeUpdate();
        }

        // Then delete the question itself
        String deleteQuestion = "DELETE FROM questions WHERE question_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(deleteQuestion)) {
            pstmt.setInt(1, questionId);
            int rowsDeleted = pstmt.executeUpdate();
            if (rowsDeleted == 0) {
                System.err.println("⚠️ No question found with ID: " + questionId);
            } else {
                System.out.println(" Question " + questionId + " deleted successfully!");
            }
        }
    }

    // Delete an answer by ID
    public void deleteAnswer(int answerId) throws SQLException {
        String deleteAnswer = "DELETE FROM answers WHERE answer_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(deleteAnswer)) {
            pstmt.setInt(1, answerId);
            int rowsDeleted = pstmt.executeUpdate();
            if (rowsDeleted == 0) {
                System.err.println("⚠️ No answer found with ID: " + answerId);
            } else {
                System.out.println(" Answer " + answerId + " deleted successfully!");
            }
        }
    }

    // Update an existing answer in the database
    public void updateAnswer(Answer answer) throws SQLException {
        String sql = "UPDATE answers SET content = ?, is_solution = ? WHERE answer_id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, answer.getContent());
            pstmt.setBoolean(2, answer.isSolution());
            pstmt.setInt(3, answer.getAnswerId());

            int rowsUpdated = pstmt.executeUpdate();

            if (rowsUpdated == 0) {
                System.err.println("⚠️ No answer found with ID: " + answer.getAnswerId());
            } else {
                System.out.println(" Answer " + answer.getAnswerId() + " updated successfully!");
            }
        }
    }

    public List<Question> getQuestionsByUser(String username) {
        List<Question> questionsByUser = new ArrayList<>();
        String sql = "SELECT * FROM questions WHERE LOWER(author) = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username.toLowerCase());
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("question_id");
                    String title = rs.getString("title");
                    String description = rs.getString("description");
                    String author = rs.getString("author");
                    Timestamp ts = rs.getTimestamp("timestamp");
                    String timestamp = ts != null ? ts.toLocalDateTime().toString() : null;
                    String statusStr = rs.getString("status");
                    boolean resolved = "Resolved".equalsIgnoreCase(statusStr) || "Unresolved".equalsIgnoreCase(statusStr);
                    int followUp = rs.getInt("follow_up");
                    if (rs.wasNull()) followUp = 0;

                    Question q = new Question(id, -1, author, title, description);
                    q.setTimestamp(timestamp);
                    q.setResolved(resolved);
                    q.setFollowUp(followUp);

                    q.setAnswers(loadAnswersForQs(id));

                    questionsByUser.add(q);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return questionsByUser;
    }

    public void updateQuestionResolved(int questionId, boolean resolved) throws SQLException {
        String statusStr = resolved ? "Resolved" : "Unresolved";  // or "Closed"/"open"
        String sql = "UPDATE questions SET status = ? WHERE question_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, statusStr);
            ps.setInt(2, questionId);
            ps.executeUpdate();
        }
    }
    
//    public void updateQuestionStatus(int questionId, String status) throws SQLException {
//        String sql = "UPDATE questions SET status = ? WHERE question_id = ?";
//        try (PreparedStatement ps = connection.prepareStatement(sql)) {
//            ps.setString(1, status);
//            ps.setInt(2, questionId);
//            ps.executeUpdate();
//        }
//    }
    
    public int updateAnswerSolutionStatus(int answerId, boolean isSolution) throws SQLException {
        String sql = "UPDATE answers SET is_solution = ? WHERE answer_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setBoolean(1, isSolution);
            ps.setInt(2, answerId);
            return ps.executeUpdate();
        }
    }
    
//    public void markQuestionResolved(int questionId) throws SQLException {
//        String sql = "UPDATE questions SET status = 'resolved' WHERE question_id = ?";
//        try (PreparedStatement ps = connection.prepareStatement(sql)) {
//            ps.setInt(1, questionId);
//            ps.executeUpdate();
//        }
//    }
//    public void markQuestionUnresolved(int questionId) throws SQLException {
//        String sql = "UPDATE questions SET status = 'resolved' WHERE question_id = ?";
//        try (PreparedStatement ps = connection.prepareStatement(sql)) {
//            ps.setInt(1, questionId);
//            ps.executeUpdate();
//        }
//    }
    
    public List<Answer> getAnswersByUser(String username) {
        List<Answer> answersByUser = new ArrayList<>();
        String sql = "SELECT * FROM answers WHERE LOWER(author) = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username.toLowerCase());
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int answerId = rs.getInt("answer_id");
                    int userId = rs.getInt("user_id");
                    int questionId = rs.getInt("question_id");
                    String author = rs.getString("author");
                    String content = rs.getString("content");
                    Timestamp ts = rs.getTimestamp("timestamp");
                    String timestamp = ts != null ? ts.toLocalDateTime().toString() : null;
                    boolean isSolution = rs.getBoolean("is_solution");

                    Answer a = new Answer(answerId, userId, questionId, author, content, timestamp, isSolution);
                    answersByUser.add(a);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return answersByUser;
    }

    public List<Answer> getAnswersByQuestionId(int questionId) throws SQLException {
        List<Answer> answers = new ArrayList<>();

        String sql = "SELECT * FROM answers WHERE question_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, questionId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                	boolean isSolution = rs.getBoolean("is_solution");
                    Answer answer = new Answer(
                        rs.getInt("answer_id"),
                        rs.getInt("user_id"),
                        rs.getInt("question_id"),
                        rs.getString("author"),
                        rs.getString("content"),
                        sql, isSolution
                    );
                    answers.add(answer);
                }
            }
        }

        return answers;
    }
    
    // End: Questions and Answers

    // Begin: Clarification functions

    // Insert Clarification
    public void insertClarification(Clarification clarification) throws SQLException {
        String sql = "INSERT INTO clarifications (question_id, answer_id, author_id, recipient_id, author, content, timestamp, is_read, is_public) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            if (clarification.getQuestionId() > 0) {
                pstmt.setInt(1, clarification.getQuestionId());
            } else {
                pstmt.setNull(1, Types.INTEGER);
            }

            if (clarification.getAnswerId() > 0) {
                pstmt.setInt(2, clarification.getAnswerId());
            } else {
                pstmt.setNull(2, Types.INTEGER);
            }

            pstmt.setInt(3, clarification.getAuthorId());
            pstmt.setInt(4, clarification.getRecipientId());
            pstmt.setString(5, clarification.getAuthor());
            pstmt.setString(6, clarification.getContent());
            pstmt.setTimestamp(7, Timestamp.valueOf(clarification.getTimestamp()));
            pstmt.setBoolean(8, clarification.isRead());
            pstmt.setBoolean(9, clarification.isPublic());

            pstmt.executeUpdate();
        }
    }
    

    // Load clarifications logic
    public List<Clarification> loadClarifications(String type, int id) throws SQLException {
        List<Clarification> clarifications = new ArrayList<>();
        String sql;

        switch (type) {
            case "question_id":
                sql = "SELECT * FROM clarifications WHERE question_id = ?";
                break;
            case "answer_id":
                if (id > 0) {
                    sql = "SELECT * FROM clarifications WHERE answer_id = ?";
                } else {
                    sql = "SELECT * FROM clarifications WHERE answer_id IS NULL";
                }
                break;
            case "recipient_id":
                sql = "SELECT * FROM clarifications WHERE recipient_id = ?";
                break;
            default:
                throw new IllegalArgumentException("Invalid clarification type: " + type);
        }

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            
            if (!(type.equals("answer_id") && id <= 0)) {
                pstmt.setInt(1, id);
            }
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                	Clarification c = extractClarification(rs);
                    clarifications.add(c);
                }
            }
        }
        return clarifications;
    }
    
    public List<Clarification> loadClarificationsForQuestionAndUser(Question question, User user) throws SQLException {
        String sql;
        boolean isPrivileged = user.getRole() != null &&
             (user.getRole().name().equalsIgnoreCase("ADMIN")
               || user.getRole().name().equalsIgnoreCase("INSTRUCTOR")
               || user.getRole().name().equalsIgnoreCase("STAFF"));
        if (isPrivileged) {
            sql = "SELECT * FROM clarifications WHERE question_id = ?";
        } else {
            sql = "SELECT * FROM clarifications WHERE question_id = ? AND (recipient_id = ? OR is_public = TRUE)";
        }
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, question.getQuestionId());
            if (!isPrivileged) {
                stmt.setInt(2, user.getId());
            }
            try (ResultSet rs = stmt.executeQuery()) {
                List<Clarification> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(extractClarification(rs));
                }
                return list;
            }
        }
    }

    private Clarification extractClarification(ResultSet rs) throws SQLException {
        Clarification c = new Clarification(
            rs.getInt("clarification_id"),
            rs.getInt("question_id"),
            rs.getInt("answer_id"),
            rs.getInt("author_id"),
            rs.getInt("recipient_id"),
            rs.getString("author"),
            rs.getString("content"),
            rs.getTimestamp("timestamp").toLocalDateTime(),
            rs.getBoolean("is_read"),
            rs.getBoolean("is_public")   // assuming you added is_public
        );
        
        if (c.getAnswerId() > 0) {
            c.setItemType(ContentType.ANSWER);
            c.setContentId(c.getAnswerId());

            Answer a = getAnswerById(c.getAnswerId());
            Question q = (a != null) ? getQuestionById(a.getQuestionId()) : null;
            c.setQuestionTitle(q != null ? "Answer to: " + q.getTitle() : "[Unknown Answer]");
        } else {
            c.setItemType(ContentType.QUESTION);
            c.setContentId(c.getQuestionId());

            Question q = getQuestionById(c.getQuestionId());
            c.setQuestionTitle(q != null ? q.getTitle() : "[Unknown Question]");
        }

        return c;
    }
    
    // Load clarifications for question
    public List<Clarification> loadClarificationsforQ(int questionId) throws SQLException {
        return loadClarifications("question_id", questionId);
    }

    // Load clarifications for answer
    public List<Clarification> loadClarificationsforA(int answerId) throws SQLException {
        return loadClarifications("answer_id", answerId);
    }
    
    public List<Clarification> loadAllSuggestionsForUser(int recipientId) throws SQLException {
        String sql = "SELECT * FROM clarifications WHERE recipient_id = ?";
        List<Clarification> suggestions = new ArrayList<>();
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, recipientId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    suggestions.add(extractClarification(rs));
                }
            }
        }
        return suggestions;
    }
    
    public List<Clarification> loadPrivateSuggestionsForUser(int recipientId) throws SQLException {
        String sql = "SELECT * FROM clarifications WHERE recipient_id = ? AND is_public = FALSE";
        List<Clarification> suggestions = new ArrayList<>();
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, recipientId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    suggestions.add(extractClarification(rs));
                }
            }
        }
        return suggestions;
    }
    
    public void makeClarificationPublic(int clarificationId) throws SQLException {
        String sql = "UPDATE clarifications SET is_public = TRUE WHERE clarification_id=?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, clarificationId);
            pstmt.executeUpdate();
        }
    }

    public List<Clarification> loadClarificationsForUser(int recipientId) throws SQLException {
        return loadClarifications("recipient_id", recipientId);
    }
    
    public void closeConnection() {
        try {
            if (statement != null) statement.close();
        } catch (SQLException se) {
            se.printStackTrace();
        }
        try {
            if (connection != null) connection.close();
        } catch (SQLException se) {
            se.printStackTrace();
        }
    }

    public void markClarificationAsRead(int clarificationId) throws SQLException {
        String sql = "UPDATE clarifications SET is_read = TRUE WHERE clarification_id=?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, clarificationId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error marking clarifications as read: " + e.getMessage());
        }
    }

    // End: Clarification functions

    // Begin: Private Message functions

    public void sendMessage(Messages msg) {

        if (msg.getMessage() == null || msg.getMessage().trim().isEmpty()) {
            System.out.println("Cannot send empty message.");
            return;
        }
        String content = msg.getMessage().trim();
        if (content.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Message cannot be empty.");
            alert.show();
            return;
        }

        try {
            String sql = "INSERT INTO privateMessages (sender_id, recipient_id, message, timestamp, is_read) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setInt(1, msg.getSenderId());
            pstmt.setInt(2, msg.getRecipientId());
            pstmt.setString(3, msg.getMessage());
            pstmt.setTimestamp(4, Timestamp.valueOf(msg.getTimestamp()));
            pstmt.setBoolean(5, msg.isRead());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error sending message: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public List<Messages> getMessagesForUser(int userId) {

        List<Messages> messages = new ArrayList<>();
        try {
            // join messages table with users table to be able to get sender's name via sender_id
            String sql = "SELECT m.id, m.sender_id, m.recipient_id, m.message, m.timestamp, m.is_read, "
                    + "COALESCE(u.name, u.userName) AS sender_name "
                    + "FROM privateMessages m "
                    + "JOIN cse360users u ON m.sender_id = u.id "
                    + "WHERE m.recipient_id=? "
                    + "ORDER BY m.timestamp DESC";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setInt(1, userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Messages msg = new Messages(
                            rs.getInt("id"),
                            rs.getInt("sender_id"),
                            rs.getInt("recipient_id"),
                            rs.getString("message"),
                            rs.getTimestamp("timestamp").toLocalDateTime(),
                            rs.getBoolean("is_read")
                    );
                    msg.setSenderName(rs.getString("sender_name"));
                    messages.add(msg);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving inbox for user " + userId + ": " + e.getMessage());
            e.printStackTrace();
        }
        return messages;
    }

    public List<Messages> getSentMessagesForUser(int userId) {

        List<Messages> sentMessages = new ArrayList<>();
        try {
            String sql = "SELECT m.id, m.sender_id, m.recipient_id, m.message, m.timestamp, m.is_read, "
                    + "COALESCE(u.name, u.userName) AS recipient_name "
                    + "FROM privateMessages m "
                    + "JOIN cse360users u ON m.recipient_id = u.id "
                    + "WHERE m.sender_id = ? "
                    + "ORDER BY m.timestamp DESC";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setInt(1, userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Messages msg = new Messages(
                            rs.getInt("id"),
                            rs.getInt("sender_id"),
                            rs.getInt("recipient_id"),
                            rs.getString("message"),
                            rs.getTimestamp("timestamp").toLocalDateTime(),
                            rs.getBoolean("is_read")
                    );
                    msg.setRecipientName(rs.getString("recipient_name"));
                    sentMessages.add(msg);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving sent messages for user " + userId + ": " + e.getMessage());
            e.printStackTrace();
        }
        return sentMessages;
    }

    public List<Messages> getMessagesBetweenUsers(int user1, int user2) {
        List<Messages> messages = new ArrayList<>();

        try {
            String sql = "SELECT * FROM privateMessages WHERE "
                    + "((sender_id=? AND recipient_id=?) OR (sender_id=? AND recipient_id=?)) "
                    + "ORDER BY timestamp DESC";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setInt(1, user1);
            pstmt.setInt(2, user2);
            pstmt.setInt(3, user2);
            pstmt.setInt(4, user1);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    messages.add(new Messages(
                            rs.getInt("id"),
                            rs.getInt("sender_id"),
                            rs.getInt("recipient_id"),
                            rs.getString("message"),
                            rs.getTimestamp("timestamp").toLocalDateTime(),
                            rs.getBoolean("is_read")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving messages between users " + user1 + ", " + user2 + ": " + e.getMessage());
            e.printStackTrace();
        }
        return messages;
    }

    public void markMessagesAsRead(int messageId) throws SQLException {
        String sql = "UPDATE privateMessages SET is_read = TRUE WHERE id=?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, messageId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error marking messages as read: " + e.getMessage());
        }
    }

    // End: Private Message functions

    // Begin: Trusted Reviewer Functions

    public boolean addTrustedReviewer(int studentId, int reviewerId) throws SQLException {
        String sql = "MERGE INTO trustedReviewers (student_id, reviewer_id) "
                + "KEY (student_id, reviewer_id) VALUES (?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, reviewerId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean removeTrustedReviewer(int studentId, int reviewerId) throws SQLException {
        String sql = "DELETE FROM trustedReviewers WHERE student_id=? AND reviewer_id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, reviewerId);
            return ps.executeUpdate() > 0;
        }
    }

    public List<Integer> getTrustedReviewerIds(int studentId) throws SQLException {
        String sql = "SELECT reviewer_id FROM trustedReviewers WHERE student_id=?";
        List<Integer> ids = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) ids.add(rs.getInt(1));
            }
        }
        return ids;
    }

    public boolean isTrusted(int studentId, int reviewerId) throws SQLException {
        String sql = "SELECT 1 FROM trustedReviewers WHERE student_id=? AND reviewer_id=? LIMIT 1";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, reviewerId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public void updateTrustedReviewerRating(int studentId, int reviewerId, int rating) throws SQLException {
        String sql = "UPDATE trustedReviewers SET rating = ? WHERE student_id = ? AND reviewer_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, rating);
            ps.setInt(2, studentId);
            ps.setInt(3, reviewerId);
            ps.executeUpdate();
        }
    }

    public Map<Integer, Integer> getTrustedReviewerRatings(int studentId) throws SQLException {
        String sql = "SELECT reviewer_id, rating FROM trustedReviewers WHERE student_id=?";
        Map<Integer, Integer> map = new HashMap<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int reviewerId = rs.getInt("reviewer_id");
                    int rating = rs.getInt("rating");
                    map.put(reviewerId, rating);
                }
            }
        }
        return map;
    }

    // End: Trusted Reviewer Functions

    // Reviews

    public List<Review> loadAllReviews() {
        List<Review> reviews = new ArrayList<>();
        String sql = "SELECT * FROM reviews";
        try (PreparedStatement pstmt = connection.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                int reviewId = rs.getInt("review_id");
                int userId = rs.getInt("user_id");
                int answerId = rs.getInt("answer_id");
                String author = rs.getString("author");
                String content = rs.getString("content");
                String timestamp = rs.getString("timestamp");

                Review r = new Review(reviewId, userId, answerId, author, content);
                reviews.add(r);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error loading reviews: " + e.getMessage());
        }
        return reviews;
    }

    /**
     * Insert a Review (no rating column).
     * Upon success, the generated review_id is set into the Review object (if Review has a setter).
     */
    public void insertReview(Review review) throws SQLException {
        String sql = "INSERT INTO reviews (user_id, answer_id, author, content, timestamp) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (review.getUserId() > 0) {
                pstmt.setInt(1, review.getUserId());
            } else {
                pstmt.setNull(1, Types.INTEGER);
            }

            if (review.getAnswerId() > 0) {
                pstmt.setInt(2, review.getAnswerId());
            } else {
                pstmt.setNull(2, Types.INTEGER);
            }

            pstmt.setString(3, review.getAuthor());
            pstmt.setString(4, review.getContent());

            // timestamp (if Review provides one, otherwise generate)
            String ts = null;
            try {
                ts = review.getTimestamp();
            } catch (NoSuchMethodError ignored) { /* ignore if Review doesn't expose timestamp */ }

            if (ts == null || ts.isBlank()) {
                ts = String.valueOf(System.currentTimeMillis());
            }
            pstmt.setString(5, ts);

            int rows = pstmt.executeUpdate();

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int generatedId = rs.getInt(1);
                    try {
                        review.setReviewId(generatedId); // assumes Review has setter
                    } catch (NoSuchMethodError ignored) {
                        // if Review does not have setter, ignore
                    }
                }
            }
        }
    }

    // Load reviews for a specific answer
    public List<Review> loadReviewsForAnswer(int answerId) throws SQLException {
        List<Review> reviews = new ArrayList<>();
        String sql = "SELECT * FROM reviews WHERE answer_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, answerId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int reviewId = rs.getInt("review_id");
                    int userId = rs.getInt("user_id");
                    String author = rs.getString("author");
                    String content = rs.getString("content");
                    String timestamp = rs.getString("timestamp");

                    Review review = new Review(reviewId, userId, answerId, author, content);
                    reviews.add(review);
                }
            }
        }
        return reviews;
    }

    // Update an existing review
    public void updateReview(Review review) throws SQLException {
        String sql = "UPDATE reviews SET content = ?, timestamp = ? WHERE review_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, review.getContent());
            try {
                pstmt.setString(2, review.getTimestamp());
            } catch (NoSuchMethodError ignored) {
                pstmt.setString(2, String.valueOf(System.currentTimeMillis()));
            }
            pstmt.setInt(3, review.getReviewId());

            int rowsUpdated = pstmt.executeUpdate();
            if (rowsUpdated == 0) {
                System.err.println("⚠️ No review found with ID: " + review.getReviewId());
            } else {
                System.out.println(" Review " + review.getReviewId() + " updated successfully!");
            }
        }
    }

    // Delete a review by ID
    public void deleteReview(int reviewId) throws SQLException {
        String sql = "DELETE FROM reviews WHERE review_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, reviewId);
            int rowsDeleted = pstmt.executeUpdate();
            if (rowsDeleted == 0) {
                System.err.println("⚠️ No review found with ID: " + reviewId);
            } else {
                System.out.println(" Review " + reviewId + " deleted successfully!");
            }
        }
    }

    public List<Review> getReviewsByReviewer(int reviewerId) throws SQLException {
        String sql = "SELECT review_id, user_id, answer_id, author, content, timestamp FROM reviews WHERE user_id = ?";

        List<Review> reviews = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, reviewerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Review r = new Review(
                            rs.getInt("review_id"),
                            rs.getInt("user_id"),
                            rs.getInt("answer_id"),
                            rs.getString("author"),
                            rs.getString("content")
                    );
                    reviews.add(r);
                }
            }
        }
        return reviews;
    }
    
    public String getInstructorScoringMethod() throws SQLException {
        String sql = "SELECT scoring_method FROM instructorSettings LIMIT 1";
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getString("scoring_method");
            }
        }
        return "HIGHEST_SUM"; // default
    }

    public void updateInstructorScoringMethod(String method) throws SQLException {
        String sql = "MERGE INTO instructorSettings (id, scoring_method) KEY(id) VALUES (1, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, method);
            ps.executeUpdate();
        }
    }
    
    
    public void updateReviewerScore(int reviewerId, int score, String method) throws SQLException {
        String sql = "MERGE INTO reviewerScoreCard (reviewer_id, score, scoring_method) KEY(reviewer_id) VALUES (?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, reviewerId);
            ps.setInt(2, score);
            ps.setString(3, method);
            ps.executeUpdate();
        }
    }

    public int calculateHighestSumScore(int reviewerId) throws SQLException { String sql = "SELECT SUM(rating) AS sum_score FROM allReviewerRatings WHERE reviewer_id = ?"; 
    try (PreparedStatement ps = connection.prepareStatement(sql)) 
    { ps.setInt(1, reviewerId); 
    try (ResultSet rs = ps.executeQuery()) { 
    	if (rs.next()) { int sumScore = (int) Math.round(rs.getDouble("sum_score")); 
    	updateReviewerScore(reviewerId, sumScore, "HIGHEST_SUM"); 
    	return sumScore; } } } return 0; // if no ratings }
    }
    
    public int calculateAverageScore(int reviewerId) throws SQLException {
        String sql = "SELECT AVG(rating) AS avg_score FROM allReviewerRatings WHERE reviewer_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, reviewerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int avgScore = (int) Math.round(rs.getDouble("avg_score"));
                    updateReviewerScore(reviewerId, avgScore, "AVERAGE");
                    return avgScore;
                }
            }
        }
        return 0; // if no ratings
    }

    public int calculateReviewerScore(int reviewerId) throws SQLException {
        String method = getInstructorScoringMethod();
        switch (method.toUpperCase()) {
            case "AVERAGE":
                return calculateAverageScore(reviewerId);
            case "HIGHEST_SUM":
                return calculateHighestSumScore(reviewerId);
            default:
                return calculateHighestSumScore(reviewerId);
        }
    }

    /**
     * Retrieves average reviewer scores for all reviewers based on the current user's ratings.
     */
    public List<TrustedReviewerRow> getReviewerScores() throws SQLException {
        String sql = "SELECT reviewer_id, AVG(rating) AS score "
                   + "FROM trustedReviewers "
                   + "GROUP BY reviewer_id "
                   + "ORDER BY score DESC";

        List<TrustedReviewerRow> scores = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int reviewerId = rs.getInt("reviewer_id");
                double score = rs.getDouble("score");

                User u = getUserById(reviewerId);
                if (u != null) {
                    scores.add(new TrustedReviewerRow(reviewerId, u.getUserName(), u.getName(), score));
                }
            }
        }
        return scores;
    }

    /**
     * Retrieves total scores for all reviewers across all students.
     */
    public List<TrustedReviewerRow> getAllReviewerScores() throws SQLException {
        String sql = "SELECT u.id AS reviewer_id, u.userName, u.name, "
                   + "COALESCE(SUM(a.rating), 0) AS total_score "
                   + "FROM cse360users u "
                   + "LEFT JOIN allReviewerRatings a ON u.id = a.reviewer_id "
                   + "GROUP BY u.id, u.userName, u.name "
                   + "ORDER BY total_score DESC";

        List<TrustedReviewerRow> scores = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int reviewerId = rs.getInt("reviewer_id");
                int totalScore = rs.getInt("total_score"); // aggregated sum

                scores.add(new TrustedReviewerRow(
                    reviewerId,
                    rs.getString("userName"),
                    rs.getString("name"),
                    totalScore
                ));
            }
        }
        return scores;
    }

    public boolean addReviewerScore(int reviewerId, int studentId, double rating) throws SQLException {
        String sql = "MERGE INTO allReviewerRatings KEY(reviewer_id, student_id) VALUES (?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, reviewerId);
            ps.setInt(2, studentId);
            ps.setDouble(3, rating);
            return ps.executeUpdate() > 0;
        }
    }

    public void updateAllReviewerRating(int reviewerId, double rating) throws SQLException {
        String sql = "UPDATE allReviewerRatings SET avg_rating = ? WHERE reviewer_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setDouble(1, rating);
            ps.setInt(2, reviewerId);
            ps.executeUpdate();
        }
    }


    // End: Reviews

    // Moderation Flags / Notes

    public int insertModerationFlag(String itemType, int itemId, int staffId, String reason) throws SQLException {
        String sql = "INSERT INTO moderationFlags (item_type, item_id, staff_id, reason) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, itemType);
            ps.setInt(2, itemId);
            ps.setInt(3, staffId);
            ps.setString(4, reason);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return -1;
    }

    public int insertModerationFlag(ContentType itemType, int itemId, int staffId, String reason) throws SQLException {
        return insertModerationFlag(itemType.toDatabaseString(), itemId, staffId, reason);
    }

    public List<ModerationFlag> loadAllModerationFlags() throws SQLException {
        List<ModerationFlag> flags = new ArrayList<>();
        String sql = "SELECT * FROM moderationFlags ORDER BY created_at DESC";
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ModerationFlag flag = new ModerationFlag(
                        rs.getInt("flag_id"),
                        rs.getString("item_type"),
                        rs.getInt("item_id"),
                        rs.getInt("staff_id"),
                        rs.getString("reason"),
                        rs.getString("status"),
                        rs.getTimestamp("created_at"));
                flags.add(flag);
            }
        }
        return flags;
    }

    public List<ModerationFlag> loadModerationFlagsByStatus(String status) throws SQLException {
        List<ModerationFlag> flags = new ArrayList<>();
        String sql = "SELECT * FROM moderationFlags WHERE status = ? ORDER BY created_at DESC";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ModerationFlag flag = new ModerationFlag(
                            rs.getInt("flag_id"),
                            rs.getString("item_type"),
                            rs.getInt("item_id"),
                            rs.getInt("staff_id"),
                            rs.getString("reason"),
                            rs.getString("status"),
                            rs.getTimestamp("created_at"));
                    flags.add(flag);
                }
            }
        }
        return flags;
    }

    public void updateModerationFlagStatus(int flagId, String status) throws SQLException {
        String sql = "UPDATE moderationFlags SET status = ? WHERE flag_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, flagId);
            ps.executeUpdate();
        }
    }

    public String retrieveFlaggedContent(String itemType, int itemId) {
        String sql = switch (itemType.toLowerCase()) {
            case "question" -> "SELECT description FROM questions WHERE question_id = ?";
            case "answer" -> "SELECT content FROM answers WHERE answer_id = ?";
            case "review" -> "SELECT content FROM reviews WHERE review_id = ?";
            case "suggestion" -> "SELECT content FROM clarifications WHERE clarification_id = ?";
            default -> null;
        };

        if (sql == null) return "Unsupported content type: " + itemType;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, itemId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "Error retrieving content: " + e.getMessage();
        }

        return "No content found.";
    }

    public User retrieveFlaggedUser(String itemType, int itemId) {
        String sql = switch (itemType.toLowerCase()) {
            case "question" -> "SELECT user_id FROM questions WHERE question_id = ?";
            case "answer" -> "SELECT user_id FROM answers WHERE answer_id = ?";
            case "review" -> "SELECT user_id FROM reviews WHERE review_id = ?";
            case "suggestion" -> "SELECT recipient_id FROM clarifications WHERE clarification_id = ?"; // clarifications stores recipient/author
            default -> null;
        };

        if (sql == null) {
            System.out.println("DEBUG: unknown itemType -> returning null");
            return null;
        }

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, itemId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int userId = rs.getInt(1);

                    // return the user
                    String userQuery = "SELECT id, userName, role, name, email FROM cse360users WHERE id = ?";
                    try (PreparedStatement userPs = connection.prepareStatement(userQuery)) {
                        userPs.setInt(1, userId);
                        try (ResultSet userRs = userPs.executeQuery()) {
                            if (userRs.next()) {
                                System.out.println("DEBUG: found user row id=" + userRs.getInt("id") +
                                        ", userName=" + userRs.getString("userName") +
                                        ", role=" + userRs.getString("role"));
                                return User.createUser(
                                        userRs.getInt("id"),
                                        userRs.getString("userName"),
                                        "",
                                        userRs.getString("role"),
                                        userRs.getString("name"),
                                        userRs.getString("email"),
                                        null
                                );
                            } else {
                                System.out.println("DEBUG: SQL returned userId=" + userId);
                                System.out.println("DEBUG: no user row found for id=" + userId);
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        System.out.println("DEBUG: retrieveFlaggedUser returning null");
        return null;
    }

    public void insertModerationNote(int flagId, int staffId, String noteText) throws SQLException {
        String sql = "INSERT INTO moderationNotes (flag_id, staff_id, note_text) VALUES (?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, flagId);
            ps.setInt(2, staffId);
            ps.setString(3, noteText);
            ps.executeUpdate();
        }
    }

    public List<ModerationNote> loadModerationNotesForFlag(int flagId) throws SQLException {
        List<ModerationNote> notes = new ArrayList<>();
        String sql = "SELECT * FROM moderationNotes WHERE flag_id = ? ORDER BY created_at DESC";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, flagId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ModerationNote note = new ModerationNote(
                            rs.getInt("note_id"),
                            rs.getInt("flag_id"),
                            rs.getInt("staff_id"),
                            rs.getString("note_text"),
                            rs.getTimestamp("created_at"));
                    notes.add(note);
                }
            }
        }
        return notes;
    }

    // ==================================================================================
    // NOTIFICATIONS
    // ==================================================================================

    /**
     * Creates the notifications table if it doesn't exist.
     */
    public void createNotificationsTable() throws SQLException {
        String createTableSQL = """
            CREATE TABLE IF NOT EXISTS notifications (
                notification_id INT AUTO_INCREMENT PRIMARY KEY,
                user_id INT NOT NULL,
                type VARCHAR(100) NOT NULL,
                related_id INT,
                title VARCHAR(255),
                message VARCHAR(2000),
                timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                is_read BOOLEAN DEFAULT FALSE,
                FOREIGN KEY (user_id) REFERENCES cse360users(id)
            )
        """;

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createTableSQL);
            System.out.println("Notifications table created or already exists.");
        }
    }

    /**
     * Creates indexes for better notification query performance.
     */
    public void createNotificationIndexes() throws SQLException {
        String[] indexes = {
                "CREATE INDEX IF NOT EXISTS idx_notifications_user ON notifications(user_id, is_read)",
                "CREATE INDEX IF NOT EXISTS idx_notifications_type ON notifications(type)"
        };

        try (Statement stmt = connection.createStatement()) {
            for (String indexSQL : indexes) {
                stmt.execute(indexSQL);
            }
            System.out.println("Notification indexes created.");
        }
    }

    /**
     * Insert a new notification into the database.
     *
     * @param notification The notification to insert
     * @return true if successful, false otherwise
     */
    public boolean insertNotification(Notification notification) {
        String sql = """
            INSERT INTO notifications (user_id, type, related_id, title, message, timestamp, is_read)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, notification.getUserId());
            pstmt.setString(2, notification.getType());
            if (notification.getRelatedId() > 0) {
                pstmt.setInt(3, notification.getRelatedId());
            } else {
                pstmt.setNull(3, Types.INTEGER);
            }
            pstmt.setString(4, notification.getTitle());
            pstmt.setString(5, notification.getMessage());

            try {
                pstmt.setTimestamp(6, Timestamp.valueOf(notification.getTimestamp()));
            } catch (Exception ex) {
                // fallback to current time
                pstmt.setTimestamp(6, new Timestamp(System.currentTimeMillis()));
            }
            pstmt.setBoolean(7, notification.isRead());

            pstmt.executeUpdate();
            System.out.println(" Notification created for user ID: " + notification.getUserId());
            return true;
        } catch (SQLException e) {
            System.err.println(" Error inserting notification: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get all unread notifications for a specific user.
     *
     * @param userId The user's ID
     * @return List of unread notifications
     * @throws SQLException if database error occurs
     */
    public List<Notification> getUnreadNotifications(int userId) throws SQLException {
        String sql = """
            SELECT * FROM notifications 
            WHERE user_id = ? AND is_read = FALSE 
            ORDER BY timestamp DESC
        """;

        List<Notification> notifications = new ArrayList<>();

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    notifications.add(new Notification(
                            rs.getInt("notification_id"),
                            rs.getInt("user_id"),
                            rs.getString("type"),
                            rs.getInt("related_id"),
                            rs.getString("title"),
                            rs.getString("message"),
                            rs.getTimestamp("timestamp").toString(),
                            rs.getBoolean("is_read")
                    ));
                }
            }
        }

        return notifications;
    }

    /**
     * Get all notifications for a user (unread first, then most recent).
     *
     * @param userId recipient user id
     * @return list of notifications (empty on error)
     * @throws SQLException if needed (this version logs and returns empty list on error)
     */
    public List<Notification> getAllNotifications(int userId) throws SQLException {

        List<Notification> notifications = new ArrayList<>();
        String sql = "SELECT notification_id, user_id, type, related_id, title, message, timestamp, is_read "
                + "FROM notifications "
                + "WHERE user_id = ? "
                + "ORDER BY is_read ASC, timestamp DESC";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Notification n;
                    try {
                        n = new Notification(
                                rs.getInt("notification_id"),
                                rs.getInt("user_id"),
                                rs.getString("type"),
                                rs.getInt("related_id"),
                                rs.getString("title"),
                                rs.getString("message"),
                                rs.getTimestamp("timestamp").toString(),
                                rs.getBoolean("is_read")
                        );
                    } catch (NoSuchMethodError | Exception ex) {
                        n = new Notification();
                        n.setNotificationId(rs.getInt("notification_id"));
                        n.setUserId(rs.getInt("user_id"));
                        n.setType(rs.getString("type"));
                        n.setRelatedId(rs.getInt("related_id"));
                        n.setTitle(rs.getString("title"));
                        n.setMessage(rs.getString("message"));
                        n.setTimestamp(rs.getTimestamp("timestamp").toString());
                        n.setRead(rs.getBoolean("is_read"));
                    }
                    notifications.add(n);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return notifications;
    }

    /**
     * Mark a notification read.
     *
     * @param notificationId the notification id
     * @return true if successful
     * @throws SQLException if database error occurs
     */
    public boolean markNotificationAsRead(int notificationId) throws SQLException {
        String sql = "UPDATE notifications SET is_read = TRUE WHERE notification_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, notificationId);
            int updated = pstmt.executeUpdate();
            return updated > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Mark all notifications as read for a user.
     *
     * @param userId the user id
     * @return number of notifications updated
     * @throws SQLException if database error occurs
     */
    public int markAllNotificationsAsRead(int userId) throws SQLException {

        String sql = "UPDATE notifications SET is_read = TRUE WHERE user_id = ? AND is_read = FALSE";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            int updated = pstmt.executeUpdate();
            System.out.println(" Marked " + updated + " notifications as read for user " + userId);
            return updated;
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * Get unread notification count for a user.
     *
     * @param userId recipient user id
     * @return number of unread notifications
     * @throws SQLException if database error occurs
     */
    public int getUnreadNotificationCount(int userId) throws SQLException {

        String sql = "SELECT COUNT(*) FROM notifications WHERE user_id = ? AND is_read = FALSE";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Delete a notification by ID.
     *
     * @param notificationId the notification id to delete
     * @return true if successful
     * @throws SQLException if database error occurs
     */
    public boolean deleteNotification(int notificationId) throws SQLException {
        String sql = "DELETE FROM notifications WHERE notification_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, notificationId);
            int deleted = pstmt.executeUpdate();
            if (deleted > 0) {
                System.out.println("Deleted notification #" + notificationId);
                return true;
            }
            return false;
        } catch (SQLException e) {
            System.err.println(" Error deleting notification: " + e.getMessage());
            throw e;
        }
    }

    // Notification helpers

    public void notifyAdminsOfNewRequest(AdminRequest request) {
        try {
            // Get all admin users
            List<User> admins = getUsersByRole(User.Role.ADMIN);

            if (admins.isEmpty()) {
                System.out.println("⚠️ No admins found to notify");
                return;
            }

            String title = "New Admin Request #" + request.getRequestId();
            String message = String.format(
                    "New %s request from %s: %s",
                    request.getCategory(),
                    request.getRequestorName(),
                    request.getTitle()
            );

            int notified = 0;
            for (User admin : admins) {
                Notification notification = new Notification(
                        admin.getId(),
                        "ADMIN_REQUEST_NEW",
                        request.getRequestId(),
                        title,
                        message
                );
                if (insertNotification(notification)) {
                    notified++;
                }
            }

            System.out.println(" Notified " + notified + " admin(s) of new request #" + request.getRequestId());

        } catch (SQLException e) {
            System.err.println(" Error notifying admins: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void notifyRequestorOfUpdate(AdminRequest request, String updateMessage) {
        String title = "Admin Request #" + request.getRequestId() + " Updated";
        String message = updateMessage;

        Notification notification = new Notification(
                request.getRequestorId(),
                "ADMIN_REQUEST_UPDATE",
                request.getRequestId(),
                title,
                message
        );

        if (insertNotification(notification)) {
            System.out.println(" Notified requestor (ID: " + request.getRequestorId() + ") of update");
        }
    }

    public void notifyRequestorOfClosure(AdminRequest request, String adminName) {
        String title = "Admin Request #" + request.getRequestId() + " Closed";
        String message = String.format(
                "Your request '%s' has been closed by %s. Click to view details.",
                request.getTitle(),
                adminName
        );

        Notification notification = new Notification(
                request.getRequestorId(),
                "ADMIN_REQUEST_CLOSED",
                request.getRequestId(),
                title,
                message
        );

        if (insertNotification(notification)) {
            System.out.println(" Notified requestor of request closure");
        }
    }

    public void notifyAdminsOfReopenedRequest(AdminRequest request) {
        try {
            List<User> admins = getUsersByRole(User.Role.ADMIN);

            String title = "Admin Request #" + request.getRequestId() + " Reopened";
            String message = String.format(
                    "%s reopened request: %s (Originally #%d)",
                    request.getRequestorName(),
                    request.getTitle(),
                    request.getOriginalRequestId() == null ? request.getRequestId() : request.getOriginalRequestId()
            );

            for (User admin : admins) {
                Notification notification = new Notification(
                        admin.getId(),
                        "ADMIN_REQUEST_REOPENED",
                        request.getRequestId(),
                        title,
                        message
                );
                insertNotification(notification);
            }

            System.out.println(" Notified admins of reopened request");

        } catch (SQLException e) {
            System.err.println(" Error notifying admins of reopened request: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void updateAdminRequestsTableForCategories() {
        try (Statement stmt = connection.createStatement()) {
            String addColumn = "ALTER TABLE admin_requests ADD COLUMN IF NOT EXISTS category VARCHAR(255) DEFAULT 'OTHER'";
            stmt.execute(addColumn);
            System.out.println("Ensured 'category' column exists (if admin_requests table exists).");
        } catch (SQLException e) {
            System.err.println(" Error updating admin_requests table: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public boolean insertAdminRequest(AdminRequest request) {
        String sql = """
                INSERT INTO admin_requests
                (requestor_id, requestor_name, category, title, description, status, timestamp, original_request_id)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, request.getRequestorId());
            pstmt.setString(2, request.getRequestorName());
            pstmt.setString(3, request.getCategory());
            pstmt.setString(4, request.getTitle());
            pstmt.setString(5, request.getDescription());
            pstmt.setString(6, request.getStatus());

            // Handle timestamp safely
            String timestamp = request.getTimestamp();
            if (timestamp == null || timestamp.trim().isEmpty()) {
                pstmt.setTimestamp(7, new Timestamp(System.currentTimeMillis()));
            } else {
                try {
                    pstmt.setTimestamp(7, Timestamp.valueOf(timestamp));
                } catch (Exception e) {
                    pstmt.setTimestamp(7, new Timestamp(System.currentTimeMillis()));
                }
            }

            if (request.getOriginalRequestId() != null) {
                pstmt.setInt(8, request.getOriginalRequestId());
            } else {
                pstmt.setNull(8, java.sql.Types.INTEGER);
            }

            pstmt.executeUpdate();

            // Get the generated ID
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    request.setRequestId(generatedKeys.getInt(1));
                }
            }

            // Send notification to admins
            if (request.isReopened()) {
                notifyAdminsOfReopenedRequest(request);
            } else {
                notifyAdminsOfNewRequest(request);
            }

            System.out.println(" Admin request #" + request.getRequestId() + " created successfully");
            return true;

        } catch (SQLException e) {
            System.err.println(" Error inserting admin request: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean closeAdminRequest(int requestId, String adminName) {
        String sql = "UPDATE admin_requests SET status = 'closed' WHERE request_id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, requestId);
            pstmt.executeUpdate();

            AdminRequest request = getAdminRequestById(requestId);
            if (request != null) {
                notifyRequestorOfClosure(request, adminName);
            }

            System.out.println(" Admin request #" + requestId + " closed by " + adminName);
            return true;

        } catch (SQLException e) {
            System.err.println(" Error closing admin request: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean insertAdminAction(AdminAction action) {
        String sql = """
                INSERT INTO admin_actions 
                (request_id, admin_id, admin_name, action_description, timestamp)
                VALUES (?, ?, ?, ?, ?)
            """;

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, action.getRequestId());
            pstmt.setInt(2, action.getAdminId());
            pstmt.setString(3, action.getAdminName());
            pstmt.setString(4, action.getActionDescription());
            pstmt.setTimestamp(5, Timestamp.valueOf(action.getTimestamp()));

            pstmt.executeUpdate();

            AdminRequest request = getAdminRequestById(action.getRequestId());
            if (request != null) {
                String updateMsg = String.format(
                        "%s added an action: %s",
                        action.getAdminName(),
                        action.getActionDescription()
                );
                notifyRequestorOfUpdate(request, updateMsg);
            }

            System.out.println(" Admin action added to request #" + action.getRequestId());
            return true;

        } catch (SQLException e) {
            System.err.println(" Error inserting admin action: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public List<AdminRequest> loadOpenAdminRequests() {
        List<AdminRequest> requests = new ArrayList<>();
        String sql = "SELECT * FROM admin_requests WHERE status = 'open' ORDER BY timestamp DESC";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                AdminRequest request = new AdminRequest(
                        rs.getInt("request_id"),
                        rs.getInt("requestor_id"),
                        rs.getString("requestor_name"),
                        rs.getString("category"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getString("status"),
                        rs.getTimestamp("timestamp").toString(),
                        rs.getObject("original_request_id", Integer.class),
                        false
                );

                // Load actions for this request
                request.setActions(getActionsForRequest(request.getRequestId()));

                requests.add(request);
            }

        } catch (SQLException e) {
            System.err.println(" Error loading open admin requests: " + e.getMessage());
            e.printStackTrace();
        }

        return requests;
    }

    public List<AdminRequest> loadClosedAdminRequests() {
        List<AdminRequest> requests = new ArrayList<>();
        String sql = "SELECT * FROM admin_requests WHERE status = 'closed' ORDER BY timestamp DESC";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                AdminRequest request = new AdminRequest(
                        rs.getInt("request_id"),
                        rs.getInt("requestor_id"),
                        rs.getString("requestor_name"),
                        rs.getString("category"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getString("status"),
                        rs.getTimestamp("timestamp").toString(),
                        rs.getObject("original_request_id", Integer.class),
                        false
                );

                // Load actions for this request
                request.setActions(getActionsForRequest(request.getRequestId()));

                requests.add(request);
            }

        } catch (SQLException e) {
            System.err.println(" Error loading closed admin requests: " + e.getMessage());
            e.printStackTrace();
        }

        return requests;
    }

    public AdminRequest getAdminRequestById(int requestId) {
        String sql = "SELECT * FROM admin_requests WHERE request_id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, requestId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    AdminRequest request = new AdminRequest(
                            rs.getInt("request_id"),
                            rs.getInt("requestor_id"),
                            rs.getString("requestor_name"),
                            rs.getString("category"),
                            rs.getString("title"),
                            rs.getString("description"),
                            rs.getString("status"),
                            rs.getTimestamp("timestamp").toString(),
                            rs.getObject("original_request_id", Integer.class),
                            false
                    );

                    request.setActions(getActionsForRequest(requestId));
                    return request;
                }
            }
        } catch (SQLException e) {
            System.err.println(" Error getting admin request: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    public List<AdminAction> getActionsForRequest(int requestId) {
        List<AdminAction> actions = new ArrayList<>();
        String sql = "SELECT * FROM admin_actions WHERE request_id = ? ORDER BY timestamp ASC";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, requestId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    actions.add(new AdminAction(
                            rs.getInt("action_id"),
                            rs.getInt("request_id"),
                            rs.getInt("admin_id"),
                            rs.getString("admin_name"),
                            rs.getString("action_description"),
                            rs.getTimestamp("timestamp").toString()
                    ));
                }
            }

        } catch (SQLException e) {
            System.err.println(" Error loading actions for request: " + e.getMessage());
            e.printStackTrace();
        }

        return actions;
    }

    public int getOpenAdminRequestCount() {
        String sql = "SELECT COUNT(*) FROM admin_requests WHERE status = 'open'";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            System.err.println(" Error getting open request count: " + e.getMessage());
        }

        return 0;
    }

    // Initialization helpers: ensure notifications/admin_requests/staff_requests exist
    public void ensureMissingTablesExist() {
        String createNotifications = "CREATE TABLE IF NOT EXISTS notifications ("
                + "notification_id INT AUTO_INCREMENT PRIMARY KEY,"
                + "user_id INT NOT NULL,"
                + "type VARCHAR(100) NOT NULL,"
                + "related_id INT,"
                + "title VARCHAR(255),"
                + "message VARCHAR(2000),"
                + "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                + "is_read BOOLEAN DEFAULT FALSE"
                + ")";

        String createAdminRequests = "CREATE TABLE IF NOT EXISTS admin_requests ("
                + "request_id INT AUTO_INCREMENT PRIMARY KEY,"
                + "requestor_id INT,"
                + "requestor_name VARCHAR(255),"
                + "category VARCHAR(100) DEFAULT 'OTHER',"
                + "title VARCHAR(255),"
                + "description VARCHAR(2000),"
                + "status VARCHAR(50) DEFAULT 'open',"
                + "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                + "original_request_id INT,"
                + "admin_response VARCHAR(2000),"
                + "handled_by_admin_id INT,"
                + "handled_by_admin_name VARCHAR(255)"
                + ")";

        String createStaffRequests = "CREATE TABLE IF NOT EXISTS staff_requests ("
                + "request_id INT AUTO_INCREMENT PRIMARY KEY,"
                + "staff_id INT,"
                + "staff_name VARCHAR(255),"
                + "request_type VARCHAR(100),"
                + "title VARCHAR(255),"
                + "description VARCHAR(2000),"
                + "status VARCHAR(50),"
                + "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                + "admin_response VARCHAR(2000),"
                + "handled_by_admin_id INT,"
                + "handled_by_admin_name VARCHAR(255)"
                + ")";

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createNotifications);
            stmt.execute(createAdminRequests);
            stmt.execute(createStaffRequests);
//            try {
//                stmt.execute("ALTER TABLE clarifications ADD COLUMN item_type VARCHAR(20)");
//            } catch (SQLException e) {
//                if (!e.getMessage().toLowerCase().contains("already exists")) throw e;
//            }
//
//            try {
//                stmt.execute("ALTER TABLE clarifications ADD COLUMN content_id INT");
//            } catch (SQLException e) {
//                if (!e.getMessage().toLowerCase().contains("already exists")) throw e;
//            }

            System.out.println(" Ensured missing tables (notifications, admin_requests, staff_requests) exist.");
        } catch (SQLException e) {
            System.err.println(" Error ensuring missing tables exist: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public boolean insertStaffRequest(StaffRequest request) {
        String createTable = "CREATE TABLE IF NOT EXISTS staff_requests ("
                + "request_id INT AUTO_INCREMENT PRIMARY KEY,"
                + "staff_id INT,"
                + "staff_name VARCHAR(255),"
                + "request_type VARCHAR(100),"
                + "title VARCHAR(255),"
                + "description VARCHAR(2000),"
                + "status VARCHAR(50),"
                + "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                + "admin_response VARCHAR(2000),"
                + "handled_by_admin_id INT,"
                + "handled_by_admin_name VARCHAR(255)"
                + ")";
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createTable);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        String sql = "INSERT INTO staff_requests (staff_id, staff_name, request_type, title, description, status, timestamp, admin_response, handled_by_admin_id, handled_by_admin_name) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (request.getStaffId() > 0) {
                pstmt.setInt(1, request.getStaffId());
            } else {
                pstmt.setNull(1, Types.INTEGER);
            }
            pstmt.setString(2, request.getStaffName());
            pstmt.setString(3, request.getRequestType());
            pstmt.setString(4, request.getTitle());
            pstmt.setString(5, request.getDescription());
            pstmt.setString(6, request.getStatus());
            pstmt.setTimestamp(7, Timestamp.valueOf(request.getTimestamp()));
            pstmt.setString(8, request.getAdminResponse());
            if (request.getHandledByAdminId() != null) {
                pstmt.setInt(9, request.getHandledByAdminId());
            } else {
                pstmt.setNull(9, Types.INTEGER);
            }
            pstmt.setString(10, request.getHandledByAdminName());

            int affected = pstmt.executeUpdate();
            if (affected == 0) {
                return false;
            }
            try (ResultSet gen = pstmt.getGeneratedKeys()) {
                if (gen.next()) {
                    request.setRequestId(gen.getInt(1));
                }
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateStaffRequestStatus(int requestId, String status, String adminRemark, int adminId, String handledByAdminName) {
        String sql = "UPDATE staff_requests SET status = ?, admin_response = ?, handled_by_admin_id = ?, handled_by_admin_name = ? WHERE request_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, status);
            pstmt.setString(2, adminRemark);
            if (adminId > 0) {
                pstmt.setInt(3, adminId);
            } else {
                pstmt.setNull(3, Types.INTEGER);
            }
            pstmt.setString(4, handledByAdminName);
            pstmt.setInt(5, requestId);
            int updated = pstmt.executeUpdate();
            return updated > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<StaffRequest> loadAllStaffRequests() {
        List<StaffRequest> list = new ArrayList<>();
        String sql = "SELECT request_id, staff_id, staff_name, request_type, title, description, status, timestamp, admin_response, handled_by_admin_id, handled_by_admin_name FROM staff_requests ORDER BY timestamp DESC";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                StaffRequest r = new StaffRequest();
                r.setRequestId(rs.getInt("request_id"));
                r.setStaffId(rs.getInt("staff_id"));
                r.setStaffName(rs.getString("staff_name"));
                r.setRequestType(rs.getString("request_type"));
                r.setTitle(rs.getString("title"));
                r.setDescription(rs.getString("description"));
                r.setStatus(rs.getString("status"));
                r.setTimestamp(rs.getTimestamp("timestamp").toString());
                r.setAdminResponse(rs.getString("admin_response"));
                int hid = rs.getInt("handled_by_admin_id");
                if (!rs.wasNull()) {
                    r.setHandledByAdminId(hid);
                } else {
                    r.setHandledByAdminId(null);
                }
                r.setHandledByAdminName(rs.getString("handled_by_admin_name"));
                list.add(r);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Integer getAuthorIdForFlag(ModerationFlag flag) {
        String query = switch (flag.getItemType()) {
            case "question" -> "SELECT user_id FROM questions WHERE question_id = ?";
            case "answer" -> "SELECT user_id FROM answers WHERE answer_id = ?";
            case "suggestion" -> "SELECT user_id FROM suggestions WHERE suggestion_id = ?";
            case "review" -> "SELECT user_id FROM reviews WHERE review_id = ?";
            default -> null;
        };
        if (query == null) return null;

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, flag.getItemId());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("user_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int getUserIdForFlag(ModerationFlag flag) {
        String query = switch (flag.getItemType()) {
            case "question" -> "SELECT user_id FROM questions WHERE question_id = ?";
            case "answer" -> "SELECT user_id FROM answers WHERE answer_id = ?";
            default -> null;
        };
        if (query == null) return 0;

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, flag.getItemId());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("user_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
        return 0;
    }

    // Reviewer Profile

    public String getReviewerExperience(int reviewerId) throws SQLException {
        String sql = "SELECT experience FROM reviewerProfile WHERE reviewer_id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, reviewerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString("experience");
            }
        }
        return "";
    }

    public boolean updateReviewerExperience(int reviewerId, String experience) throws SQLException {
        String sql = "UPDATE reviewerProfile SET experience = ? WHERE reviewer_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, experience);
            ps.setInt(2, reviewerId);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                return true;
            }
        }

        String iSql = "INSERT INTO reviewerProfile (reviewer_id, experience) VALUES (?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(iSql)) {
            ps.setInt(1, reviewerId);
            ps.setString(2, experience);
            return ps.executeUpdate() > 0;
        }
    }

    // End of class
    //Edits
    public void insertEditHistory(Edits edit, int questionId) throws SQLException {
        String sql = "INSERT INTO edits (question_id, old_title, old_description, new_title, new_description, edited_by, edit_time) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, questionId);
            stmt.setString(2, edit.getQuestionOldTitle());
            stmt.setString(3, edit.getQuestionOldDescription());
            stmt.setString(4, edit.getQuestionNewTitle());
            stmt.setString(5, edit.getQuestionNewDescription());
            stmt.setString(6, edit.getQuestionEditedBy());
            stmt.setString(7, edit.getQuestionEditTime().toString());

            stmt.executeUpdate();
        }
    }
    
    public List<Edits> loadEditHistoryForQuestion(int questionId) throws SQLException {
        List<Edits> edits = new ArrayList<>();

        String sql = "SELECT old_title, old_description, new_title, new_description, edited_by, edit_time "
                   + "FROM edits WHERE question_id = ? ORDER BY edit_time DESC";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, questionId);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Edits e = new Edits(
                    rs.getString("old_title"),
                    rs.getString("old_description"),
                    rs.getString("new_title"),
                    rs.getString("new_description"),
                    rs.getString("edited_by")
                );

                e.setQuestionEditTime(LocalDateTime.parse(rs.getString("edit_time")));
                edits.add(e);
            }
        }

        return edits;
    }
    
    
}
