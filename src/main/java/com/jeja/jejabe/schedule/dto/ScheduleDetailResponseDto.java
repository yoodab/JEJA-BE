package com.jeja.jejabe.schedule.dto;

import com.jeja.jejabe.attendance.dto.AttendanceRecordDto;
import com.jeja.jejabe.schedule.domain.Schedule;
import lombok.Getter;

import java.util.List;

@Getter
public class ScheduleDetailResponseDto extends ScheduleResponseDto {

    private final Long linkedAlbumId;
    private final List<AttendanceRecordDto> attendees;

    public ScheduleDetailResponseDto(Schedule schedule, Long linkedAlbumId, List<AttendanceRecordDto> attendees) {
        super(schedule);
        this.linkedAlbumId = linkedAlbumId;
        this.attendees = attendees;
    }
}
