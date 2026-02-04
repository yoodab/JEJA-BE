package com.jeja.jejabe.newcomer.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MdAssignmentRequestDto {
    private Long memberId; // 팀원 ID
    private String charge; // 역할
    private String ageGroup; // 나이대
}
