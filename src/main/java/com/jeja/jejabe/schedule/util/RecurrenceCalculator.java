package com.jeja.jejabe.schedule.util;

import com.jeja.jejabe.schedule.domain.RecurrenceRule;
import com.jeja.jejabe.schedule.domain.Schedule;
import com.jeja.jejabe.schedule.dto.ScheduleResponseDto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class RecurrenceCalculator {

    public static List<ScheduleResponseDto> generateSchedules(Schedule schedule, int year, int month) {
        List<ScheduleResponseDto> result = new ArrayList<>();

        YearMonth targetMonth = YearMonth.of(year, month);
        LocalDateTime monthStart = targetMonth.atDay(1).atStartOfDay();
        LocalDateTime monthEnd = targetMonth.atEndOfMonth().atTime(23, 59, 59);

        // 반복 없음 (단순 날짜 체크)
        if (schedule.getRecurrenceRule() == RecurrenceRule.NONE) {
            if (isOverlapping(schedule.getStartDate(), schedule.getEndDate(), monthStart, monthEnd)) {
                result.add(new ScheduleResponseDto(schedule));
            }
            return result;
        }

        // 반복 일정 계산
        LocalDateTime currentStart = schedule.getStartDate();
        LocalDateTime currentEnd = schedule.getEndDate();
        long durationSeconds = ChronoUnit.SECONDS.between(currentStart, currentEnd);
        LocalDate recurEnd = schedule.getRecurrenceEndDate();

        while (currentStart.isBefore(monthEnd)) {
            // 1. 반복 종료일 체크
            if (recurEnd != null && currentStart.toLocalDate().isAfter(recurEnd)) {
                break;
            }

            // 2. [NEW] 예외 날짜 체크 (THIS_ONLY로 삭제/수정된 날짜는 건너뜀)
            if (schedule.getExceptionDates().contains(currentStart.toLocalDate())) {
                currentStart = getNextOccurrence(currentStart, schedule.getRecurrenceRule());
                continue;
            }

            // 3. 이번 달 범위 내인지 확인
            LocalDateTime calculatedEnd = currentStart.plusSeconds(durationSeconds);
            if (isOverlapping(currentStart, calculatedEnd, monthStart, monthEnd)) {
                result.add(new ScheduleResponseDto(schedule, currentStart, calculatedEnd));
            }

            // 4. 다음 반복 날짜로 이동
            currentStart = getNextOccurrence(currentStart, schedule.getRecurrenceRule());
        }

        return result;
    }

    private static LocalDateTime getNextOccurrence(LocalDateTime current, RecurrenceRule rule) {
        return switch (rule) {
            case DAILY -> current.plusDays(1);
            case WEEKLY -> current.plusWeeks(1);
            case MONTHLY -> current.plusMonths(1);
            case YEARLY -> current.plusYears(1);
            default -> current;
        };
    }

    private static boolean isOverlapping(LocalDateTime start1, LocalDateTime end1, LocalDateTime start2, LocalDateTime end2) {
        return start1.isBefore(end2) && end1.isAfter(start2);
    }
}
