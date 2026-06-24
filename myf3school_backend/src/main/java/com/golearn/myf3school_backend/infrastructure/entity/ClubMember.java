package com.golearn.myf3school_backend.infrastructure.entity;

import com.golearn.myf3school_backend.contract.enums.ClubMemberRole;
import com.golearn.myf3school_backend.contract.enums.ClubMemberStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Tracks each student's membership in a club, their role within the club,
 * and their status (pending approval, active, left, expelled).
 */
@Entity
@Table(
        name = "club_members",
        uniqueConstraints = @UniqueConstraint(columnNames = {"club_id", "student_id"})
)
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class ClubMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_id", nullable = false)
    private Club club;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private StudentProfile student;

    @Enumerated(EnumType.STRING)
    @Column(name = "club_role", nullable = false, length = 30)
    @Builder.Default
    private ClubMemberRole clubRole = ClubMemberRole.MEMBER;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ClubMemberStatus status = ClubMemberStatus.PENDING;

    @Column(name = "join_date")
    private LocalDate joinDate;

    @Column(name = "leave_date")
    private LocalDate leaveDate;

    @Column(name = "leave_reason", columnDefinition = "TEXT")
    private String leaveReason;

    @Column(name = "created_at", updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}