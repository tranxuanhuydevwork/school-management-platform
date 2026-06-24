package com.golearn.myf3school_backend.application_service.service;

import com.golearn.myf3school_backend.application_service.dtos.response.UserProfileResponse;
import com.golearn.myf3school_backend.contract.enums.RoleType;
import com.golearn.myf3school_backend.infrastructure.entity.ParentStudent;
import com.golearn.myf3school_backend.infrastructure.entity.StudentProfile;
import com.golearn.myf3school_backend.infrastructure.entity.User;
import com.golearn.myf3school_backend.infrastructure.repository.ParentStudentRepository;
import com.golearn.myf3school_backend.infrastructure.repository.StudentProfileRepository;
import com.golearn.myf3school_backend.infrastructure.repository.UserRepository;
import com.golearn.myf3school_backend.application_service.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserRepository           userRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final ParentStudentRepository  parentStudentRepository;

    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("Không tìm thấy người dùng"));

        Set<RoleType> roles = user.getRoles().stream()
                .map(r -> r.getName())
                .collect(Collectors.toSet());

        UserProfileResponse.UserProfileResponseBuilder builder = UserProfileResponse.builder()
                .userId(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .avatarUrl(user.getAvatarUrl())
                .address(user.getAddress())
                .dateOfBirth(user.getDateOfBirth())
                .roles(roles);

        // ── STUDENT ──────────────────────────────────────────────────────────
        if (roles.contains(RoleType.STUDENT)) {
            studentProfileRepository.findByUserId(userId).ifPresent(profile -> {
                builder
                    .studentCode(profile.getStudentCode())
                    .className(profile.getSchoolClass() != null
                            ? profile.getSchoolClass().getName() : null)
                    .gpa(profile.getGpa())
                    .academicRank(profile.getAcademicRank())
                    .enrollmentDate(profile.getEnrollmentDate())
                    .emergencyContactName(profile.getEmergencyContactName())
                    .emergencyContactPhone(profile.getEmergencyContactPhone())
                    .semester(calculateSemester(profile.getEnrollmentDate()));
            });
        }

        // ── PARENT ───────────────────────────────────────────────────────────
        if (roles.contains(RoleType.PARENT)) {
            List<ParentStudent> links = parentStudentRepository.findByParentId(userId);

            List<UserProfileResponse.ChildInfo> children = links.stream().map(link -> {
                StudentProfile child = link.getStudent();
                return UserProfileResponse.ChildInfo.builder()
                        .studentProfileId(child.getId())
                        .fullName(child.getUser().getFullName())
                        .studentCode(child.getStudentCode())
                        .className(child.getSchoolClass() != null
                                ? child.getSchoolClass().getName() : null)
                        .relationship(link.getRelationship() != null
                                ? link.getRelationship().name() : "GUARDIAN")
                        .gpa(child.getGpa())
                        .build();
            }).collect(Collectors.toList());

            builder.children(children);
        }

        return builder.build();
    }

    /**
     * Tính học kỳ hiện tại dựa trên ngày nhập học.
     * Mỗi học kỳ ≈ 6 tháng → semester = (số tháng / 6) + 1, tối đa 9.
     */
    private String calculateSemester(LocalDate enrollmentDate) {
        if (enrollmentDate == null) return null;
        int months = Period.between(enrollmentDate, LocalDate.now()).getMonths()
                   + Period.between(enrollmentDate, LocalDate.now()).getYears() * 12;
        int sem = Math.min((months / 6) + 1, 9);
        return "Học kỳ " + sem;
    }
}