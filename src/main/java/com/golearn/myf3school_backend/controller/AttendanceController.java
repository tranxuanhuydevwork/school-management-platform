package com.golearn.myf3school_backend.controller;

import com.golearn.myf3school_backend.application_service.dtos.request.AttendanceRequest;
import com.golearn.myf3school_backend.application_service.dtos.response.ApiResponse;
import com.golearn.myf3school_backend.application_service.dtos.response.AttendanceStudentResponse;
import com.golearn.myf3school_backend.application_service.exception.BadRequestException;
import com.golearn.myf3school_backend.application_service.exception.NotFoundException;
import com.golearn.myf3school_backend.application_service.service.AttendanceService;
import com.golearn.myf3school_backend.infrastructure.entity.*;
import com.golearn.myf3school_backend.infrastructure.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.format.annotation.DateTimeFormat;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("api/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceSessionRepository sessionRepository;
    private final AttendanceRecordRepository  recordRepository;
    private final CourseSectionRepository     sectionRepository;
    private final StudentProfileRepository    studentRepository;
    private final UserRepository              userRepository;
    private final AttendanceService           attendanceService; // ← inject service mới

    // ── Điểm danh (giữ nguyên, thêm notification) ─────────────────────────

    @PostMapping("/sessions")
    @Transactional
    public ResponseEntity<ApiResponse<AttendanceSession>> takeAttendance(
            @RequestBody AttendanceRequest request) {

        CourseSection section = sectionRepository.findById(request.getCourseSectionId())
                .orElseThrow(() -> new NotFoundException("CourseSection", request.getCourseSectionId()));

        if (sessionRepository.findByCourseSectionIdAndSessionDateAndPeriodStart(
                section.getId(), request.getSessionDate(), request.getPeriodStart()).isPresent()) {
            throw new BadRequestException("Đã điểm danh buổi học này rồi");
        }

        User takenBy = request.getTakenById() != null
                ? userRepository.findById(request.getTakenById()).orElse(null)
                : null;

        AttendanceSession session = AttendanceSession.builder()
                .courseSection(section)
                .sessionDate(request.getSessionDate())
                .periodStart(request.getPeriodStart())
                .periodEnd(request.getPeriodEnd())
                .lessonTopic(request.getLessonTopic())
                .takenBy(takenBy)
                .takenAt(LocalDateTime.now())
                .build();
        sessionRepository.save(session);

        List<AttendanceRecord> records = new ArrayList<>();
        if (request.getRecords() != null) {
            for (AttendanceRequest.RecordEntry entry : request.getRecords()) {
                StudentProfile student = studentRepository.findById(entry.getStudentId())
                        .orElseThrow(() -> new NotFoundException("Student", entry.getStudentId()));

                AttendanceRecord rec = AttendanceRecord.builder()
                        .session(session)
                        .student(student)
                        .status(entry.getStatus())
                        .note(entry.getNote())
                        .build();
                records.add(rec);
            }
            recordRepository.saveAll(records);

            // ── Trigger notification cho từng record ABSENT ──────────────
            records.forEach(attendanceService::processRecordAndNotify);
        }

        log.info("Attendance taken: section={}, date={}, records={}",
                section.getId(), request.getSessionDate(), records.size());
        return ResponseEntity.ok(ApiResponse.created(session));
    }

    // ── API mới cho Flutter: điểm danh theo học sinh ─────────────────────

    /**
     * GET /api/attendance/students/{studentId}?semesterId=1
     * Trả về AttendanceStudentResponse — danh sách môn + từng buổi học
     */
    @GetMapping("/students/{studentId}")
    public ResponseEntity<ApiResponse<AttendanceStudentResponse>> getStudentAttendance(
            @PathVariable Long studentId,
            @RequestParam Long semesterId) {

        AttendanceStudentResponse response =
                attendanceService.getStudentAttendance(studentId, semesterId);
        return ResponseEntity.ok(ApiResponse.ok("Lấy điểm danh thành công", response));
    }

    // ── Các endpoint cũ giữ nguyên ────────────────────────────────────────

    @GetMapping("/sessions/{sessionId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSession(
            @PathVariable Long sessionId) {
        AttendanceSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new NotFoundException("AttendanceSession", sessionId));
        List<AttendanceRecord> records = recordRepository.findBySessionId(sessionId);
        return ResponseEntity.ok(ApiResponse.ok(Map.of("session", session, "records", records)));
    }

    @GetMapping("/sections/{sectionId}/sessions")
    public ResponseEntity<ApiResponse<List<AttendanceSession>>> getSectionSessions(
            @PathVariable Long sectionId) {
        return ResponseEntity.ok(ApiResponse.ok(
                sessionRepository.findByCourseSectionIdOrderBySessionDateDesc(sectionId)));
    }

    @GetMapping("/students/{studentId}/summary")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStudentSummary(
            @PathVariable Long studentId,
            @RequestParam Long semesterId) {

        List<AttendanceRecord> records =
                recordRepository.findByStudentAndSemester(studentId, semesterId);
        long total   = records.size();
        Map<String, Long> cnt = records.stream()
                .collect(Collectors.groupingBy(r -> r.getStatus().name(), Collectors.counting()));

        long present = cnt.getOrDefault("PRESENT", 0L);
        long absent  = cnt.getOrDefault("ABSENT",  0L);
        long late    = cnt.getOrDefault("LATE",    0L);
        long excused = cnt.getOrDefault("EXCUSED", 0L);
        double pct   = total > 0 ? (double)(present + late) / total * 100 : 0;

        return ResponseEntity.ok(ApiResponse.ok(Map.of(
                "studentId", studentId,
                "totalSessions", total,
                "present", present, "absent", absent,
                "late", late, "excused", excused,
                "attendancePercentage", String.format("%.1f%%", pct),
                "atRisk", pct < 80.0
        )));
    }

    @PutMapping("/records/{recordId}")
    public ResponseEntity<ApiResponse<AttendanceRecord>> updateRecord(
            @PathVariable Long recordId,
            @RequestBody AttendanceRecord body) {
        AttendanceRecord record = recordRepository.findById(recordId)
                .orElseThrow(() -> new NotFoundException("AttendanceRecord", recordId));
        if (body.getStatus() != null) {
            record.setStatus(body.getStatus());
            // Nếu cập nhật thành ABSENT → gửi thông báo lại
            attendanceService.processRecordAndNotify(record);
        }
        if (body.getNote() != null) record.setNote(body.getNote());
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật thành công",
                recordRepository.save(record)));
    }
    @GetMapping("/sessions/check")
    public ResponseEntity<ApiResponse<AttendanceSession>> checkSession(
            @RequestParam Long sectionId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam Integer periodStart) {

        // Bắt buộc date phải là hôm nay
        if (!date.isEqual(LocalDate.now())) {
            throw new BadRequestException("Chỉ được kiểm tra điểm danh trong ngày hôm nay");
        }

        Optional<AttendanceSession> existing =
                sessionRepository.findByCourseSectionIdAndSessionDateAndPeriodStart(
                        sectionId, date, periodStart);

        return ResponseEntity.ok(ApiResponse.ok(
                existing.isPresent() ? "Đã có buổi học" : "Chưa có buổi học",
                existing.orElse(null)
        ));
    }

// PUT /api/attendance/records/session/{sessionId}/student/{studentId}
    /**
     * Chỉ cho phép sửa nếu session.sessionDate == hôm nay.
     * Giáo viên chỉ sửa được record thuộc section mình dạy
     * (kiểm tra qua session → courseSection → teacher).
     */
    @PutMapping("/records/session/{sessionId}/student/{studentId}")
    @Transactional
    public ResponseEntity<ApiResponse<AttendanceRecord>> updateRecordBySession(
            @PathVariable Long sessionId,
            @PathVariable Long studentId,
            @RequestBody AttendanceRequest.RecordEntry body) {

        // 1. Lấy session — validate ngày
        AttendanceSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new NotFoundException("AttendanceSession", sessionId));

        if (!session.getSessionDate().isEqual(LocalDate.now())) {
            throw new BadRequestException("Chỉ được sửa điểm danh trong ngày hôm nay");
        }

        // 2. Tìm record
        AttendanceRecord record = recordRepository
                .findBySessionIdAndStudentId(sessionId, studentId)
                .orElseThrow(() -> new NotFoundException(
                        "AttendanceRecord", "session=" + sessionId + "/student=" + studentId));

        // 3. Cập nhật
        if (body.getStatus() != null) {
            record.setStatus(body.getStatus());
            attendanceService.processRecordAndNotify(record);   // gửi notif nếu ABSENT
        }
        if (body.getNote() != null) record.setNote(body.getNote());

        return ResponseEntity.ok(ApiResponse.ok("Cập nhật thành công",
                recordRepository.save(record)));
    }
    // GET /api/attendance/sessions?teacherId=X&size=50
// Thêm vào AttendanceSessionRepository:
//   List<AttendanceSession> findByTakenByIdOrderBySessionDateDesc(Long teacherId, Pageable p);
//   → gọi: sessionRepository.findByTakenByIdOrderBySessionDateDesc(teacherId, PageRequest.of(0, size))

    @GetMapping("/sessions")
    public ResponseEntity<ApiResponse<List<AttendanceSession>>> getSessionsByTeacher(
            @RequestParam(required = false) Long teacherId,
            @RequestParam(defaultValue = "50") int size) {

        List<AttendanceSession> sessions;
        if (teacherId != null) {
            sessions = sessionRepository
                    .findByTakenByIdOrderBySessionDateDesc(teacherId, size);
        } else {
            sessions = sessionRepository.findAll(
                            org.springframework.data.domain.PageRequest.of(0, size,
                                    org.springframework.data.domain.Sort.by("sessionDate").descending()))
                    .getContent();
        }

        return ResponseEntity.ok(ApiResponse.ok(sessions));
    }
}
