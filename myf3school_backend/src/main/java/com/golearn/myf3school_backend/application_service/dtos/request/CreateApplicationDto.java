package com.golearn.myf3school_backend.application_service.dtos.request;

import com.golearn.myf3school_backend.contract.enums.RequestStatus;
import com.golearn.myf3school_backend.contract.enums.RequestType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

// ── Tạo đơn mới ──────────────────────────────────────────────
@Data
public class CreateApplicationDto {

    @NotNull(message = "studentId là bắt buộc")
    private Long studentId;

    @NotNull(message = "requestType là bắt buộc")
    private RequestType requestType;

    @NotBlank(message = "title là bắt buộc")
    private String title;

    @NotBlank(message = "description là bắt buộc")
    private String description;
}