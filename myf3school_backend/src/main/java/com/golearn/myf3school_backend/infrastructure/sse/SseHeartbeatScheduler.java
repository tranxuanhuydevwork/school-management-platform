package com.golearn.myf3school_backend.infrastructure.sse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Gửi ping mỗi 25 giây tới tất cả kết nối đang active.
 * Mục đích: tránh proxy/nginx ngắt kết nối do idle timeout.
 *
 * Cần @EnableScheduling trong main class hoặc config.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SseHeartbeatScheduler {

    private final SseEmitterRegistry registry;

    @Scheduled(fixedDelay = 25_000)  // 25 giây
    public void heartbeat() {
        int users = registry.activeUserCount();
        int conns = registry.totalConnectionCount();
        if (conns == 0) return;

        log.debug("[SSE] heartbeat → {} users, {} connections", users, conns);

        SseEmitter.SseEventBuilder ping = SseEmitter.event()
                .name("ping")
                .data("{}");

        // Duyệt qua tất cả emitters trong registry
        // (registry expose method getAllEmitters để tránh coupling)
        registry.getAllEmitters().forEach((userId, emitters) -> {
            for (SseEmitter emitter : emitters) {
                try {
                    emitter.send(ping);
                } catch (IOException e) {
                    registry.remove(userId, emitter);
                }
            }
        });
    }
}