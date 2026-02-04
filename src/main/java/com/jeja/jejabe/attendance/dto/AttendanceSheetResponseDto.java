package com.jeja.jejabe.attendance.dto;

import java.util.List;

public record AttendanceSheetResponseDto(
        String attendanceMode,
        List<AttendanceRecordDto> records
) {}
