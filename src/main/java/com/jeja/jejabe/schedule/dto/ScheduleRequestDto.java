package com.jeja.jejabe.schedule.dto;

import com.jeja.jejabe.schedule.domain.RecurrenceRule;
import com.jeja.jejabe.schedule.domain.ScheduleType;
import com.jeja.jejabe.schedule.domain.SharingScope;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class ScheduleRequestDto {
    private String title;
    private String content;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private ScheduleType type;
    private String location;
    private SharingScope sharingScope;
    private RecurrenceRule recurrenceRule;
    private LocalDate recurrenceEndDate;

    private Long worshipCategoryId;
}
