package com.golearn.myf3school_backend.application_service.dtos.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.golearn.myf3school_backend.contract.enums.CorrectionStatus;
import com.golearn.myf3school_backend.infrastructure.entity.GradeCorrectionRequest;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class GradeCorrectionResponse {

    private Long   id;
    private Long   teacherId;
    private String teacherName;

    private Long   studentProfileId;
    private String studentName;

    private Long   gradeComponentId;
    private String componentName;   // "Thi cuối kỳ", "Kiểm tra giữa kỳ"...
    private String subjectCode;     // "MATH101"
    private String subjectName;     // "Toán cao cấp"
    private String className;       // "KTPM2023A"

    private BigDecimal oldScore;
    private BigDecimal proposedScore;
    private String     reason;
    private CorrectionStatus status;
    private Long       reviewedBy;
    private String     reviewNote;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime reviewedAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    /**
     * Map cơ bản — không có thông tin môn học hay học sinh.
     * Chỉ dùng nội bộ làm base, gọi fromFull() cho hầu hết trường hợp.
     */
    public static GradeCorrectionResponse from(GradeCorrectionRequest r) {
        return GradeCorrectionResponse.builder()
                .id(r.getId())
                .teacherId(r.getTeacher().getId())
                .teacherName(r.getTeacher().getFullName())
                .studentProfileId(r.getStudentProfileId())
                .gradeComponentId(r.getGradeComponentId())
                .oldScore(r.getOldScore())
                .proposedScore(r.getProposedScore())
                .reason(r.getReason())
                .status(r.getStatus())
                .reviewedBy(r.getReviewedBy())
                .reviewNote(r.getReviewNote())
                .reviewedAt(r.getReviewedAt())
                .createdAt(r.getCreatedAt())
                .build();
    }

    /**
     * Map đầy đủ — dùng cho mọi endpoint trả về danh sách / chi tiết đơn.
     *
     * @param r             entity đơn phúc khảo
     * @param studentName   tên học sinh  (lookup từ StudentProfile)
     * @param componentName tên đầu điểm  (lookup từ GradeComponent)
     * @param subjectCode   mã môn         (GradeComponent → CourseSection → Subject)
     * @param subjectName   tên môn        (GradeComponent → CourseSection → Subject)
     * @param className     mã lớp         (GradeComponent → CourseSection → SchoolClass)
     */
    public static GradeCorrectionResponse fromFull(
            GradeCorrectionRequest r,
            String studentName,
            String componentName,
            String subjectCode,
            String subjectName,
            String className) {

        GradeCorrectionResponse resp = from(r);
        resp.setStudentName(studentName);
        resp.setComponentName(componentName);
        resp.setSubjectCode(subjectCode);
        resp.setSubjectName(subjectName);
        resp.setClassName(className);
        return resp;
    }

    /**
     * Giữ lại để không break code cũ đang dùng fromWithStudentName().
     * @deprecated Dùng fromFull() thay thế.
     */
    @Deprecated
    public static GradeCorrectionResponse fromWithStudentName(
            GradeCorrectionRequest r, String studentName) {
        GradeCorrectionResponse resp = from(r);
        resp.setStudentName(studentName);
        return resp;
    }
}