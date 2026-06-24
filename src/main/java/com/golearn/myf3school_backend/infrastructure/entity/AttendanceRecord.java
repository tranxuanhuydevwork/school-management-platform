package com.golearn.myf3school_backend.infrastructure.entity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.golearn.myf3school_backend.contract.enums.AttendanceStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity @Table(name = "attendance_records")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})

public class AttendanceRecord {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "session_id", nullable = false) private AttendanceSession session;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "student_id", nullable = false) private StudentProfile student;
    @Enumerated(EnumType.STRING) @Column(nullable = false) @Builder.Default private AttendanceStatus status = AttendanceStatus.PRESENT;
    @Column(columnDefinition = "TEXT") private String note;
    @Column(name = "created_at", updatable = false) @Builder.Default private LocalDateTime createdAt = LocalDateTime.now();
}
