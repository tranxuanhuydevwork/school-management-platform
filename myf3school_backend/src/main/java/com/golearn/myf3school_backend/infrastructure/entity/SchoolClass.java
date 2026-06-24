package com.golearn.myf3school_backend.infrastructure.entity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "school_classes")@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class SchoolClass {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, length = 20) private String code;
    @Column(nullable = false, length = 50) private String name;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "grade_id", nullable = false) private Grade grade;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "academic_year_id", nullable = false) private AcademicYear academicYear;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "homeroom_teacher_id") private User homeroomTeacher;
    @Column(name = "max_students") @Builder.Default private Integer maxStudents = 40;
    @Column(name = "created_at", updatable = false) @Builder.Default private LocalDateTime createdAt = LocalDateTime.now();
}
