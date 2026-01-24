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
    // 초기 status와 role은 고정값으로 주거나, 엑셀에 포함시킬 수 있습니다.
}