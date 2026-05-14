package org.example.sportslivev1.serversideevents;

import java.io.IOException;

import org.example.sportslivev1.entity.Alerts;
import org.example.sportslivev1.service.AlertsServiceImp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.example.sportslivev1.entity.Alerts;
import org.example.sportslivev1.service.AlertsServiceImp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
public class SSE {

    @Autowired
    private SSERegistry registry;
    
    @Autowired
    private AlertsServiceImp alertsService;

    @GetMapping("/sse")
    public SseEmitter alertsNotifier() {
        SseEmitter emitter = registry.subscribe();
        
        List<Alerts> pending = alertsService.getAlertsByStatusAndNotificationReady(Alerts.AlertStatus.TRIGGERED, true, null);
        
        for (Alerts a : pending) {
            try {
                emitter.send(SseEmitter.event().name("ALERT").data(a));
            } catch (IOException e) {
                
                break;
            }
        }
        
        return emitter;
    }
}
