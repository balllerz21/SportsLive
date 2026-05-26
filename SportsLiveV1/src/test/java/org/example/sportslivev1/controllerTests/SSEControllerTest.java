package org.example.sportslivev1.controllerTests;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

import org.example.sportslivev1.entity.Alerts;
import org.example.sportslivev1.entity.Games;
import org.example.sportslivev1.serversideevents.SSE;
import org.example.sportslivev1.serversideevents.SSERegistry;
import org.example.sportslivev1.entity.Alerts.AlertStatus;
import org.example.sportslivev1.service.AlertsServiceImp;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc(addFilters = false)
public class SSEControllerTest {

    @Mock AlertsServiceImp service;
    @Mock SSERegistry registry;
    @Mock SseEmitter emitter;
    
    @InjectMocks SSE controller;
    
    @Test
    void sendsSnapshotOnSubscribe() throws IOException {
        Games game = new Games("401866759", "Phoenix Suns", "Golden State Warriors",
                111, 96, Games.Status.FINAL,
                Instant.parse("2026-04-18T02:00:00Z"));
        Alerts a1 = new Alerts(game, "Phoenix Suns", Alerts.AlertType.SCORE_OVER, 100);
        a1.setId(1L);
        a1.setIsNotification(true);
        a1.setTriggeredAt(Instant.now());

        when(registry.subscribe()).thenReturn(emitter);
        when(service.getAlertsByStatusAndNotificationReady(
                Alerts.AlertStatus.TRIGGERED, true, null))
            .thenReturn(List.of(a1));

        SseEmitter result = controller.alertsNotifier();

        verify(registry).subscribe();
        verify(emitter).send(any(SseEmitter.SseEventBuilder.class));
        assertThat(result).isSameAs(emitter);
    }
}
