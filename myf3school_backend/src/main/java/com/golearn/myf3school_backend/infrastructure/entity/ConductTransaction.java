package com.golearn.myf3school_backend.infrastructure.entity;

import com.golearn.myf3school_backend.contract.enums.ConductTransactionType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Audit trail for each individual conduct point change.
 * Allows full transparency: students and parents can see exactly
 * why their score changed (e.g., "+5 for winning inter-school competition",
 * "-10 for three unexcused absences").
 */
@Entity
@Table(name = "conduct_transactions")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class ConductTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private StudentProfile student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "semester_id", nullable = false)
    private Semester semester;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 30)
    private ConductTransactionType transactionType;

    // Positive = bonus, negative = deduction
    @Column(nullable = false)
    private Integer points;

    @Column(nullable = false, length = 300)
    private String reason;

    // Optional: link back to the source entity (club_activity_id, violation_id, etc.)
    @Column(name = "ref_type", length = 50)
    private String refType;

    @Column(name = "ref_id")
    private Long refId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @Column(name = "created_at", updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}