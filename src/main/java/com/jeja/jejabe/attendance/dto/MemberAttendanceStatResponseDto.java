package com.jeja.jejabe.attendance.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class MemberAttendanceStatResponseDto {
    private Long memberId;
    private String name;
    private String cellName;
    private int attendanceRate;
    private int attendanceCount;
    private int consecutiveAbsenceCount;
    private List<Boolean> attendanceHistory;
}
