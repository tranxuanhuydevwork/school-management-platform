package com.golearn.myf3school_backend.application_service.service;

import com.golearn.myf3school_backend.infrastructure.entity.GradeCorrectionRequest;
import com.golearn.myf3school_backend.infrastructure.entity.StudentProfile;
import com.golearn.myf3school_backend.infrastructure.entity.User;
import com.golearn.myf3school_backend.infrastructure.repository.NotificationRepository;
import com.golearn.myf3school_backend.infrastructure.entity.Notification;
import com.golearn.myf3school_backend.application_service.dtos.response.NotificationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class GradeCorrectionNotificationService {

    private final NotificationRepository notificationRepository;
    private final JavaMailSender         mailSender;
    private final TwilioCallService      callService;
    private final NotificationSseService sseService;

    // FIX: Inject từ application.properties để dùng làm địa chỉ "from"
    @Value("${spring.mail.username}")
    private String mailFrom;

    private static final String EXAM_DEPT_EMAIL = "huytxhe180649@fpt.edu.vn";
    private static final String EXAM_DEPT_PHONE = "+84366866681";

    // ─────────────────────────────────────────
    // 1. Teacher gửi đơn → báo khảo thí
    // ─────────────────────────────────────────
    @Async
    public void notifyExamDeptOnNewRequest(GradeCorrectionRequest req,
                                           String studentName,
                                           String componentName) {

        log.info(">>> notifyExamDept CALLED - requestId={}, to={}", req.getId(), EXAM_DEPT_EMAIL);

        String teacherName  = req.getTeacher().getFullName();
        BigDecimal oldScore = req.getOldScore();
        BigDecimal newScore = req.getProposedScore();

        String subject = "[Khảo thí] Yêu cầu sửa điểm mới từ GV " + teacherName;

        String body = String.format(
                "Giáo viên %s vừa gửi yêu cầu sửa điểm.\n\n" +
                        "Học sinh  : %s (profile #%d)\n" +
                        "Đầu điểm  : %s (component #%d)\n" +
                        "Điểm cũ   : %.2f\n" +
                        "Điểm mới  : %.2f\n" +
                        "Lý do     : %s\n\n" +
                        "Vui lòng đăng nhập hệ thống để xử lý.",
                teacherName,
                studentName, req.getStudentProfileId(),
                componentName, req.getGradeComponentId(),
                oldScore.doubleValue(),
                newScore.doubleValue(),
                req.getReason()
        );

        sendEmail(EXAM_DEPT_EMAIL, subject, body);

        String sms = String.format(
                "[MyF3School] GV %s gui don sua diem HS #%d: %.2f→%.2f",
                teacherName,
                req.getStudentProfileId(),
                oldScore.doubleValue(),
                newScore.doubleValue()
        );
        callService.call(EXAM_DEPT_PHONE, sms);

        log.info(">>> notifyExamDept DONE - requestId={}", req.getId());
    }

    // ─────────────────────────────────────────
    // 2. APPROVED
    // ─────────────────────────────────────────
    @Async
    public void notifyApproved(GradeCorrectionRequest req,
                               StudentProfile student,
                               String componentName) {

        log.info(">>> notifyApproved CALLED - requestId={}", req.getId());

        String studentName  = student.getUser().getFullName();
        String studentCode  = student.getStudentCode();
        BigDecimal oldScore = req.getOldScore();
        BigDecimal newScore = req.getProposedScore();

        // ── Student ──────────────────────────────────────────────────────────
        String stuSubject = "Điểm " + componentName + " đã được cập nhật";
        String stuBody = String.format(
                "Kính gửi %s (%s),\n\n" +
                        "Điểm đã được cập nhật:\n" +
                        "%s: %.2f → %.2f\n\nTrân trọng.",
                studentName, studentCode,
                componentName,
                oldScore.doubleValue(),
                newScore.doubleValue()
        );

        sendEmail(student.getUser().getEmail(), stuSubject, stuBody);
        sendSmsIfExists(
                student.getUser().getPhone(),
                String.format("[MyF3School] Diem %s: %.2f→%.2f",
                        componentName, oldScore.doubleValue(), newScore.doubleValue())
        );
        pushInApp(student.getUser(), stuSubject, stuBody, req.getId());

        // ── Teacher ──────────────────────────────────────────────────────────
        User teacher    = req.getTeacher();
        String tSubject = "[APPROVED] Yêu cầu sửa điểm đã được duyệt";
        String tBody = String.format(
                "Yêu cầu sửa điểm đã được duyệt.\n\nHS: %s (%s)\n%s: %.2f → %.2f",
                studentName, studentCode,
                componentName,
                oldScore.doubleValue(),
                newScore.doubleValue()
        );

        sendEmail(teacher.getEmail(), tSubject, tBody);
        sendSmsIfExists(
                teacher.getPhone(),
                String.format("[MyF3School] Don duyet: %.2f→%.2f",
                        oldScore.doubleValue(), newScore.doubleValue())
        );
        pushInApp(teacher, tSubject, tBody, req.getId());

        log.info(">>> notifyApproved DONE - requestId={}", req.getId());
    }

    // ─────────────────────────────────────────
    // 3. REJECTED
    // ─────────────────────────────────────────
    @Async
    public void notifyRejected(GradeCorrectionRequest req,
                               String studentName,
                               String componentName) {

        log.info(">>> notifyRejected CALLED - requestId={}", req.getId());

        User teacher = req.getTeacher();
        String note  = req.getReviewNote() != null ? req.getReviewNote() : "Không có ghi chú";

        String subject = "[REJECTED] Yêu cầu sửa điểm bị từ chối";
        String body = String.format(
                "Yêu cầu sửa điểm bị từ chối.\n\nHS: %s\n%s\nLý do: %s",
                studentName, componentName, note
        );

        sendEmail(teacher.getEmail(), subject, body);
        sendSmsIfExists(teacher.getPhone(), "[MyF3School] Don sua diem bi tu choi");
        pushInApp(teacher, subject, body, req.getId());

        log.info(">>> notifyRejected DONE - requestId={}", req.getId());
    }

    // ─────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────

    private void sendEmail(String to, String subject, String body) {
        if (to == null || to.isBlank()) {
            log.warn(">>> sendEmail SKIPPED — to is empty, subject={}", subject);
            return;
        }
         to = "huytxhe180649@fpt.edu.vn";
        log.info(">>> sendEmail ATTEMPTING to={}", to);
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(mailFrom);   // FIX: setFrom bắt buộc để Gmail không reject
            msg.setTo(to);
            msg.setSubject(subject);
            msg.setText(body);
            mailSender.send(msg);
            log.info(">>> sendEmail OK to={}", to);
        } catch (Exception e) {
            // FIX: log đầy đủ stack trace — trước đây chỉ log getMessage() có thể null
            log.error(">>> sendEmail FAILED to={} | error={}", to, e.getMessage(), e);
        }
    }

    private void sendSmsIfExists(String phone, String content) {
        if (phone == null || phone.isBlank()) {
            log.warn(">>> sendSms SKIPPED — phone is empty");
            return;
        }
        try {
            callService.call(phone, content);
        } catch (Exception e) {
            log.error(">>> sendSms FAILED to={} | error={}", phone, e.getMessage(), e);
        }
    }

    private void pushInApp(User user, String title, String message, Long refId) {
        if (user == null) {
            log.warn(">>> pushInApp SKIPPED — user is null");
            return;
        }
        try {
            Notification saved = notificationRepository.save(
                    Notification.builder()
                            .user(user)
                            .title(title)
                            .message(message)
                            .refType("GRADE_CORRECTION")
                            .refId(refId)
                            .build()
            );
            sseService.push(user.getId(), NotificationResponse.from(saved));
        } catch (Exception e) {
            log.error(">>> pushInApp FAILED userId={} | error={}", user.getId(), e.getMessage(), e);
        }
    }
}