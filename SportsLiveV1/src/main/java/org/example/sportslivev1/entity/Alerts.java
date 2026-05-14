package org.example.sportslivev1.entity;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;

// Done: Change createdAt and isActive to backend instead of user controlled
import java.time.Instant;
import java.util.List;
@Entity
public class Alerts {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    
    private Long id;
    @ManyToOne
    @JoinColumn(name = "game_id")
    private Games game;

    private String teamName;
   
    public enum AlertType {
        SCORE_OVER, SCORE_UNDER
    }
    @Enumerated(EnumType.STRING)
    private AlertType alertType;
    private int targetVal;
    public enum AlertStatus
    {
        CREATED,
        TRIGGERED,
        FINISHED
    }
    @Enumerated(EnumType.STRING)
    private AlertStatus status;
    private Instant createdAt;
    private Instant triggeredAt;
    private Instant notifiedAt;
    private boolean isNotification;


    protected Alerts() {}
    public Alerts(Games game, String teamName, AlertType alertType, int targetVal) {
        this.game = game;
        this.teamName = teamName;
        this.alertType = alertType; 
        this.targetVal = targetVal;
        this.status = AlertStatus.CREATED;
        this.createdAt = Instant.now();
        this.notifiedAt = null;
        this.isNotification = false;
    }

    // getters
    public Long getId() {
        return id;
    }
    public String getTeamName() {
        return teamName;        
    }
    public AlertType getAlertType() {
        return alertType;
    }
    public int getTargetVal() {
        return targetVal;
    }
    public AlertStatus getAlertStatus()
    {
        return status;
    }
    public Instant getCreatedAt(){
        return createdAt;
    }
    public Instant getTriggeredAt() {
        return triggeredAt;
    }
    public Games getGame()
    {
        return game;
    }
    public Instant getNotifiedAt()
    {
        return notifiedAt;
    }
    public boolean getIsNotification()
    {
        return isNotification;
    }

    // setters
     public void setId(Long id)
    {
        this.id = id;
    }
    public void setTeamName(String team)
    {
        this.teamName = team;
    }
    public void setAlertType(AlertType type)
    {
        this.alertType = type;
    }
    public void setTargetVal(int val)
    {
        this.targetVal = val;
    }
    public void setAlertStatus(AlertStatus stat)
    {
        this.status = stat;
    }
    public void setCreatedAt(Instant time)
    {
        this.createdAt = time;
    }
    public void setTriggeredAt(Instant time)
    {
        this.triggeredAt = time;
    }
    public void setGame(Games g)
    {
        this.game = g;
    }
    public void setNotifiedAt(Instant notifiedAt)
    {
        this.notifiedAt = notifiedAt;
    }
    public void setIsNotification(boolean isNotification)
    {
        this.isNotification = isNotification;
    }

}
