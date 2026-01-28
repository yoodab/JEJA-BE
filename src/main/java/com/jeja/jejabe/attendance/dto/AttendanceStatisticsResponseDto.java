package com.jeja.jejabe.attendance.dto;

import com.jeja.jejabe.schedule.domain.WorshipCategory;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Getter
@Builder
public class AttendanceStatisticsResponseDto {
    private SummaryDto summary;
    private List<ScheduleStatDto> scheduleStats;

    @Getter
    @Builder
    public static class SummaryDto {
        private double averageAttendance;
        private LocalDate maxAttendanceDate;
        private String maxAttendanceScheduleName;
        private long totalOffering;
    }

    @Getter
    @Builder
    public static class ScheduleStatDto {
        private Long scheduleId;
        private LocalDate date;
        private LocalTime time;
        private String scheduleName;
        private WorshipCategory category;
        private long count;
        private long offering;
    }
}
