package org.example.sportslivev1.serversideevents;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.example.sportslivev1.entity.Alerts;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Component
public class SSERegistry {
    
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        emitters.add(emitter);
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError(e -> emitters.remove(emitter));
        return emitter;
    }

    public void broadcast(Alerts alert) {
        for (SseEmitter e : emitters) {
            try {
                e.send(SseEmitter.event().name("ALERT").data(alert));
            } catch (IOException ex) {
                emitters.remove(e);
            }
        }
    }
}
