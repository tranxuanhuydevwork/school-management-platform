package com.golearn.myf3school_backend.infrastructure.repository;

import com.golearn.myf3school_backend.infrastructure.entity.OtpCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OtpCodeRepository extends JpaRepository<OtpCode, Long> {

    /** Lấy OTP mới nhất chưa dùng, chưa revoke, chưa hết hạn */
    @Query("""
        SELECT o FROM OtpCode o
        WHERE o.target  = :target
          AND o.used    = false
          AND o.revoked = false
          AND o.expiresAt > CURRENT_TIMESTAMP
        ORDER BY o.createdAt DESC
        LIMIT 1
    """)
    Optional<OtpCode> findLatestValid(@Param("target") String target);

    /** Revoke tất cả OTP cũ của target trước khi tạo mới */
    @Modifying
    @Query("UPDATE OtpCode o SET o.revoked = true WHERE o.target = :target AND o.revoked = false")
    void revokeAll(@Param("target") String target);
}