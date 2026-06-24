package com.golearn.myf3school_backend.infrastructure.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Lưu số lần giáo viên đã tự sửa điểm cho 1 học sinh
 * trong 1 đợt chốt điểm (gradeDeadline).
 *
 * Ràng buộc:
 *   - teacher chỉ được tự sửa tối đa MAX_SELF_EDITS lần
 *   - chỉ được sửa khi LocalDate.now() <= gradeDeadline
 *   - sau khi hết quota HOẶC hết hạn → phải gửi GradeCorrectionRequest
 */
@Entity
@Table(
    name = "grade_edit_policies",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_policy_student_deadline",
        columnNames = {"student_profile_id", "grade_deadline"}
    )
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class GradeEditPolicy {

    /** Số lần tự sửa tối đa mỗi học sinh mỗi đợt */
    public static final int MAX_SELF_EDITS = 2;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Học sinh được sửa điểm (student_profiles.id).
     * Không cần @ManyToOne để tránh load nặng — chỉ lưu FK id.
     */
    @Column(name = "student_profile_id", nullable = false)
    private Long studentProfileId;

    /**
     * Ngày chốt điểm của đợt này (do admin cấu hình).
     * Teacher chỉ được tự sửa khi now() <= gradeDeadline.
     */
    @Column(name = "grade_deadline", nullable = false)
    private LocalDate gradeDeadline;

    /**
     * Số lần đã tự sửa trong đợt này (tăng mỗi khi teacher PUT /grades/...).
     * Khi editCount >= MAX_SELF_EDITS → bị chặn, phải gửi đơn.
     */
    @Column(name = "edit_count", nullable = false)
    @Builder.Default
    private int editCount = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ── Domain helpers ─────────────────────────────────────────────────────

    /** Còn quota tự sửa không? */
    public boolean hasQuotaRemaining() {
        return editCount < MAX_SELF_EDITS;
    }

    /** Còn trong thời hạn cho phép không? */
    public boolean isWithinDeadline() {
        return !LocalDate.now().isAfter(gradeDeadline);
    }

    /** Có thể tự sửa không (cả quota + deadline)? */
    public boolean canSelfEdit() {
        return hasQuotaRemaining() && isWithinDeadline();
    }

    /** Tăng bộ đếm, ném exception nếu vượt giới hạn */
    public void incrementEditCount() {
        if (!canSelfEdit()) {
            throw new IllegalStateException(
                "Không thể tự sửa điểm: " +
                (!isWithinDeadline()
                    ? "đã quá hạn chốt điểm (" + gradeDeadline + ")"
                    : "đã đạt giới hạn " + MAX_SELF_EDITS + " lần sửa")
            );
        }
        this.editCount++;
    }
}