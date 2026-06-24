package com.golearn.myf3school_backend.infrastructure.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Cấu hình ngày chốt điểm do Admin thiết lập.
 *
 * Mỗi record đại diện cho 1 đợt (học kỳ / đợt thi).
 * GradeService dùng deadlineDate để kiểm tra teacher còn tự sửa không.
 *
 * Ví dụ:
 *   name         = "HK1 2025-2026"
 *   deadlineDate = 2026-01-15
 *   isActive     = true   ← đợt đang hiệu lực (chỉ 1 record active tại 1 thời điểm)
 */
@Entity
@Table(name = "grade_deadline_configs")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class GradeDeadlineConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Tên đợt, ví dụ "HK2 2025-2026" */
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    /** Ngày chốt điểm — teacher chỉ được tự sửa khi now() <= deadlineDate */
    @Column(name = "deadline_date", nullable = false)
    private LocalDate deadlineDate;

    /**
     * Đợt đang hiệu lực.
     * Chỉ 1 record được active = true tại 1 thời điểm
     * (kiểm tra ở service, không dùng DB constraint).
     */
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}