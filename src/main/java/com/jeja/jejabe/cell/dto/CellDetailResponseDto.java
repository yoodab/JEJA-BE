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
        private final MemberSimpleDto subLeader; // 부리더 정보
        private final boolean isActive;
        private final List<MemberSimpleDto> members; // 일반 멤버(순원) 정보

        public CellDetailResponseDto(Cell cell) {
                this.cellId = cell.getCellId();
                this.cellName = cell.getCellName();
                this.year = cell.getYear();

                // 활성 상태인 히스토리만 필터링
                List<MemberCellHistory> histories = cell.getMemberHistories().stream()
                                .filter(MemberCellHistory::isActive)
                                .collect(Collectors.toList());

                // 1. isActive 설정
                // 히스토리 중 하나라도 가져와서 상태 확인 (활성 멤버가 있으면 활성 순)
                this.isActive = !histories.isEmpty();

                // 2. 리더 정보 추출 (활성 멤버 중 리더)
                this.leader = histories.stream()
                                .filter(MemberCellHistory::isLeader)
                                .findFirst()
                                .map(history -> new MemberSimpleDto(history.getMember()))
                                .orElse(null);

                // 2-1. 부리더 정보 추출 (활성 멤버 중 부리더)
                this.subLeader = histories.stream()
                                .filter(MemberCellHistory::isSubLeader)
                                .findFirst()
                                .map(history -> new MemberSimpleDto(history.getMember()))
                                .orElse(null);

                // 3. 일반 순원 정보 추출 (활성 멤버 중 리더 및 부리더 제외)
                this.members = histories.stream()
                                .filter(history -> !history.isLeader() && !history.isSubLeader())
                                .map(history -> new MemberSimpleDto(history.getMember()))
                                .collect(Collectors.toList());
        }
}
