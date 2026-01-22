package com.jeja.jejabe.attendance.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class AttendanceStatusDto {
    private boolean hasAttended;
    private LocalDateTime attendanceTime;
    private LocalDate date;
}