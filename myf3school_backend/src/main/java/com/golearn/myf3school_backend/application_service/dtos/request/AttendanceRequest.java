package com.golearn.myf3school_backend.application_service.dtos.request;
import com.golearn.myf3school_backend.contract.enums.AttendanceStatus;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class AttendanceRequest {
    private Long courseSectionId;
    private LocalDate sessionDate;
    private Integer periodStart;
    private Integer periodEnd;
    private String lessonTopic;
    private Long takenById;
    private List<RecordEntry> records;

    @Data
    public static class RecordEntry {
        private Long studentId;
        private AttendanceStatus status;
        private String note;
    }
}
