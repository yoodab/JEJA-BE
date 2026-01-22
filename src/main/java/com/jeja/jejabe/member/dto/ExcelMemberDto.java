package com.jeja.jejabe.member.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ExcelMemberDto {
    private String name;
    private String birthDate; // 엑셀에서 읽어들인 그대로의 문자열
    private String phone;
    // 초기 status와 role은 고정값으로 주거나, 엑셀에 포함시킬 수 있습니다.
    // 여기서는 기본값을 사용한다고 가정합니다.
}