package org.inharidge.fairact_contract_be.config;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SseEmitterManager {

    // 구조 변경: 유저별 → 이벤트별 emitter 관리
    private final Map<Long, Map<String, SseEmitter>> emitterMap = new ConcurrentHashMap<>();

    public SseEmitter addEmitter(Long userId, String eventType) {
        SseEmitter emitter = new SseEmitter(60 * 60 * 1000L); // 1시간 유지

        emitter.onCompletion(() -> removeEmitter(userId, eventType));
        emitter.onTimeout(() -> removeEmitter(userId, eventType));
        emitter.onError(e -> removeEmitter(userId, eventType));

        emitterMap.computeIfAbsent(userId, id -> new ConcurrentHashMap<>())
                .put(eventType, emitter);

        return emitter;
    }

    public void sendToUser(Long userId, String eventType, Object data) {
        Map<String, SseEmitter> userEmitters = emitterMap.get(userId);
        if (userEmitters == null) return;

        SseEmitter emitter = userEmitters.get(eventType);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name(eventType)
                        .data(data));
            } catch (IOException e) {
                emitter.completeWithError(e);
                userEmitters.remove(eventType);
            }
        }
    }

    public void sendErrorToClient(SseEmitter emitter, String message) {
        try {
            emitter.send(SseEmitter.event()
                    .name("error")
                    .data(Collections.singletonMap("message", message)));
            emitter.complete();
        } catch (IOException ioException) {
            emitter.completeWithError(ioException);
        }
    }

    private void removeEmitter(Long userId, String eventType) {
        Map<String, SseEmitter> userEmitters = emitterMap.get(userId);
        if (userEmitters != null) {
            userEmitters.remove(eventType);
            if (userEmitters.isEmpty()) {
                emitterMap.remove(userId);
            }
        }
    }
}

