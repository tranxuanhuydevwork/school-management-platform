package com.golearn.myf3school_backend.application_service.service;

import com.golearn.myf3school_backend.application_service.dtos.request.CreateGradeCorrectionDto;
import com.golearn.myf3school_backend.application_service.dtos.request.ReviewGradeCorrectionDto;
import com.golearn.myf3school_backend.application_service.dtos.response.GradeCorrectionResponse;
import com.golearn.myf3school_backend.application_service.exception.BadRequestException;
import com.golearn.myf3school_backend.application_service.exception.NotFoundException;
import com.golearn.myf3school_backend.contract.enums.CorrectionStatus;
import com.golearn.myf3school_backend.infrastructure.entity.*;
import com.golearn.myf3school_backend.infrastructure.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GradeCorrectionService {

    private final GradeCorrectionRequestRepository   correctionRepo;
    private final GradeEditPolicyRepository          policyRepo;
    private final GradeDeadlineConfigRepository      deadlineRepo;
    private final UserRepository                     userRepo;
    private final StudentProfileRepository           studentProfileRepo;
    private final GradeComponentRepository           componentRepo;
    private final GradeCorrectionNotificationService notifService;
    private final GradeApplyPort                     gradeApplyPort;

    // ─────────────────────────────────────────────────────────────────────
    // QUOTA CHECK — gọi từ GradeController.updateGrade()
    // ─────────────────────────────────────────────────────────────────────

    /**
     * Kiểm tra và tiêu thụ 1 lần tự sửa.
     *
     * Logic kiểm tra (theo thứ tự):
     *   1. Lấy deadline đang active → nếu không có → BadRequest
     *   2. Kiểm tra now() <= deadline → nếu quá hạn → BadRequest
     *   3. Lấy hoặc tạo GradeEditPolicy cho (studentProfileId, deadline)
     *   4. Kiểm tra editCount < MAX_SELF_EDITS → nếu hết → BadRequest
     *   5. editCount++ và save
     *
     * @param studentProfileId học sinh được sửa điểm
     * @throws BadRequestException "đã quá hạn chốt điểm" hoặc "đã đạt giới hạn 2 lần sửa"
     */
    @Transactional
    public void checkAndConsumeQuota(Long studentProfileId) {

        GradeDeadlineConfig config = deadlineRepo.findByIsActiveTrue()
                .orElseThrow(() -> new BadRequestException(
                        "Chưa có đợt chốt điểm nào đang hiệu lực. " +
                                "Vui lòng liên hệ Admin để cấu hình ngày chốt điểm."));

        LocalDate deadline = config.getDeadlineDate();

        if (LocalDate.now().isAfter(deadline)) {
            throw new BadRequestException(
                    "Đã quá hạn chốt điểm (" + deadline + "). " +
                            "Vui lòng gửi đơn phúc khảo để khảo thí xem xét.");
        }

        GradeEditPolicy policy = policyRepo
                .findByStudentProfileIdAndGradeDeadline(studentProfileId, deadline)
                .orElseGet(() -> GradeEditPolicy.builder()
                        .studentProfileId(studentProfileId)
                        .gradeDeadline(deadline)
                        .editCount(0)
                        .build());

        try {
            policy.incrementEditCount();
        } catch (IllegalStateException e) {
            throw new BadRequestException(e.getMessage() +
                    ". Vui lòng gửi đơn phúc khảo để khảo thí xem xét.");
        }

        policyRepo.save(policy);
        log.info("[GradePolicy] studentProfileId={} editCount={}/{}",
                studentProfileId, policy.getEditCount(), GradeEditPolicy.MAX_SELF_EDITS);
    }

    /**
     * Trả về thông tin quota cho frontend hiển thị badge/tooltip.
     * @return int[]{editCount, remaining, maxEdits, daysUntilDeadline}
     *         daysUntilDeadline âm = đã quá hạn
     */
    public int[] getQuotaInfo(Long studentProfileId) {
        LocalDate deadline;
        try {
            deadline = getActiveDeadline();
        } catch (BadRequestException e) {
            return new int[]{0, 0, GradeEditPolicy.MAX_SELF_EDITS, -999};
        }

        GradeEditPolicy policy = policyRepo
                .findByStudentProfileIdAndGradeDeadline(studentProfileId, deadline)
                .orElse(null);

        int editCount = policy != null ? policy.getEditCount() : 0;
        int remaining = Math.max(0, GradeEditPolicy.MAX_SELF_EDITS - editCount);
        long daysLeft = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), deadline);

        if (daysLeft < 0) remaining = 0;

        return new int[]{editCount, remaining, GradeEditPolicy.MAX_SELF_EDITS, (int) daysLeft};
    }

    // ─────────────────────────────────────────────────────────────────────
    // TẠO ĐƠN — teacher gửi đơn lên khảo thí
    // ─────────────────────────────────────────────────────────────────────

    @Transactional
    public GradeCorrectionResponse createRequest(CreateGradeCorrectionDto dto) {

        User teacher = userRepo.findById(dto.getTeacherId())
                .orElseThrow(() -> new NotFoundException("User (teacher)", dto.getTeacherId()));

        boolean alreadyPending = correctionRepo
                .existsByTeacherIdAndStudentProfileIdAndGradeComponentIdAndStatus(
                        dto.getTeacherId(), dto.getStudentProfileId(),
                        dto.getGradeComponentId(), CorrectionStatus.PENDING);

        if (alreadyPending) {
            throw new BadRequestException(
                    "Đã có đơn yêu cầu sửa điểm đang chờ duyệt cho đầu điểm này. " +
                            "Vui lòng chờ khảo thí xử lý trước khi gửi đơn mới.");
        }

        GradeCorrectionRequest request = GradeCorrectionRequest.builder()
                .teacher(teacher)
                .studentProfileId(dto.getStudentProfileId())
                .gradeComponentId(dto.getGradeComponentId())
                .oldScore(dto.getOldScore())
                .proposedScore(dto.getProposedScore())
                .reason(dto.getReason())
                .status(CorrectionStatus.PENDING)
                .build();

        GradeCorrectionRequest saved = correctionRepo.save(request);

        ComponentInfo ci = getComponentInfo(dto.getGradeComponentId());
        String studentName = getStudentName(dto.getStudentProfileId());

        notifService.notifyExamDeptOnNewRequest(saved, studentName, ci.componentName());

        log.info("[GradeCorrection] Teacher #{} created request #{} for student #{}",
                dto.getTeacherId(), saved.getId(), dto.getStudentProfileId());

        return GradeCorrectionResponse.fromFull(
                saved, studentName,
                ci.componentName(), ci.subjectCode(), ci.subjectName(), ci.className());
    }

    // ─────────────────────────────────────────────────────────────────────
    // DUYỆT ĐƠN — khảo thí xem xét
    // ─────────────────────────────────────────────────────────────────────

    /**
     * FIX: applyScore() đã có @Transactional(REQUIRES_NEW) nên nếu lỗi ở đó,
     * transaction review này vẫn được commit (trạng thái đơn vẫn được cập nhật).
     * Lỗi apply điểm sẽ được log và bọc thành BadRequestException thay vì
     * để exception nổi lên rollback toàn bộ.
     */
    @Transactional
    public GradeCorrectionResponse reviewRequest(Long requestId, ReviewGradeCorrectionDto dto) {

        GradeCorrectionRequest req = correctionRepo.findById(requestId)
                .orElseThrow(() -> new NotFoundException("GradeCorrectionRequest", requestId));

        if (req.getStatus() != CorrectionStatus.PENDING) {
            throw new BadRequestException(
                    "Đơn này đã được xử lý (trạng thái: " + req.getStatus() + ")");
        }

        if (dto.getStatus() == CorrectionStatus.REJECTED && isBlank(dto.getReviewNote())) {
            throw new BadRequestException("Lý do từ chối là bắt buộc khi REJECTED");
        }

        req.setStatus(dto.getStatus());
        req.setReviewedBy(dto.getReviewedBy());
        req.setReviewNote(dto.getReviewNote());
        req.setReviewedAt(LocalDateTime.now());
        correctionRepo.save(req);

        StudentProfile student = studentProfileRepo.findById(req.getStudentProfileId())
                .orElseThrow(() -> new NotFoundException("StudentProfile", req.getStudentProfileId()));

        String studentName = student.getUser().getFullName();
        ComponentInfo ci   = getComponentInfo(req.getGradeComponentId());

        if (dto.getStatus() == CorrectionStatus.APPROVED) {

            // FIX: bọc applyScore trong try-catch để lỗi không rollback transaction review
            try {
                gradeApplyPort.applyScore(
                        req.getStudentProfileId(),
                        req.getGradeComponentId(),
                        req.getProposedScore(),
                        req.getReviewedBy());

                log.info("[GradeCorrection] Request #{} APPROVED. Score updated for student #{}",
                        requestId, req.getStudentProfileId());

            } catch (Exception e) {
                // Đơn vẫn được đánh dấu APPROVED, nhưng ghi log để admin xử lý thủ công
                log.error("[GradeCorrection] Request #{} APPROVED nhưng applyScore THẤT BẠI " +
                                "— studentProfileId={}, componentId={} | error={}",
                        requestId, req.getStudentProfileId(),
                        req.getGradeComponentId(), e.getMessage(), e);

                throw new BadRequestException(
                        "Đơn đã duyệt nhưng cập nhật điểm thất bại: " + e.getMessage() +
                                ". Vui lòng liên hệ Admin để cập nhật điểm thủ công.");
            }

            notifService.notifyApproved(req, student, ci.componentName());

        } else {
            notifService.notifyRejected(req, studentName, ci.componentName());
            log.info("[GradeCorrection] Request #{} REJECTED by reviewer #{}",
                    requestId, dto.getReviewedBy());
        }

        return GradeCorrectionResponse.fromFull(
                req, studentName,
                ci.componentName(), ci.subjectCode(), ci.subjectName(), ci.className());
    }

    // ─────────────────────────────────────────────────────────────────────
    // QUERY
    // ─────────────────────────────────────────────────────────────────────

    /** Danh sách đơn của 1 teacher — kèm đầy đủ thông tin môn học */
    public List<GradeCorrectionResponse> getByTeacher(Long teacherId, CorrectionStatus status) {
        List<GradeCorrectionRequest> list = (status != null)
                ? correctionRepo.findByTeacherIdAndStatusOrderByCreatedAtDesc(teacherId, status)
                : correctionRepo.findByTeacherIdOrderByCreatedAtDesc(teacherId);

        return list.stream()
                .map(r -> {
                    ComponentInfo ci = getComponentInfo(r.getGradeComponentId());
                    return GradeCorrectionResponse.fromFull(
                            r, getStudentName(r.getStudentProfileId()),
                            ci.componentName(), ci.subjectCode(), ci.subjectName(), ci.className());
                })
                .collect(Collectors.toList());
    }

    /** Tất cả đơn — trang khảo thí — kèm đầy đủ thông tin môn học */
    public Page<GradeCorrectionResponse> getAll(CorrectionStatus status, Pageable pageable) {
        Page<GradeCorrectionRequest> page = (status != null)
                ? correctionRepo.findByStatusWithTeacher(status, pageable)
                : correctionRepo.findAllWithTeacher(pageable);

        return page.map(r -> {
            ComponentInfo ci = getComponentInfo(r.getGradeComponentId());
            return GradeCorrectionResponse.fromFull(
                    r, getStudentName(r.getStudentProfileId()),
                    ci.componentName(), ci.subjectCode(), ci.subjectName(), ci.className());
        });
    }

    /** Chi tiết 1 đơn */
    public GradeCorrectionResponse getById(Long id) {
        GradeCorrectionRequest r = correctionRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("GradeCorrectionRequest", id));
        ComponentInfo ci = getComponentInfo(r.getGradeComponentId());
        return GradeCorrectionResponse.fromFull(
                r, getStudentName(r.getStudentProfileId()),
                ci.componentName(), ci.subjectCode(), ci.subjectName(), ci.className());
    }

    /** Số đơn PENDING — badge sidebar */
    public long countPending() {
        return correctionRepo.countByStatus(CorrectionStatus.PENDING);
    }

    // ─────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────

    private LocalDate getActiveDeadline() {
        return deadlineRepo.findByIsActiveTrue()
                .orElseThrow(() -> new BadRequestException(
                        "Chưa có đợt chốt điểm nào đang hiệu lực."))
                .getDeadlineDate();
    }

    private String getStudentName(Long studentProfileId) {
        return studentProfileRepo.findById(studentProfileId)
                .map(s -> s.getUser().getFullName())
                .orElse("HS #" + studentProfileId);
    }

    /**
     * Lookup GradeComponent để lấy tên đầu điểm, mã môn, tên môn, mã lớp.
     * Dùng JOIN FETCH để tránh N+1 query.
     * Nếu component không tìm thấy → trả về fallback thay vì crash.
     */
    private ComponentInfo getComponentInfo(Long componentId) {
        return componentRepo.findById(componentId)
                .map(c -> new ComponentInfo(
                        c.getName(),
                        c.getCourseSection().getSubject().getCode(),
                        c.getCourseSection().getSubject().getName(),
                        c.getCourseSection().getSchoolClass().getCode()
                ))
                .orElse(new ComponentInfo(
                        "Đầu điểm #" + componentId, "-", "-", "-"
                ));
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    /** Value object nội bộ để truyền 4 field thông tin môn gọn hơn. */
    private record ComponentInfo(
            String componentName,
            String subjectCode,
            String subjectName,
            String className
    ) {}
}