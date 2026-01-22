package com.jeja.jejabe.cell;

import com.jeja.jejabe.auth.User;
import com.jeja.jejabe.cell.dto.CellCreateRequestDto;
import com.jeja.jejabe.cell.dto.CellDetailResponseDto;
import com.jeja.jejabe.cell.dto.CellUpdateRequestDto;
import com.jeja.jejabe.cell.dto.MyCellResponseDto;
import com.jeja.jejabe.global.exception.CommonErrorCode;
import com.jeja.jejabe.global.exception.GeneralException;
import com.jeja.jejabe.member.domain.Member;
import com.jeja.jejabe.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CellService {

    private final CellRepository cellRepository;
    private final MemberRepository memberRepository;
    private final MemberCellHistoryRepository memberCellHistoryRepository;

    // 1. 새로운 Cell 생성 (리더 지정 포함)
    public Long createCell(CellCreateRequestDto requestDto) {
        // a. 새로운 Cell 객체 생성 및 저장
        Cell newCell = Cell.builder()
                .cellName(requestDto.getCellName())
                .year(requestDto.getYear())
                .build();
        Cell savedCell = cellRepository.save(newCell);

        // b. 지정된 리더(순장)를 찾아 Cell에 배정
        Member leader = memberRepository.findById(requestDto.getLeaderMemberId())
                .orElseThrow(() -> new GeneralException(CommonErrorCode.MEMBER_NOT_FOUND));

        // c. 리더의 기존 셀 활동을 종료시킴 (있을 경우)
        terminatePreviousCellActivity(leader);

        // d. 새로운 셀 배정 기록 생성 (리더로 지정)
        MemberCellHistory leaderHistory = MemberCellHistory.builder()
                .member(leader)
                .cell(savedCell)
                .startDate(LocalDate.now())
                .isLeader(true) // ★★★ 리더로 지정 ★★★
                .build();
        memberCellHistoryRepository.save(leaderHistory);

        return savedCell.getCellId();
    }

    // 2. 특정 Cell에 멤버(순원) 배정
    public void assignMembersToCell(Long cellId, List<Long> memberIds) {
        Cell cell = cellRepository.findById(cellId)
                .orElseThrow(() -> new GeneralException(CommonErrorCode.CELL_NOT_FOUND)); // ★★★ 에러코드 추가 필요

        List<Member> membersToAssign = memberRepository.findAllById(memberIds);

        for (Member member : membersToAssign) {
            // a. 멤버의 기존 셀 활동을 종료시킴
            terminatePreviousCellActivity(member);

            // b. 새로운 셀 배정 기록 생성 (일반 순원으로 지정)
            MemberCellHistory newHistory = MemberCellHistory.builder()
                    .member(member)
                    .cell(cell)
                    .startDate(LocalDate.now())
                    .isLeader(false) // ★★★ 일반 멤버로 지정 ★★★
                    .build();
            memberCellHistoryRepository.save(newHistory);
        }
    }

    @Transactional(readOnly = true)
    public MyCellResponseDto getMyCellInfo(User user) {
        // 1. 로그인한 User와 연결된 Member를 찾는다.
        Member currentMember = user.getMember();
        if (currentMember == null) {
            // 이 경우는 User는 있지만 Member 정보가 없는 비정상적인 상황
            throw new GeneralException(CommonErrorCode.MEMBER_NOT_FOUND);
        }

        // 2. 해당 Member의 현재 활동 중인 셀 기록을 찾는다.
        MemberCellHistory activeHistory = memberCellHistoryRepository.findByMemberAndIsActiveTrue(currentMember)
                .orElseThrow(() -> new GeneralException(CommonErrorCode.NOT_ASSIGNED_TO_CELL)); // ★★★ 에러코드 추가 필요

        // 3. 찾은 기록을 통해 현재 속한 Cell 객체를 가져온다.
        Cell myCell = activeHistory.getCell();

        // 4. 해당 Cell에 속한 모든 '활동 중인' 멤버 기록들을 조회한다.
        List<MemberCellHistory> allHistoriesInMyCell = memberCellHistoryRepository.findAllByCellAndIsActiveTrue(myCell);

        // 5. 조회된 정보를 DTO로 변환하여 반환한다.
        return new MyCellResponseDto(myCell, allHistoriesInMyCell);
    }

    // ★★★ 1. 특정 연도의 모든 Cell 목록 조회 ★★★
    @Transactional(readOnly = true)
    public List<CellDetailResponseDto> getAllCellsByYear(Integer year) {
        List<Cell> cells = cellRepository.findAllByYear(year);
        return cells.stream()
                .map(CellDetailResponseDto::new)
                .collect(Collectors.toList());
    }

    // ★★★ 2. Cell 정보 수정 ★★★
    public void updateCell(Long cellId, CellUpdateRequestDto requestDto) {
        Cell cell = cellRepository.findById(cellId)
                .orElseThrow(() -> new GeneralException(CommonErrorCode.CELL_NOT_FOUND));

        cell.update(requestDto.getCellName(), requestDto.getYear());
    }

    // ★★★ 3. Cell 삭제 ★★★
    public void deleteCell(Long cellId) {
        // 해당 Cell에 배정된 멤버가 있는지 확인하는 로직 추가 가능
        // (MemberCellHistory에 데이터가 있으면 삭제를 막는 등)
        Cell cell = cellRepository.findById(cellId)
                .orElseThrow(() -> new GeneralException(CommonErrorCode.CELL_NOT_FOUND));

        // 연관된 MemberCellHistory도 함께 삭제됨 (CascadeType.ALL)
        cellRepository.delete(cell);
    }


    // (Helper Method) 멤버의 이전 셀 활동을 종료시키는 로직
    private void terminatePreviousCellActivity(Member member) {
        memberCellHistoryRepository.findByMemberAndIsActiveTrue(member)
                .ifPresent(history -> history.endActivity(LocalDate.now().minusDays(1)));
    }
}
