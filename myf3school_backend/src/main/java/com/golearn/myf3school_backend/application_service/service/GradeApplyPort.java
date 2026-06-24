package com.golearn.myf3school_backend.application_service.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Port (interface) để GradeCorrectionService gọi vào GradeService
 * mà không tạo circular dependency.
 *
 * GradeService implements interface này:
 *
 *   @Service
 *   public class GradeService implements GradeApplyPort { ... }
 *
 * Khi khảo thí APPROVED đơn sửa điểm, GradeCorrectionService gọi:
 *   gradeApplyPort.applyScore(studentProfileId, gradeComponentId,
 *                             proposedScore, reviewedBy);
 * → GradeService xử lý UPDATE grade_components và audit log.
 */

public interface GradeApplyPort {

    /**
     * Cập nhật điểm thực tế (do khảo thí duyệt).
     *
     * @param studentProfileId  student_profiles.id
     * @param gradeComponentId  đầu điểm cần cập nhật
     * @param newScore          điểm mới được duyệt
     * @param approvedBy        users.id của nhân viên khảo thí
     */
    void applyScore(Long studentProfileId,
                    Long gradeComponentId,
                    BigDecimal newScore,
                    Long approvedBy);
}