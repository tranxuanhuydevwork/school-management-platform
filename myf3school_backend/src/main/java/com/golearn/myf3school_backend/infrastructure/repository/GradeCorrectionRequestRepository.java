package com.golearn.myf3school_backend.infrastructure.repository;

import com.golearn.myf3school_backend.contract.enums.CorrectionStatus;
import com.golearn.myf3school_backend.infrastructure.entity.GradeCorrectionRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GradeCorrectionRequestRepository
        extends JpaRepository<GradeCorrectionRequest, Long> {

    // ── Teacher queries ───────────────────────────────────────────────────

    /** Danh sách đơn của teacher, mới nhất trước */
    List<GradeCorrectionRequest> findByTeacherIdOrderByCreatedAtDesc(Long teacherId);

    /** Đơn của teacher theo trạng thái */
    List<GradeCorrectionRequest> findByTeacherIdAndStatusOrderByCreatedAtDesc(
            Long teacherId, CorrectionStatus status);

    // ── Khảo thí queries ──────────────────────────────────────────────────

    /** Tất cả đơn, phân trang — trang quản lý khảo thí */
    @Query("""
        SELECT r FROM GradeCorrectionRequest r
        JOIN FETCH r.teacher
        ORDER BY r.createdAt DESC
    """)
    Page<GradeCorrectionRequest> findAllWithTeacher(Pageable pageable);

    /** Filter theo status, phân trang */
    @Query("""
        SELECT r FROM GradeCorrectionRequest r
        JOIN FETCH r.teacher
        WHERE r.status = :status
        ORDER BY r.createdAt DESC
    """)
    Page<GradeCorrectionRequest> findByStatusWithTeacher(
            @Param("status") CorrectionStatus status, Pageable pageable);

    /** Đếm đơn PENDING — dùng cho badge sidebar khảo thí */
    long countByStatus(CorrectionStatus status);

    // ── Kiểm tra trùng đơn ────────────────────────────────────────────────

    /**
     * Kiểm tra đã có đơn PENDING cho component + học sinh này chưa.
     * Tránh teacher gửi nhiều đơn cùng lúc cho 1 đầu điểm.
     */
    boolean existsByTeacherIdAndStudentProfileIdAndGradeComponentIdAndStatus(
            Long teacherId, Long studentProfileId,
            Long gradeComponentId, CorrectionStatus status);
}