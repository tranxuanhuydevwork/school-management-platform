package com.golearn.myf3school_backend.infrastructure.entity;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity @Table(name = "student_profiles")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class StudentProfile {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @OneToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id", nullable = false, unique = true) private User user;
    @Column(name = "student_code", nullable = false, unique = true, length = 20) private String studentCode;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "class_id") private SchoolClass schoolClass;
    @Column(name = "enrollment_date") private LocalDate enrollmentDate;
    @Column(name = "gpa", precision = 4, scale = 2) private BigDecimal gpa;

    // Đổi tên field + column cho nhất quán với DTO
    @Column(name = "students_rank", length = 50)
    private String academicRank;

    // Xóa conduct (đã chuyển sang ConductScore per-semester)

    // Thêm 2 field mới
    @Column(name = "emergency_contact_name", length = 100)
    private String emergencyContactName;

    @Column(name = "emergency_contact_phone", length = 20)
    private String emergencyContactPhone;

    @Column(name = "created_at", updatable = false) @Builder.Default private LocalDateTime createdAt = LocalDateTime.now();
    @Column(name = "updated_at") @Builder.Default private LocalDateTime updatedAt = LocalDateTime.now();
    @PreUpdate public void preUpdate() { this.updatedAt = LocalDateTime.now(); }
}