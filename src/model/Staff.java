package model;

import model.User.Role;

public class Staff extends User {

    public Staff(int id, String userName, String password, String name, String email, String tempPw) {
        super(id, userName, password, Role.STAFF, name, email, tempPw);
    }

    public Staff(String userName, String password, String name, String email, String tempPw) {
        super(userName, password, Role.STAFF, name, email, tempPw);
    }

    public Staff(String userName, String password) {
        super(userName, password, Role.STAFF, "", "", null);
    }

    @Override
    public Role getRole() {
        return Role.STAFF;
    }
}