package com.golearn.myf3school_backend.infrastructure.entity;

import com.golearn.myf3school_backend.contract.enums.GradeEditStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Yêu cầu sửa điểm do giáo viên gửi khi đã hết lượt (> 2 lần)
 * hoặc sau thời hạn khoá điểm của học kỳ.
 */
@Entity
@Table(name = "grade_edit_requests")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class GradeEditRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Đầu điểm cần sửa */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "component_id", nullable = false)
    private GradeComponent component;

    /** Học sinh cần sửa điểm */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private StudentProfile student;

    /** Giáo viên yêu cầu sửa */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requested_by_id", nullable = false)
    private User requestedBy;

    /** Điểm mới giáo viên muốn đặt */
    @Column(name = "new_score", nullable = false, precision = 5, scale = 2)
    private BigDecimal newScore;

    /** Điểm hiện tại tại thời điểm gửi yêu cầu */
    @Column(name = "old_score", precision = 5, scale = 2)
    private BigDecimal oldScore;

    /** Lý do sửa điểm */
    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private GradeEditStatus status = GradeEditStatus.PENDING;

    /** Người xét duyệt (khảo thí) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by_id")
    private User reviewedBy;

    /** Lý do từ chối (nếu REJECTED) */
    @Column(name = "rejected_reason", columnDefinition = "TEXT")
    private String rejectedReason;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}