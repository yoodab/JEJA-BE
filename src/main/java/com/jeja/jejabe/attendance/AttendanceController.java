package com.jeja.jejabe.attendance;

import com.jeja.jejabe.attendance.dto.*;
import com.jeja.jejabe.auth.UserDetailsImpl;
import com.jeja.jejabe.global.response.ApiResponseForm;
import com.jeja.jejabe.schedule.domain.ScheduleType;
import com.jeja.jejabe.schedule.domain.WorshipCategory;
import com.jeja.jejabe.schedule.dto.ScheduleResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping("/admin/schedules/{scheduleId}/register")
    public ResponseEntity<ApiResponseForm<Void>> registerAttendees(
            @PathVariable Long scheduleId,
            @RequestBody AttendanceRegistrationDto requestDto) {
        attendanceService.registerAttendees(scheduleId, requestDto);
        return ResponseEntity.ok(ApiResponseForm.success("출석 명단 등록이 완료되었습니다."));
    }

    @PostMapping("/admin/schedules/{scheduleId}/attendees/remove")
    public ResponseEntity<ApiResponseForm<Void>> removeAttendees(
            @PathVariable Long scheduleId,
            @RequestBody AttendanceRemoveRequestDto requestDto) {

        attendanceService.removeRegisteredMembers(scheduleId, requestDto);
        return ResponseEntity.ok(ApiResponseForm.success("선택한 인원이 명단에서 제외되었습니다."));
    }

    // [New] 2. 사용자: 참석 신청
    @PostMapping("/schedules/{scheduleId}/participation")
    public ResponseEntity<ApiResponseForm<Void>> applyForSchedule(
            @PathVariable Long scheduleId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        attendanceService.applyForSchedule(scheduleId, userDetails);
        return ResponseEntity.ok(ApiResponseForm.success("참석 신청이 완료되었습니다."));
    }

    // [New] 3. 사용자: 참석 신청 취소
    @DeleteMapping("/schedules/{scheduleId}/participation")
    public ResponseEntity<ApiResponseForm<Void>> cancelParticipation(
            @PathVariable Long scheduleId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        attendanceService.cancelApplication(scheduleId, userDetails);
        return ResponseEntity.ok(ApiResponseForm.success("참석 신청이 취소되었습니다."));
    }

    @PostMapping("/admin/schedules/{scheduleId}/attendance")
    public ResponseEntity<ApiResponseForm<Void>> checkInByAdmin(
            @PathVariable Long scheduleId,
            @RequestBody AdminAttendanceRequestDto requestDto) {
        attendanceService.checkInByAdmin(scheduleId, requestDto);
        return ResponseEntity.ok(ApiResponseForm.success("일괄 출석 처리가 완료되었습니다."));
    }

    @GetMapping("/schedule/checkable")
    public ResponseEntity<ApiResponseForm<List<ScheduleResponseDto>>> getCheckableSchedules() {
        List<ScheduleResponseDto> schedules = attendanceService.getCheckableSchedules();
        return ResponseEntity.ok(ApiResponseForm.success(schedules));
    }

    @PostMapping("/schedule/{scheduleId}/check-in")
    public ResponseEntity<ApiResponseForm<Void>> checkIn(
            @PathVariable Long scheduleId,
            @RequestBody CheckInRequestDto requestDto,
            HttpServletRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        attendanceService.checkIn(scheduleId, requestDto, request, userDetails);
        return ResponseEntity.ok(ApiResponseForm.success("출석이 완료되었습니다."));
    }

    @GetMapping("/attendance/today")
    public ResponseEntity<ApiResponseForm<AttendanceStatusDto>> getMyTodayAttendance(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        AttendanceStatusDto status = attendanceService.getMyTodayAttendance(userDetails.getUser().getMember());
        return ResponseEntity.ok(ApiResponseForm.success(status));
    }


    @GetMapping("/admin/schedules/{scheduleId}/attendance-sheet")
    public ResponseEntity<ApiResponseForm<AttendanceSheetResponseDto>> getAttendanceSheet(
            @PathVariable Long scheduleId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return ResponseEntity.ok(ApiResponseForm.success(attendanceService.getAttendanceSheet(scheduleId, date)));
    }

    @GetMapping("/admin/attendance/statistics")
    public ResponseEntity<ApiResponseForm<AttendanceStatisticsResponseDto>> getPeriodStatistics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long cellId,
            @RequestParam(required = false) List<ScheduleType> scheduleTypes,       // List로 변경
            @RequestParam(required = false) List<WorshipCategory> worshipCategories // List로 변경
    ) {

        AttendanceStatisticsResponseDto result = attendanceService.getPeriodStatistics(
                startDate, endDate, cellId, scheduleTypes, worshipCategories
        );
        return ResponseEntity.ok(ApiResponseForm.success(result));
    }

    // [New] 3-7. 멤버별 출석 현황 요약 (Admin)
    @GetMapping("/admin/attendance/member-stats")
    public ResponseEntity<ApiResponseForm<List<MemberAttendanceStatResponseDto>>> getMemberStats(
            @RequestParam int year,
            @RequestParam(required = false) Long cellId) {

        List<MemberAttendanceStatResponseDto> result = attendanceService.getMemberStats(year, cellId);
        return ResponseEntity.ok(ApiResponseForm.success(result));
    }

}
