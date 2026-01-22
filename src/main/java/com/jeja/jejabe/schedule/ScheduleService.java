package com.jeja.jejabe.schedule;

import com.jeja.jejabe.auth.UserDetailsImpl;
import com.jeja.jejabe.global.exception.CommonErrorCode;
import com.jeja.jejabe.global.exception.GeneralException;
import com.jeja.jejabe.schedule.domain.Schedule;
import com.jeja.jejabe.schedule.domain.SharingScope;
import com.jeja.jejabe.schedule.domain.WorshipCategory;
import com.jeja.jejabe.schedule.dto.ScheduleRequestDto;
import com.jeja.jejabe.schedule.dto.ScheduleResponseDto;
import com.jeja.jejabe.schedule.util.RecurrenceCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final WorshipCategoryRepository worshipCategoryRepository;

    public Long createSchedule(ScheduleRequestDto requestDto) {
        // 카테고리 ID가 있으면 조회
        WorshipCategory category = null;
        if (requestDto.getWorshipCategoryId() != null) {
            category = worshipCategoryRepository.findById(requestDto.getWorshipCategoryId())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 예배 카테고리입니다."));
        }

        Schedule schedule = Schedule.builder()
                .title(requestDto.getTitle())
                .content(requestDto.getContent())
                .startDate(requestDto.getStartDate())
                .endDate(requestDto.getEndDate())
                .type(requestDto.getType())
                .location(requestDto.getLocation())
                .sharingScope(requestDto.getSharingScope())
                .recurrenceRule(requestDto.getRecurrenceRule())
                .recurrenceEndDate(requestDto.getRecurrenceEndDate())
                .worshipCategory(category) // 카테고리 설정
                .build();
        return scheduleRepository.save(schedule).getScheduleId();
    }

    @Transactional(readOnly = true)
    public List<ScheduleResponseDto> getSchedulesByMonth(int year, int month, UserDetailsImpl userDetails) {
        List<Schedule> allSchedules = scheduleRepository.findAll();

        List<ScheduleResponseDto> calculatedSchedules = allSchedules.stream()
                .flatMap(schedule -> RecurrenceCalculator.generateSchedules(schedule, year, month).stream())
                .collect(Collectors.toList());

        return filterBySharingScope(calculatedSchedules, userDetails);
    }

    public void updateSchedule(Long scheduleId, ScheduleRequestDto requestDto) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new GeneralException(CommonErrorCode.SCHEDULE_NOT_FOUND));

        WorshipCategory category = null;
        if (requestDto.getWorshipCategoryId() != null) {
            category = worshipCategoryRepository.findById(requestDto.getWorshipCategoryId())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 예배 카테고리입니다."));
        }

        schedule.update(requestDto, category);
    }

    public void deleteSchedule(Long scheduleId) {
        scheduleRepository.deleteById(scheduleId);
    }

    // 공유 범위 필터링 로직 (기존 유지)
    private List<ScheduleResponseDto> filterBySharingScope(List<ScheduleResponseDto> schedules, UserDetailsImpl userDetails) {
        if (userDetails == null) {
            return schedules.stream()
                    .filter(s -> s.getSharingScope() == SharingScope.PUBLIC)
                    .collect(Collectors.toList());
        }
        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (isAdmin) return schedules;
        return schedules.stream()
                .filter(s -> s.getSharingScope() == SharingScope.PUBLIC || s.getSharingScope() == SharingScope.LOGGED_IN_USERS)
                .collect(Collectors.toList());
    }
}