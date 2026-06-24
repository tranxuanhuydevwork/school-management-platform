package com.golearn.myf3school_backend.infrastructure.entity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity @Table(name = "attendance_sessions")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})

public class AttendanceSession {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "course_section_id", nullable = false) private CourseSection courseSection;
    @Column(name = "session_date", nullable = false) private LocalDate sessionDate;
    @Column(name = "period_start", nullable = false) private Integer periodStart;
    @Column(name = "period_end", nullable = false) private Integer periodEnd;
    @Column(name = "lesson_topic", length = 500) private String lessonTopic;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "taken_by") private User takenBy;
    @Column(name = "taken_at") private LocalDateTime takenAt;
    @Column(name = "created_at", updatable = false) @Builder.Default private LocalDateTime createdAt = LocalDateTime.now();
}
