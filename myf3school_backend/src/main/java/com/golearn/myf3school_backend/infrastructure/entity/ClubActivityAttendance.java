package com.golearn.myf3school_backend.infrastructure.entity;

import com.golearn.myf3school_backend.contract.enums.AttendanceStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Tracks which student attended a specific club activity.
 * If present, the student receives the activity's conductPoints.
 */
@Entity
@Table(
        name = "club_activity_attendance",
        uniqueConstraints = @UniqueConstraint(columnNames = {"activity_id", "student_id"})
)
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class ClubActivityAttendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "activity_id", nullable = false)
    private ClubActivity activity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private StudentProfile student;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private AttendanceStatus status = AttendanceStatus.PRESENT;

    @Column(columnDefinition = "TEXT")
    private String note;

    // Person who recorded this attendance (advisor or club leader)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recorded_by")
    private User recordedBy;

    @Column(name = "created_at", updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}