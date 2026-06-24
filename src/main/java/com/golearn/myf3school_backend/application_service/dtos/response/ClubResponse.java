package com.golearn.myf3school_backend.application_service.dtos.response;

import com.golearn.myf3school_backend.contract.enums.ClubActivityStatus;
import com.golearn.myf3school_backend.contract.enums.ClubMemberRole;
import com.golearn.myf3school_backend.contract.enums.ClubMemberStatus;
import lombok.Builder;
import lombok.Data;

import java.util.List;

public class ClubResponse {

    // ══════════════════════════════════════════════════════════════════════════
    // CLB summary — trả về danh sách / chi tiết
    // ══════════════════════════════════════════════════════════════════════════
    @Data @Builder
    public static class ClubSummary {
        private Long    id;
        private String  code;
        private String  name;
        private String  description;
        private String  logoUrl;
        private String  meetingLocation;
        private Integer maxMembers;
        private String  foundedDate;
        private String  status;

        // Thông tin cố vấn & chủ tịch
        private String advisorName;
        private String presidentName;

        // Thống kê
        private int  memberCount;
        private int  upcomingEvents;

        // Trạng thái của học sinh đang query (null = chưa tham gia)
        private ClubMemberStatus myStatus;
        private ClubMemberRole   myRole;

        // Còn slot tuyển thành viên không
        private boolean canJoin;
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Thành viên CLB
    // ══════════════════════════════════════════════════════════════════════════
    @Data @Builder
    public static class MemberResponse {
        private Long             id;           // ClubMember.id
        private Long             studentId;    // StudentProfile.id
        private String           studentName;
        private String           studentCode;
        private String           avatarUrl;
        private ClubMemberRole   role;
        private ClubMemberStatus status;
        private String           joinDate;
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Sự kiện CLB
    // ══════════════════════════════════════════════════════════════════════════
    @Data @Builder
    public static class ActivityResponse {
        private Long                id;
        private Long                clubId;
        private String              clubName;
        private String              clubCode;
        private String              title;
        private String              description;
        private String              startTime;
        private String              endTime;
        private String              location;
        private int                 conductPoints;
        private ClubActivityStatus  status;
    }
}