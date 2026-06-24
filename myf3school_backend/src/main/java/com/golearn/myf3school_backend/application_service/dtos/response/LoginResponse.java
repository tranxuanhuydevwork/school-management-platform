package com.golearn.myf3school_backend.application_service.dtos.response;

import com.golearn.myf3school_backend.contract.enums.RoleType;
import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class LoginResponse {
    private Long         userId;
    private String       username;
    private String       email;
    private String       fullName;
    private Set<RoleType> roles;          // was: RoleType role
    private String       avatarUrl;
    private String       accessToken;    // short-lived JWT (e.g. 15 min)
    private String       refreshToken;   // long-lived opaque token
    private Long         classId;        // null if not a student
    private Long         studentId;      // StudentProfile.id (null if not student)
}