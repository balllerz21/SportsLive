package org.example.sportslivev1.dto;

import org.example.sportslivev1.entity.Alerts;

public class AlertsRequest {
    private Long gameId;
    private String teamName;
    private Alerts.AlertType alertType;
    private int targetVal;

    public AlertsRequest() {}

    public Long getGameId() {
        return gameId;
    }

    public void setGameId(Long gameId) {
        this.gameId = gameId;
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
}
