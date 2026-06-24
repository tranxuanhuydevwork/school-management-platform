package com.golearn.myf3school_backend.application_service.dtos.request;

import com.golearn.myf3school_backend.contract.enums.RequestStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReviewApplicationDto {

    @NotNull(message = "status là bắt buộc")
    private RequestStatus status;     // APPROVED | REJECTED

    private String rejectedReason;    // bắt buộc khi status = REJECTED

    private Long reviewedBy;          // users.id của người duyệt
}