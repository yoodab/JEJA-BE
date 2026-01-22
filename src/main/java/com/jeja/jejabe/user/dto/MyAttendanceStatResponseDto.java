package com.jeja.jejabe.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class MyAttendanceStatResponseDto {
    private int thisMonthCount;
    private int thisYearCount;
    private List<String> recentDates; // "2024-01-15" 형식
}
