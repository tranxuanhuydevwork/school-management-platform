package com.golearn.myf3school_backend.application_service.dtos.response;


import com.golearn.myf3school_backend.contract.enums.RequestStatus;
import com.golearn.myf3school_backend.contract.enums.RequestType;
import com.golearn.myf3school_backend.infrastructure.entity.Application;
import lombok.Data;

import java.time.LocalDateTime;

// Object JSON trả về cho Flutter
// Flutter dùng RequestForm.fromJson() để parse
@Data
public class ApplicationResponse {

    private Long          id;
    private Long          studentId;
    private RequestType requestType;   // Flutter đọc field "requestType"
    private String        title;
    private String        description;
    private RequestStatus status;
    private String        rejectedReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ApplicationResponse from(Application app) {
        ApplicationResponse dto = new ApplicationResponse();
        dto.setId(app.getId());
        dto.setStudentId(app.getStudentId());
        dto.setRequestType(app.getRequestType());
        dto.setTitle(app.getTitle());
        dto.setDescription(app.getDescription());
        dto.setStatus(app.getStatus());
        dto.setRejectedReason(app.getRejectedReason());
        dto.setCreatedAt(app.getCreatedAt());
        dto.setUpdatedAt(app.getUpdatedAt());
        return dto;
    }
}