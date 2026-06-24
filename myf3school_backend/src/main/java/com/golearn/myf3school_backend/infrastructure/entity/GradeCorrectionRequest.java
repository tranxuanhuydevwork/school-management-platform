package com.golearn.myf3school_backend.infrastructure.entity;

import com.golearn.myf3school_backend.contract.enums.CorrectionStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Đơn yêu cầu sửa điểm (gửi từ Teacher → Khảo thí).
 *
 * Vòng đời:
 *   PENDING → APPROVED (khảo thí duyệt, điểm được cập nhật)
 *           → REJECTED (khảo thí từ chối, giữ điểm cũ)
 *
 * Khi APPROVED:
 *   - GradeService.applyCorrection() cập nhật grade_components
 *   - NotificationService gửi email + SMS tới học sinh VÀ teacher
 */
@Entity
@Table(name = "grade_correction_requests",
       indexes = {
           @Index(name = "idx_gcr_teacher",  columnList = "teacher_id"),
           @Index(name = "idx_gcr_student",  columnList = "student_profile_id"),
           @Index(name = "idx_gcr_status",   columnList = "status"),
           @Index(name = "idx_gcr_component",columnList = "grade_component_id")
       })
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class GradeCorrectionRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ── Ai gửi đơn ────────────────────────────────────────────────────────

    /** Giáo viên gửi đơn (users.id — role TEACHER) */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "teacher_id", nullable = false)
    private User teacher;

    // ── Điểm nào cần sửa ──────────────────────────────────────────────────

    /** Học sinh bị ảnh hưởng (student_profiles.id) */
    @Column(name = "student_profile_id", nullable = false)
    private Long studentProfileId;

    /**
     * Đầu điểm cụ thể cần sửa (grade_components.id hoặc
     * bảng tương đương trong project của bạn).
     */
    @Column(name = "grade_component_id", nullable = false)
    private Long gradeComponentId;

    // ── Nội dung đề xuất ──────────────────────────────────────────────────

    /** Điểm hiện tại tại thời điểm gửi đơn (snapshot để khảo thí so sánh) */
    @Column(name = "old_score", nullable = false, precision = 5, scale = 2)
    private BigDecimal oldScore;

    /** Điểm mới giáo viên đề xuất */
    @Column(name = "proposed_score", nullable = false, precision = 5, scale = 2)
    private BigDecimal proposedScore;

    /** Lý do sửa điểm (bắt buộc) */
    @Column(name = "reason", nullable = false, length = 1000)
    private String reason;

    // ── Phản hồi từ khảo thí ─────────────────────────────────────────────

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private CorrectionStatus status = CorrectionStatus.PENDING;

    /** Người duyệt (users.id — role EXAM_DEPT) */
    @Column(name = "reviewed_by")
    private Long reviewedBy;

    /** Ghi chú / lý do từ chối của khảo thí */
    @Column(name = "review_note", length = 500)
    private String reviewNote;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    // ── Audit ─────────────────────────────────────────────────────────────

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}