package com.jeja.jejabe.schedule;

import com.jeja.jejabe.auth.UserDetailsImpl;
import com.jeja.jejabe.global.response.ApiResponseForm;
import com.jeja.jejabe.schedule.dto.ScheduleRequestDto;
import com.jeja.jejabe.schedule.dto.ScheduleResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;

    @GetMapping("/schedules")
    public ResponseEntity<ApiResponseForm<List<ScheduleResponseDto>>> getSchedulesByMonth(
            @RequestParam int year,
            @RequestParam int month,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        List<ScheduleResponseDto> schedules = scheduleService.getSchedulesByMonth(year, month, userDetails);
        return ResponseEntity.ok(ApiResponseForm.success(schedules));
    }

    @PostMapping("/schedules")
    public ResponseEntity<ApiResponseForm<Long>> createSchedule(@RequestBody ScheduleRequestDto requestDto) {
        Long scheduleId = scheduleService.createSchedule(requestDto);
        return ResponseEntity.ok(ApiResponseForm.success(scheduleId, "일정이 등록되었습니다."));
    }

    @PatchMapping("/schedules/{scheduleId}")
    public ResponseEntity<ApiResponseForm<Void>> updateSchedule(
            @PathVariable Long scheduleId,
            @RequestBody ScheduleRequestDto requestDto) {
        scheduleService.updateSchedule(scheduleId, requestDto);
        return ResponseEntity.ok(ApiResponseForm.success("일정이 수정되었습니다."));
    }

    @DeleteMapping("/schedules/{scheduleId}")
    public ResponseEntity<ApiResponseForm<Void>> deleteSchedule(@PathVariable Long scheduleId) {
        scheduleService.deleteSchedule(scheduleId);
        return ResponseEntity.ok(ApiResponseForm.success("일정이 삭제되었습니다."));
    }
}