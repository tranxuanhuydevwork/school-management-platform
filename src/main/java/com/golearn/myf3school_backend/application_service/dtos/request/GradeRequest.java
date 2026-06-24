package com.golearn.myf3school_backend.application_service.dtos.request;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Map;

@Data
public class GradeRequest {
    private BigDecimal score;          // điểm cho 1 học sinh
    private String notes;
    private Long gradedById;           // id giáo viên chấm
    private Map<Long, BigDecimal> studentScores; // bulk: { studentId: score }
}