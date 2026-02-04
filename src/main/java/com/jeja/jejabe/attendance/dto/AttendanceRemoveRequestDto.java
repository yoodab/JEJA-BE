package com.jeja.jejabe.attendance.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class AttendanceRemoveRequestDto {
    private LocalDate targetDate;
    private List<Long> memberIds;
}
