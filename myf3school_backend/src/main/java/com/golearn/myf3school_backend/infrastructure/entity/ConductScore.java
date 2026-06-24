package com.golearn.myf3school_backend.infrastructure.entity;

import com.golearn.myf3school_backend.contract.enums.ConductRating;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Stores a student's final conduct (rèn luyện) evaluation for a semester.
 * Points are accumulated from: class discipline, club activities,
 * voluntary service, violations, etc.
 *
 * Standard Vietnamese high-school ratings:
 *   EXCELLENT (Tốt)    >= 90
 *   GOOD (Khá)         >= 65
 *   AVERAGE (TB)       >= 35
 *   WEAK (Yếu)         < 35
 */
@Entity
@Table(
        name = "conduct_scores",
        uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "semester_id"})
)
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class ConductScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private StudentProfile student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "semester_id", nullable = false)
    private Semester semester;

    // Base score from class teacher evaluation (0-100)
    @Column(name = "class_score", nullable = false)
    @Builder.Default
    private Integer classScore = 0;

    // Extra points from club participation, achievements, etc.
    @Column(name = "club_bonus_points")
    @Builder.Default
    private Integer clubBonusPoints = 0;

    // Deductions for violations (late, absent, breaking rules, etc.)
    @Column(name = "deduction_points")
    @Builder.Default
    private Integer deductionPoints = 0;

    // Final = classScore + clubBonusPoints - deductionPoints (capped 0-100)
    @Column(name = "final_score", nullable = false)
    @Builder.Default
    private Integer finalScore = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ConductRating rating = ConductRating.AVERAGE;

    @Column(name = "teacher_comment", columnDefinition = "TEXT")
    private String teacherComment;

    // Homeroom teacher who finalised this record
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evaluated_by")
    private User evaluatedBy;

    @Column(name = "is_finalized")
    @Builder.Default
    private Boolean isFinalized = false;

    @Column(name = "finalized_at")
    private LocalDateTime finalizedAt;

    @Column(name = "created_at", updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}