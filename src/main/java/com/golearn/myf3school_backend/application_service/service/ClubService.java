package com.golearn.myf3school_backend.application_service.service;

import com.golearn.myf3school_backend.application_service.dtos.request.ClubRequest;
import com.golearn.myf3school_backend.application_service.dtos.response.ClubResponse;
import com.golearn.myf3school_backend.application_service.exception.BadRequestException;
import com.golearn.myf3school_backend.application_service.exception.ForbiddenException;
import com.golearn.myf3school_backend.application_service.exception.NotFoundException;
import com.golearn.myf3school_backend.contract.enums.*;
import com.golearn.myf3school_backend.infrastructure.entity.*;
import com.golearn.myf3school_backend.infrastructure.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClubService {

    private final ClubRepository                  clubRepository;
    private final ClubMemberRepository            memberRepository;
    private final ClubActivityRepository          activityRepository;
    private final ClubActivityAttendanceRepository attendanceRepository;
    private final StudentProfileRepository        studentRepository;
    private final UserRepository                  userRepository;

    // ══════════════════════════════════════════════════════════════════════════
    // CLUBS
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Danh sách CLB đang ACTIVE.
     * studentId nullable — nếu có sẽ trả thêm myStatus / myRole / canJoin.
     */
    @Transactional(readOnly = true)
    public List<ClubResponse.ClubSummary> getClubs(String search, Long studentId) {
        List<Club> clubs = clubRepository.search(
                (search == null || search.isBlank()) ? null : search);

        return clubs.stream()
                .map(c -> toSummary(c, studentId))
                .collect(Collectors.toList());
    }

    /** CLB mà học sinh đang là thành viên (ACTIVE) hoặc đang chờ duyệt (PENDING) */
    @Transactional(readOnly = true)
    public List<ClubResponse.ClubSummary> getMyClubs(Long studentId) {
        return memberRepository.findByStudentId(studentId).stream()
                .map(cm -> toSummary(cm.getClub(), studentId))
                .collect(Collectors.toList());
    }

    /** Tạo CLB mới (admin / giáo viên) */
    @Transactional
    public ClubResponse.ClubSummary createClub(ClubRequest.CreateClub req) {
        if (clubRepository.findByCode(req.getCode()).isPresent())
            throw new BadRequestException("Mã CLB " + req.getCode() + " đã tồn tại");

        User advisor = req.getAdvisorId() != null
                ? userRepository.findById(req.getAdvisorId()).orElse(null) : null;

        StudentProfile president = req.getPresidentStudentId() != null
                ? studentRepository.findById(req.getPresidentStudentId()).orElse(null) : null;

        Club club = Club.builder()
                .code(req.getCode())
                .name(req.getName())
                .description(req.getDescription())
                .logoUrl(req.getLogoUrl())
                .meetingLocation(req.getMeetingLocation())
                .maxMembers(req.getMaxMembers() != null ? req.getMaxMembers() : 50)
                .advisor(advisor)
                .president(president)
                .foundedDate(req.getFoundedDate() != null
                        ? LocalDate.parse(req.getFoundedDate()) : null)
                .build();

        return toSummary(clubRepository.save(club), null);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // MEMBERSHIP
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Học sinh đăng ký tham gia CLB → tạo ClubMember với status=PENDING.
     * Nghiệp vụ: CLB phải ACTIVE, còn slot (memberCount < maxMembers).
     */
    @Transactional
    public ClubResponse.MemberResponse joinClub(Long clubId, Long studentId) {
        Club club = getClubOrThrow(clubId);

        if (club.getStatus() != ClubStatus.ACTIVE)
            throw new BadRequestException("CLB không còn hoạt động");

        // Kiểm tra slot
        long activeCount = memberRepository.countByClubIdAndStatus(
                clubId, ClubMemberStatus.ACTIVE);
        if (club.getMaxMembers() != null && activeCount >= club.getMaxMembers())
            throw new BadRequestException("CLB đã đủ số lượng thành viên tối đa");

        StudentProfile student = getStudentOrThrow(studentId);

        Optional<ClubMember> existing =
                memberRepository.findByClubIdAndStudentId(clubId, studentId);

        if (existing.isPresent()) {
            ClubMember cm = existing.get();
            switch (cm.getStatus()) {
                case ACTIVE   -> throw new BadRequestException("Bạn đã là thành viên của CLB này");
                case PENDING  -> throw new BadRequestException("Đơn đăng ký đang chờ duyệt");
                case EXPELLED -> throw new BadRequestException("Bạn đã bị khai trừ khỏi CLB này");
                case LEFT -> {
                    // Cho phép đăng ký lại
                    cm.setStatus(ClubMemberStatus.PENDING);
                    cm.setLeaveDate(null);
                    cm.setLeaveReason(null);
                    return toMemberResponse(memberRepository.save(cm));
                }
            }
        }

        ClubMember member = ClubMember.builder()
                .club(club)
                .student(student)
                .clubRole(ClubMemberRole.MEMBER)
                .status(ClubMemberStatus.PENDING)
                .build();

        log.info("Student {} applied to club {}", studentId, clubId);
        return toMemberResponse(memberRepository.save(member));
    }

    /** Học sinh tự rời CLB */
    @Transactional
    public void leaveClub(Long clubId, Long studentId, String reason) {
        ClubMember member = memberRepository
                .findByClubIdAndStudentId(clubId, studentId)
                .orElseThrow(() -> new NotFoundException("ClubMember", 0L));

        if (member.getStatus() == ClubMemberStatus.LEFT)
            throw new BadRequestException("Bạn đã rời CLB này rồi");

        member.setStatus(ClubMemberStatus.LEFT);
        member.setLeaveDate(LocalDate.now());
        member.setLeaveReason(reason);
        memberRepository.save(member);
        log.info("Student {} left club {}", studentId, clubId);
    }

    /** Leader xem danh sách thành viên CLB */
    @Transactional(readOnly = true)
    public List<ClubResponse.MemberResponse> getMembers(Long clubId, Long requesterId) {
        assertLeader(clubId, requesterId);
        return memberRepository.findByClubId(clubId).stream()
                .map(this::toMemberResponse)
                .collect(Collectors.toList());
    }

    /** Leader duyệt thành viên PENDING */
    @Transactional
    public ClubResponse.MemberResponse approveMember(Long memberId, Long requesterId) {
        ClubMember cm = getMemberOrThrow(memberId);
        assertLeader(cm.getClub().getId(), requesterId);

        if (cm.getStatus() != ClubMemberStatus.PENDING)
            throw new BadRequestException("Thành viên không ở trạng thái chờ duyệt");

        cm.setStatus(ClubMemberStatus.ACTIVE);
        cm.setJoinDate(LocalDate.now());
        log.info("Member {} approved by {}", memberId, requesterId);
        return toMemberResponse(memberRepository.save(cm));
    }

    /** Leader từ chối đơn PENDING */
    @Transactional
    public void rejectMember(Long memberId, Long requesterId) {
        ClubMember cm = getMemberOrThrow(memberId);
        assertLeader(cm.getClub().getId(), requesterId);

        if (cm.getStatus() != ClubMemberStatus.PENDING)
            throw new BadRequestException("Thành viên không ở trạng thái chờ duyệt");

        memberRepository.delete(cm);
        log.info("Member {} rejected by {}", memberId, requesterId);
    }

    /** Leader khai trừ thành viên ACTIVE */
    @Transactional
    public void expelMember(Long memberId, Long requesterId, String reason) {
        ClubMember cm = getMemberOrThrow(memberId);
        assertLeader(cm.getClub().getId(), requesterId);

        if (cm.getStatus() != ClubMemberStatus.ACTIVE)
            throw new BadRequestException("Chỉ khai trừ thành viên đang active");

        cm.setStatus(ClubMemberStatus.EXPELLED);
        cm.setLeaveDate(LocalDate.now());
        cm.setLeaveReason(reason);
        memberRepository.save(cm);
        log.info("Member {} expelled by {}", memberId, requesterId);
    }

    /** Leader đổi role thành viên */
    @Transactional
    public ClubResponse.MemberResponse changeRole(Long memberId,
                                                   Long requesterId,
                                                   ClubMemberRole newRole) {
        ClubMember cm = getMemberOrThrow(memberId);
        assertLeader(cm.getClub().getId(), requesterId);

        if (cm.getStatus() != ClubMemberStatus.ACTIVE)
            throw new BadRequestException("Chỉ đổi role cho thành viên đang active");

        cm.setClubRole(newRole);
        return toMemberResponse(memberRepository.save(cm));
    }

    // ══════════════════════════════════════════════════════════════════════════
    // ACTIVITIES
    // ══════════════════════════════════════════════════════════════════════════

    /** Tất cả sự kiện của CLB đang ACTIVE — toàn trường xem được */
    @Transactional(readOnly = true)
    public List<ClubResponse.ActivityResponse> getAllActivities() {
        return activityRepository.findAllActiveClubActivities().stream()
                .map(this::toActivityResponse)
                .collect(Collectors.toList());
    }

    /** Sự kiện của 1 CLB cụ thể */
    @Transactional(readOnly = true)
    public List<ClubResponse.ActivityResponse> getClubActivities(Long clubId) {
        return activityRepository.findByClubIdOrderByStartTimeDesc(clubId).stream()
                .map(this::toActivityResponse)
                .collect(Collectors.toList());
    }

    /** Leader tạo sự kiện */
    @Transactional
    public ClubResponse.ActivityResponse createActivity(Long clubId,
                                                         Long requesterId,
                                                         ClubRequest.CreateActivity req) {
        Club club = getClubOrThrow(clubId);
        assertLeader(clubId, requesterId);

        ClubActivity activity = ClubActivity.builder()
                .club(club)
                .title(req.getTitle())
                .description(req.getDescription())
                .startTime(LocalDateTime.parse(req.getStartTime()))
                .endTime(req.getEndTime() != null
                        ? LocalDateTime.parse(req.getEndTime()) : null)
                .location(req.getLocation())
                .conductPoints(req.getConductPoints())
                .status(ClubActivityStatus.SCHEDULED)
                .build();

        log.info("Activity '{}' created for club {}", req.getTitle(), clubId);
        return toActivityResponse(activityRepository.save(activity));
    }

    /** Leader cập nhật trạng thái sự kiện */
    @Transactional
    public ClubResponse.ActivityResponse updateActivityStatus(Long activityId,
                                                               Long requesterId,
                                                               ClubActivityStatus status) {
        ClubActivity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new NotFoundException("ClubActivity", activityId));
        assertLeader(activity.getClub().getId(), requesterId);
        activity.setStatus(status);
        return toActivityResponse(activityRepository.save(activity));
    }

    // ══════════════════════════════════════════════════════════════════════════
    // ACTIVITY ATTENDANCE
    // ══════════════════════════════════════════════════════════════════════════

    /** Leader xem danh sách điểm danh sự kiện */
    @Transactional(readOnly = true)
    public List<ClubActivityAttendance> getActivityAttendance(Long activityId,
                                                               Long requesterId) {
        ClubActivity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new NotFoundException("ClubActivity", activityId));
        assertLeader(activity.getClub().getId(), requesterId);
        return attendanceRepository.findByActivityId(activityId);
    }

    /** Leader ghi nhận điểm danh */
    @Transactional
    public ClubActivityAttendance recordAttendance(Long activityId,
                                                    Long requesterId,
                                                    ClubRequest.RecordAttendance req) {
        ClubActivity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new NotFoundException("ClubActivity", activityId));
        assertLeader(activity.getClub().getId(), requesterId);

        StudentProfile student = getStudentOrThrow(req.getStudentId());
        User recorder = userRepository.findById(requesterId).orElse(null);

        ClubActivityAttendance att = attendanceRepository
                .findByActivityIdAndStudentId(activityId, req.getStudentId())
                .orElse(ClubActivityAttendance.builder()
                        .activity(activity)
                        .student(student)
                        .build());

        att.setStatus(AttendanceStatus.valueOf(req.getStatus()));
        att.setNote(req.getNote());
        att.setRecordedBy(recorder);
        return attendanceRepository.save(att);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // HELPERS
    // ══════════════════════════════════════════════════════════════════════════

    private Club getClubOrThrow(Long clubId) {
        return clubRepository.findById(clubId)
                .orElseThrow(() -> new NotFoundException("Club", clubId));
    }

    private StudentProfile getStudentOrThrow(Long studentId) {
        return studentRepository.findById(studentId)
                .orElseThrow(() -> new NotFoundException("StudentProfile", studentId));
    }

    private ClubMember getMemberOrThrow(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException("ClubMember", memberId));
    }

    /**
     * Kiểm tra quyền leader (PRESIDENT hoặc VICE_PRESIDENT, status ACTIVE).
     * Ném ForbiddenException nếu không đủ quyền.
     */
    private void assertLeader(Long clubId, Long studentId) {
        memberRepository.findLeaderRole(clubId, studentId)
                .orElseThrow(() ->
                        new ForbiddenException("Bạn không có quyền thực hiện thao tác này"));
    }

    // ── Mappers ───────────────────────────────────────────────────────────────

    private ClubResponse.ClubSummary toSummary(Club c, Long studentId) {
        long activeCount    = memberRepository.countByClubIdAndStatus(
                c.getId(), ClubMemberStatus.ACTIVE);
        long upcomingEvents = activityRepository.countByClubIdAndStatus(
                c.getId(), ClubActivityStatus.SCHEDULED);

        // canJoin: còn slot AND club ACTIVE
        boolean canJoin = c.getStatus() == ClubStatus.ACTIVE
                && (c.getMaxMembers() == null || activeCount < c.getMaxMembers());

        ClubMemberStatus myStatus = null;
        ClubMemberRole   myRole   = null;

        if (studentId != null) {
            Optional<ClubMember> membership =
                    memberRepository.findByClubIdAndStudentId(c.getId(), studentId);
            if (membership.isPresent()) {
                myStatus = membership.get().getStatus();
                myRole   = membership.get().getClubRole();
                // Đã là thành viên hoặc pending → không cho join lại
                if (myStatus == ClubMemberStatus.ACTIVE
                        || myStatus == ClubMemberStatus.PENDING) {
                    canJoin = false;
                }
            }
        }

        return ClubResponse.ClubSummary.builder()
                .id(c.getId())
                .code(c.getCode())
                .name(c.getName())
                .description(c.getDescription())
                .logoUrl(c.getLogoUrl())
                .meetingLocation(c.getMeetingLocation())
                .maxMembers(c.getMaxMembers())
                .foundedDate(c.getFoundedDate() != null
                        ? c.getFoundedDate().toString() : null)
                .status(c.getStatus().name())
                .advisorName(c.getAdvisor() != null
                        ? c.getAdvisor().getFullName() : null)
                .presidentName(c.getPresident() != null
                        ? c.getPresident().getUser().getFullName() : null)
                .memberCount((int) activeCount)
                .upcomingEvents((int) upcomingEvents)
                .myStatus(myStatus)
                .myRole(myRole)
                .canJoin(canJoin)
                .build();
    }

    private ClubResponse.MemberResponse toMemberResponse(ClubMember cm) {
        StudentProfile s = cm.getStudent();
        User u = s.getUser();
        return ClubResponse.MemberResponse.builder()
                .id(cm.getId())
                .studentId(s.getId())
                .studentName(u.getFullName())
                .studentCode(s.getStudentCode())
                .avatarUrl(u.getAvatarUrl())
                .role(cm.getClubRole())
                .status(cm.getStatus())
                .joinDate(cm.getJoinDate() != null
                        ? cm.getJoinDate().toString() : null)
                .build();
    }

    private ClubResponse.ActivityResponse toActivityResponse(ClubActivity a) {
        return ClubResponse.ActivityResponse.builder()
                .id(a.getId())
                .clubId(a.getClub().getId())
                .clubName(a.getClub().getName())
                .clubCode(a.getClub().getCode())
                .title(a.getTitle())
                .description(a.getDescription())
                .startTime(a.getStartTime().toString())
                .endTime(a.getEndTime() != null ? a.getEndTime().toString() : null)
                .location(a.getLocation())
                .conductPoints(a.getConductPoints() != null ? a.getConductPoints() : 0)
                .status(a.getStatus())
                .build();
    }
}