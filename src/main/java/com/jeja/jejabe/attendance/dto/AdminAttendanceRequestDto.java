package com.jeja.jejabe.attendance.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AdminAttendanceRequestDto {
    private List<Long> attendedMemberIds;
}
