package com.golearn.myf3school_backend.infrastructure.sse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Lưu trữ tất cả SSE connections đang active, phân theo userId.
 * Thread-safe: dùng ConcurrentHashMap + CopyOnWriteArrayList.
 */
@Slf4j
@Component
public class SseEmitterRegistry {

    // userId → danh sách emitter (1 user có thể mở nhiều tab)
    private final Map<Long, CopyOnWriteArrayList<SseEmitter>> registry =
            new ConcurrentHashMap<>();

    /**
     * Đăng ký emitter mới cho userId.
     * Khi emitter complete/timeout/error → tự xóa khỏi registry.
     */
    public SseEmitter register(Long userId, long timeoutMs) {
        SseEmitter emitter = new SseEmitter(timeoutMs);

        registry.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>())
                .add(emitter);

        log.debug("[SSE] register userId={} total={}", userId,
                registry.getOrDefault(userId, new CopyOnWriteArrayList<>()).size());

        Runnable cleanup = () -> remove(userId, emitter);
        emitter.onCompletion(cleanup);
        emitter.onTimeout(cleanup);
        emitter.onError(e -> {
            log.debug("[SSE] error userId={}: {}", userId, e.getMessage());
            cleanup.run();
        });

        return emitter;
    }

    /** Xóa 1 emitter cụ thể khỏi registry. */
    public void remove(Long userId, SseEmitter emitter) {
        CopyOnWriteArrayList<SseEmitter> list = registry.get(userId);
        if (list != null) {
            list.remove(emitter);
            if (list.isEmpty()) registry.remove(userId);
            log.debug("[SSE] remove userId={} remaining={}", userId,
                    list.size());
        }
    }

    /**
     * Lấy snapshot danh sách emitter của userId.
     * Trả về list rỗng nếu user không online.
     */
    public List<SseEmitter> getEmitters(Long userId) {
        CopyOnWriteArrayList<SseEmitter> list = registry.get(userId);
        return list != null ? new ArrayList<>(list) : List.of();
    }

    /** Số user đang kết nối (để monitor). */
    public int activeUserCount() {
        return registry.size();
    }

    /** Tổng số kết nối (để monitor). */
    public int totalConnectionCount() {
        return registry.values().stream().mapToInt(List::size).sum();
    }

    /**
     * Snapshot toàn bộ registry (dùng cho heartbeat scheduler).
     * Trả về Map bất biến — an toàn để iterate ngoài class.
     */
    public Map<Long, List<SseEmitter>> getAllEmitters() {
        Map<Long, List<SseEmitter>> snapshot = new ConcurrentHashMap<>();
        registry.forEach((userId, list) -> snapshot.put(userId, new ArrayList<>(list)));
        return snapshot;
    }
}