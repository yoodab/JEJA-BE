package com.jeja.jejabe.schedule.util;

import com.jeja.jejabe.schedule.domain.RecurrenceRule;
import com.jeja.jejabe.schedule.domain.Schedule;
import com.jeja.jejabe.schedule.dto.ScheduleResponseDto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RecurrenceCalculator {

    public static List<ScheduleResponseDto> generateSchedules(Schedule schedule, int year, int month) {
        List<LocalDateTime> occurrences = new ArrayList<>();
        RecurrenceRule rule = schedule.getRecurrenceRule();

        if (rule == null || rule == RecurrenceRule.NONE) {
            // 반복 없는 경우, 해당 월에 일정이 포함되는지 확인
            if (schedule.getStartDate().getYear() == year && schedule.getStartDate().getMonthValue() == month) {
                occurrences.add(schedule.getStartDate());
            }
        } else {
            // 반복 있는 경우, 해당 월의 발생일 계산
            LocalDateTime cursor = schedule.getStartDate();
            LocalDate recurrenceEnd = schedule.getRecurrenceEndDate();
            YearMonth targetMonth = YearMonth.of(year, month);

            while (!cursor.toLocalDate().isAfter(recurrenceEnd) && !YearMonth.from(cursor).isAfter(targetMonth)) {
                if (YearMonth.from(cursor).equals(targetMonth)) {
                    occurrences.add(cursor);
                }

                cursor = switch (rule) {
                    case DAILY -> cursor.plusDays(1);
                    case WEEKLY -> cursor.plusWeeks(1);
                    case MONTHLY -> cursor.plusMonths(1);
                    case YEARLY -> cursor.plusYears(1);
                    default -> recurrenceEnd.plusDays(1).atStartOfDay(); // 무한 루프 방지
                };
            }
        }

        // DTO로 변환
        long durationMinutes = java.time.Duration.between(schedule.getStartDate(), schedule.getEndDate()).toMinutes();
        return occurrences.stream()
                .map(occurrenceStart -> {
                    LocalDateTime occurrenceEnd = occurrenceStart.plusMinutes(durationMinutes);
                    return new ScheduleResponseDto(schedule, occurrenceStart, occurrenceEnd);
                })
                .collect(Collectors.toList());
    }
}
