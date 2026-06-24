package com.golearn.myf3school_backend.application_service.dtos.response;

import com.golearn.myf3school_backend.infrastructure.entity.Schedule;
import lombok.Builder;
import lombok.Data;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Data
@Builder
public class ScheduleResponse {

    private Long    id;
    private String  subjectCode;
    private String  subjectName;
    private String  teacherName;
    private String  room;
    private String  classCode;
    private Integer periodStart;
    private Integer periodEnd;
    private String  time;       // "07:00 - 09:25"
    private String  dayOfWeek;  // "1" … "7"

    private static final DateTimeFormatter TIME_FMT =
            DateTimeFormatter.ofPattern("HH:mm");

    public static ScheduleResponse from(Schedule s) {
        return ScheduleResponse.builder()
                .id(s.getId())
                .subjectCode(s.getCourseSection().getSubject().getCode())
                .subjectName(s.getCourseSection().getSubject().getName())
                .teacherName(s.getCourseSection().getTeacher().getFullName())
                .room(s.getRoom() != null ? s.getRoom() : "")
                .classCode(s.getCourseSection().getSchoolClass().getCode())
                .periodStart(s.getPeriodStart())
                .periodEnd(s.getPeriodEnd())
                .time(formatTime(s.getTimeStart(), s.getTimeEnd()))
                .dayOfWeek(s.getDayOfWeek() != null
                        ? s.getDayOfWeek().toString() : "")
                .build();
    }

    private static String formatTime(LocalTime start, LocalTime end) {
        if (start == null && end == null) return "";
        if (start == null) return "- " + end.format(TIME_FMT);
        if (end   == null) return start.format(TIME_FMT) + " -";
        return start.format(TIME_FMT) + " - " + end.format(TIME_FMT);
    }
}