package com.golearn.myf3school_backend.controller;

import com.golearn.myf3school_backend.application_service.dtos.response.ApiResponse;
import com.golearn.myf3school_backend.application_service.dtos.response.ScheduleResponse;
import com.golearn.myf3school_backend.application_service.exception.NotFoundException;
import com.golearn.myf3school_backend.infrastructure.entity.Schedule;
import com.golearn.myf3school_backend.infrastructure.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleRepository scheduleRepository;

    @GetMapping("/classes/{classId}")
    public ResponseEntity<ApiResponse<List<ScheduleResponse>>> getByClass(
            @PathVariable Long classId) {

        List<ScheduleResponse> result = scheduleRepository
                .findCurrentByClass(classId, LocalDate.now())
                .stream().map(ScheduleResponse::from).toList();

        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    /**
     * GET /api/schedules/classes/{classId}/by-day?day=MONDAY&date=2026-03-13
     *
     * date = ngày THỰC TẾ Flutter đang xem
     * → dùng để kiểm tra effectiveFrom/effectiveTo
     * → KHÔNG dùng LocalDate.now() cứng
     */
    @GetMapping("/classes/{classId}/by-day")
    public ResponseEntity<ApiResponse<List<ScheduleResponse>>> getByClassAndDay(
            @PathVariable Long classId,
            @RequestParam DayOfWeek day,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        LocalDate queryDate = (date != null) ? date : LocalDate.now();
        int dayValue = day.getValue();

        // Log để debug — xem Flutter có gửi đúng date không
        log.info(">>> by-day classId={} day={} dayValue={} queryDate={}",
                classId, day, dayValue, queryDate);

        List<ScheduleResponse> result = scheduleRepository
                .findByClassAndDay(classId, dayValue, queryDate)
                .stream().map(ScheduleResponse::from).toList();

        log.info(">>> result size={}", result.size());

        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    /**
     * GET /api/schedules/classes/{classId}/by-week?date=2026-03-18
     *
     * Flutter gọi khi load toàn tuần
     * date = bất kỳ ngày nào trong tuần cần xem
     */
    @GetMapping("/classes/{classId}/by-week")
    public ResponseEntity<ApiResponse<List<ScheduleResponse>>> getByClassAndWeek(
            @PathVariable Long classId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        LocalDate queryDate = (date != null) ? date : LocalDate.now();

        log.info(">>> by-week classId={} queryDate={}", classId, queryDate);

        List<ScheduleResponse> result = scheduleRepository
                .findCurrentByClass(classId, queryDate)
                .stream().map(ScheduleResponse::from).toList();

        log.info(">>> result size={}", result.size());

        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @GetMapping("/teachers/{teacherId}")
    public ResponseEntity<ApiResponse<List<ScheduleResponse>>> getByTeacher(
            @PathVariable Long teacherId) {

        List<ScheduleResponse> result = scheduleRepository
                .findCurrentByTeacher(teacherId, LocalDate.now())
                .stream().map(ScheduleResponse::from).toList();

        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Schedule>> create(
            @RequestBody Schedule schedule) {
        return ResponseEntity.ok(
                ApiResponse.created(scheduleRepository.save(schedule)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Schedule>> update(
            @PathVariable Long id, @RequestBody Schedule body) {

        Schedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Schedule", id));

        if (body.getDayOfWeek()   != null) schedule.setDayOfWeek(body.getDayOfWeek());
        if (body.getPeriodStart() != null) schedule.setPeriodStart(body.getPeriodStart());
        if (body.getPeriodEnd()   != null) schedule.setPeriodEnd(body.getPeriodEnd());
        if (body.getTimeStart()   != null) schedule.setTimeStart(body.getTimeStart());
        if (body.getTimeEnd()     != null) schedule.setTimeEnd(body.getTimeEnd());
        if (body.getRoom()        != null) schedule.setRoom(body.getRoom());

        return ResponseEntity.ok(ApiResponse.ok("Cập nhật thành công",
                scheduleRepository.save(schedule)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        if (!scheduleRepository.existsById(id))
            throw new NotFoundException("Schedule", id);
        scheduleRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.ok("Xóa thành công", null));
    }
}