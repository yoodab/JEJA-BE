package com.jeja.jejabe.cell.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class CellMemberUpdateRequestDto {
    private Long leaderId;          // 순장 ID
    private Long subLeaderId;       // 부순장 ID
    private List<Long> memberIds;   // 순원 ID 목록
}
