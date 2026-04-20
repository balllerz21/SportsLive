package org.example.sportslivev1.dto;

import java.time.Instant;
import java.util.List;

public class GameDetailResponse {
    private Long id;
    private String actualGameId;
    private String homeTeam;
    private String awayTeam;
    private int homeScore;
    private int awayScore;
    private Instant schedTime;
    private Instant updatedTime;
    private String status;
    private List<AlertListResponse> alerts;

    public GameDetailResponse() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getActualGameId() {
        return actualGameId;
    }

    public void setActualGameId(String actualGameId) {
        this.actualGameId = actualGameId;
    }

    public String getHomeTeam() {
        return homeTeam;
    }

    public void setHomeTeam(String homeTeam) {
        this.homeTeam = homeTeam;
    }

    public String getAwayTeam() {
        return awayTeam;
    }

    public void setAwayTeam(String awayTeam) {
        this.awayTeam = awayTeam;
    }

    public int getHomeScore() {
        return homeScore;
    }

    public void setHomeScore(int homeScore) {
        this.homeScore = homeScore;
    }

    public int getAwayScore() {
        return awayScore;
    }

    public void setAwayScore(int awayScore) {
        this.awayScore = awayScore;
    }

    public Instant getSchedTime() {
        return schedTime;
    }

    public void setSchedTime(Instant schedTime) {
        this.schedTime = schedTime;
    }

    public Instant getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(Instant updatedTime) {
        this.updatedTime = updatedTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<AlertListResponse> getAlerts() {
        return alerts;
    }

    public void setAlerts(List<AlertListResponse> alerts) {
        this.alerts = alerts;
    }
}