package com.jeja.jejabe.care.dto;

import com.jeja.jejabe.care.domain.CareStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class AbsenceCareUpdateRequestDto {
    private CareStatus status;
    private LocalDate nextContactDate;
    private String careMemo;
}
