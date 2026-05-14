package org.example.sportslivev1.service;
import org.example.sportslivev1.entity.Alerts;
import org.example.sportslivev1.entity.Alerts.AlertStatus;
import org.example.sportslivev1.entity.Alerts.AlertType;
import org.example.sportslivev1.entity.Games;
import org.example.sportslivev1.repository.AlertsRepo;
import org.example.sportslivev1.repository.GamesRepo;
import org.example.sportslivev1.specifications.AlertsSpecifications;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;

import java.time.Instant;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import javax.naming.NameNotFoundException;


@Service
public class AlertsServiceImp implements AlertsService {
    @Autowired
    AlertsRepo alertsRepo;
    @Autowired
    GamesRepo gamesRepo;
    // Without autowired //
    // public AlertsServiceImp(AlertsRepo alertsRepo) {
    //     this.alertsRepo = alertsRepo;
    // }

    // add error handiling
    @Override
    public Alerts createAlert(Games game, String teamName, Alerts.AlertType alertType, int targetVal) {
        Optional<Games> g1 = gamesRepo.findById(game.getId());
        if (g1.isPresent())
        {
            Alerts alert = new Alerts(g1.get(), teamName, alertType, targetVal);
            alertsRepo.save(alert);
            return alert;
        }
        else 
        {
            throw new IllegalArgumentException("Wrong Fields for Alert.");
        }
    }

    @Override
    public List<Alerts> getAllAlerts(Alerts.AlertStatus status, Alerts.AlertType type, String team, Instant date) {
        Specification<Alerts> spec = Specification.unrestricted();
        if (status != null){
            spec = spec.and(AlertsSpecifications.hasStatus(status));
        }
        if (team != null && !team.isEmpty()){
            spec = spec.and(AlertsSpecifications.hasTeam(team));
        }
        if (type != null){
            spec = spec.and(AlertsSpecifications.hasType(type));
        }
        if (date != null){
            spec = spec.and(AlertsSpecifications.hasDate(date));
        }
        return (List<Alerts>) alertsRepo.findAll(spec);
    }
    public List<Alerts> getAllAlerts()
    {
        return (List<Alerts>) alertsRepo.findAll();
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

    // Why is this marked as Transactional?
    // Fix: should check by alert status not by game
    @Transactional
    public void updateAlertsStatus(Alerts.AlertStatus stat) {
        List<Alerts> alerts = alertsRepo.findByStatus(stat);
        if (alerts.isEmpty()) {
            return;
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
                    }
                } else if (type == Alerts.AlertType.SCORE_UNDER) {
                    if (game.getStatus() == Games.Status.FINAL &&
                        score <= a.getTargetVal()) {
                        a.setAlertStatus(AlertStatus.TRIGGERED);
                        a.setTriggeredAt(Instant.now());
                    }
                }
            }
            if (game.getStatus() == Games.Status.FINAL) {
                if (game.getStatus() == Games.Status.FINAL && Instant.now().isAfter(game.getUpdatedTime().plusSeconds(3600))) {
                    a.setAlertStatus(AlertStatus.FINISHED);
                }
            }
        }
    }
}
