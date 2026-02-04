package com.jeja.jejabe.attendance.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class ParticipationRequestDto {
    private LocalDate targetDate;
}
