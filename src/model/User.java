package model;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import databasePart1.DatabaseHelper;
import logic.StatusData;
import model.User.Role;


/**
 * Represents a generic user in the system, encapsulating common attributes such as
 * ID, username, password, role, name, and email.
 * <p>
 * This class serves as the base for all user types, including students, reviewers,
 * instructors, and administrators. It also provides helper methods that interact
 * with the database to manage trusted reviewer relationships, allowing any user
 * to add, remove, or check trusted reviewers.
 * </p>
 */
public class User {
    private int id;
    private String userName;
    private String password;
    private Role role;
    private List<Role> roles;
    private String name;
    private String email;
    private String phone;
    private String bio;
    private String tempPw;
    
    /* ---------- Factory helpers ---------- */

    // Accept Role

/** 
 *
 * It is a constructor. 
 *
 * @param id  the id. 
 * @param userName  the user name. 
 * @param password  the password. 
 * @param role  the role. 
 * @param name  the name. 
 * @param email  the email. 
 * @param tempPw  the temp pw. 
 */
    public static User createUser(int id, String userName, String password, Role role, String name, String email, String tempPw) { 

        return new User(id, userName, password, role, name, email, tempPw);
    }

    // Accept role as String

/** 
 *
 * It is a constructor. 
 *
 * @param id  the id. 
 * @param userName  the user name. 
 * @param password  the password. 
 * @param role  the role. 
 * @param name  the name. 
 * @param email  the email. 
 * @param tempPw  the temp pw. 
 */
    public static User createUser(int id, String userName, String password, String role, String name, String email, String tempPw) { 

        return new User(id, userName, password, Role.fromString(role), name, email, tempPw);
    }
    

/** 
 *
 * It is a constructor. 
 *
 * @param userName  the user name. 
 * @param password  the password. 
 * @param role  the role. 
 * @param name  the name. 
 * @param email  the email. 
 * @param tempPw  the temp pw. 
 */
    public static User createUser(String userName, String password, Role role, String name, String email, String tempPw) { 

        return new User(0, userName, password, role, name, email, tempPw);
    }


/** 
 *
 * It is a constructor. 
 *
 * @param userName  the user name. 
 * @param password  the password. 
 * @param role  the role. 
 * @param name  the name. 
 * @param email  the email. 
 * @param tempPw  the temp pw. 
 */
    public static User createUser(String userName, String password, String role, String name, String email, String tempPw) { 

        return new User(0, userName, password, Role.fromString(role), name, email, tempPw);
    }


/** 
 *
 * It is a constructor. 
 *
 * @param userName  the user name. 
 * @param password  the password. 
 * @param role  the role. 
 */
    public static User createUser(String userName, String password, String role) { 

        return new User(0, userName, password, Role.fromString(role), "", "", null);
    }

    /* ---------- Constructors ---------- */


/** 
 *
 * It is a constructor. 
 *
 * @param id  the id. 
 * @param userName  the user name. 
 * @param password  the password. 
 * @param role  the role. 
 * @param name  the name. 
 * @param email  the email. 
 * @param tempPw  the temp pw. 
 */
    public User(int id, String userName, String password, Role role, String name, String email, String tempPw) { 

        this.id = id;
        this.userName = userName;
        this.password = password;
        this.role = role == null ? Role.UNKNOWN : role;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.bio = bio;
        this.tempPw = tempPw;
        
        this.roles = new ArrayList<>();
        this.roles.add(this.role);  // initialize with single primary role
    }


/** 
 *
 * It is a constructor. 
 *
 * @param userName  the user name. 
 * @param password  the password. 
 * @param role  the role. 
 * @param name  the name. 
 * @param email  the email. 
 * @param tempPw  the temp pw. 
 */
    public User(String userName, String password, Role role, String name, String email, String tempPw) { 

        this(0, userName, password, role, name, email, tempPw);
    }


/** 
 *
 * It is a constructor. 
 *
 * @param userName  the user name. 
 * @param password  the password. 
 * @param role  the role. 
 * @param name  the name. 
 * @param email  the email. 
 * @param tempPw  the temp pw. 
 */
    public User(String userName, String password, String role, String name, String email, String tempPw) { 

        this(0, userName, password, Role.fromString(role), name, email, tempPw);

    }
    
    //Constructor with phone and bio to maintain table completeness

/** 
 *
 * It is a constructor. 
 *
 * @param id  the id. 
 * @param userName  the user name. 
 * @param password  the password. 
 * @param role  the role. 
 * @param name  the name. 
 * @param email  the email. 
 * @param phone  the phone. 
 * @param bio  the bio. 
 * @param tempPw  the temp pw. 
 */
    public User(int id, String userName, String password, Role role, String name, String email, String phone, String bio, String tempPw) { 

        this.id = id;
        this.userName = userName;
        this.password = password;
        this.role = role == null ? Role.UNKNOWN : role;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.bio = bio;
        this.tempPw = tempPw;
    }
    

/** 
 *
 * It is a constructor. 
 *
 * @param id  the id. 
 * @param userName  the user name. 
 * @param password  the password. 
 * @param role  the role. 
 * @param name  the name. 
 * @param email  the email. 
 * @param phone  the phone. 
 * @param bio  the bio. 
 * @param tempPw  the temp pw. 
 */
    public static User createUser(int id, String userName, String password, String role, String name, String email, String phone, String bio, String tempPw) { 

        return new User(id, userName, password, Role.fromString(role), name, email, phone, bio, tempPw);
    }

    //constructor accepting multiple roles

/** 
 *
 * It is a constructor. 
 *
 * @param id  the id. 
 * @param userName  the user name. 
 * @param password  the password. 
 * @param roles  the roles. 
 * @param name  the name. 
 * @param email  the email. 
 * @param phone  the phone. 
 * @param bio  the bio. 
 * @param tempPw  the temp pw. 
 */
    public User(int id, String userName, String password, List<Role> roles, String name, String email, String phone, String bio, String tempPw) { 

        this.id = id;
        this.userName = userName;
        this.password = password;
        this.roles = roles != null ? roles : new ArrayList<>();
        this.role = roles != null && !roles.isEmpty() ? roles.get(0) : Role.UNKNOWN;  // fallback to first role
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.bio = bio;
        this.tempPw = tempPw;
    }
    

/** 
 *
 * It is a constructor. 
 *
 * @param id  the id. 
 * @param userName  the user name. 
 * @param password  the password. 
 * @param roles  the roles. 
 * @param name  the name. 
 * @param email  the email. 
 * @param tempPw  the temp pw. 
 */
    public User(int id, String userName, String password, List<Role> roles, String name, String email, String tempPw) { 

        this(id, userName, password, roles, name, email, null, null, tempPw);
    }
    /* ---------- Role enum ---------- */

    public enum Role {
        ADMIN,
        INSTRUCTOR,
        STUDENT,
        REVIEWER,
        TA,
        STAFF,
        UNKNOWN;


/** 
 *
 * From string
 *
 * @param s  the s. 
 * @return Role
 */
        public static Role fromString(String s) { 

            if (s == null) return UNKNOWN;
            try {
                return Role.valueOf(s.trim().toUpperCase());
            } catch (IllegalArgumentException ex) {
                return UNKNOWN;
            }
        }
    }

    /* ---------- Getters / Setters ---------- */



/** 
 *
 * Gets the identifier
 *
 * @return the identifier
 */
    public int getId() { return id; } 
    public String getUserName() { return userName; }
    public String getPassword() { return password; }

    // return Role enum
    public Role getRole() { return role == null ? Role.UNKNOWN : role; }

    // convenience: return role name as String
    public String getRoleName() { return getRole().name(); }

    //when user has more than one role
    public List<Role> getRoles() {

    	return roles == null ? List.of() : roles;
    }
    


/** 
 *
 * Gets the name
 *
 * @return the name
 */
    public String getName() { return name; } 
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getBio() { return bio; }
    public String getTempPw() { return tempPw; }

    public List<Question> getUserQuestions() {

        return StatusData.databaseHelper.searchQuestions(null, getName(), null);
    }

    public void setId(int id) { this.id = id; }
    public void setUserName(String userName) { this.userName = userName; }
    public void setPassword(String password) { this.password = password; }

    // set by Role
    public void setRole(Role role) { this.role = role == null ? Role.UNKNOWN : role; }

    // set by string
    public void setRole(String role) { this.role = Role.fromString(role); }
    
    //set role in list

/** 
 *
 * Sets the roles
 *
 * @param roles  the roles. 
 */
    public void setRoles(List<Role> roles) { 

    	this.roles = roles;
    }
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setBio(String bio) { this.bio = bio; }
    public void setTempPw(String tempPw) { this.tempPw = tempPw; }

    @Override
    /*public String toString() {
        return "User{" +
                "id=" + id +
                ", userName='" + userName + '\'' +
                ", role='" + getRoleName() + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                '}';
    }*/

/** 
 *
 * To string
 *
 * @return String
 */
    public String toString() { 

    	if (name != null && !name.isEmpty()) {
    		return name + " (" + userName + ")";
    	}
    	return userName;
    }

    /* ---------- Permission helpers ---------- */

    /**
     * Returns true for roles that should be treated as privileged (admin/instructor/reviewer/ta/staff).
     */
    public boolean isPrivileged() { 

        Role r = getRole();
        return r == Role.ADMIN || r == Role.INSTRUCTOR || r == Role.REVIEWER || r == Role.TA || r == Role.STAFF;
    }

    /**
     * Convenience used by the UI: can this user mark an answer as solution?
     * By default privileged users or the question author may mark as solution.
     */
    public boolean canMarkSolution(Question q) { 

        if (isPrivileged()) return true;
        if (q == null) return false;
        if (this.name == null) return false;
        return Objects.equals(this.name, q.getAuthor());
    }
    
    /* ---------------- Trusted Reviewer Functions ---------------- */

/** 
 *
 * Is trusted
 *
 * @param reviewer  the reviewer. 
 * @param db  the db. 
 * @return boolean
 * @throws   SQLException 
 */
    public boolean isTrusted(User reviewer, DatabaseHelper db) throws SQLException { 

    	return db.isTrusted(this.getId(), reviewer.getId());
    }
    

/** 
 *
 * Add trusted reviewer
 *
 * @param reviewer  the reviewer. 
 * @param db  the db. 
 * @return boolean
 * @throws   SQLException 
 */
    public boolean addTrustedReviewer(User reviewer, DatabaseHelper db) throws SQLException { 

    	return db.addTrustedReviewer(this.getId(), reviewer.getId());
    }
    

/** 
 *
 * Remove trusted reviewer
 *
 * @param reviewer  the reviewer. 
 * @param db  the db. 
 * @return boolean
 * @throws   SQLException 
 */
    public boolean removeTrustedReviewer(User reviewer, DatabaseHelper db) throws SQLException { 

    	return db.removeTrustedReviewer(this.getId(), reviewer.getId());
    }
    
    
}

