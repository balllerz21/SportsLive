package org.example.sportslivev1.serversideevents;

import java.io.IOException;
import java.util.List;

import org.example.sportslivev1.dto.AlertMapper;
import org.example.sportslivev1.entity.Alerts;
import org.example.sportslivev1.service.AlertsServiceImp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
public class SSE {

    @Autowired
    private SSERegistry registry;
    
    @Autowired
    private AlertsServiceImp alertsService;

    @GetMapping("/sse")
    public SseEmitter alertsNotifier(@RequestParam(name = "clientId") Long client_id) {
        SseEmitter emitter = registry.subscribe(client_id);
        
        List<Alerts> pending = alertsService.getAlertsByStatusAndNotificationReady(Alerts.AlertStatus.TRIGGERED, true, null);
        
        for (Alerts a : pending) {
            if (a.getUser() == null || !client_id.equals(a.getUser().getId())) {
                continue;
            }
            try {
                emitter.send(SseEmitter.event().name("ALERT").data(AlertMapper.toResponse(a)));
            } catch (IOException e) {
                
                break;
            }
        }
        
        return emitter;
    }
}
