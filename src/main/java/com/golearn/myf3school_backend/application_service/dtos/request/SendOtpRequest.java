// ─────────────────────────────────────────────────────────────────
// File: SendOtpRequest.java
// ─────────────────────────────────────────────────────────────────
package com.golearn.myf3school_backend.application_service.dtos.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * POST /api/auth/forgot-password/send-otp
 *
 * channel = "PHONE"  → target = số điện thoại (0901234567)
 * channel = "EMAIL"  → target = địa chỉ email  (abc@fpt.edu.vn)
 */
@Data
public class SendOtpRequest {

    @NotBlank(message = "target không được để trống")
    private String target;   // phone hoặc email

    @NotBlank(message = "channel không được để trống")
    private String channel;  // "PHONE" | "EMAIL"
}