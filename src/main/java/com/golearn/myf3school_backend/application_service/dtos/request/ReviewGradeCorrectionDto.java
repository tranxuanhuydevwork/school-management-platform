package com.golearn.myf3school_backend.application_service.dtos.request;

import com.golearn.myf3school_backend.contract.enums.CorrectionStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
 @Builder
@Data
public class ReviewGradeCorrectionDto {

    /** APPROVED hoặc REJECTED */
    @NotNull(message = "status là bắt buộc")
    private CorrectionStatus status;

    /** users.id của nhân viên khảo thí duyệt đơn */
    @NotNull(message = "reviewedBy là bắt buộc")
    private Long reviewedBy;

    /** Ghi chú / lý do từ chối (bắt buộc khi REJECTED) */
    private String reviewNote;
}