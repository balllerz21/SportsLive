package org.example.sportslivev1.serviceTests;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.example.sportslivev1.entity.Alerts;
import org.example.sportslivev1.entity.Games;
import org.example.sportslivev1.serversideevents.SSERegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import static org.assertj.core.api.Assertions.assertThat;


@ExtendWith(MockitoExtension.class)
public class SSETests {
    private SSERegistry registry;
    private Alerts sampleAlert;

    @BeforeEach
    public void setUp() {
        registry = new SSERegistry();
        Games game = new Games("401866759", "Phoenix Suns", "Golden State Warriors",
                111, 96, Games.Status.FINAL,
                Instant.parse("2026-04-18T02:00:00Z"));
        sampleAlert = new Alerts(game, "Phoenix Suns", 
                Alerts.AlertType.SCORE_OVER, 100);
        sampleAlert.setId(1L);
    }

    @Test
    public void subscribe_returnsNonNullEmitter() {
        assertThat(registry.subscribe()).isNotNull();
    }

    @Test
    public void subscribe_addsEmitterToInternalList() {
        registry.subscribe();
        registry.subscribe();
        
        List<SseEmitter> emitters = 
            (List<SseEmitter>) ReflectionTestUtils.getField(registry, "emitters");
        assertThat(emitters).hasSize(2);
    }

    @Test
    public void broadcast_sendsToAllSubscribers() throws IOException {
        SseEmitter e1 = mock(SseEmitter.class);
        SseEmitter e2 = mock(SseEmitter.class);
        injectEmitters(e1, e2);

        registry.broadcast(sampleAlert);

        verify(e1).send(any(SseEmitter.SseEventBuilder.class));
        verify(e2).send(any(SseEmitter.SseEventBuilder.class));
    }

    @Test
    public void broadcast_removesDeadEmitters() throws IOException {
        SseEmitter alive = mock(SseEmitter.class);
        SseEmitter dead = mock(SseEmitter.class);
        doThrow(new IOException("client gone"))
            .when(dead).send(any(SseEmitter.SseEventBuilder.class));
        injectEmitters(alive, dead);

        registry.broadcast(sampleAlert);

        List<SseEmitter> remaining = 
            (List<SseEmitter>) ReflectionTestUtils.getField(registry, "emitters");
        assertThat(remaining).containsExactly(alive).doesNotContain(dead);
    }

    private void injectEmitters(SseEmitter... emitters) {
        ReflectionTestUtils.setField(registry, "emitters", new CopyOnWriteArrayList<>(List.of(emitters)));
    }
}
