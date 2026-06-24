package com.golearn.myf3school_backend.application_service.service;

import com.golearn.myf3school_backend.application_service.dtos.response.AttendanceStudentResponse;
import com.golearn.myf3school_backend.contract.enums.AttendanceStatus;
import com.golearn.myf3school_backend.infrastructure.entity.*;
import com.golearn.myf3school_backend.infrastructure.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRecordRepository  recordRepository;
    private final AttendanceSessionRepository sessionRepository;
    private final CourseSectionRepository     sectionRepository;
    private final StudentProfileRepository    studentRepository;
    private final NotificationRepository      notificationRepository;
    private final UserRepository              userRepository;

    // ── Lấy điểm danh toàn kỳ của 1 học sinh ─────────────────────────────

    /**
     * @param studentProfileId  ID của StudentProfile (không phải User.id)
     * @param semesterId        ID học kỳ
     */
    public AttendanceStudentResponse getStudentAttendance(Long studentProfileId, Long semesterId) {

        // 1. Lấy thông tin học sinh
        StudentProfile profile = studentRepository.findById(studentProfileId)
                .orElseThrow(() -> new RuntimeException(
                        "Không tìm thấy học sinh id=" + studentProfileId));

        // 2. Lấy tất cả attendance records của học sinh trong học kỳ
        List<AttendanceRecord> allRecords =
                recordRepository.findByStudentAndSemester(studentProfileId, semesterId);

        // 3. Group records theo courseSection.id
        Map<Long, List<AttendanceRecord>> bySectionId = allRecords.stream()
                .collect(Collectors.groupingBy(
                        r -> r.getSession().getCourseSection().getId()
                ));

        // 4. Lấy danh sách course section từ lớp học của học sinh trong kỳ
        //    Dùng classId của profile + semesterId thay vì userId
        List<CourseSection> sections =
                sectionRepository.findByClassIdAndSemesterId(
                        profile.getSchoolClass().getId(), semesterId);

        // 5. Build danh sách chi tiết từng môn
        List<AttendanceStudentResponse.SubjectAttendance> subjectList = sections.stream()
                .map(cs -> buildSubjectAttendance(
                        cs, bySectionId.getOrDefault(cs.getId(), List.of())))
                .toList();

        // 6. Tổng hợp toàn kỳ
        int total   = allRecords.size();
        int present = count(allRecords, AttendanceStatus.PRESENT);
        int absent  = count(allRecords, AttendanceStatus.ABSENT);
        int late    = count(allRecords, AttendanceStatus.LATE);
        int excused = count(allRecords, AttendanceStatus.EXCUSED);
        double pct  = total > 0 ? (double)(present + late + excused) / total * 100.0 : 0;

        return AttendanceStudentResponse.builder()
                .studentId(studentProfileId)
                .studentCode(profile.getStudentCode())
                .studentName(profile.getUser().getFullName())
                .semesterId(semesterId)
                .totalSessions(total)
                .presentCount(present)
                .absentCount(absent)
                .lateCount(late)
                .excusedCount(excused)
                .attendancePercent(Math.round(pct * 10.0) / 10.0)
                .atRisk(pct < 80.0)
                .subjects(subjectList)
                .build();
    }

    // ── Gửi notification khi học sinh vắng mặt ───────────────────────────

    public void processRecordAndNotify(AttendanceRecord record) {
        if (record.getStatus() != AttendanceStatus.ABSENT) return;

        StudentProfile student = record.getStudent();
        if (student == null || student.getUser() == null) return;

        String subjectName = record.getSession().getCourseSection().getSubject().getName();
        String date        = record.getSession().getSessionDate().toString();

        Notification notification = Notification.builder()
                .user(student.getUser())
                .title("Cảnh báo vắng học")
                .message(String.format(
                        "Bạn đã vắng buổi học môn %s ngày %s. Vui lòng liên hệ giáo viên nếu có lý do.",
                        subjectName, date))
                .refType("ATTENDANCE")
                .refId(record.getId())
                .build();

        notificationRepository.save(notification);
        log.info("[Attendance] Notified absent: student={}, subject={}, date={}",
                student.getUser().getId(), subjectName, date);
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private AttendanceStudentResponse.SubjectAttendance buildSubjectAttendance(
            CourseSection cs, List<AttendanceRecord> records) {

        List<AttendanceSession> sessions =
                sessionRepository.findByCourseSectionIdOrderBySessionDateAsc(cs.getId());

        Map<Long, AttendanceRecord> recordMap = records.stream()
                .collect(Collectors.toMap(
                        r -> r.getSession().getId(), r -> r, (a, b) -> a));

        int total   = sessions.size();
        int present = count(records, AttendanceStatus.PRESENT);
        int absent  = count(records, AttendanceStatus.ABSENT);
        int late    = count(records, AttendanceStatus.LATE);
        int excused = count(records, AttendanceStatus.EXCUSED);
        double pct  = total > 0 ? (double)(present + late + excused) / total * 100.0 : 0;

        List<AttendanceStudentResponse.SessionRecord> sessionRecords = sessions.stream()
                .map(s -> {
                    AttendanceRecord rec = recordMap.get(s.getId());
                    return AttendanceStudentResponse.SessionRecord.builder()
                            .sessionId(s.getId())
                            .sessionDate(s.getSessionDate())
                            .periodStart(s.getPeriodStart())
                            .periodEnd(s.getPeriodEnd())
                            .lessonTopic(s.getLessonTopic())
                            .status(rec != null
                                    ? rec.getStatus().name()
                                    : AttendanceStatus.NOT_YET.name())
                            .note(rec != null ? rec.getNote() : null)
                            .build();
                })
                .toList();

        return AttendanceStudentResponse.SubjectAttendance.builder()
                .sectionId(cs.getId())
                .subjectCode(cs.getSubject().getCode())
                .subjectName(cs.getSubject().getName())
                .totalSessions(total)
                .presentCount(present)
                .absentCount(absent)
                .lateCount(late)
                .excusedCount(excused)
                .attendancePercent(Math.round(pct * 10.0) / 10.0)
                .atRisk(pct < 80.0)
                .sessions(sessionRecords)
                .build();
    }

    private int count(List<AttendanceRecord> records, AttendanceStatus status) {
        return (int) records.stream().filter(r -> r.getStatus() == status).count();
    }
}