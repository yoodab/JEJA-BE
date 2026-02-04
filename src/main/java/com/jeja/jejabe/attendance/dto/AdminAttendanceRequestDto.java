package com.jeja.jejabe.attendance.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class AdminAttendanceRequestDto {
    private LocalDate targetDate;
    private List<Long> attendedMemberIds;
}
