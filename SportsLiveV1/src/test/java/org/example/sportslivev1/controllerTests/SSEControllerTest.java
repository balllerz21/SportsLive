package org.example.sportslivev1.controllerTests;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

import org.example.sportslivev1.entity.Alerts;
import org.example.sportslivev1.entity.Games;
import org.example.sportslivev1.entity.Users;
import org.example.sportslivev1.serversideevents.SSE;
import org.example.sportslivev1.serversideevents.SSERegistry;
import org.example.sportslivev1.service.AlertsServiceImp;
import org.example.sportslivev1.service.UsersServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc(addFilters = false)
public class SSEControllerTest {

    @Mock AlertsServiceImp service;
    @Mock SSERegistry registry;
    @Mock SseEmitter emitter;
    @Mock UsersServiceImpl usersService;
    
    @InjectMocks SSE controller;
    
    @Test
    void sendsSnapshotOnSubscribe() throws IOException {
        Long clientId = 7L;
        Games game = new Games("401866759", "Phoenix Suns", "Golden State Warriors",
                111, 96, Games.Status.FINAL,
                Instant.parse("2026-04-18T02:00:00Z"));
        Users user = new Users("testuser", "password", Users.UserRole.USER);
        user.setId(clientId);
        Alerts a1 = new Alerts(game, "Phoenix Suns", Alerts.AlertType.SCORE_OVER, 100);
        a1.setId(1L);
        a1.setUser(user);
        a1.setIsNotification(true);
        a1.setTriggeredAt(Instant.now());

        when(usersService.getUserByUserName("testuser")).thenReturn(user);
        when(registry.subscribe(clientId)).thenReturn(emitter);
        when(service.getAlertsByStatusAndNotificationReady(
                Alerts.AlertStatus.TRIGGERED, true, null))
            .thenReturn(List.of(a1));

        SseEmitter result = controller.alertsNotifier(clientId, new UsernamePasswordAuthenticationToken("testuser", null));

        verify(registry).subscribe(clientId);
        verify(emitter).send(any(SseEmitter.SseEventBuilder.class));
        assertThat(result).isSameAs(emitter);
    }

    @Test
    void sendsOnlyClientSnapshotOnSubscribe() throws IOException {
        Long clientId = 7L;
        Games game = new Games("401866759", "Phoenix Suns", "Golden State Warriors",
                111, 96, Games.Status.FINAL,
                Instant.parse("2026-04-18T02:00:00Z"));
        Users matchingUser = new Users("testuser", "password", Users.UserRole.USER);
        matchingUser.setId(clientId);
        Users otherUser = new Users("otheruser", "password", Users.UserRole.USER);
        otherUser.setId(8L);

        Alerts matchingAlert = new Alerts(game, "Phoenix Suns", Alerts.AlertType.SCORE_OVER, 100);
        matchingAlert.setId(1L);
        matchingAlert.setUser(matchingUser);

        Alerts otherAlert = new Alerts(game, "Golden State Warriors", Alerts.AlertType.SCORE_OVER, 100);
        otherAlert.setId(2L);
        otherAlert.setUser(otherUser);

        when(usersService.getUserByUserName("testuser")).thenReturn(matchingUser);
        when(registry.subscribe(clientId)).thenReturn(emitter);
        when(service.getAlertsByStatusAndNotificationReady(
                Alerts.AlertStatus.TRIGGERED, true, null))
            .thenReturn(List.of(matchingAlert, otherAlert));

        SseEmitter result = controller.alertsNotifier(clientId, new UsernamePasswordAuthenticationToken("testuser", null));

        verify(registry).subscribe(clientId);
        verify(emitter, times(1)).send(any(SseEmitter.SseEventBuilder.class));
        assertThat(result).isSameAs(emitter);
    }
}
