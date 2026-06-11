package org.example.sportslivev1.serviceTests;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
        assertThat(registry.subscribe(1L)).isNotNull();
    }

    @Test
    public void subscribe_addsEmitterToInternalMapByClientId() {
        registry.subscribe(1L);
        registry.subscribe(2L);
        
        Map<Long, SseEmitter> emitters = 
            (Map<Long, SseEmitter>) ReflectionTestUtils.getField(registry, "emitters");
        assertThat(emitters).hasSize(2);
        assertThat(emitters).containsKeys(1L, 2L);
    }

    @Test
    public void subscribe_replacesExistingEmitterForClientId() {
        SseEmitter oldEmitter = mock(SseEmitter.class);
        injectEmitters(Map.of(1L, oldEmitter));

        SseEmitter newEmitter = registry.subscribe(1L);

        Map<Long, SseEmitter> emitters =
            (Map<Long, SseEmitter>) ReflectionTestUtils.getField(registry, "emitters");
        assertThat(emitters).containsEntry(1L, newEmitter);
        verify(oldEmitter).complete();
    }

    @Test
    public void broadcast_sendsOnlyToMatchingSubscriber() throws IOException {
        SseEmitter matching = mock(SseEmitter.class);
        SseEmitter other = mock(SseEmitter.class);
        injectEmitters(Map.of(1L, matching, 2L, other));

        registry.broadcast(1L, sampleAlert);

        verify(matching).send(any(SseEmitter.SseEventBuilder.class));
        verify(other, never()).send(any(SseEmitter.SseEventBuilder.class));
    }

    @Test
    public void broadcast_removesDeadEmitters() throws IOException {
        SseEmitter alive = mock(SseEmitter.class);
        SseEmitter dead = mock(SseEmitter.class);
        doThrow(new IOException("client gone"))
            .when(dead).send(any(SseEmitter.SseEventBuilder.class));
        injectEmitters(Map.of(1L, alive, 2L, dead));

        registry.broadcast(2L, sampleAlert);

        Map<Long, SseEmitter> remaining = 
            (Map<Long, SseEmitter>) ReflectionTestUtils.getField(registry, "emitters");
        assertThat(remaining).containsEntry(1L, alive);
        assertThat(remaining).doesNotContainKey(2L);
    }

    @Test
    public void broadcast_doesNothingWhenSubscriberMissing() throws IOException {
        SseEmitter other = mock(SseEmitter.class);
        injectEmitters(Map.of(2L, other));

        registry.broadcast(1L, sampleAlert);

        verify(other, never()).send(any(SseEmitter.SseEventBuilder.class));
    }

    private void injectEmitters(Map<Long, SseEmitter> emitters) {
        ReflectionTestUtils.setField(registry, "emitters", new ConcurrentHashMap<>(emitters));
    }
}
