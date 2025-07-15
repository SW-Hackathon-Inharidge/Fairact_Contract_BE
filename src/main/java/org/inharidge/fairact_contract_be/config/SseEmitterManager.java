package org.inharidge.fairact_contract_be.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.*;

@Slf4j
@Component
public class SseEmitterManager {

    private final Map<Long, Map<String, SseEmitter>> emitterMap = new ConcurrentHashMap<>();
    private final Map<SseEmitter, Object> emitterLocks = new ConcurrentHashMap<>();
    private final Map<SseEmitter, ScheduledExecutorService> schedulerMap = new ConcurrentHashMap<>();

    public SseEmitter addEmitter(Long userId, String eventType) {
        Map<String, SseEmitter> userEmitters = emitterMap.computeIfAbsent(userId, id -> new ConcurrentHashMap<>());

        if (userEmitters.containsKey(eventType)) {
            SseEmitter existingEmitter = userEmitters.get(eventType);
            if (existingEmitter != null) {
                log.info("[SSE] 동일 userId와 eventType 조합의 기존 emitter 존재. 연결 종료 처리. userId={}, eventType={}", userId, eventType);
                existingEmitter.complete();
            }
        }

        SseEmitter emitter = new SseEmitter(60 * 60 * 1000L);
        Object lock = new Object();
        emitterLocks.put(emitter, lock);
        userEmitters.put(eventType, emitter);

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        schedulerMap.put(emitter, scheduler);

        scheduler.scheduleAtFixedRate(() -> {
            synchronized (lock) {
                try {
                    emitter.send(SseEmitter.event().name("keep-alive").data("ping"));
                } catch (IOException e) {
                    log.warn("[SSE] keep-alive 전송 실패. userId={}, eventType={}", userId, eventType);
                }
            }
        }, 0, 30, TimeUnit.SECONDS);

        emitter.onCompletion(() -> {
            log.info("[SSE] emitter 종료 - onCompletion. userId={}, eventType={}", userId, eventType);
            removeEmitter(userId, eventType, emitter);
        });

        emitter.onTimeout(() -> {
            log.info("[SSE] emitter 종료 - onTimeout. userId={}, eventType={}", userId, eventType);
            removeEmitter(userId, eventType, emitter);
        });

        emitter.onError(e -> {
            log.warn("[SSE] emitter 종료 - onError. userId={}, eventType={}, error={}", userId, eventType, e.toString());
            removeEmitter(userId, eventType, emitter);
        });

        log.info("[SSE] 새 emitter 등록 완료. userId={}, eventType={}", userId, eventType);
        return emitter;
    }

    public void sendToUser(Long userId, String eventType, Object data) {
        Map<String, SseEmitter> userEmitters = emitterMap.get(userId);
        if (userEmitters == null) return;

        SseEmitter emitter = userEmitters.get(eventType);
        if (emitter != null) {
            Object lock = emitterLocks.get(emitter);
            synchronized (lock) {
                try {
                    emitter.send(SseEmitter.event().name(eventType).data(data));
                    log.info("[SSE] 이벤트 전송 성공. userId={}, eventType={}", userId, eventType);
                } catch (IOException e) {
                    log.warn("[SSE] 이벤트 전송 실패. userId={}, eventType={}, error={}", userId, eventType, e.getMessage());
                    emitter.completeWithError(e);
                }
            }
        }
    }

    public void sendErrorToClient(Long userId, String eventType, String message) {
        Map<String, SseEmitter> userEmitters = emitterMap.get(userId);
        if (userEmitters == null) return;

        SseEmitter emitter = userEmitters.get(eventType);
        if (emitter != null) {
            Object lock = emitterLocks.get(emitter);
            synchronized (lock) {
                try {
                    emitter.send(SseEmitter.event()
                            .name("error")
                            .data(Collections.singletonMap("message", message)));
                    log.info("[SSE] 에러 메시지 전송. userId={}, message={}", userId, message);
                } catch (IOException ioException) {
                    log.warn("[SSE] 에러 메시지 전송 실패. userId={}, error={}", userId, ioException.getMessage());
                } finally {
                    emitter.complete();
                }
            }
        }
    }

    private void removeEmitter(Long userId, String eventType, SseEmitter emitter) {
        Map<String, SseEmitter> userEmitters = emitterMap.get(userId);
        if (userEmitters != null) {
            SseEmitter current = userEmitters.get(eventType);
            if (current == emitter) {
                userEmitters.remove(eventType);
                log.info("[SSE] removeEmitter: 등록된 emitter와 일치하여 제거함. userId={}, eventType={}", userId, eventType);
            } else {
                log.info("[SSE] removeEmitter: 등록된 emitter와 다르므로 제거하지 않음. userId={}, eventType={}", userId, eventType);
            }

            if (userEmitters.isEmpty()) {
                emitterMap.remove(userId);
            }
        }

        ScheduledExecutorService scheduler = schedulerMap.remove(emitter);
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdownNow();
        }

        emitterLocks.remove(emitter);
    }
}