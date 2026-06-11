package org.example.sportslivev1.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.example.sportslivev1.entity.Alerts;

public class AlertsRequest {
    @NotNull(message = "Game ID is required")
    private Long gameId;

    @NotBlank(message = "Team name is required")
    private String teamName;

    @NotNull(message = "Alert type is required")
    private Alerts.AlertType alertType;

    @Min(value = 0, message = "Target value must be 0 or greater")
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
