package com.golearn.myf3school_backend.application_service.dtos.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.golearn.myf3school_backend.infrastructure.entity.Notification;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class NotificationResponse {

    private Long id;
    private String title;
    private String message;
    private String refType;
    private Long refId;
    private Boolean isRead;

    // @JsonFormat đảm bảo Flutter nhận đúng dạng "2026-03-20T07:30:00"
    // thay vì array [2026, 3, 20, 7, 30, 0] mà Jackson mặc định serialize
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime readAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    public static NotificationResponse from(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId())
                .title(n.getTitle())
                .message(n.getMessage())
                .refType(n.getRefType())
                .refId(n.getRefId())
                .isRead(n.getIsRead())
                .readAt(n.getReadAt())
                .createdAt(n.getCreatedAt())
                .build();
    }
}