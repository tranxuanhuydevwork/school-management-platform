package com.golearn.myf3school_backend.controller;

import com.golearn.myf3school_backend.api.config.JwtTokenProvider;
import com.golearn.myf3school_backend.application_service.dtos.request.ClubRequest;
import com.golearn.myf3school_backend.application_service.dtos.response.ApiResponse;
import com.golearn.myf3school_backend.application_service.dtos.response.ClubResponse;
import com.golearn.myf3school_backend.application_service.service.ClubService;
import com.golearn.myf3school_backend.contract.enums.ClubActivityStatus;
import com.golearn.myf3school_backend.contract.enums.ClubMemberRole;
import com.golearn.myf3school_backend.infrastructure.entity.ClubActivityAttendance;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ╔══════════════════════════════════════════════════════════════╗
 * ║  Base URL : /api/clubs                                       ║
 * ╠══════════════════════════════════════════════════════════════╣
 * ║  STUDENT (tất cả)                                            ║
 * ║  GET  /api/clubs                   danh sách CLB             ║
 * ║  GET  /api/clubs/my                CLB của tôi               ║
 * ║  GET  /api/clubs/{id}              chi tiết CLB              ║
 * ║  POST /api/clubs/{id}/join         đăng ký tham gia          ║
 * ║  DELETE /api/clubs/{id}/leave      rời CLB                   ║
 * ║  GET  /api/clubs/activities        sự kiện toàn trường       ║
 * ║  GET  /api/clubs/{id}/activities   sự kiện của CLB           ║
 * ╠══════════════════════════════════════════════════════════════╣
 * ║  LEADER (PRESIDENT / VICE_PRESIDENT)                         ║
 * ║  GET  /api/clubs/{id}/members      danh sách thành viên      ║
 * ║  PUT  /api/clubs/members/{id}/approve   duyệt               ║
 * ║  PUT  /api/clubs/members/{id}/reject    từ chối              ║
 * ║  PUT  /api/clubs/members/{id}/expel     khai trừ             ║
 * ║  PUT  /api/clubs/members/{id}/role      đổi role             ║
 * ║  POST /api/clubs/{id}/activities        tạo sự kiện          ║
 * ║  PUT  /api/clubs/activities/{id}/status cập nhật trạng thái  ║
 * ║  GET  /api/clubs/activities/{id}/attendance  điểm danh       ║
 * ║  POST /api/clubs/activities/{id}/attendance  ghi điểm danh   ║
 * ╠══════════════════════════════════════════════════════════════╣
 * ║  ADMIN / TEACHER                                             ║
 * ║  POST /api/clubs                   tạo CLB mới               ║
 * ╚══════════════════════════════════════════════════════════════╝
 */
@RestController
@RequestMapping("/api/clubs")
@RequiredArgsConstructor
public class ClubController {

    private final ClubService      clubService;
    private final JwtTokenProvider jwtProvider;

    /** Lấy studentId từ JWT Bearer token */
    private Long studentId(String auth) {
        return jwtProvider.getStudentIdFromToken(
                auth.replace("Bearer ", "").trim());
    }

    // ════════════════════════════════════════════════════════════════════════
    // CLB
    // ════════════════════════════════════════════════════════════════════════

    /**
     * GET /api/clubs?search=FPT&studentId=5
     * studentId tùy chọn — nếu có, trả myStatus / myRole / canJoin.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ClubResponse.ClubSummary>>> getClubs(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long   studentId) {

        return ResponseEntity.ok(ApiResponse.ok(
                "Lấy danh sách CLB thành công",
                clubService.getClubs(search, studentId)));
    }

    /**
     * GET /api/clubs/my
     * Header: Authorization: Bearer <token>
     */
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<ClubResponse.ClubSummary>>> getMyClubs(
            @RequestHeader("Authorization") String auth) {

        return ResponseEntity.ok(ApiResponse.ok(
                "CLB của tôi",
                clubService.getMyClubs(studentId(auth))));
    }

    /**
     * GET /api/clubs/{id}?studentId=5
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ClubResponse.ClubSummary>> getClub(
            @PathVariable Long id,
            @RequestParam(required = false) Long studentId) {

        // getClubs với list rồi filter — hoặc tạo hàm riêng nếu cần
        return ResponseEntity.ok(ApiResponse.ok(
                "Chi tiết CLB",
                clubService.getClubs(null, studentId).stream()
                        .filter(c -> c.getId().equals(id))
                        .findFirst()
                        .orElseThrow()));
    }

    /**
     * POST /api/clubs  — tạo CLB (admin / giáo viên)
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ClubResponse.ClubSummary>> createClub(
            @Valid @RequestBody ClubRequest.CreateClub req) {

        return ResponseEntity.ok(ApiResponse.created(clubService.createClub(req)));
    }

    // ════════════════════════════════════════════════════════════════════════
    // MEMBERSHIP
    // ════════════════════════════════════════════════════════════════════════

    /**
     * POST /api/clubs/{id}/join
     * Body: { "studentId": 5 }
     */
    @PostMapping("/{id}/join")
    public ResponseEntity<ApiResponse<ClubResponse.MemberResponse>> join(
            @PathVariable Long id,
            @Valid @RequestBody ClubRequest.JoinClub req) {

        return ResponseEntity.ok(ApiResponse.ok(
                "Đã gửi đơn tham gia CLB, chờ leader duyệt",
                clubService.joinClub(id, req.getStudentId())));
    }

    /**
     * DELETE /api/clubs/{id}/leave
     * Body: { "studentId": 5, "leaveReason": "..." }
     */
    @DeleteMapping("/{id}/leave")
    public ResponseEntity<ApiResponse<Void>> leave(
            @PathVariable Long id,
            @RequestBody ClubRequest.LeaveClub req) {

        clubService.leaveClub(id, req.getStudentId(), req.getLeaveReason());
        return ResponseEntity.ok(ApiResponse.ok("Đã rời CLB", null));
    }

    /**
     * GET /api/clubs/{id}/members
     * Yêu cầu: caller phải là PRESIDENT / VICE_PRESIDENT của CLB đó.
     */
    @GetMapping("/{id}/members")
    public ResponseEntity<ApiResponse<List<ClubResponse.MemberResponse>>> getMembers(
            @PathVariable Long id,
            @RequestHeader("Authorization") String auth) {

        return ResponseEntity.ok(ApiResponse.ok(
                "Danh sách thành viên",
                clubService.getMembers(id, studentId(auth))));
    }

    /** PUT /api/clubs/members/{memberId}/approve */
    @PutMapping("/members/{memberId}/approve")
    public ResponseEntity<ApiResponse<ClubResponse.MemberResponse>> approve(
            @PathVariable Long memberId,
            @RequestHeader("Authorization") String auth) {

        return ResponseEntity.ok(ApiResponse.ok(
                "Đã duyệt thành viên",
                clubService.approveMember(memberId, studentId(auth))));
    }

    /** PUT /api/clubs/members/{memberId}/reject */
    @PutMapping("/members/{memberId}/reject")
    public ResponseEntity<ApiResponse<Void>> reject(
            @PathVariable Long memberId,
            @RequestHeader("Authorization") String auth) {

        clubService.rejectMember(memberId, studentId(auth));
        return ResponseEntity.ok(ApiResponse.ok("Đã từ chối đơn", null));
    }

    /**
     * PUT /api/clubs/members/{memberId}/expel
     * Body: { "studentId": 0, "leaveReason": "Vi phạm nội quy" }
     */
    @PutMapping("/members/{memberId}/expel")
    public ResponseEntity<ApiResponse<Void>> expel(
            @PathVariable Long memberId,
            @RequestBody(required = false) ClubRequest.LeaveClub req,
            @RequestHeader("Authorization") String auth) {

        clubService.expelMember(memberId, studentId(auth),
                req != null ? req.getLeaveReason() : null);
        return ResponseEntity.ok(ApiResponse.ok("Đã khai trừ thành viên", null));
    }

    /**
     * PUT /api/clubs/members/{memberId}/role
     * Body: { "role": "SECRETARY" }
     */
    @PutMapping("/members/{memberId}/role")
    public ResponseEntity<ApiResponse<ClubResponse.MemberResponse>> changeRole(
            @PathVariable Long memberId,
            @Valid @RequestBody ClubRequest.ChangeRole req,
            @RequestHeader("Authorization") String auth) {

        return ResponseEntity.ok(ApiResponse.ok(
                "Đã cập nhật vai trò",
                clubService.changeRole(memberId, studentId(auth), req.getRole())));
    }

    // ════════════════════════════════════════════════════════════════════════
    // ACTIVITIES
    // ════════════════════════════════════════════════════════════════════════

    /** GET /api/clubs/activities — sự kiện toàn trường */
    @GetMapping("/activities")
    public ResponseEntity<ApiResponse<List<ClubResponse.ActivityResponse>>> getAllActivities() {
        return ResponseEntity.ok(ApiResponse.ok(
                "Sự kiện toàn trường",
                clubService.getAllActivities()));
    }

    /** GET /api/clubs/{id}/activities — sự kiện của 1 CLB */
    @GetMapping("/{id}/activities")
    public ResponseEntity<ApiResponse<List<ClubResponse.ActivityResponse>>> getClubActivities(
            @PathVariable Long id) {

        return ResponseEntity.ok(ApiResponse.ok(
                "Sự kiện CLB",
                clubService.getClubActivities(id)));
    }

    /**
     * POST /api/clubs/{id}/activities — tạo sự kiện
     * Yêu cầu: caller phải là leader của CLB.
     */
    @PostMapping("/{id}/activities")
    public ResponseEntity<ApiResponse<ClubResponse.ActivityResponse>> createActivity(
            @PathVariable Long id,
            @Valid @RequestBody ClubRequest.CreateActivity req,
            @RequestHeader("Authorization") String auth) {

        return ResponseEntity.ok(ApiResponse.ok(
                "Đã tạo sự kiện",
                clubService.createActivity(id, studentId(auth), req)));
    }

    /**
     * PUT /api/clubs/activities/{actId}/status
     * Body: { "status": "IN_PROGRESS" }
     */
    @PutMapping("/activities/{actId}/status")
    public ResponseEntity<ApiResponse<ClubResponse.ActivityResponse>> updateActivityStatus(
            @PathVariable Long actId,
            @Valid @RequestBody ClubRequest.UpdateActivityStatus req,
            @RequestHeader("Authorization") String auth) {

        return ResponseEntity.ok(ApiResponse.ok(
                "Đã cập nhật trạng thái sự kiện",
                clubService.updateActivityStatus(actId, studentId(auth), req.getStatus())));
    }

    // ════════════════════════════════════════════════════════════════════════
    // ACTIVITY ATTENDANCE
    // ════════════════════════════════════════════════════════════════════════

    /** GET /api/clubs/activities/{actId}/attendance */
    @GetMapping("/activities/{actId}/attendance")
    public ResponseEntity<ApiResponse<List<ClubActivityAttendance>>> getAttendance(
            @PathVariable Long actId,
            @RequestHeader("Authorization") String auth) {

        return ResponseEntity.ok(ApiResponse.ok(
                "Danh sách điểm danh",
                clubService.getActivityAttendance(actId, studentId(auth))));
    }

    /**
     * POST /api/clubs/activities/{actId}/attendance
     * Body: { "studentId": 5, "status": "PRESENT", "note": "..." }
     */
    @PostMapping("/activities/{actId}/attendance")
    public ResponseEntity<ApiResponse<ClubActivityAttendance>> recordAttendance(
            @PathVariable Long actId,
            @Valid @RequestBody ClubRequest.RecordAttendance req,
            @RequestHeader("Authorization") String auth) {

        return ResponseEntity.ok(ApiResponse.ok(
                "Đã ghi nhận điểm danh",
                clubService.recordAttendance(actId, studentId(auth), req)));
    }
}