package com.jeja.jejabe.member.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class ExcelMemberDto {
    private String name;
    private LocalDate birthDate;
    private String phone;
    private String gender;
    private String memberStatus;
}