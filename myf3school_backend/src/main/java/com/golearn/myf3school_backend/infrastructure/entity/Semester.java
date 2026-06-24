package com.golearn.myf3school_backend.infrastructure.entity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity @Table(name = "semesters")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})

public class Semester {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "academic_year_id", nullable = false) private AcademicYear academicYear;
    @Column(nullable = false, length = 50) private String name;
    @Column(name = "semester_number", nullable = false) private Integer semesterNumber;
    @Column(name = "start_date", nullable = false) private LocalDate startDate;
    @Column(name = "end_date", nullable = false) private LocalDate endDate;
    @Column(name = "is_current") @Builder.Default private Boolean isCurrent = false;
    @Column(name = "created_at", updatable = false) @Builder.Default private LocalDateTime createdAt = LocalDateTime.now();
}
