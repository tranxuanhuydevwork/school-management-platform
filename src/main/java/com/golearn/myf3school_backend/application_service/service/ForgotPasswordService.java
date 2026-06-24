package com.golearn.myf3school_backend.application_service.service;

import com.golearn.myf3school_backend.application_service.dtos.request.ResetPasswordRequest;
import com.golearn.myf3school_backend.application_service.dtos.request.SendOtpRequest;
import com.golearn.myf3school_backend.application_service.dtos.request.VerifyOtpRequest;
import com.golearn.myf3school_backend.application_service.exception.BadRequestException;
import com.golearn.myf3school_backend.application_service.exception.NotFoundException;
import com.golearn.myf3school_backend.infrastructure.entity.OtpCode;
import com.golearn.myf3school_backend.infrastructure.entity.User;
import com.golearn.myf3school_backend.infrastructure.repository.OtpCodeRepository;
import com.golearn.myf3school_backend.infrastructure.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.mail.internet.MimeMessage;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Luồng quên mật khẩu:
 *
 *  1) POST /send-otp   → sinh OTP 6 số, lưu DB, gửi qua Email hoặc SMS
 *  2) POST /verify-otp → kiểm tra OTP, trả về resetToken (UUID) nếu đúng
 *  3) POST /reset      → dùng resetToken + newPassword để đổi mật khẩu
 *
 * resetToken được lưu in-memory (ConcurrentHashMap) với TTL 10 phút.
 * Đủ đơn giản cho MVP; nếu cần scale thì chuyển sang Redis.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ForgotPasswordService {

    private final OtpCodeRepository otpRepo;
    private final UserRepository    userRepository;
    private final PasswordEncoder   passwordEncoder;
    private final JavaMailSender    mailSender;

    /** resetToken → (target, expiresAt) — in-memory cache */
    private final Map<String, ResetTokenEntry> resetTokenStore = new ConcurrentHashMap<>();

    private static final int OTP_EXPIRY_MINUTES   = 5;
    private static final int RESET_EXPIRY_MINUTES = 10;

    // ══════════════════════════════════════════════════════════════════════════
    // STEP 1 — SEND OTP
    // ══════════════════════════════════════════════════════════════════════════

    @Transactional
    public void sendOtp(SendOtpRequest request) {
        String target  = request.getTarget().trim();
        String channel = request.getChannel().toUpperCase();

        if (!channel.equals("PHONE") && !channel.equals("EMAIL"))
            throw new BadRequestException("channel phải là PHONE hoặc EMAIL");

        // Kiểm tra target tồn tại trong hệ thống
        User user = resolveUser(target, channel);
        if (user == null)
            throw new NotFoundException("Không tìm thấy tài khoản với thông tin này");

        // Revoke OTP cũ (nếu có)
        otpRepo.revokeAll(target);

        // Sinh OTP 6 số ngẫu nhiên
        String code = generateOtp();

        OtpCode otp = OtpCode.builder()
                .target(target)
                .code(code)
                .channel(channel)
                .expiresAt(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES))
                .used(false)
                .revoked(false)
                .build();
        otpRepo.save(otp);

        // Gửi OTP
        if ("EMAIL".equals(channel)) {
            sendOtpEmail(target, code, user.getFullName());
        } else {
            // Tích hợp SMS provider tại đây (VIETTEL, FPT Telecom, Twilio…)
            // Ví dụ mock: chỉ log — thay bằng HTTP call thực tế
            sendOtpSms(target, code);
        }

        log.info("OTP sent: target={} channel={}", target, channel);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // STEP 2 — VERIFY OTP
    // ══════════════════════════════════════════════════════════════════════════

    @Transactional
    public String verifyOtp(VerifyOtpRequest request) {
        String target = request.getTarget().trim();
        String code   = request.getCode().trim();

        OtpCode otp = otpRepo.findLatestValid(target)
                .orElseThrow(() -> new BadRequestException(
                        "Mã OTP không hợp lệ hoặc đã hết hạn"));

        if (!otp.getCode().equals(code))
            throw new BadRequestException("Mã OTP không đúng");

        // Đánh dấu đã dùng
        otp.setUsed(true);
        otpRepo.save(otp);

        // Tạo resetToken tạm thời
        String resetToken = UUID.randomUUID().toString();
        resetTokenStore.put(resetToken, new ResetTokenEntry(
                target,
                LocalDateTime.now().plusMinutes(RESET_EXPIRY_MINUTES)
        ));

        log.info("OTP verified OK: target={} → resetToken issued", target);
        return resetToken;
    }

    // ══════════════════════════════════════════════════════════════════════════
    // STEP 3 — RESET PASSWORD
    // ══════════════════════════════════════════════════════════════════════════

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        String resetToken = request.getResetToken();

        ResetTokenEntry entry = resetTokenStore.get(resetToken);
        if (entry == null || entry.expiresAt().isBefore(LocalDateTime.now())) {
            resetTokenStore.remove(resetToken);
            throw new BadRequestException("Phiên đặt lại mật khẩu đã hết hạn, vui lòng thử lại");
        }

        // Validate target khớp với token
        if (!entry.target().equalsIgnoreCase(request.getTarget().trim()))
            throw new BadRequestException("Thông tin không hợp lệ");

        // Tìm user và đổi mật khẩu
        String target = entry.target();
        User user;
        if (target.contains("@")) {
            user = (User) userRepository.findByEmail(target)
                    .orElseThrow(() -> new NotFoundException("Không tìm thấy tài khoản"));
        } else {
            user = (User) userRepository.findByPhone(target)
                    .orElseThrow(() -> new NotFoundException("Không tìm thấy tài khoản"));
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Xoá resetToken sau khi dùng
        resetTokenStore.remove(resetToken);
        log.info("Password reset OK: userId={}", user.getId());
    }

    // ══════════════════════════════════════════════════════════════════════════
    // PRIVATE HELPERS
    // ══════════════════════════════════════════════════════════════════════════

    private User resolveUser(String target, String channel) {
        if ("EMAIL".equals(channel)) {
            return (User) userRepository.findByEmail(target).orElse(null);
        } else {
            return (User) userRepository.findByPhone(target).orElse(null);
        }
    }

    private String generateOtp() {
        SecureRandom rng = new SecureRandom();
        int num = 100_000 + rng.nextInt(900_000);
        return String.valueOf(num);
    }

    /**
     * Gửi OTP qua Gmail SMTP.
     * Config trong application.yml:
     *   spring.mail.host=smtp.gmail.com
     *   spring.mail.port=587
     *   spring.mail.username=your@gmail.com
     *   spring.mail.password=<app-password>
     *   spring.mail.properties.mail.smtp.auth=true
     *   spring.mail.properties.mail.smtp.starttls.enable=true
     */
    private void sendOtpEmail(String toEmail, String code, String fullName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("🔑 Mã OTP đặt lại mật khẩu - FPT School");
            helper.setText(buildEmailHtml(fullName, code), true); // true = HTML

            mailSender.send(message);
            log.info("OTP email sent to {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send OTP email to {}: {}", toEmail, e.getMessage());
            throw new RuntimeException("Không thể gửi email OTP. Vui lòng thử lại sau.");
        }
    }

    /**
     * Gửi OTP qua SMS.
     * MVP: chỉ log ra console.
     * Production: tích hợp Twilio / VIETTEL / FPT SMS API.
     */
    private void sendOtpSms(String phone, String code) {
        // TODO: Tích hợp SMS provider thực tế
        // Ví dụ Twilio:
        //   Message.creator(new PhoneNumber(phone),
        //                   new PhoneNumber(twilioFrom),
        //                   "Mã OTP FPT School của bạn là: " + code)
        //          .create();
        log.info("[SMS-MOCK] Gửi OTP {} đến số điện thoại {}", code, phone);
        // Trong môi trường dev, bạn có thể xem OTP trong log server
    }

    /** HTML email template */
    private String buildEmailHtml(String fullName, String code) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
              <meta charset="UTF-8">
              <style>
                body { font-family: Arial, sans-serif; background: #f4f4f4; margin: 0; padding: 0; }
                .container { max-width: 500px; margin: 40px auto; background: #fff;
                             border-radius: 12px; overflow: hidden; box-shadow: 0 4px 12px rgba(0,0,0,.1); }
                .header { background: linear-gradient(135deg, #FF6F00, #0D47A1);
                          padding: 32px; text-align: center; }
                .header h1 { color: #fff; margin: 0; font-size: 22px; }
                .body { padding: 32px; }
                .otp-box { background: #FFF3E0; border: 2px dashed #FF6F00;
                           border-radius: 8px; text-align: center; padding: 20px; margin: 24px 0; }
                .otp-code { font-size: 40px; font-weight: 900; letter-spacing: 12px;
                            color: #FF6F00; margin: 0; }
                .note { color: #666; font-size: 13px; line-height: 1.6; }
                .footer { background: #f4f4f4; padding: 16px; text-align: center;
                          font-size: 12px; color: #999; }
              </style>
            </head>
            <body>
              <div class="container">
                <div class="header">
                  <h1>🎓 FPT University Academic Portal</h1>
                </div>
                <div class="body">
                  <p>Xin chào <strong>%s</strong>,</p>
                  <p>Chúng tôi nhận được yêu cầu đặt lại mật khẩu cho tài khoản của bạn.</p>
                  <div class="otp-box">
                    <p style="margin:0 0 8px 0; color:#555; font-size:14px;">Mã OTP của bạn</p>
                    <p class="otp-code">%s</p>
                  </div>
                  <p class="note">
                    ⏱ Mã có hiệu lực trong <strong>5 phút</strong>.<br>
                    🔒 Không chia sẻ mã này với bất kỳ ai.<br>
                    Nếu bạn không yêu cầu đặt lại mật khẩu, hãy bỏ qua email này.
                  </p>
                </div>
                <div class="footer">© 2026 FPT University. All rights reserved.</div>
              </div>
            </body>
            </html>
            """.formatted(fullName != null ? fullName : "bạn", code);
    }

    // ── Inner record để lưu resetToken ────────────────────────────────────
    private record ResetTokenEntry(String target, LocalDateTime expiresAt) {}
}