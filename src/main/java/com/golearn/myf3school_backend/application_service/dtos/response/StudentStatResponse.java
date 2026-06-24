package com.golearn.myf3school_backend.application_service.dtos.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StudentStatResponse {
    private double gpa;
    private int attendancePercent;
    private int earnedCredits;
    private int totalCredits;
    private int classRank;
}