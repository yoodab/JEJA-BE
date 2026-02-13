package com.jeja.jejabe.schedule.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class UpcomingScheduleResponseDto {
    private List<ScheduleResponseDto> publicSchedules;
    private List<ScheduleResponseDto> memberSchedules;
}
