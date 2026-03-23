package model;


/**
 * Represents an administrative user with full privileged access in the system.
 * All instances of this class are created with the {@link User.Role#ADMIN} role.
 */
public class Admin extends User {


/** 
 *
 * Admin
 *
 * @param id  the id. 
 * @param userName  the user name. 
 * @param password  the password. 
 * @param name  the name. 
 * @param email  the email. 
 * @param tempPw  the temp pw. 
 * @return public
 */
    public Admin(int id, String userName, String password, String name, String email, String tempPw) { 

        super(id, userName, password, Role.ADMIN, name, email, tempPw);
    }


/** 
 *
 * Admin
 *
 * @param userName  the user name. 
 * @param password  the password. 
 * @param name  the name. 
 * @param email  the email. 
 * @param tempPw  the temp pw. 
 * @return public
 */
    public Admin(String userName, String password, String name, String email, String tempPw) { 

        super(userName, password, Role.ADMIN, name, email, tempPw);
    }


/** 
 *
 * Admin
 *
 * @param userName  the user name. 
 * @param password  the password. 
 * @return public
 */
    public Admin(String userName, String password) { 

        super(userName, password, Role.ADMIN, "", "", null);
    }

    @Override

/** 
 *
 * Gets the role
 *
 * @return the role
 */
    public Role getRole() { 

        return Role.ADMIN;
    }
}

