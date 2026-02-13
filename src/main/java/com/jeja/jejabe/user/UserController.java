package com.jeja.jejabe.user;

import com.jeja.jejabe.attendance.AttendanceService;
import com.jeja.jejabe.auth.UserDetailsImpl;
import com.jeja.jejabe.auth.UserStatus;
import com.jeja.jejabe.global.exception.CommonErrorCode;
import com.jeja.jejabe.global.exception.GeneralException;
import com.jeja.jejabe.global.response.ApiResponseForm;
import com.jeja.jejabe.user.dto.*;
import com.jeja.jejabe.user.dto.MyInfoUpdateRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    private final AttendanceService attendanceService; // 주입 필요

    @GetMapping("/me/attendance-stats")
    public ResponseEntity<ApiResponseForm<MyAttendanceStatResponseDto>> getMyAttendanceStats(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(ApiResponseForm.success(
                attendanceService.getMyAttendanceStats(userDetails.getUser().getMember())));
    }

    @GetMapping("/me/attendance-history")
    public ResponseEntity<ApiResponseForm<MyAttendanceHistoryResponseDto>> getMyAttendanceHistory(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(ApiResponseForm.success(
                attendanceService.getMyAttendanceHistory(userDetails.getUser().getMember(), startDate, endDate)));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponseForm<MyInfoResponseDto>> getMyInfo(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        // 로그인한 사용자 정보를 서비스로 전달
        MyInfoResponseDto myInfo = userService.getMyInfo(userDetails.getUser());

        return ResponseEntity.ok(ApiResponseForm.success(myInfo, "내 정보 조회 성공"));
    }

    @PatchMapping("/me")
    public ResponseEntity<ApiResponseForm<Void>> updateMyInfo(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody MyInfoUpdateRequestDto requestDto) {

        userService.updateMyInfo(userDetails.getUser().getId(), requestDto);
        return ResponseEntity.ok(ApiResponseForm.success("내 정보가 수정되었습니다."));
    }

    @DeleteMapping("/me")
    public ResponseEntity<ApiResponseForm<Void>> withdraw(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody WithdrawRequestDto requestDto) {

        userService.withdraw(userDetails.getUser().getId(), requestDto.getPassword());
        return ResponseEntity.ok(ApiResponseForm.success("회원 탈퇴가 완료되었습니다."));
    }

    /**
     * 사용자 목록 조회
     * GET /api/users -> 전체 조회
     * GET /api/users?status=PENDING -> 승인 대기자 조회
     * GET /api/users?status=ACTIVE -> 활동 중인 유저 조회
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'PASTOR', 'EXECUTIVE')")
    @GetMapping
    public ResponseEntity<ApiResponseForm<List<UserResponseDto>>> getUsers(
            @RequestParam(value = "status", required = false) String statusStr,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        UserStatus status = null;
        if (statusStr != null && !statusStr.isEmpty()) {
            try {
                status = UserStatus.valueOf(statusStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new GeneralException(CommonErrorCode.BAD_REQUEST);
            }
        }

        List<UserResponseDto> users = userService.getUsers(status, userDetails);
        return ResponseEntity.ok(ApiResponseForm.success(users, "사용자 목록 조회 성공"));
    }

    /**
     * 사용자 상태 변경
     * PATCH /api/users/{userId}/status
     * Body: { "status": "ACTIVE" } -> 승인
     * Body: { "status": "REJECTED" } -> 거절
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'PASTOR', 'EXECUTIVE')")
    @PatchMapping("/{userId}/status")
    public ResponseEntity<ApiResponseForm<Void>> updateUserStatus(
            @PathVariable Long userId,
            @RequestBody UserStatusUpdateDto requestDto,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        if (requestDto.getStatus() == null) {
            throw new GeneralException(CommonErrorCode.BAD_REQUEST);
        }

        userService.updateUserStatus(userId, requestDto.getStatus(), userDetails);

        return ResponseEntity.ok(ApiResponseForm.success(
                "사용자 상태가 " + requestDto.getStatus() + "(으)로 변경되었습니다."));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'PASTOR', 'EXECUTIVE')")
    @PatchMapping("/admin/users/{userId}/password-reset")
    public ResponseEntity<ApiResponseForm<Void>> resetPassword(@PathVariable Long userId) {
        userService.resetPassword(userId, "1234");
        return ResponseEntity.ok(ApiResponseForm.success("비밀번호가 1234로 초기화되었습니다."));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'PASTOR', 'EXECUTIVE')")
    @GetMapping("/admin/dashboard/stats") // URL: /api/users/admin/dashboard/stats
    public ResponseEntity<ApiResponseForm<AdminDashboardStatsDto>> getDashboardStats() {
        return ResponseEntity.ok(ApiResponseForm.success(userService.getDashboardStats()));
    }
}
