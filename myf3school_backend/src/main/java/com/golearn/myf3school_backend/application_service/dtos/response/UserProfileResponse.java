package com.golearn.myf3school_backend.application_service.dtos.response;

import com.golearn.myf3school_backend.contract.enums.RoleType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/**
 * DTO trả về cho màn hình Profile.
 *
 * Các field chung (user):       userId, fullName, email, phone, avatarUrl, address, dateOfBirth, roles
 * Nếu STUDENT:                  studentCode, className, semester, gpa, academicRank, enrollmentDate,
 *                               emergencyContactName, emergencyContactPhone
 * Nếu PARENT:                   children (danh sách con, mỗi con gồm tên + lớp + mã HS)
 */
@Data
@Builder
public class UserProfileResponse {

    // ── Thông tin chung ──────────────────────────────────────────────────────
    private Long          userId;
    private String        fullName;
    private String        email;
    private String        phone;
    private String        avatarUrl;
    private String        address;
    private LocalDate     dateOfBirth;
    private Set<RoleType> roles;

    // ── Dành cho STUDENT ─────────────────────────────────────────────────────
    private String     studentCode;
    private String     className;       // tên lớp, vd "SE1715"
    private String     semester;        // vd "Học kỳ 7"  (tính từ ngày nhập học)
    private BigDecimal gpa;
    private String     academicRank;    // Giỏi / Khá / Trung bình …
    private LocalDate  enrollmentDate;
    private String     emergencyContactName;
    private String     emergencyContactPhone;

    // ── Dành cho PARENT ──────────────────────────────────────────────────────
    /** Danh sách con của phụ huynh */
    private List<ChildInfo> children;

    @Data
    @Builder
    public static class ChildInfo {
        private Long   studentProfileId;
        private String fullName;
        private String studentCode;
        private String className;
        private String relationship;   // FATHER / MOTHER / GUARDIAN
        private BigDecimal gpa;
    }
}