// src/main/java/com/example/olx/domain/model/Admin.java
package com.example.olx.domain.model;
public class Admin extends User {
    private static final long serialVersionUID = 3L;
    private String accessLevel;
    public Admin(String username, String passwordHash, String email, String accessLevel) {
        super(username, passwordHash, email, UserType.ADMIN);
        this.accessLevel = accessLevel;
    }
    public String getAccessLevel() { return accessLevel; }
    public void setAccessLevel(String accessLevel) { this.accessLevel = accessLevel; }
    public void banUser(User userToBan) { /* ... логіка бану ... */ }
    @Override
    public void viewDashboard() { System.out.println("Viewing Dashboard for ADMIN: " + getUsername()); }
    @Override
    public String toString() { return "Admin[" + super.toString() + ", accessLevel='" + accessLevel + '\'' + "]"; }
}