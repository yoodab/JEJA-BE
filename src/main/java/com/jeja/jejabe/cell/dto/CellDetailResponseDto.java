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
    private final boolean isActive;
    private final List<MemberSimpleDto> members; // 일반 멤버(순원) 정보

    public CellDetailResponseDto(Cell cell) {
        this.cellId = cell.getCellId();
        this.cellName = cell.getCellName();
        this.year = cell.getYear();


        List<MemberCellHistory> histories = cell.getMemberHistories(); // Getter 이름 확인 필요 (getHistories 또는 getMemberHistories)

        // 1. isActive 설정
        // 히스토리 중 하나라도 가져와서 상태 확인 (모두 상태가 같으므로 첫 번째 것 확인)
        // 멤버가 없는 빈 순이라면 기본값 false (비활성) 처리
        this.isActive = histories.stream()
                .findFirst()
                .map(MemberCellHistory::isActive)
                .orElse(false);

        // 2. 리더 정보 추출
        this.leader = histories.stream()
                .filter(MemberCellHistory::isLeader)
                .findFirst()
                .map(history -> new MemberSimpleDto(history.getMember()))
                .orElse(null);

        // 3. 일반 순원 정보 추출
        this.members = histories.stream()
                .filter(history -> !history.isLeader())
                .map(history -> new MemberSimpleDto(history.getMember()))
                .collect(Collectors.toList());
    }
}
