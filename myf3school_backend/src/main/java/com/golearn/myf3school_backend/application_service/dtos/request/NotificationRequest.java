package com.golearn.myf3school_backend.application_service.dtos.request;

import lombok.Data;

@Data
public class NotificationRequest {
    private Long userId;       // bắt buộc
    private String title;      // bắt buộc
    private String message;    // bắt buộc
    private String refType;    // tuỳ chọn: SCHEDULE, SCORE, ATTENDANCE, ENROLLMENT
    private Long refId;        // tuỳ chọn: id của bản ghi liên quan
}