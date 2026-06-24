package com.golearn.myf3school_backend.controller;

import com.golearn.myf3school_backend.application_service.dtos.request.LoginRequest;
import com.golearn.myf3school_backend.application_service.dtos.request.RefreshTokenRequest;
import com.golearn.myf3school_backend.application_service.dtos.request.ResetPasswordRequest;
import com.golearn.myf3school_backend.application_service.dtos.request.SendOtpRequest;
import com.golearn.myf3school_backend.application_service.dtos.request.VerifyOtpRequest;
import com.golearn.myf3school_backend.application_service.dtos.response.ApiResponse;
import com.golearn.myf3school_backend.application_service.dtos.response.LoginResponse;
import com.golearn.myf3school_backend.application_service.exception.BadRequestException;
import com.golearn.myf3school_backend.application_service.service.AuthService;
import com.golearn.myf3school_backend.application_service.service.ForgotPasswordService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService            authService;
    private final ForgotPasswordService  forgotPasswordService;

    // ══════════════════════════════════════════════════════════════════════════
    // LOGIN / REFRESH / LOGOUT
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * POST /api/auth/login
     * Body: { "phoneNumber": "...", "password": "..." }
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {

        String ip        = httpRequest.getRemoteAddr();
        String userAgent = httpRequest.getHeader("User-Agent");

        LoginResponse response = authService.login(request, ip, userAgent);
        log.info("Login OK: user={} ip={}", request.getPhoneNumber(), ip);
        return ResponseEntity.ok(ApiResponse.ok("Đăng nhập thành công", response));
    }

    /**
     * POST /api/auth/refresh
     * Body: { "refreshToken": "uuid..." }
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<LoginResponse>> refresh(
            @RequestBody RefreshTokenRequest request) {

        if (request.getRefreshToken() == null || request.getRefreshToken().isBlank())
            throw new BadRequestException("refreshToken không được để trống");

        LoginResponse response = authService.refresh(request.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.ok("Token đã được làm mới", response));
    }

    /**
     * POST /api/auth/logout
     * Body: { "refreshToken": "uuid..." }
     * Revokes only the token for the current device.
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestBody RefreshTokenRequest request) {

        if (request.getRefreshToken() != null && !request.getRefreshToken().isBlank())
            authService.logout(request.getRefreshToken());

        return ResponseEntity.ok(ApiResponse.ok("Đăng xuất thành công", null));
    }

    /**
     * POST /api/auth/logout-all
     * Header: Authorization: Bearer <accessToken>
     * Revokes every refresh token for the authenticated user.
     */
    @PostMapping("/logout-all")
    public ResponseEntity<ApiResponse<Void>> logoutAll(
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.replace("Bearer ", "").trim();
        authService.logoutAll(token);
        return ResponseEntity.ok(ApiResponse.ok("Đã đăng xuất khỏi tất cả thiết bị", null));
    }

    // ══════════════════════════════════════════════════════════════════════════
    // FORGOT PASSWORD — 3 bước
    // Tất cả PUBLIC (không cần JWT).
    // SecurityConfig: .requestMatchers("/api/auth/forgot-password/**").permitAll()
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * BƯỚC 1 — Gửi OTP
     *
     * POST /api/auth/forgot-password/send-otp
     * Body: { "target": "0901234567",      "channel": "PHONE" }
     *    or { "target": "abc@fpt.edu.vn",  "channel": "EMAIL" }
     *
     * Response: { "success": true, "message": "Mã OTP đã được gửi đến ..." }
     */
    @PostMapping("/forgot-password/send-otp")
    public ResponseEntity<ApiResponse<Void>> sendOtp(
            @Valid @RequestBody SendOtpRequest request) {

        forgotPasswordService.sendOtp(request);

        String msg = "EMAIL".equalsIgnoreCase(request.getChannel())
                ? "Mã OTP đã được gửi đến email " + maskEmail(request.getTarget())
                : "Mã OTP đã được gửi đến số "    + maskPhone(request.getTarget());

        return ResponseEntity.ok(ApiResponse.ok(msg, null));
    }

    /**
     * BƯỚC 2 — Xác thực OTP
     *
     * POST /api/auth/forgot-password/verify-otp
     * Body: { "target": "0901234567", "code": "123456" }
     *
     * Response: { "success": true, "data": { "resetToken": "uuid..." } }
     * resetToken có hiệu lực 10 phút, dùng 1 lần cho bước reset.
     */
    @PostMapping("/forgot-password/verify-otp")
    public ResponseEntity<ApiResponse<Map<String, String>>> verifyOtp(
            @Valid @RequestBody VerifyOtpRequest request) {

        String resetToken = forgotPasswordService.verifyOtp(request);
        return ResponseEntity.ok(ApiResponse.ok(
                "Xác thực OTP thành công",
                Map.of("resetToken", resetToken)
        ));
    }

    /**
     * BƯỚC 3 — Đặt lại mật khẩu
     *
     * POST /api/auth/forgot-password/reset
     * Body: {
     *   "target":      "0901234567",
     *   "resetToken":  "uuid-từ-bước-2",
     *   "newPassword": "matkhaumoi123"
     * }
     *
     * Response: { "success": true, "message": "Mật khẩu đã được đặt lại thành công" }
     */
    @PostMapping("/forgot-password/reset")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {

        forgotPasswordService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.ok("Mật khẩu đã được đặt lại thành công", null));
    }

    // ══════════════════════════════════════════════════════════════════════════
    // PRIVATE HELPERS
    // ══════════════════════════════════════════════════════════════════════════

    /** abc@fpt.edu.vn  →  a***@fpt.edu.vn */
    private String maskEmail(String email) {
        int at = email.indexOf('@');
        if (at <= 2) return email;
        return email.charAt(0) + "***" + email.substring(at - 1);
    }

    /** 0901234567  →  090****567 */
    private String maskPhone(String phone) {
        if (phone.length() < 4) return phone;
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 3);
    }
}