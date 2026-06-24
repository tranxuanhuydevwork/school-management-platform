package com.golearn.myf3school_backend.infrastructure.repository;

import com.golearn.myf3school_backend.infrastructure.entity.GradeEditPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

/**
 * BUG FIX #6: File này bị thiếu trong project.
 *
 * GradeCorrectionService gọi policyRepo.findByStudentProfileIdAndGradeDeadline()
 * nhưng repository chưa được tạo → compile error.
 */
@Repository
public interface GradeEditPolicyRepository extends JpaRepository<GradeEditPolicy, Long> {

    /**
     * Lấy policy của 1 học sinh trong 1 đợt chốt điểm cụ thể.
     * Dùng để kiểm tra editCount và tăng bộ đếm.
     */
    Optional<GradeEditPolicy> findByStudentProfileIdAndGradeDeadline(
            Long studentProfileId, LocalDate gradeDeadline);
}