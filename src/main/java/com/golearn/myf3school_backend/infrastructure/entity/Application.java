package com.golearn.myf3school_backend.infrastructure.entity;

import com.golearn.myf3school_backend.contract.enums.RequestStatus;
import com.golearn.myf3school_backend.contract.enums.RequestType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "requests")           // tên bảng trong DB từ SQL migration
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "request_type", nullable = false, length = 50)
    private RequestType requestType;

    // Tiêu đề đơn
    @Column(nullable = false)
    private String title;

    // Nội dung chi tiết
    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    // Trạng thái — mặc định PENDING khi tạo mới
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private RequestStatus status = RequestStatus.PENDING;

    // Lý do từ chối (chỉ có khi status = REJECTED)
    @Column(name = "rejected_reason", columnDefinition = "TEXT")
    private String rejectedReason;

    // Người duyệt — FK → users.id (giáo viên / admin)
    @Column(name = "reviewed_by")
    private Long reviewedBy;

    // Thời điểm duyệt
    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}