package com.golearn.myf3school_backend.application_service.dtos.request;

import com.golearn.myf3school_backend.contract.enums.ClubActivityStatus;
import com.golearn.myf3school_backend.contract.enums.ClubMemberRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

public class ClubRequest {

    // ── Tạo / cập nhật CLB ────────────────────────────────────────────────────
    @Data
    public static class CreateClub {
        @NotBlank private String  code;              // "CLUB-IT01"
        @NotBlank private String  name;
        private String            description;
        private String            logoUrl;
        private String            meetingLocation;
        private Integer           maxMembers;
        private Long              advisorId;
        private Long              presidentStudentId; // StudentProfile.id
        private String            foundedDate;        // "yyyy-MM-dd"
    }

    // ── Đăng ký tham gia CLB ─────────────────────────────────────────────────
    @Data
    public static class JoinClub {
        @NotNull private Long studentId;
    }

    // ── Rời / khai trừ khỏi CLB ──────────────────────────────────────────────
    @Data
    public static class LeaveClub {
        @NotNull private Long   studentId;
        private String          leaveReason;
    }

    // ── Đổi role thành viên ───────────────────────────────────────────────────
    @Data
    public static class ChangeRole {
        @NotNull private ClubMemberRole role;
    }

    // ── Tạo sự kiện ──────────────────────────────────────────────────────────
    @Data
    public static class CreateActivity {
        @NotBlank private String title;
        private String           description;
        @NotNull  private String startTime;     // "yyyy-MM-ddTHH:mm:ss"
        private String           endTime;
        private String           location;
        private int              conductPoints;
    }

    // ── Cập nhật trạng thái sự kiện ──────────────────────────────────────────
    @Data
    public static class UpdateActivityStatus {
        @NotNull private ClubActivityStatus status;
    }

    // ── Điểm danh sự kiện CLB ────────────────────────────────────────────────
    @Data
    public static class RecordAttendance {
        @NotNull private Long   studentId;
        @NotNull private String status;  // PRESENT / ABSENT / LATE
        private String          note;
    }
}