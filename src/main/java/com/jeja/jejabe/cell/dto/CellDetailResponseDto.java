package com.jeja.jejabe.cell.dto;

import com.jeja.jejabe.cell.Cell;
import com.jeja.jejabe.cell.MemberCellHistory;
import com.jeja.jejabe.member.dto.MemberSimpleDto;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
public class CellDetailResponseDto {

    private final Long cellId;
    private final String cellName;
    private final Integer year;
    private final MemberSimpleDto leader; // 리더 정보
    private final List<MemberSimpleDto> members; // 일반 멤버(순원) 정보

    public CellDetailResponseDto(Cell cell) {
        this.cellId = cell.getCellId();
        this.cellName = cell.getCellName();
        this.year = cell.getYear();

        // Cell에 속한 멤버 기록(History) 중에서 리더와 일반 멤버를 구분
        this.leader = cell.getMemberHistories().stream()
                .filter(MemberCellHistory::isLeader)
                .findFirst()
                .map(history -> new MemberSimpleDto(history.getMember()))
                .orElse(null);

        this.members = cell.getMemberHistories().stream()
                .filter(history -> !history.isLeader())
                .map(history -> new MemberSimpleDto(history.getMember()))
                .collect(Collectors.toList());
    }
}
