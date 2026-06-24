package com.golearn.myf3school_backend.infrastructure.entity;

import com.golearn.myf3school_backend.contract.enums.ClubActivityStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * A specific event/activity organized by a club.
 * E.g.: "Weekly Meeting", "Inter-School Coding Contest", "Annual Exhibition".
 * Used to track attendance and award behavioural conduct points.
 */
@Entity
@Table(name = "club_activities")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class ClubActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_id", nullable = false)
    private Club club;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "location", length = 200)
    private String location;

    // Conduct points awarded just for attending this activity
    @Column(name = "conduct_points")
    @Builder.Default
    private Integer conductPoints = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ClubActivityStatus status = ClubActivityStatus.SCHEDULED;

    // Teacher/advisor who approved the activity
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private User approvedBy;

    @Column(name = "created_at", updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}