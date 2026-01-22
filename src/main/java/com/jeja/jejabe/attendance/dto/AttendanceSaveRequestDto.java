package com.jeja.jejabe.attendance.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class AttendanceSaveRequestDto {
    private LocalDate attendanceDate;
    private List<Long> attendedMemberIds;
}
