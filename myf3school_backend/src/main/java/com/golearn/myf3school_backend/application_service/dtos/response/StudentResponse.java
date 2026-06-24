package com.golearn.myf3school_backend.application_service.dtos.response;

import com.golearn.myf3school_backend.infrastructure.entity.StudentProfile;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * BREAKING CHANGE:
 *   - `conduct: String`  removed — conduct is now per-semester via ConductScore.
 *     Use GET /api/conduct/students/{id}?semesterId=X instead.
 *   - `rank`  renamed to  `academicRank`  (matches entity field).
 *   - Added `emergencyContactName`, `emergencyContactPhone`.
 */
@Data
public class StudentResponse {
    private Long       id;
    private String     studentCode;
    private String     fullName;
    private String     email;
    private String     phone;
    private String     avatarUrl;
    private LocalDate  dateOfBirth;
    private String     gender;
    private String     className;
    private Long       classId;
    private String     gradeName;
    private String     academicYear;
    private LocalDate  enrollmentDate;
    private BigDecimal gpa;
    private String     academicRank;
    private String     emergencyContactName;
    private String     emergencyContactPhone;

    public static StudentResponse from(StudentProfile p) {
        StudentResponse dto = new StudentResponse();
        dto.setId(p.getId());
        dto.setStudentCode(p.getStudentCode());
        dto.setFullName(p.getUser().getFullName());
        dto.setEmail(p.getUser().getEmail());
        dto.setPhone(p.getUser().getPhone());
        dto.setAvatarUrl(p.getUser().getAvatarUrl());
        dto.setDateOfBirth(p.getUser().getDateOfBirth());
        dto.setGender(p.getUser().getGender() != null
                ? p.getUser().getGender().name() : null);
        dto.setEnrollmentDate(p.getEnrollmentDate());
        dto.setGpa(p.getGpa());
        dto.setAcademicRank(p.getAcademicRank());
        dto.setEmergencyContactName(p.getEmergencyContactName());
        dto.setEmergencyContactPhone(p.getEmergencyContactPhone());
        if (p.getSchoolClass() != null) {
            dto.setClassId(p.getSchoolClass().getId());
            dto.setClassName(p.getSchoolClass().getName());
            if (p.getSchoolClass().getGrade() != null)
                dto.setGradeName(p.getSchoolClass().getGrade().getName());
            if (p.getSchoolClass().getAcademicYear() != null)
                dto.setAcademicYear(p.getSchoolClass().getAcademicYear().getName());
        }
        return dto;
    }
}