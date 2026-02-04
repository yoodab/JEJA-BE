package com.jeja.jejabe.schedule.dto;

import com.jeja.jejabe.schedule.domain.RecurrenceRule;
import com.jeja.jejabe.schedule.domain.ScheduleType;
import com.jeja.jejabe.schedule.domain.SharingScope;
import com.jeja.jejabe.schedule.domain.WorshipCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor // Builder 패턴을 쓰려면 필요
@Builder
public class ScheduleCreateRequestDto {
    private String title;
    private String content;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private ScheduleType type;
    private String location;
    private SharingScope sharingScope;

    // 반복 설정
    private RecurrenceRule recurrenceRule;
    private LocalDate recurrenceEndDate;

    // 카테고리
    private WorshipCategory worshipCategory;

    private boolean createAlbum;

    public static ScheduleCreateRequestDtoBuilder from(ScheduleUpdateRequestDto updateDto) {
        return ScheduleCreateRequestDto.builder()
                .title(updateDto.getTitle())
                .content(updateDto.getContent())
                .startDate(updateDto.getStartDate())
                .endDate(updateDto.getEndDate())
                .type(updateDto.getType())
                .location(updateDto.getLocation())
                .sharingScope(updateDto.getSharingScope())
                .worshipCategory(updateDto.getWorshipCategory())
                .recurrenceRule(updateDto.getRecurrenceRule())
                .recurrenceEndDate(updateDto.getRecurrenceEndDate());
    }
}
