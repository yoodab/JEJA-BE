package com.jeja.jejabe.schedule.util;

import com.jeja.jejabe.schedule.domain.RecurrenceRule;
import com.jeja.jejabe.schedule.domain.Schedule;
import com.jeja.jejabe.schedule.domain.ScheduleType;
import com.jeja.jejabe.schedule.domain.SharingScope;
import com.jeja.jejabe.schedule.dto.ScheduleResponseDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class RecurrenceCalculatorTest {

    @Test
    @DisplayName("특정 요일 반복(WEEKLY_DAYS) - 생성일과 반복 요일이 다른 경우")
    void generateSchedules_WeeklyDays_DifferentStartDay() {
        // 기준: 2024-05-21 (화요일)
        // 일정 생성: 2024-05-19 (일요일) 10:00 ~ 12:00
        // 반복 규칙: 매주 화요일 (WEEKLY_DAYS, TUESDAY)
        // 기대 결과: 2024-05-21 10:00 ~ 12:00 일정 생성됨

        LocalDateTime now = LocalDateTime.of(2024, 5, 21, 10, 0); // 화요일
        LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = now.toLocalDate().atTime(LocalTime.MAX);

        LocalDateTime scheduleStart = LocalDateTime.of(2024, 5, 19, 10, 0); // 일요일
        LocalDateTime scheduleEnd = LocalDateTime.of(2024, 5, 19, 12, 0);

        Schedule schedule = Schedule.builder()
                .title("Tuesday Worship")
                .startDate(scheduleStart)
                .endDate(scheduleEnd)
                .type(ScheduleType.WORSHIP)
                .recurrenceRule(RecurrenceRule.WEEKLY_DAYS)
                .recurrenceDays(Set.of(DayOfWeek.TUESDAY))
                .build();

        List<ScheduleResponseDto> result = RecurrenceCalculator.generateSchedules(schedule, startOfDay, endOfDay);

        assertEquals(1, result.size());
        assertEquals(LocalDateTime.of(2024, 5, 21, 10, 0), result.get(0).getStartDate());
    }

    @Test
    @DisplayName("반복 없는 일정 - 오늘 날짜 포함 시 생성")
    void generateSchedules_NoRecurrence() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);

        // 오늘 14:00 ~ 15:00 일정
        LocalDateTime scheduleStart = startOfDay.plusHours(14);
        LocalDateTime scheduleEnd = startOfDay.plusHours(15);

        Schedule schedule = Schedule.builder()
                .title("Test Schedule")
                .startDate(scheduleStart)
                .endDate(scheduleEnd)
                .type(ScheduleType.WORSHIP)
                .sharingScope(SharingScope.PUBLIC)
                .recurrenceRule(RecurrenceRule.NONE)
                .build();

        // When
        List<ScheduleResponseDto> result = RecurrenceCalculator.generateSchedules(schedule, startOfDay, endOfDay);

        // Then
        assertEquals(1, result.size());
        assertEquals(scheduleStart, result.get(0).getStartDate());
    }

    @Test
    @DisplayName("매주 반복 일정 - 오늘 요일 일치 시 생성")
    void generateSchedules_Weekly() {
        // Given
        // 오늘 날짜
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

        // 1주일 전 같은 요일에 시작된 일정
        LocalDateTime scheduleStart = startOfDay.minusWeeks(1).plusHours(10); // 지난주 오늘 10시
        LocalDateTime scheduleEnd = startOfDay.minusWeeks(1).plusHours(12); // 지난주 오늘 12시

        Schedule schedule = Schedule.builder()
                .title("Weekly Schedule")
                .startDate(scheduleStart)
                .endDate(scheduleEnd)
                .type(ScheduleType.WORSHIP)
                .sharingScope(SharingScope.PUBLIC)
                .recurrenceRule(RecurrenceRule.WEEKLY)
                .build();

        // When
        List<ScheduleResponseDto> result = RecurrenceCalculator.generateSchedules(schedule, startOfDay, endOfDay);

        // Then
        // 오늘 날짜의 인스턴스가 생성되어야 함 (시간은 10:00 ~ 12:00)
        assertEquals(1, result.size());
        LocalDateTime instanceStart = result.get(0).getStartDate();
        assertEquals(today, instanceStart.toLocalDate());
        assertEquals(10, instanceStart.getHour());
    }

    @Test
    @DisplayName("시간 필터링 시뮬레이션 - 120분 범위 내")
    void simulateTimeFiltering() {
        // Given
        LocalDateTime now = LocalDateTime.of(2025, 5, 20, 10, 0); // 기준 시간: 10:00

        // 일정 시작: 11:50 (110분 후) -> 범위 내
        LocalDateTime scheduleStart = now.plusMinutes(110);

        LocalDateTime windowStart = scheduleStart.minusMinutes(120);
        LocalDateTime windowEnd = scheduleStart.plusMinutes(120);

        // When
        boolean isCheckable = now.isAfter(windowStart) && now.isBefore(windowEnd);

        // Then
        // scheduleStart - 120 = 09:50
        // scheduleStart + 120 = 13:50
        // now(10:00) is between 09:50 and 13:50 -> True
        assertTrue(isCheckable);
    }

    @Test
    @DisplayName("시간 필터링 시뮬레이션 - 120분 범위 밖")
    void simulateTimeFiltering_OutOfRange() {
        // Given
        LocalDateTime now = LocalDateTime.of(2025, 5, 20, 10, 0); // 기준 시간: 10:00

        // 일정 시작: 12:10 (130분 후) -> 범위 밖
        LocalDateTime scheduleStart = now.plusMinutes(130);

        LocalDateTime windowStart = scheduleStart.minusMinutes(120);
        LocalDateTime windowEnd = scheduleStart.plusMinutes(120);

        // When
        boolean isCheckable = now.isAfter(windowStart) && now.isBefore(windowEnd);

        // Then
        // scheduleStart - 120 = 10:10
        // scheduleStart + 120 = 14:10
        // now(10:00) is NOT after 10:10 -> False
        assertFalse(isCheckable);
    }

    @Test
    @DisplayName("시간 필터링 시뮬레이션 - 20분 범위 내 (기존 로직)")
    void simulateTimeFiltering_20Min_InRange() {
        // Given
        LocalDateTime now = LocalDateTime.of(2025, 5, 20, 10, 0); // 기준 시간: 10:00

        // 일정 시작: 10:15 (15분 후) -> 범위 내
        LocalDateTime scheduleStart = now.plusMinutes(15);

        LocalDateTime windowStart = scheduleStart.minusMinutes(20);
        LocalDateTime windowEnd = scheduleStart.plusMinutes(20);

        // When
        boolean isCheckable = now.isAfter(windowStart) && now.isBefore(windowEnd);

        // Then
        // scheduleStart - 20 = 09:55
        // scheduleStart + 20 = 10:35
        // now(10:00) is between 09:55 and 10:35 -> True
        assertTrue(isCheckable);
    }
}
