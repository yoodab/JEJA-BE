package com.jeja.jejabe.attendance.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CheckInRequestDto {
    private String name;
    private String birthDate;
    private Double latitude;
    private Double longitude;
}
