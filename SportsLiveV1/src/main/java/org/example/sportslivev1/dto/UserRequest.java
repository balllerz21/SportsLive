package org.example.sportslivev1.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.example.sportslivev1.entity.Users;

public class UserRequest {
    @NotBlank(message = "Username is required")
    @Size(min = 5, max = 50, message = "Username must be between 5 and 50 characters")
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
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
