package com.golearn.myf3school_backend.controller;

import com.golearn.myf3school_backend.application_service.dtos.request.CreateApplicationDto;
import com.golearn.myf3school_backend.application_service.dtos.request.ReviewApplicationDto;
import com.golearn.myf3school_backend.application_service.dtos.response.ApiResponse;
import com.golearn.myf3school_backend.application_service.dtos.response.ApplicationResponse;
import com.golearn.myf3school_backend.application_service.service.ApplicationService;
import com.golearn.myf3school_backend.contract.enums.RequestStatus;
import com.golearn.myf3school_backend.contract.enums.RequestType;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/api/requests")
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService service;

    // GET /api/requests/students/{studentId}
    // studentId = student_profiles.id (AuthController.studentId)
    @GetMapping("/students/{studentId}")
    public ResponseEntity<ApiResponse<List<ApplicationResponse>>> getByStudent(
            @PathVariable Long studentId,
            @RequestParam(required = false) RequestStatus status) {

        List<ApplicationResponse> list = (status != null)
                ? service.getByStudentAndStatus(studentId, status)
                : service.getByStudent(studentId);

        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ApplicationResponse>> getById(
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(service.getById(id)));
    }

    // POST /api/requests
    // roles kiểm tra ở service — STUDENT không được gửi LEAVE_REQUEST
    @PostMapping
    public ResponseEntity<ApiResponse<ApplicationResponse>> create(
            @Valid @RequestBody CreateApplicationDto dto) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.created(service.create(dto)));
    }

    @PatchMapping("/{id}/review")
    public ResponseEntity<ApiResponse<ApplicationResponse>> review(
            @PathVariable Long id,
            @Valid @RequestBody ReviewApplicationDto dto) {
        return ResponseEntity.ok(
                ApiResponse.ok("Duyệt đơn thành công.", service.review(id, dto)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> delete(
            @PathVariable Long id,
            @RequestParam Long studentId) {
        service.delete(id, studentId);
        return ResponseEntity.ok(ApiResponse.ok("Đã xóa đơn thành công.", null));
    }

    // GET /api/requests?status=PENDING&requestType=LEAVE_REQUEST&page=0&size=15
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ApplicationResponse>>> getAll(
            @RequestParam(required = false) RequestStatus status,
            @RequestParam(required = false) RequestType requestType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size) {
        return ResponseEntity.ok(ApiResponse.ok(
                service.getAll(status, requestType, PageRequest.of(page, size))));
    }
}