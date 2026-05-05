package org.example.sportslivev1.repository;


import java.util.List;
import java.util.Optional;
import java.time.Instant;

import org.example.sportslivev1.entity.*;
import org.example.sportslivev1.entity.Alerts.AlertStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

// Next DONE: See what searching mechanisms the app will need. Example: CreatedAt and isActive can be combined as well as AlertType and targetVal
@Repository
public interface AlertsRepo extends JpaRepository<Alerts, Long>, JpaSpecificationExecutor<Alerts>
{
    List<Alerts> findByTeamName(String teamName);
    List<Alerts> findByAlertType(Alerts.AlertType alertType);
    Optional<Alerts> findById(Long id);
    List<Alerts> findByStatus(Alerts.AlertStatus status);
    List<Alerts> findByCreatedAt(Instant createdAt);
    List<Alerts> findByCreatedAtAndStatusOrderByCreatedAt(Instant createdAt, Alerts.AlertStatus status);
    List<Alerts> findByAlertTypeAndTargetVal(Alerts.AlertType type, int targetVal);
    List<Alerts> findByTeamNameAndAlertType(String team, Alerts.AlertType type);
    List<Alerts> findByGame_Id(Long gameId);
}
