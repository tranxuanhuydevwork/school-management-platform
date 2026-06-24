package com.golearn.myf3school_backend.application_service.service;

import com.golearn.myf3school_backend.api.config.JwtTokenProvider;
import com.golearn.myf3school_backend.application_service.dtos.request.LoginRequest;
import com.golearn.myf3school_backend.application_service.dtos.response.LoginResponse;
import com.golearn.myf3school_backend.application_service.exception.BadRequestException;
import com.golearn.myf3school_backend.application_service.exception.UnauthorizedException;
import com.golearn.myf3school_backend.contract.enums.RoleType;
import com.golearn.myf3school_backend.infrastructure.entity.ParentStudent;
import com.golearn.myf3school_backend.infrastructure.entity.RefreshToken;
import com.golearn.myf3school_backend.infrastructure.entity.StudentProfile;
import com.golearn.myf3school_backend.infrastructure.entity.User;
import com.golearn.myf3school_backend.infrastructure.repository.ParentStudentRepository;
import com.golearn.myf3school_backend.infrastructure.repository.RefreshTokenRepository;
import com.golearn.myf3school_backend.infrastructure.repository.StudentProfileRepository;
import com.golearn.myf3school_backend.infrastructure.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository           userRepository;
    private final StudentProfileRepository studentRepository;
    private final RefreshTokenRepository   refreshTokenRepository;
    private final ParentStudentRepository  parentStudentRepository;
    private final PasswordEncoder          passwordEncoder;
    private final JwtTokenProvider         jwtProvider;

    @Value("${app.jwt.refresh-token-expiry-days:30}")
    private int refreshTokenExpiryDays;

    // ══════════════════════════════════════════════════════════════════════════
    // LOGIN
    // ══════════════════════════════════════════════════════════════════════════

    @Transactional
    public LoginResponse login(LoginRequest request, String ip, String userAgent) {
        log.info(">>> LOGIN REQUEST: phone='{}', ip='{}'", request.getPhoneNumber(), ip);

        User user = (User) userRepository.findByPhone(request.getPhoneNumber())
                .orElseThrow(() -> new BadRequestException(
                        "Số điện thoại hoặc mật khẩu không đúng"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword()))
            throw new BadRequestException("Số điện thoại hoặc mật khẩu không đúng");

        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        Set<RoleType> roles = user.getRoles().stream()
                .map(r -> r.getName())
                .collect(Collectors.toSet());

        // ── BƯỚC 1: resolve studentId + classId TRƯỚC ────────────────────────
        Long classId   = null;
        Long studentId = null;

        if (roles.contains(RoleType.STUDENT)) {
            StudentProfile profile =
                    studentRepository.findByUserId(user.getId()).orElse(null);
            if (profile != null) {
                studentId = profile.getId();
                if (profile.getSchoolClass() != null)
                    classId = profile.getSchoolClass().getId();
            }
        } else if (roles.contains(RoleType.PARENT)) {
            List<ParentStudent> children =
                    parentStudentRepository.findByParentId(user.getId());
            if (!children.isEmpty()) {
                StudentProfile firstChild = children.get(0).getStudent();
                studentId = firstChild.getId();
                if (firstChild.getSchoolClass() != null)
                    classId = firstChild.getSchoolClass().getId();
            }
        }

        // ── BƯỚC 2: generate token SAU khi đã có studentId ───────────────────
        // studentId được embed vào JWT claim để ClubController / các service
        // khác dùng jwtProvider.getStudentIdFromToken() mà không cần query DB.
        String accessToken  = jwtProvider.generateAccessToken(user, roles, studentId);
        String refreshToken = createRefreshToken(user, ip, userAgent);

        log.info(">>> Login OK: userId={} roles={} studentId={}", user.getId(), roles, studentId);

        return LoginResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .roles(roles)
                .avatarUrl(user.getAvatarUrl())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .classId(classId)
                .studentId(studentId)
                .build();
    }

    // ══════════════════════════════════════════════════════════════════════════
    // REFRESH
    // ══════════════════════════════════════════════════════════════════════════

    @Transactional
    public LoginResponse refresh(String rawToken) {
        RefreshToken stored = refreshTokenRepository.findByToken(rawToken)
                .orElseThrow(() -> new UnauthorizedException("Refresh token không hợp lệ"));

        if (Boolean.TRUE.equals(stored.getIsRevoked()))
            throw new UnauthorizedException("Refresh token đã bị thu hồi");
        if (stored.getExpiresAt().isBefore(LocalDateTime.now()))
            throw new UnauthorizedException("Refresh token đã hết hạn");

        stored.setIsRevoked(true);
        stored.setRevokedAt(LocalDateTime.now());
        refreshTokenRepository.save(stored);

        User user = stored.getUser();
        Set<RoleType> roles = user.getRoles().stream()
                .map(r -> r.getName())
                .collect(Collectors.toSet());

        // ── BƯỚC 1: resolve studentId + classId TRƯỚC ────────────────────────
        Long classId   = null;
        Long studentId = null;

        if (roles.contains(RoleType.STUDENT)) {
            StudentProfile p =
                    studentRepository.findByUserId(user.getId()).orElse(null);
            if (p != null) {
                studentId = p.getId();
                if (p.getSchoolClass() != null)
                    classId = p.getSchoolClass().getId();
            }
        } else if (roles.contains(RoleType.PARENT)) {
            List<ParentStudent> children =
                    parentStudentRepository.findByParentId(user.getId());
            if (!children.isEmpty()) {
                StudentProfile firstChild = children.get(0).getStudent();
                studentId = firstChild.getId();
                if (firstChild.getSchoolClass() != null)
                    classId = firstChild.getSchoolClass().getId();
            }
        }

        // ── BƯỚC 2: generate token SAU khi đã có studentId ───────────────────
        String newAccessToken  = jwtProvider.generateAccessToken(user, roles, studentId);
        String newRefreshToken = createRefreshToken(
                user, stored.getIpAddress(), stored.getDeviceInfo());

        return LoginResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .roles(roles)
                .avatarUrl(user.getAvatarUrl())
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .classId(classId)
                .studentId(studentId)
                .build();
    }

    // ══════════════════════════════════════════════════════════════════════════
    // LOGOUT
    // ══════════════════════════════════════════════════════════════════════════

    @Transactional
    public void logout(String rawToken) {
        refreshTokenRepository.findByToken(rawToken).ifPresent(t -> {
            t.setIsRevoked(true);
            t.setRevokedAt(LocalDateTime.now());
            refreshTokenRepository.save(t);
        });
    }

    @Transactional
    public void logoutAll(String accessToken) {
        Long userId = jwtProvider.getUserIdFromToken(accessToken);
        refreshTokenRepository.revokeAllByUserId(userId);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // PRIVATE
    // ══════════════════════════════════════════════════════════════════════════

    private String createRefreshToken(User user, String ip, String deviceInfo) {
        String token = UUID.randomUUID().toString();
        refreshTokenRepository.save(RefreshToken.builder()
                .user(user)
                .token(token)
                .ipAddress(ip)
                .deviceInfo(deviceInfo)
                .expiresAt(LocalDateTime.now().plusDays(refreshTokenExpiryDays))
                .build());
        return token;
    }
}