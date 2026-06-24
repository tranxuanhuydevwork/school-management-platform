package com.golearn.myf3school_backend.contract.enums;

/**
 * Trạng thái đơn yêu cầu sửa điểm (GradeCorrectionRequest).
 *
 * PENDING  — Chờ khảo thí xem xét
 * APPROVED — Khảo thí duyệt, điểm đã được cập nhật
 * REJECTED — Khảo thí từ chối, điểm giữ nguyên
 */
public enum CorrectionStatus {
    PENDING,
    APPROVED,
    REJECTED
}