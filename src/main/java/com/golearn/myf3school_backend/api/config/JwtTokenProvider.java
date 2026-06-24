package com.golearn.myf3school_backend.api.config;

import com.golearn.myf3school_backend.contract.enums.RoleType;
import com.golearn.myf3school_backend.infrastructure.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtTokenProvider {

    private final SecretKey key;
    private final long      accessTokenExpiryMs;

    public JwtTokenProvider(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.access-token-expiry-ms:900000}") long expiryMs) {

        this.key                 = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpiryMs = expiryMs;
    }

    // ── Generate ──────────────────────────────────────────────────────────────

    /**
     * Tạo JWT với claims:
     *   sub       = userId
     *   username  = username
     *   roles     = "STUDENT,PARENT" (csv)
     *   studentId = StudentProfile.id (null nếu không phải STUDENT/PARENT)
     */
    public String generateAccessToken(User user, Set<RoleType> roles) {
        return generateAccessToken(user, roles, null);
    }

    /**
     * Overload có studentId — dùng trong AuthService sau khi resolve StudentProfile.
     */
    public String generateAccessToken(User user, Set<RoleType> roles, Long studentId) {
        Date now    = new Date();
        Date expiry = new Date(now.getTime() + accessTokenExpiryMs);

        JwtBuilder builder = Jwts.builder()
                .subject(String.valueOf(user.getId()))
                .claim("username", user.getUsername())
                .claim("roles", roles.stream()
                        .map(RoleType::name)
                        .collect(Collectors.joining(",")))
                .issuedAt(now)
                .expiration(expiry);

        // Embed studentId nếu có (STUDENT hoặc PARENT cần xem CLB, điểm danh...)
        if (studentId != null) {
            builder.claim("studentId", studentId);
        }

        return builder.signWith(key, Jwts.SIG.HS512).compact();
    }

    // ── Validate ──────────────────────────────────────────────────────────────

    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Invalid JWT: {}", e.getMessage());
            return false;
        }
    }

    // ── Extract claims ────────────────────────────────────────────────────────

    /** userId (sub) */
    public Long getUserIdFromToken(String token) {
        return Long.parseLong(parseClaims(token).getSubject());
    }

    /** studentId claim — null nếu không embed (role không phải STUDENT/PARENT) */
    public Long getStudentIdFromToken(String token) {
        Object val = parseClaims(token).get("studentId");
        if (val == null) return null;
        // jsonwebtoken deserializes numbers as Integer nếu nhỏ, Long nếu lớn
        if (val instanceof Long)    return (Long) val;
        if (val instanceof Integer) return ((Integer) val).longValue();
        return Long.parseLong(val.toString());
    }

    public Set<RoleType> getRolesFromToken(String token) {
        String rolesStr = parseClaims(token).get("roles", String.class);
        return Arrays.stream(rolesStr.split(","))
                .map(RoleType::valueOf)
                .collect(Collectors.toSet());
    }

    // ── Private ───────────────────────────────────────────────────────────────

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}