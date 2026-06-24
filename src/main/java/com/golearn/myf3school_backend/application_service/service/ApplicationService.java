package com.golearn.myf3school_backend.application_service.service;

import com.golearn.myf3school_backend.application_service.dtos.request.CreateApplicationDto;
import com.golearn.myf3school_backend.application_service.dtos.request.ReviewApplicationDto;
import com.golearn.myf3school_backend.application_service.dtos.response.ApplicationResponse;
import com.golearn.myf3school_backend.application_service.exception.BadRequestException;
import com.golearn.myf3school_backend.application_service.exception.NotFoundException;
import com.golearn.myf3school_backend.contract.enums.RequestStatus;
import com.golearn.myf3school_backend.contract.enums.RequestType;
import com.golearn.myf3school_backend.infrastructure.entity.Application;
import com.golearn.myf3school_backend.infrastructure.repository.ApplicationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final ApplicationRepository repo;

    // ── Lấy đơn theo học sinh ─────────────────────────────────

    public List<ApplicationResponse> getByStudent(Long studentId) {
        return repo.findByStudentIdOrderByCreatedAtDesc(studentId)
                .stream().map(ApplicationResponse::from).toList();
    }

    public List<ApplicationResponse> getByStudentAndStatus(Long studentId, RequestStatus status) {
        return repo.findByStudentIdAndStatusOrderByCreatedAtDesc(studentId, status)
                .stream().map(ApplicationResponse::from).toList();
    }

    public ApplicationResponse getById(Long id) {
        return ApplicationResponse.from(findOrThrow(id));
    }

    // ── Tạo đơn mới ──────────────────────────────────────────

    @Transactional
    public ApplicationResponse create(CreateApplicationDto dto) {
        Application app = Application.builder()
                .studentId(dto.getStudentId())
                .requestType(dto.getRequestType())
                .title(dto.getTitle())
                .description(dto.getDescription())
                .status(RequestStatus.PENDING)
                .build();
        Application saved = repo.save(app);
        log.info("[Application] Created: id={}, student={}, type={}", saved.getId(), saved.getStudentId(), saved.getRequestType());
        return ApplicationResponse.from(saved);
    }

    // ── Duyệt / Từ chối ──────────────────────────────────────

    @Transactional
    public ApplicationResponse review(Long id, ReviewApplicationDto dto) {
        Application app = findOrThrow(id);

        if (app.getStatus() != RequestStatus.PENDING) {
            throw new BadRequestException("Đơn đã được xử lý (status=" + app.getStatus() + ")");
        }
        if (dto.getStatus() == RequestStatus.REJECTED
                && (dto.getRejectedReason() == null || dto.getRejectedReason().isBlank())) {
            throw new BadRequestException("Phải cung cấp lý do từ chối khi REJECTED");
        }

        app.setStatus(dto.getStatus());
        app.setRejectedReason(dto.getRejectedReason());
        app.setReviewedBy(dto.getReviewedBy());
        app.setReviewedAt(LocalDateTime.now());

        Application saved = repo.save(app);
        log.info("[Application] Reviewed: id={}, status={}, by={}", id, dto.getStatus(), dto.getReviewedBy());
        return ApplicationResponse.from(saved);
    }

    // ── Xóa đơn ──────────────────────────────────────────────

    @Transactional
    public void delete(Long id, Long studentId) {
        Application app = findOrThrow(id);
        if (!app.getStudentId().equals(studentId)) {
            throw new BadRequestException("Không có quyền xóa đơn này");
        }
        if (app.getStatus() == RequestStatus.APPROVED) {
            throw new BadRequestException("Không thể xóa đơn đã được duyệt");
        }
        repo.deleteById(id);
        log.info("[Application] Deleted: id={}", id);
    }

    // ── Lấy tất cả (admin) với filter + phân trang ───────────

    public Page<ApplicationResponse> getAll(RequestStatus status, RequestType type, Pageable pageable) {
        Page<Application> page;

        if (status != null && type != null) {
            page = repo.findByStatusAndRequestTypeOrderByCreatedAtDesc(status, type, pageable);
        } else if (status != null) {
            page = repo.findByStatusOrderByCreatedAtDesc(status, pageable);
        } else if (type != null) {
            page = repo.findByRequestTypeOrderByCreatedAtDesc(type, pageable);
        } else {
            page = repo.findAllByOrderByCreatedAtDesc(pageable);
        }

        return page.map(ApplicationResponse::from);
    }

    // ── Helper ───────────────────────────────────────────────

    private Application findOrThrow(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Application", id));
    }
}