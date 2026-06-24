package com.golearn.myf3school_backend.application_service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.golearn.myf3school_backend.application_service.dtos.response.NotificationResponse;
import com.golearn.myf3school_backend.infrastructure.sse.SseEmitterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;

/**
 * Đẩy thông báo real-time tới browser qua SSE.
 *
 * Cách dùng — gọi sau khi lưu Notification vào DB:
 *   notificationSseService.push(notification.getUser().getId(),
 *                               NotificationResponse.from(notification));
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationSseService {

    private final SseEmitterRegistry registry;

    private final ObjectMapper mapper = buildMapper();

    /**
     * Gửi event "notification" tới tất cả tab của userId.
     * Dead emitters bị xóa tự động.
     */
    public void push(Long userId, NotificationResponse payload) {
        List<SseEmitter> emitters = registry.getEmitters(userId);
        if (emitters.isEmpty()) return;

        String json;
        try {
            json = mapper.writeValueAsString(payload);
        } catch (Exception e) {
            log.error("[SSE] serialize error: {}", e.getMessage());
            return;
        }

        SseEmitter.SseEventBuilder event = SseEmitter.event()
                .name("notification")   // JS: source.addEventListener('notification', ...)
                .data(json);

        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(event);
            } catch (IOException e) {
                // Emitter đã đóng → cleanup tự động qua onError callback
                log.debug("[SSE] dead emitter for userId={}, removing", userId);
                registry.remove(userId, emitter);
            }
        }
        log.debug("[SSE] pushed to userId={} ({} tabs)", userId, emitters.size());
    }

    /**
     * Gửi heartbeat để giữ kết nối sống (tránh proxy timeout).
     * Gọi từ @Scheduled task mỗi 25 giây.
     */
    public void sendHeartbeat(Long userId) {
        List<SseEmitter> emitters = registry.getEmitters(userId);
        SseEmitter.SseEventBuilder ping = SseEmitter.event()
                .name("ping")
                .data("{}");
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(ping);
            } catch (IOException e) {
                registry.remove(userId, emitter);
            }
        }
    }

    private static ObjectMapper buildMapper() {
        ObjectMapper m = new ObjectMapper();
        m.registerModule(new JavaTimeModule());
        m.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return m;
    }
}