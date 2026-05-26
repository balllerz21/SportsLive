package org.example.sportslivev1.dto;

import java.util.List;

import org.example.sportslivev1.entity.Alerts;
import org.example.sportslivev1.entity.Users;

public class UserResponse {
    private String username;
    private Users.UserRole role;
    private List<AlertResponse> alerts;
    public UserResponse() {}
    public String getUsername() {
        return username;
    }
    public Users.UserRole getRole() {
        return role;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public void setRole(Users.UserRole role) {
        this.role = role;
    }
    public List<AlertResponse> getAlerts() {
        return alerts;
    }
    public void setAlerts(List<AlertResponse> alerts) {
        this.alerts = alerts;
    }
}
