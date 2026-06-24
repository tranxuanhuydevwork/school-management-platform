package com.golearn.myf3school_backend.infrastructure.entity;
import com.golearn.myf3school_backend.contract.enums.GradeType;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity @Table(name = "grade_components")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class GradeComponent {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "course_section_id", nullable = false) private CourseSection courseSection;
    @Column(nullable = false, length = 100) private String name;
    @Enumerated(EnumType.STRING) @Column(name = "grade_type", nullable = false) private GradeType gradeType;
    @Column(nullable = false, precision = 5, scale = 2) private BigDecimal weight;
    @Column(name = "max_score", precision = 5, scale = 2) @Builder.Default private BigDecimal maxScore = BigDecimal.TEN;
    @Column(name = "grading_date") private LocalDate gradingDate;
    @Column(name = "order_index") @Builder.Default private Integer orderIndex = 0;
    @Column(name = "created_at", updatable = false) @Builder.Default private LocalDateTime createdAt = LocalDateTime.now();
}
