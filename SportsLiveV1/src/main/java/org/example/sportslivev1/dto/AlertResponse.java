package org.example.sportslivev1.dto;

import java.time.Instant;
import org.example.sportslivev1.entity.Alerts;

public class AlertResponse {
    private Long id;
    private Long gameId;
    private String actualGameId;

    private String teamName;
    private Alerts.AlertType alertType;
    private int targetVal;
    private Alerts.AlertStatus status;

    private Instant createdAt;
    private Instant triggeredAt;

    private String homeTeam;
    private String awayTeam;
    private int homeScore;
    private int awayScore;
    private String gameStatus;

    public AlertResponse() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getGameId() { return gameId; }
    public void setGameId(Long gameId) { this.gameId = gameId; }

    public String getActualGameId() { return actualGameId; }
    public void setActualGameId(String actualGameId) { this.actualGameId = actualGameId; }

    public String getTeamName() { return teamName; }
    public void setTeamName(String teamName) { this.teamName = teamName; }

    public Alerts.AlertType getAlertType() { return alertType; }
    public void setAlertType(Alerts.AlertType alertType) { this.alertType = alertType; }

    public int getTargetVal() { return targetVal; }
    public void setTargetVal(int targetVal) { this.targetVal = targetVal; }

    public Alerts.AlertStatus getStatus() { return status; }
    public void setStatus(Alerts.AlertStatus status) { this.status = status; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getTriggeredAt() { return triggeredAt; }
    public void setTriggeredAt(Instant triggeredAt) { this.triggeredAt = triggeredAt; }

    public String getHomeTeam() { return homeTeam; }
    public void setHomeTeam(String homeTeam) { this.homeTeam = homeTeam; }

    public String getAwayTeam() { return awayTeam; }
    public void setAwayTeam(String awayTeam) { this.awayTeam = awayTeam; }

    public int getHomeScore() { return homeScore; }
    public void setHomeScore(int homeScore) { this.homeScore = homeScore; }

    public int getAwayScore() { return awayScore; }
    public void setAwayScore(int awayScore) { this.awayScore = awayScore; }

    public String getGameStatus() { return gameStatus; }
    public void setGameStatus(String gameStatus) { this.gameStatus = gameStatus; }
}