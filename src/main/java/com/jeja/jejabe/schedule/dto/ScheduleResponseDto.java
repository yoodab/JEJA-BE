package com.jeja.jejabe.schedule.dto;

import com.jeja.jejabe.schedule.domain.*;
import lombok.Getter;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
public class ScheduleResponseDto {
    private final Long scheduleId;
    private final String title;
    private final LocalDateTime startDate;
    private final LocalDateTime endDate;
    private final ScheduleType type;
    private final String location;
    private final SharingScope sharingScope;
    private final RecurrenceRule recurrenceRule;
    private final LocalDate recurrenceEndDate;
    private final Set<DayOfWeek> recurrenceDays;

    private final WorshipCategory worshipCategory;
    private final String worshipCategoryName;

    // 일반 일정용 생성자
    public ScheduleResponseDto(Schedule schedule) {
        this.scheduleId = schedule.getScheduleId();
        this.title = schedule.getTitle();
        this.startDate = schedule.getStartDate();
        this.endDate = schedule.getEndDate();
        this.type = schedule.getType();
        this.location = schedule.getLocation();
        this.sharingScope = schedule.getSharingScope();
        this.recurrenceRule = schedule.getRecurrenceRule();
        this.recurrenceEndDate = schedule.getRecurrenceEndDate();
        this.recurrenceDays = schedule.getRecurrenceDays();
        this.worshipCategory = schedule.getWorshipCategory();
        this.worshipCategoryName = schedule.getWorshipCategory() != null
                ? schedule.getWorshipCategory().getDescription()
                : null;
    }

    // 반복 일정용 생성자
    public ScheduleResponseDto(Schedule originalSchedule, LocalDateTime occurrenceStart, LocalDateTime occurrenceEnd) {
        this.scheduleId = originalSchedule.getScheduleId();
        this.title = originalSchedule.getTitle();
        this.startDate = occurrenceStart;
        this.endDate = occurrenceEnd;
        this.type = originalSchedule.getType();
        this.location = originalSchedule.getLocation();
        this.sharingScope = originalSchedule.getSharingScope();
        this.recurrenceRule = originalSchedule.getRecurrenceRule();
        this.recurrenceEndDate = originalSchedule.getRecurrenceEndDate();
        this.recurrenceDays = originalSchedule.getRecurrenceDays();
        this.worshipCategory = originalSchedule.getWorshipCategory();
        this.worshipCategoryName = originalSchedule.getWorshipCategory() != null
                ? originalSchedule.getWorshipCategory().getDescription()
                : null;
    }
}
