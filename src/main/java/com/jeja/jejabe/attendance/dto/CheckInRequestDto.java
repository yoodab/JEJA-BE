package com.jeja.jejabe.attendance.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class CheckInRequestDto {
    private String name;
    private LocalDate birthDate;
    private Double latitude;
    private Double longitude;
}
