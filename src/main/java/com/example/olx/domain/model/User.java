// src/main/java/com/example/olx/domain/model/User.java
package com.example.olx.domain.model;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;
public abstract class User implements Serializable {
    private static final long serialVersionUID = 1L;
    private String userId;
    private String username;
    private String passwordHash;
    private String email;
    private UserType userType;
    public User(String username, String passwordHash, String email, UserType userType) {
        this.userId = UUID.randomUUID().toString();
        this.username = username;
        this.passwordHash = passwordHash;
        this.email = email;
        this.userType = userType;
    }
    public String getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public String getEmail() { return email; }
    public UserType getUserType() { return userType; }
    public void setUsername(String username) { this.username = username; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public void setEmail(String email) { this.email = email; }
    public abstract void viewDashboard();
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(userId, user.userId);
    }
    @Override
    public int hashCode() { return Objects.hash(userId); }
    @Override
    public String toString() {
        return "User{" + "userId='" + userId + '\'' + ", username='" + username + '\'' + ", email='" + email + '\'' + ", userType=" + userType + '}';
    }
}