package com.golearn.myf3school_backend.infrastructure.repository;

import com.golearn.myf3school_backend.infrastructure.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    List<RefreshToken> findByUserId(Long userId);

    @Modifying
    @Query("UPDATE RefreshToken t SET t.isRevoked = true, t.revokedAt = NOW() " +
           "WHERE t.user.id = :userId AND t.isRevoked = false")
    void revokeAllByUserId(@Param("userId") Long userId);
}