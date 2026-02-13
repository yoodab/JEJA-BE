package com.jeja.jejabe.cell.dto;

import com.jeja.jejabe.cell.Cell;
import com.jeja.jejabe.cell.MemberCellHistory;
import com.jeja.jejabe.member.dto.MemberDto;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
public class MyCellResponseDto {

    private final Long cellId;
    private final String cellName;
    private final Integer year;
    private final MemberDto leader; // 리더 정보
    private final MemberDto subLeader; // 부리더 정보
    private final List<MemberDto> members; // 셀원 목록

    public MyCellResponseDto(Cell cell, List<MemberCellHistory> histories) {
        this.cellId = cell.getCellId();
        this.cellName = cell.getCellName();
        this.year = cell.getYear();

        // 히스토리 목록에서 리더, 부리더와 일반 멤버를 분리
        this.leader = histories.stream()
                .filter(MemberCellHistory::isLeader)
                .map(history -> new MemberDto(history.getMember()))
                .findFirst()
                .orElse(null);

        this.subLeader = histories.stream()
                .filter(MemberCellHistory::isSubLeader)
                .map(history -> new MemberDto(history.getMember()))
                .findFirst()
                .orElse(null);

        this.members = histories.stream()
                .filter(history -> !history.isLeader() && !history.isSubLeader()) // 리더와 부리더가 아닌 멤버들만 필터링
                .map(history -> new MemberDto(history.getMember()))
                .collect(Collectors.toList());
    }
}
