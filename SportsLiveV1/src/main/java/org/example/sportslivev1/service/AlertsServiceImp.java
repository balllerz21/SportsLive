package org.example.sportslivev1.service;
import org.example.sportslivev1.entity.Alerts;
import org.example.sportslivev1.entity.Alerts.AlertStatus;
import org.example.sportslivev1.entity.Games;
import org.example.sportslivev1.entity.Users;
import org.example.sportslivev1.repository.AlertsRepo;
import org.example.sportslivev1.repository.GamesRepo;
import org.example.sportslivev1.repository.UsersRepo;
import org.example.sportslivev1.specifications.AlertsSpecifications;
import org.example.sportslivev1.utils.Utilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.JpaSort;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;



@Service
public class AlertsServiceImp implements AlertsService {
    @Autowired
    AlertsRepo alertsRepo;
    @Autowired
    GamesRepo gamesRepo;
    @Autowired
    UsersRepo usersRepo;
    // Without autowired //
    // public AlertsServiceImp(AlertsRepo alertsRepo) {
    //     this.alertsRepo = alertsRepo;
    // }

    @Override
    public Alerts createAlert(Games game, Users user,String teamName, Alerts.AlertType alertType, int targetVal) {
        Optional<Games> g1 = gamesRepo.findById(game.getId());
        Optional<Users> u1 = usersRepo.findById(user.getId());
        if (g1.isPresent() && u1.isPresent() && g1.get().getStatus() != Games.Status.FINAL)
        {
            Alerts alert = new Alerts(g1.get(), teamName, alertType, targetVal);
            alert.setUser(u1.get());
            alertsRepo.save(alert);
            return alert;
        }
        else if (g1.get().getStatus() == Games.Status.FINAL)
        {
            throw new IllegalArgumentException("Game is finalized. Cannot add alerts to finalized game");
        }
        else if (g1.isEmpty())
        {
            throw new IllegalArgumentException("Game ID not found");
        }
        else if (u1.isEmpty())
        {
            throw new IllegalArgumentException("User ID not found");
        }
        else {
            throw new IllegalArgumentException("Unexpected error occurred while creating alert");
        }
    }

    @Override
    public Page<Alerts> getAllAlerts(Alerts.AlertStatus status, Alerts.AlertType type, String team, String timeframe, String username, Pageable pageable) {
        // adding custom order by 
        String customOrderSql = "CASE status " +
                        "  WHEN 'TRIGGERED' THEN 1 " +
                        "  WHEN 'CREATED' THEN 2 " +
                        "  WHEN 'FINISHED' THEN 3 " +
                        "  ELSE 4 END";
        Sort customSort = JpaSort.unsafe(Sort.Direction.ASC, customOrderSql)
            .and(Sort.by(Sort.Direction.DESC, "id"));
        Pageable serverPageable = PageRequest.of(
            pageable.getPageNumber(),
            pageable.getPageSize(),
            customSort
        );
        Specification<Alerts> spec = Specification.unrestricted();
        spec = spec.and(AlertsSpecifications.belongsToUser(username));
        if (status != null){
            spec = spec.and(AlertsSpecifications.hasStatus(status));
        }
        if (team != null && !team.isEmpty()){
            spec = spec.and(AlertsSpecifications.hasTeam(team));
        }
        if (type != null){
            spec = spec.and(AlertsSpecifications.hasType(type));
        }
        if (timeframe != null){
            try {
                Instant timeframeStart = Utilities.convertToInstant(timeframe);
                spec = spec.and(AlertsSpecifications.hasDateRange(timeframeStart, Instant.now()));
            }
            catch (IllegalArgumentException e)
            {
                throw new IllegalArgumentException("Invalid timeframe format. Use formats like 'daily', 'weekly', 'monthly', or 'yearly'.");
            }
        }
        return alertsRepo.findAll(spec, serverPageable);
    }   
    @Override
    public Alerts getAlertById(Long id) {
        Optional<Alerts> alert = alertsRepo.findById(id);
        if (alert.isPresent())
            return alert.get();
        else
        {
            throw new EntityNotFoundException("Alert ID not found");
        }

    }
    @Override
    public List<Alerts> getAlertsByTeamName(String teamName) {
        return alertsRepo.findByTeamName(teamName);
    }

    @Override
    public List<Alerts> getAlertsByAlertType(Alerts.AlertType alertType) {
        return alertsRepo.findByAlertType(alertType);
    }


    @Override
    public List<Alerts> getAlertsByStatus(Alerts.AlertStatus status)
    {
        return alertsRepo.findByStatus(status);
    }

    @Override
    public List<Alerts> getAlertsByCreatedAt(Instant createdAt)
    {
        return alertsRepo.findByCreatedAt(createdAt);
    }

    @Override
    public List<Alerts> getAlertsByTypeAndTeam(String team, Alerts.AlertType type)
    {
        return alertsRepo.findByTeamNameAndAlertType(team, type);
    }
    @Override
    public void deleteAlert(Long id) {
        alertsRepo.deleteById(id);
    }
    @Override
    public List<Alerts> getAlertsByStatusAndNotificationReady(Alerts.AlertStatus stat, boolean isNotification, Instant notifiedAt)
    {
        return alertsRepo.findByStatusAndIsNotificationAndNotifiedAt(stat, isNotification, notifiedAt);
    }

    @Override
    public Alerts saveAlert(Alerts alert) {
        return alertsRepo.save(alert);
    }

    // DONE: should check by alert status not by game
    @Transactional
    public List<Alerts> updateAlertsStatus(Alerts.AlertStatus stat) {
        List<Alerts> alerts = alertsRepo.findByStatus(stat);
        List<Alerts> triggered = new ArrayList<Alerts>();
        if (alerts.isEmpty()) {
            return new ArrayList<Alerts>();
        }
        for (Alerts a : alerts) {
            String alertTeam = a.getTeamName();
            Alerts.AlertType type = a.getAlertType();
            Games game = a.getGame();
            int score;
            if (game == null) continue;
            if (alertTeam == null || type == null) continue;
            if (alertTeam.equalsIgnoreCase(game.getHomeTeam())) {
                score = game.getHomeScore();
            } else if (alertTeam.equalsIgnoreCase(game.getAwayTeam())) {
                score = game.getAwayScore();
            } else {
                continue; 
            }
            if (a.getAlertStatus() == AlertStatus.CREATED) {
                if (type == Alerts.AlertType.SCORE_OVER) {
                    if (score >= a.getTargetVal()) {
                        a.setAlertStatus(AlertStatus.TRIGGERED);
                        a.setTriggeredAt(Instant.now());
                        a.setIsNotification(true);
                        triggered.add(a);
                    }
                } else if (type == Alerts.AlertType.SCORE_UNDER) {
                    if (game.getStatus() == Games.Status.FINAL &&
                        score <= a.getTargetVal()) {
                        a.setAlertStatus(AlertStatus.TRIGGERED);
                        a.setTriggeredAt(Instant.now());
                        a.setIsNotification(true);
                        triggered.add(a);
                    }
                }
            }
            if (game.getStatus() == Games.Status.FINAL) {
                if (game.getStatus() == Games.Status.FINAL && Instant.now().isAfter(game.getUpdatedTime().plusSeconds(3600))) {
                    a.setAlertStatus(AlertStatus.FINISHED);
                }
            }
        }
        return triggered;
    }
}
