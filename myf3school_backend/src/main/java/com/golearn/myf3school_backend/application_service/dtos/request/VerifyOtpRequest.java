package com.golearn.myf3school_backend.application_service.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * POST /api/auth/forgot-password/verify-otp
 * Body: { "target": "0901234567", "code": "123456" }
 */
@Data
public class VerifyOtpRequest {

    /** Số điện thoại hoặc email — phải khớp với target đã gửi OTP */
    @NotBlank(message = "target không được để trống")
    private String target;

    /** Mã OTP 6 chữ số */
    @NotBlank(message = "code không được để trống")
    @Size(min = 6, max = 6, message = "Mã OTP phải đúng 6 chữ số")
    @Pattern(regexp = "\\d{6}", message = "Mã OTP chỉ gồm chữ số")
    private String code;
}