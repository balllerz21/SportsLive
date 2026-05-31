package org.example.sportslivev1.serversideevents;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.example.sportslivev1.dto.AlertMapper;
import org.example.sportslivev1.entity.Alerts;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Component
public class SSERegistry {
    
    // private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();
    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    public SseEmitter subscribe(Long client_id) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        emitters.put(client_id, emitter);
        emitter.onCompletion(() -> emitters.remove(client_id));
        emitter.onTimeout(() -> emitters.remove(client_id));
        emitter.onError(e -> emitters.remove(client_id));
        return emitter;
    }

    public void broadcast(Long id, Alerts alert) {
        SseEmitter emitter = emitters.get(id);
        if (emitter == null)
        {
            return;
        }
        try {
            emitter.send(SseEmitter.event().name("ALERT").data(AlertMapper.toResponse(alert)));
        } catch (IOException ex) {
            emitters.remove(id);
        }
    }
}
