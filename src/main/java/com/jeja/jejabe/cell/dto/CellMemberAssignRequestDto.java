package com.jeja.jejabe.cell.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CellMemberAssignRequestDto {
    private Long cellId;
    private List<Long> memberIds; // 이 셀에 배정할 멤버들의 ID 목록
}
