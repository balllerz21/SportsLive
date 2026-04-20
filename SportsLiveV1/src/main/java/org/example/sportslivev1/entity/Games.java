package org.example.sportslivev1.entity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;

@Entity
public class Games {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String actualGameId;
    private String homeTeam;
    private String awayTeam;
    private int homeScore;
    private int awayScore;
    private Instant schedTime;
    private Instant updatedTime;
    public enum Status{
        SCHEDULED,
        LIVE,
        FINAL
    }
    @Enumerated(EnumType.STRING)
    private Status status;

    @OneToMany(mappedBy = "game")
    private List<Alerts> alerts;

    protected Games() {}
    public Games(String actualGameId, String homeTeam, String awayTeam, int homeScore, int awayScore, Status status, Instant schedTime)
    {
        this.actualGameId = actualGameId;
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.homeScore = homeScore;
        this.awayScore = awayScore;
        this.schedTime = schedTime;
        this.updatedTime = Instant.now();
        this.status = status;
        this.alerts = new ArrayList<>();
    }

    public void addAlert(Alerts alert) {
        alerts.add(alert);
        alert.setGame(this);
    }
    // getters
    public Long getId() {
        return id;
    }
    public String getActualGameId()
    {
        return actualGameId;
    }
    public String getHomeTeam() {
        return homeTeam;               
    }
    public String getAwayTeam() {
        return awayTeam;               
    }
    public int getHomeScore() {
        return homeScore;
    }
    public int getAwayScore() {
        return awayScore;  
    }
    public Instant getSchedTime() {
        return schedTime;
    }
    public Instant getUpdatedTime() {
        return updatedTime; 
    }
    public Status getStatus()
    {
        return status;
    }
    public List<Alerts> getAlerts()
    {
        return alerts;
    }

    // setters
    public void setId(Long id)
    {
        this.id = id;
    }
    public void setActualGameId(String id)
    {
        this.actualGameId = id;
    }
    public void setHomeTeam(String home)    {
        this.homeTeam = home;  
    }
    public void setAwayTeam(String away)    {
        this.awayTeam = away;  
    }
   public void setHomeScore(int home_score)    {
        this.homeScore = home_score;  
    }
    public void setAwayScore(int away_score)    {
        this.awayScore = away_score;  
    }
    public void setSchedTime(Instant schedTime)    {
        this.schedTime = schedTime;  
    }
    public void setUpdatedTime(Instant time)
    {
        this.updatedTime = time;
    }
    public void setStatus(Status status)
    {
        this.status = status;
    }
    public void setAlerts(List<Alerts> a)
    {
        this.alerts = a;
    }
}
