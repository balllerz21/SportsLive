package org.example.sportslivev1.service;
import org.example.sportslivev1.entity.Alerts;
import org.example.sportslivev1.entity.Alerts.AlertStatus;
import org.example.sportslivev1.entity.Alerts.AlertType;
import org.example.sportslivev1.entity.Games;
import org.example.sportslivev1.repository.AlertsRepo;
import java.lang.reflect.Array;
import java.time.Instant;
import java.util.List;

public interface AlertsService {
    void createAlert(Games game, String teamName, Alerts.AlertType alertType, int targetValt);
    List<Alerts> getAllAlerts();
    Alerts getAlertById(Long id);
    List<Alerts> getAlertsByTeamName(String teamName);
    List<Alerts> getAlertsByAlertType(Alerts.AlertType alertType);
    List<Alerts> getAlertsByStatus(Alerts.AlertStatus status);
    List<Alerts> getAlertsByCreatedAt(Instant createdAt);
    List<Alerts> getAlertsByTypeAndTeam(String team, Alerts.AlertType type);
    void deleteAlert(Long id);
}
