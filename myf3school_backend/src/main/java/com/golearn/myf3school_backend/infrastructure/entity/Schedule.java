package com.golearn.myf3school_backend.infrastructure.entity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;


@Entity @Table(name = "schedules")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Schedule {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_section_id", nullable = false)
    @JsonIgnoreProperties({"schedules", "hibernateLazyInitializer", "handler"})
    private CourseSection courseSection;

    @Column(name = "day_of_week", nullable = false)
    private Integer dayOfWeek;

    @Column(name = "period_start", nullable = false)
    private Integer periodStart;

    @Column(name = "period_end", nullable = false)
    private Integer periodEnd;

    @Column(name = "time_start")
    private LocalTime timeStart; // VD: 07:30

    @Column(name = "time_end")
    private LocalTime timeEnd;   // VD: 09:50

    @Column(name = "room", length = 30)
    private String room;

    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    @Column(name = "created_at", updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
