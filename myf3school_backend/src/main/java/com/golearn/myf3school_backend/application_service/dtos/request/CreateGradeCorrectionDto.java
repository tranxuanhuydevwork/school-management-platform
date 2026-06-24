package com.golearn.myf3school_backend.application_service.dtos.request;

import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateGradeCorrectionDto {

    private Long teacherId;
    private Long studentProfileId;
    private Long gradeComponentId;

    private BigDecimal oldScore;
    private BigDecimal proposedScore;

    private String reason;
}