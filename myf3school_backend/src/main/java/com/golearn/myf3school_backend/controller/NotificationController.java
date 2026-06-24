package com.golearn.myf3school_backend.controller;

import com.golearn.myf3school_backend.application_service.dtos.request.NotificationRequest;
import com.golearn.myf3school_backend.application_service.dtos.response.ApiResponse;
import com.golearn.myf3school_backend.application_service.dtos.response.NotificationResponse;
import com.golearn.myf3school_backend.application_service.dtos.response.PagedResponse;
import com.golearn.myf3school_backend.application_service.exception.NotFoundException;
import com.golearn.myf3school_backend.application_service.service.NotificationSseService;
import com.golearn.myf3school_backend.infrastructure.entity.Notification;
import com.golearn.myf3school_backend.infrastructure.entity.User;
import com.golearn.myf3school_backend.infrastructure.repository.NotificationRepository;
import com.golearn.myf3school_backend.infrastructure.repository.UserRepository;
import com.golearn.myf3school_backend.infrastructure.sse.SseEmitterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationRepository notificationRepository;
    private final UserRepository         userRepository;
    private final SseEmitterRegistry     sseRegistry;
    private final NotificationSseService sseService;

    // ─────────────────────────────────────────────────────────
    // GET /api/notifications/users/{userId}/sse
    //
    // Browser kết nối 1 lần, server đẩy event về bất cứ khi nào
    // có thông báo mới.
    //
    // Timeout 5 phút — browser tự reconnect khi mất kết nối
    // (EventSource có built-in retry logic).
    //
    // Produces: text/event-stream (SSE standard)
    // ─────────────────────────────────────────────────────────
    @GetMapping(
            value    = "/users/{userId}/sse",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE
    )
    public SseEmitter subscribe(@PathVariable Long userId) {

        // Timeout 5 phút. Browser tự reconnect sau khi hết.
        SseEmitter emitter = sseRegistry.register(userId, 5 * 60 * 1000L);

        // Gửi event "connected" ngay lập tức để:
        // 1) Xác nhận kết nối thành công với browser
        // 2) Tránh browser hiểu là kết nối trống → đóng sớm
        try {
            emitter.send(
                    SseEmitter.event()
                            .name("connected")
                            .data("{\"userId\":" + userId + "}")
            );
        } catch (Exception e) {
            emitter.completeWithError(e);
        }

        return emitter;
    }

    // ─────────────────────────────────────────────────────────
    // GET /api/notifications/users/{userId}?page=0&size=20
    // ─────────────────────────────────────────────────────────
    @GetMapping("/users/{userId}")
    public ResponseEntity<ApiResponse<PagedResponse<NotificationResponse>>> getByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<NotificationResponse> result = notificationRepository
                .findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(page, size))
                .map(NotificationResponse::from);

        return ResponseEntity.ok(ApiResponse.ok(PagedResponse.of(result)));
    }

    // ─────────────────────────────────────────────────────────
    // GET /api/notifications/users/{userId}/unread-count
    // ─────────────────────────────────────────────────────────
    @GetMapping("/users/{userId}/unread-count")
    public ResponseEntity<ApiResponse<Map<String, Long>>> unreadCount(
            @PathVariable Long userId) {

        long count = notificationRepository.countByUserIdAndIsReadFalse(userId);
        return ResponseEntity.ok(ApiResponse.ok(Map.of("count", count)));
    }

    // ─────────────────────────────────────────────────────────
    // POST /api/notifications
    // Sau khi lưu DB → đẩy SSE tới browser của user đó ngay lập tức
    // ─────────────────────────────────────────────────────────
    @PostMapping
    public ResponseEntity<ApiResponse<NotificationResponse>> send(
            @RequestBody NotificationRequest req) {

        if (req.getUserId() == null || req.getTitle() == null || req.getMessage() == null) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(400, "userId, title và message là bắt buộc"));
        }

        User user = userRepository.findById(req.getUserId())
                .orElseThrow(() -> new NotFoundException("User", req.getUserId()));

        Notification notification = Notification.builder()
                .user(user)
                .title(req.getTitle())
                .message(req.getMessage())
                .refType(req.getRefType())
                .refId(req.getRefId())
                .build();

        Notification saved = notificationRepository.save(notification);
        NotificationResponse response = NotificationResponse.from(saved);

        // ── Push SSE ngay sau khi lưu DB ──────────────────────
        sseService.push(req.getUserId(), response);

        return ResponseEntity.ok(ApiResponse.created(response));
    }

    // ─────────────────────────────────────────────────────────
    // PUT /api/notifications/{id}/read
    // ─────────────────────────────────────────────────────────
    @PutMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Void>> markRead(@PathVariable Long id) {
        Notification n = notificationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Notification", id));

        if (Boolean.FALSE.equals(n.getIsRead())) {
            n.setIsRead(true);
            n.setReadAt(LocalDateTime.now());
            notificationRepository.save(n);
        }

        return ResponseEntity.ok(ApiResponse.ok("Đã đọc", null));
    }

    // ─────────────────────────────────────────────────────────
    // PUT /api/notifications/users/{userId}/read-all
    // ─────────────────────────────────────────────────────────
    @PutMapping("/users/{userId}/read-all")
    @Transactional
    public ResponseEntity<ApiResponse<Void>> markAllRead(@PathVariable Long userId) {
        notificationRepository.markAllAsRead(userId);
        return ResponseEntity.ok(ApiResponse.ok("Đã đọc tất cả", null));
    }
}