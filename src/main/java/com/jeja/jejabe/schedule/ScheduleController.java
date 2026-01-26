package com.jeja.jejabe.schedule;

import com.jeja.jejabe.auth.UserDetailsImpl;
import com.jeja.jejabe.global.exception.GeneralException;
import com.jeja.jejabe.global.response.ApiResponseForm;
import com.jeja.jejabe.schedule.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;
    // [New] Guard 주입
    private final ScheduleGuard scheduleGuard;

    @GetMapping
    public ResponseEntity<ApiResponseForm<List<ScheduleResponseDto>>> getSchedules(
            @RequestParam int year,
            @RequestParam int month,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        List<ScheduleResponseDto> result = scheduleService.getSchedulesByMonth(year, month, userDetails);
        return ResponseEntity.ok(ApiResponseForm.success(result));
    }

    // [New] 일정 상세 조회 API (단건)
    @GetMapping("/{scheduleId}")
    public ResponseEntity<ApiResponseForm<ScheduleDetailResponseDto>> getScheduleDetail(
            @PathVariable Long scheduleId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        ScheduleDetailResponseDto detail = scheduleService.getScheduleDetail(scheduleId, userDetails);
        return ResponseEntity.ok(ApiResponseForm.success(detail));
    }

    @PostMapping
    public ResponseEntity<ApiResponseForm<Long>> createSchedule(@RequestBody ScheduleCreateRequestDto dto) {
        Long id = scheduleService.createSchedule(dto);
        return ResponseEntity.ok(ApiResponseForm.success(id, "일정이 등록되었습니다."));
    }

    @PatchMapping("/{scheduleId}")
    public ResponseEntity<ApiResponseForm<Void>> updateSchedule(
            @PathVariable Long scheduleId,
            @RequestBody ScheduleUpdateRequestDto dto) {
        scheduleService.updateSchedule(scheduleId, dto);
        return ResponseEntity.ok(ApiResponseForm.success("일정이 수정되었습니다."));
    }

    @DeleteMapping("/{scheduleId}")
    public ResponseEntity<ApiResponseForm<Void>> deleteSchedule(
            @PathVariable Long scheduleId,
            @RequestParam(defaultValue = "ALL") UpdateType updateType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate targetDate) {

        scheduleService.deleteSchedule(scheduleId, updateType, targetDate);
        return ResponseEntity.ok(ApiResponseForm.success("일정이 삭제되었습니다."));
    }
}