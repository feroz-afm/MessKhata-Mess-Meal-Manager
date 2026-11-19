package com.messkhata.model;

/**
 * The person who runs the mess and logs in to the app.
 */
public class Manager extends Person {

    private final String username;
    private String password;

    public Manager(String id, String name, String phone, String username, String password) {
        super(id, name, phone);
        this.username = username;
        this.password = password;
    }

    @Override
    public String getRole() {
        return "Manager";
    }

    public String getUsername() {
        return username;
    }

    public boolean checkLogin(String user, String pass) {
        return username.equals(user) && password.equals(pass);
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toLine() {
        return String.join(SEPARATOR, getId(), getName(), getPhone(), username, password);
    }

    public static Manager fromParts(String[] p) {
        return new Manager(p[0], p[1], p[2], p[3], p[4]);
    }
}
