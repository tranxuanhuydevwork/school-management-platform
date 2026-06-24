package com.golearn.myf3school_backend.infrastructure.entity;

import com.golearn.myf3school_backend.contract.enums.ClubStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "clubs")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Club {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String code;               // "CLUB-IT01"

    @Column(nullable = false, length = 100)
    private String name;               // "Coding Club"

    @Column(columnDefinition = "TEXT")
    private String description;

    // Giáo viên cố vấn
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "advisor_id")
    private User advisor;

    // Học sinh làm chủ tịch CLB
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "president_id")
    private StudentProfile president;

    @Column(name = "founded_date")
    private LocalDate foundedDate;

    @Column(name = "max_members")
    @Builder.Default
    private Integer maxMembers = 50;

    @Column(name = "meeting_location", length = 100)
    private String meetingLocation;    // "Phòng 201 - Tòa B"

    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ClubStatus status = ClubStatus.ACTIVE;

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