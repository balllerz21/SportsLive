package org.example.sportslivev1.dto;

import org.example.sportslivev1.entity.Alerts;

public class AlertListResponse {
    private Long id;
    private String teamName;
    private Alerts.AlertType alertType;
    private int targetVal;
    private Alerts.AlertStatus status;

    public AlertListResponse() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public Alerts.AlertType getAlertType() {
        return alertType;
    }

    public void setAlertType(Alerts.AlertType alertType) {
        this.alertType = alertType;
    }

    public int getTargetVal() {
        return targetVal;
    }

    public void setTargetVal(int targetVal) {
        this.targetVal = targetVal;
    }

    public Alerts.AlertStatus getStatus() {
        return status;
    }

    public void setStatus(Alerts.AlertStatus status) {
        this.status = status;
    }
}
