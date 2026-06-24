package com.golearn.myf3school_backend.application_service.service;

import com.golearn.myf3school_backend.infrastructure.entity.*;
import com.golearn.myf3school_backend.infrastructure.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Gửi thông báo nghỉ học qua 3 kênh:
 *  1. In-app  — lưu vào bảng notifications
 *  2. Email   — qua JavaMailSender (Spring Mail / Gmail SMTP)
 *  3. SMS     — qua Twilio REST API
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final JavaMailSender         mailSender;
    private final TwilioCallService      callService;

    // FIX: Inject username từ config để dùng làm địa chỉ "from"
    // Tránh lỗi bị Gmail reject do from không khớp với tài khoản đăng nhập
    @Value("${spring.mail.username}")
    private String mailFrom;

    /**
     * Gọi sau khi lưu AttendanceRecord có status = ABSENT.
     *
     * @param student  học sinh bị đánh vắng
     * @param session  buổi học tương ứng
     * @param note     ghi chú (có phép / không phép)
     */
    @Async
    public void sendAbsentNotification(StudentProfile student,
                                       AttendanceSession session,
                                       String note) {

        String subject     = session.getCourseSection().getSubject().getName();
        String date        = session.getSessionDate().toString();
        String studentName = student.getUser().getFullName();
        String studentCode = student.getStudentCode();

        String title   = "Thông báo nghỉ học – " + subject;
        String message = String.format(
                "Học sinh %s (%s) đã vắng mặt buổi học môn %s ngày %s.%s",
                studentName, studentCode, subject, date,
                note != null ? " Ghi chú: " + note : ""
        );

        saveInApp(student.getUser(), title, message, "ATTENDANCE", session.getId());

        String email = student.getUser().getEmail();
        if (email != null && !email.isBlank()) {
            sendEmail(email, title, message);
        }

        String phone = student.getEmergencyContactPhone() != null
                ? student.getEmergencyContactPhone()
                : student.getUser().getPhone();

        if (phone != null && !phone.isBlank()) {
            String callMessage = String.format(
                    "[MyF3School] %s (%s) vang mon %s ngay %s.%s",
                    studentName, studentCode, subject, date,
                    note != null ? " GC: " + note : ""
            );
            callService.call(phone, callMessage);
        }
    }


    private void saveInApp(User user, String title, String message,
                           String refType, Long refId) {
        try {
            notificationRepository.save(
                    Notification.builder()
                            .user(user)
                            .title(title)
                            .message(message)
                            .refType(refType)
                            .refId(refId)
                            .build()
            );
        } catch (Exception e) {
            log.error("Failed to save in-app notification for user {}: {}",
                    user != null ? user.getId() : "null", e.getMessage(), e);
        }
    }

    private void sendEmail(String to, String subject, String body) {
        if (to == null || to.isBlank()) {
            log.warn("sendEmail skipped — recipient address is empty");
            return;
        }
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(mailFrom);
            msg.setTo("huytxhe180649@fpt.edu.vn");
            msg.setSubject(subject);
            msg.setText(body);
            mailSender.send(msg);
            log.info("Email sent to {}", to);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage(), e);
        }
    }
}