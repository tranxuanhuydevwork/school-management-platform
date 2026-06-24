package com.golearn.myf3school_backend.infrastructure.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Lưu mã OTP để đặt lại mật khẩu.
 * Mỗi lần gửi OTP mới sẽ tạo một bản ghi mới (cũ bị revoke).
 */
@Entity
@Table(name = "otp_codes")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class OtpCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Phone hoặc email — tuỳ channel người dùng chọn */
    @Column(nullable = false)
    private String target;          // e.g. "0901234567" hoặc "abc@fpt.edu.vn"

    @Column(nullable = false, length = 6)
    private String code;            // 6 chữ số

    @Column(nullable = false)
    private String channel;         // "PHONE" | "EMAIL"

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private boolean used;

    @Column(nullable = false)
    private boolean revoked;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}