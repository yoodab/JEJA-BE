package com.jeja.jejabe.schedule.dto;

import com.jeja.jejabe.schedule.domain.RecurrenceRule;
import com.jeja.jejabe.schedule.domain.Schedule;
import com.jeja.jejabe.schedule.domain.ScheduleType;
import com.jeja.jejabe.schedule.domain.SharingScope;
import lombok.Getter;

import java.time.LocalDateTime;

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
    }
}
