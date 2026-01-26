package com.jeja.jejabe.schedule.dto;

import com.jeja.jejabe.schedule.domain.RecurrenceRule;
import com.jeja.jejabe.schedule.domain.ScheduleType;
import com.jeja.jejabe.schedule.domain.SharingScope;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class ScheduleUpdateRequestDto {
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

    // 어떤 방식으로 수정할지 결정 (ALL, FUTURE, THIS_ONLY)
    private UpdateType updateType;

    // 반복 일정 중 사용자가 클릭한 날짜 (수정 기준일)
    private LocalDate targetDate;

}
