package org.example.sportslivev1.service;
import org.example.sportslivev1.entity.Alerts;
import org.example.sportslivev1.entity.Alerts.AlertStatus;
import org.example.sportslivev1.entity.Alerts.AlertType;
import org.example.sportslivev1.entity.Games;
import org.example.sportslivev1.entity.Users;
import org.example.sportslivev1.repository.AlertsRepo;
import java.lang.reflect.Array;
import java.time.Instant;
import java.util.List;
public interface AlertsService {
    Alerts createAlert(Games game, Users user, String teamName, Alerts.AlertType alertType, int targetValt);
    List<Alerts> getAllAlerts(Alerts.AlertStatus status, Alerts.AlertType type, String team, String date);
    Alerts getAlertById(Long id);
    List<Alerts> getAlertsByTeamName(String teamName);
    List<Alerts> getAlertsByAlertType(Alerts.AlertType alertType);
    List<Alerts> getAlertsByStatus(Alerts.AlertStatus status);
    List<Alerts> getAlertsByCreatedAt(Instant createdAt);
    List<Alerts> getAlertsByTypeAndTeam(String team, Alerts.AlertType type);
    List<Alerts> getAlertsByStatusAndNotificationReady(Alerts.AlertStatus stat, boolean isNotification, Instant notifiedAt);
    void deleteAlert(Long id);
}
