package com.golearn.myf3school_backend.controller;

import com.golearn.myf3school_backend.application_service.dtos.request.CreateGradeCorrectionDto;
import com.golearn.myf3school_backend.application_service.dtos.request.ReviewGradeCorrectionDto;
import com.golearn.myf3school_backend.application_service.dtos.response.ApiResponse;
import com.golearn.myf3school_backend.application_service.dtos.response.GradeCorrectionResponse;
import com.golearn.myf3school_backend.application_service.service.GradeCorrectionService;
import com.golearn.myf3school_backend.contract.enums.CorrectionStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller cho nghiệp vụ yêu cầu sửa điểm.
 *
 * ── Dành cho TEACHER ──────────────────────────────────────────
 *   POST   /api/grade-corrections
 *   GET    /api/grade-corrections/teachers/{teacherId}
 *   GET    /api/grade-corrections/quota/{studentProfileId}
 *
 * ── Dành cho KHẢO THÍ (EXAM_DEPT) ────────────────────────────
 *   GET    /api/grade-corrections
 *   GET    /api/grade-corrections/{id}
 *   PATCH  /api/grade-corrections/{id}/review
 *
 *   BUG FIX #3: HTML gọi PUT /approve và PUT /reject (không tồn tại).
 *   Thêm 2 endpoint shortcut:
 *   PATCH  /api/grade-corrections/{id}/approve   ← gọi review với APPROVED
 *   PATCH  /api/grade-corrections/{id}/reject    ← gọi review với REJECTED + note bắt buộc
 *
 *   GET    /api/grade-corrections/pending-count
 */
@RestController
@RequestMapping("/api/grade-corrections")
@RequiredArgsConstructor
public class GradeCorrectionController {

    private final GradeCorrectionService service;    private final JavaMailSender mailSender; // ← thêm dòng này

    // ─────────────────────────────────────────────────────────────────────
    // TEACHER endpoints
    // ─────────────────────────────────────────────────────────────────────
    @GetMapping("/test-mail")
    public ResponseEntity<String> testMail() {
        try {
            org.springframework.mail.SimpleMailMessage msg =
                    new org.springframework.mail.SimpleMailMessage();
            msg.setFrom("tranxuanhuy.it.work@gmail.com");
            msg.setTo("huytxhe180649@fpt.edu.vn");
            msg.setSubject("Test mail");
            msg.setText("Test OK");
            mailSender.send(msg);   // inject mailSender vào controller tạm
            return ResponseEntity.ok("Sent OK");
        } catch (Exception e) {
            return ResponseEntity.ok("ERROR: " + e.getMessage()
                    + " | cause: " + (e.getCause() != null ? e.getCause().getMessage() : "null"));
        }
    }
    @PostMapping
    public ResponseEntity<ApiResponse<GradeCorrectionResponse>> create(
            @Valid @RequestBody CreateGradeCorrectionDto dto) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.created(service.createRequest(dto)));
    }

    @GetMapping("/teachers/{teacherId}")
    public ResponseEntity<ApiResponse<List<GradeCorrectionResponse>>> getByTeacher(
            @PathVariable Long teacherId,
            @RequestParam(required = false) CorrectionStatus status) {
        return ResponseEntity.ok(ApiResponse.ok(service.getByTeacher(teacherId, status)));
    }

    @GetMapping("/quota/{studentProfileId}")
    public ResponseEntity<ApiResponse<Map<String, Integer>>> getQuota(
            @PathVariable Long studentProfileId) {
        int[] info = service.getQuotaInfo(studentProfileId);
        Map<String, Integer> result = Map.of(
                "editCount",         info[0],
                "remaining",         info[1],
                "maxEdits",          info[2],
                "daysUntilDeadline", info[3]
        );
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    // ─────────────────────────────────────────────────────────────────────
    // KHẢO THÍ endpoints
    // ─────────────────────────────────────────────────────────────────────

    @GetMapping
    public ResponseEntity<ApiResponse<Page<GradeCorrectionResponse>>> getAll(
            @RequestParam(required = false) CorrectionStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size) {
        return ResponseEntity.ok(ApiResponse.ok(
                service.getAll(status, PageRequest.of(page, size))));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<GradeCorrectionResponse>> getById(
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(service.getById(id)));
    }

    /**
     * Endpoint review đầy đủ — dùng cho tích hợp chính xác.
     * PATCH /api/grade-corrections/7/review
     * Body: { "status": "APPROVED", "reviewedBy": 3 }
     *       { "status": "REJECTED", "reviewedBy": 3, "reviewNote": "Lý do..." }
     */
    @PatchMapping("/{id}/review")
    public ResponseEntity<ApiResponse<GradeCorrectionResponse>> review(
            @PathVariable Long id,
            @Valid @RequestBody ReviewGradeCorrectionDto dto) {
        return ResponseEntity.ok(ApiResponse.ok(
                "Đã xử lý đơn sửa điểm.", service.reviewRequest(id, dto)));
    }

    /**
     * BUG FIX #3: Shortcut APPROVE — HTML gọi PUT /approve nhưng endpoint không tồn tại.
     *
     * PATCH /api/grade-corrections/{id}/approve
     * Body: { "reviewedBy": 3 }
     *
     * Tương đương gọi /review với status=APPROVED.
     * reviewedBy mặc định = 1 nếu không truyền (để test nhanh).
     */
    @PatchMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<GradeCorrectionResponse>> approve(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, Long> body) {

        Long reviewedBy = (body != null && body.containsKey("reviewedBy"))
                ? body.get("reviewedBy")
                : 1L; // default reviewer id cho testing

        ReviewGradeCorrectionDto dto = ReviewGradeCorrectionDto.builder()
                .status(CorrectionStatus.APPROVED)
                .reviewedBy(reviewedBy)
                .reviewNote(null)
                .build();

        return ResponseEntity.ok(ApiResponse.ok(
                "Đã duyệt đơn sửa điểm.", service.reviewRequest(id, dto)));
    }

    /**
     * BUG FIX #3: Shortcut REJECT — HTML gọi PUT /reject nhưng endpoint không tồn tại.
     *
     * PATCH /api/grade-corrections/{id}/reject
     * Body: { "reviewedBy": 3, "reviewNote": "Lý do từ chối" }
     *
     * Nếu không có reviewNote → mặc định "Không đạt yêu cầu" để không bị 400.
     */
    @PatchMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<GradeCorrectionResponse>> reject(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, Object> body) {

        Long reviewedBy = (body != null && body.containsKey("reviewedBy"))
                ? Long.valueOf(body.get("reviewedBy").toString())
                : 1L;

        String reviewNote = (body != null && body.containsKey("reviewNote"))
                ? body.get("reviewNote").toString()
                : "Không đạt yêu cầu phúc khảo"; // default để test

        ReviewGradeCorrectionDto dto = ReviewGradeCorrectionDto.builder()
                .status(CorrectionStatus.REJECTED)
                .reviewedBy(reviewedBy)
                .reviewNote(reviewNote)
                .build();

        return ResponseEntity.ok(ApiResponse.ok(
                "Đã từ chối đơn sửa điểm.", service.reviewRequest(id, dto)));
    }

    @GetMapping("/pending-count")
    public ResponseEntity<ApiResponse<Map<String, Long>>> pendingCount() {
        return ResponseEntity.ok(ApiResponse.ok(
                Map.of("count", service.countPending())));
    }
}