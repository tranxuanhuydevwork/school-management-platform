package com.golearn.myf3school_backend.application_service.dtos.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * Response cho Flutter: GET /api/attendance/students/{studentId}?semesterId=X
 *
 * Flutter mapping:
 *   AttendanceRecord.fromJson ← subjects[]
 *   SessionRecord.fromJson    ← subjects[].sessions[]
 */
@Data
@Builder
public class AttendanceStudentResponse {

    private Long   studentId;
    private String studentCode;
    private String studentName;
    private Long   semesterId;
    private String semesterName;   // optional, fill nếu cần

    // Tổng hợp toàn kỳ
    private int     totalSessions;
    private int     presentCount;
    private int     absentCount;
    private int     lateCount;
    private int     excusedCount;
    private double  attendancePercent;  // 0–100, làm tròn 1 chữ số
    private boolean atRisk;             // true nếu < 80 %

    // Chi tiết từng môn
    private List<SubjectAttendance> subjects;

    // ── Chi tiết từng môn ─────────────────────────────────────────────────
    @Data
    @Builder
    public static class SubjectAttendance {
        private Long   sectionId;       // → AttendanceRecord.subjectId (flutter)
        private String subjectCode;     // → AttendanceRecord.subjectCode
        private String subjectName;     // → AttendanceRecord.subjectName
        private int    totalSessions;   // → AttendanceRecord.total
        private int    presentCount;    // → AttendanceRecord.present
        private int    absentCount;     // → AttendanceRecord.absent
        private int    lateCount;       // → AttendanceRecord.late
        private int    excusedCount;    // → AttendanceRecord.excused
        private double attendancePercent;
        private boolean atRisk;
        private List<SessionRecord> sessions;
    }

    // ── Chi tiết từng buổi học ────────────────────────────────────────────
    @Data
    @Builder
    public static class SessionRecord {
        private Long   sessionId;

        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate sessionDate;

        private int    periodStart;
        private int    periodEnd;
        private String lessonTopic;  // chủ đề buổi học, có thể null
        private String status;       // PRESENT | ABSENT | LATE | EXCUSED | NOT_YET
        private String note;
    }
}