package com.jeja.jejabe.attendance.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class AttendanceRemoveRequestDto {
    private List<Long> memberIds;
}
