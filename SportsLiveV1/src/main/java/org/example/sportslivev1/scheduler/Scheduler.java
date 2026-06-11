package org.example.sportslivev1.scheduler;

import java.util.List;

import org.example.sportslivev1.entity.Alerts;
import org.example.sportslivev1.entity.Games;
import org.example.sportslivev1.entity.Alerts.AlertStatus;
import org.example.sportslivev1.serversideevents.SSERegistry;
import org.example.sportslivev1.service.AlertsServiceImp;
import org.example.sportslivev1.service.GamesService;
import org.example.sportslivev1.service.GamesServiceImp;
import org.example.sportslivev1.service.PollingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class Scheduler {
    @Autowired
    AlertsServiceImp alertsService;
    @Autowired
    PollingService pollingService;
    @Autowired
    SSERegistry sseRegistry;

    @Scheduled(fixedRate = 60000)
    public void scheduleLive() {
        pollingService.createOrUpdateGame("nba");
        pollingService.createOrUpdateGame("wnba");
        List<Alerts> newlyTriggered = alertsService.updateAlertsStatus(AlertStatus.CREATED);
        alertsService.updateAlertsStatus(AlertStatus.TRIGGERED); 
        newlyTriggered.forEach(a -> {
        if (a.getUser() != null) {
            sseRegistry.broadcast(a.getUser().getId(), a);
        }
        });
    }

}
