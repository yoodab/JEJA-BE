package com.jeja.jejabe.cell.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CellCreateRequestDto {
    private String cellName;
    private Integer year;
    private Long leaderMemberId; // 이 셀의 리더(순장)로 지정할 멤버의 ID
    private Long subLeaderMemberId; // 이 셀의 부리더(부순장)로 지정할 멤버의 ID
}
