package org.example.sportslivev1.serversideevents;

import java.io.IOException;
import java.security.Principal;
import java.util.List;

import org.example.sportslivev1.dto.AlertMapper;
import org.example.sportslivev1.entity.Alerts;
import org.example.sportslivev1.entity.Users;
import org.example.sportslivev1.service.AlertsServiceImp;
import org.example.sportslivev1.service.UsersServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
public class SSE {

    @Autowired
    private SSERegistry registry;
    
    @Autowired
    private AlertsServiceImp alertsService;

    @Autowired
    private UsersServiceImpl usersService;

    @GetMapping("/sse")
    public SseEmitter alertsNotifier(@RequestParam(name = "clientId") Long client_id, Principal principal) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }

        Users user = usersService.getUserByUserName(principal.getName());
        if (!client_id.equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot subscribe to another user's alerts");
        }

        SseEmitter emitter = registry.subscribe(client_id);
        
        List<Alerts> pending = alertsService.getAlertsByStatusAndNotificationReady(Alerts.AlertStatus.TRIGGERED, true, null);
        
        for (Alerts a : pending) {
            if (a.getUser() == null || !client_id.equals(a.getUser().getId())) {
                continue;
            }
            try {
                emitter.send(SseEmitter.event()
                    .id(String.valueOf(a.getId()))
                    .name("ALERT")
                    .data(AlertMapper.toResponse(a)));
            } catch (IOException e) {
                
                break;
            }
        }
        
        return emitter;
    }
}
