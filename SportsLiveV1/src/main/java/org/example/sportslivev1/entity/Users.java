package org.example.sportslivev1.entity;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
@Entity
public class Users {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String userName;
    private String passwordHash;
    public enum UserRole {
        USER,
        ADMIN
    }
    @Enumerated(EnumType.STRING)
    private UserRole role;

    @OneToMany(mappedBy = "user")
    private List<Alerts> alerts;

    protected Users() {}

    public Users(String userName, String passwordHash, UserRole role) {
        this.userName = userName;
        this.passwordHash = passwordHash;
        this.role = role;
    }
    public Long getId() {
        return id;
    }
    public String getUserName() {
        return userName;
    }
    public String getPasswordHash() {
        return passwordHash;    
    }
    public UserRole getRole() {
        return role;
    }
    public List<Alerts> getAlerts() {
        return alerts;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public void setUserName(String userName) {
        this.userName = userName;
    }
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
    public void setRole(UserRole role) {
        this.role = role;
    }
    public void setAlerts(List<Alerts> alerts) {
        this.alerts = alerts;
    }
}
