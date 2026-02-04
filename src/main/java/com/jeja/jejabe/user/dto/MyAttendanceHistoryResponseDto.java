package com.jeja.jejabe.user.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;

@Getter
@Builder
@RequiredArgsConstructor
public class MyAttendanceHistoryResponseDto {
    private final Map<String, Integer> stats;
    private final List<MyAttendanceRecordDto> records;
}
