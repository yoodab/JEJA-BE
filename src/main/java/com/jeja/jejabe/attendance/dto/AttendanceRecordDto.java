package com.jeja.jejabe.attendance.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AttendanceRecordDto {
    private Long memberId;
    private String name;
    private String phone;
    private boolean attended;
    private String attendanceTime;
}
