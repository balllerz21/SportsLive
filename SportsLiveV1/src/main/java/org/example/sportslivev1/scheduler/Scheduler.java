package org.example.sportslivev1.scheduler;

import java.util.List;

import org.example.sportslivev1.entity.Alerts;
import org.example.sportslivev1.entity.Alerts.AlertStatus;
import org.example.sportslivev1.serversideevents.SSERegistry;
import org.example.sportslivev1.service.AlertsServiceImp;
import org.example.sportslivev1.service.PollingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
    name = "sportslive.scheduling.enabled",
    havingValue = "true",
    matchIfMissing = true
)
public class Scheduler {
    @Autowired
    AlertsServiceImp alertsService;
    @Autowired
    PollingService pollingService;
    @Autowired
    SSERegistry sseRegistry;

    @Scheduled(fixedRate = 60000)
    public void scheduleLive() {
        pollingService.createOrUpdateGame("basketball", "nba");
        pollingService.createOrUpdateGame("basketball", "wnba");
        pollingService.createOrUpdateGame("soccer", "fifa.world");
        List<Alerts> newlyTriggered = alertsService.updateAlertsStatus(AlertStatus.CREATED);
        alertsService.updateAlertsStatus(AlertStatus.TRIGGERED); 
        newlyTriggered.forEach(a -> {
        if (a.getUser() != null) {
            sseRegistry.broadcast(a.getUser().getId(), a);
        }
        });
    }

}
