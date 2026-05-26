package org.example.sportslivev1.dto;

import org.example.sportslivev1.entity.Users;

public class UserRequest {
    private String username;
    private String passwordHash;
    private Users.UserRole role;
    public UserRequest() {}
    public String getUsername() {
        return username;
    }
    public String getPasswordHash() {
        return passwordHash;
    }
    public Users.UserRole getRole() {
        return role;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
    public void setRole(Users.UserRole role) {
        this.role = role;
    }
}
