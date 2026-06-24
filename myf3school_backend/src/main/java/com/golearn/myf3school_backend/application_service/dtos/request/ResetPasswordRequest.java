package com.golearn.myf3school_backend.application_service.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * POST /api/auth/forgot-password/reset
 * Body: {
 *   "target":      "0901234567",          // phone hoặc email
 *   "resetToken":  "uuid-từ-verify-otp",  // token nhận được sau verify OTP
 *   "newPassword": "matkhaumoi123"
 * }
 */
@Data
public class ResetPasswordRequest {

    /** Phone hoặc email — dùng để xác định user cần đổi mật khẩu */
    @NotBlank(message = "target không được để trống")
    private String target;

    /** Token tạm thời trả về từ /verify-otp, TTL 10 phút */
    @NotBlank(message = "resetToken không được để trống")
    private String resetToken;

    /** Mật khẩu mới — tối thiểu 6 ký tự */
    @NotBlank(message = "newPassword không được để trống")
    @Size(min = 6, message = "Mật khẩu phải có ít nhất 6 ký tự")
    private String newPassword;
}