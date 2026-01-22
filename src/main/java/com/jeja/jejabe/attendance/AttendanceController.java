package com.jeja.jejabe.attendance;

import com.jeja.jejabe.attendance.dto.AdminAttendanceRequestDto;
import com.jeja.jejabe.attendance.dto.AttendanceRecordDto;
import com.jeja.jejabe.attendance.dto.AttendanceStatusDto;
import com.jeja.jejabe.attendance.dto.CheckInRequestDto;
import com.jeja.jejabe.auth.UserDetailsImpl;
import com.jeja.jejabe.global.response.ApiResponseForm;
import com.jeja.jejabe.schedule.dto.ScheduleResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

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
        attendanceService.checkIn(scheduleId, requestDto, request,userDetails);
        return ResponseEntity.ok(ApiResponseForm.success("출석이 완료되었습니다."));
    }
    @GetMapping("/attendance/today")
    public ResponseEntity<ApiResponseForm<AttendanceStatusDto>> getMyTodayAttendance(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        AttendanceStatusDto status = attendanceService.getMyTodayAttendance(userDetails.getUser().getMember());
        return ResponseEntity.ok(ApiResponseForm.success(status));
    }


    @GetMapping("/admin/schedules/{scheduleId}/attendance-sheet")
    public ResponseEntity<ApiResponseForm<List<AttendanceRecordDto>>> getAttendanceSheet(@PathVariable Long scheduleId) {
        return ResponseEntity.ok(ApiResponseForm.success(attendanceService.getAttendanceSheet(scheduleId)));
    }

}
