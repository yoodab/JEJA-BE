package com.jeja.jejabe.cell;

import com.jeja.jejabe.auth.User;
import com.jeja.jejabe.cell.dto.*;
import com.jeja.jejabe.global.exception.CommonErrorCode;
import com.jeja.jejabe.global.exception.GeneralException;
import com.jeja.jejabe.member.domain.Member;
import com.jeja.jejabe.member.domain.MemberRole;
import com.jeja.jejabe.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
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
        Cell cell = Cell.builder()
                .cellName(requestDto.getCellName())
                .year(requestDto.getYear())
                .build();
        Cell savedCell = cellRepository.save(cell);

        // 리더 배정 (Draft 상태로 저장됨)
        if (requestDto.getLeaderMemberId() != null) {
            Member leader = memberRepository.findById(requestDto.getLeaderMemberId())
                    .orElseThrow(() -> new GeneralException(CommonErrorCode.MEMBER_NOT_FOUND));

            MemberCellHistory history = MemberCellHistory.builder()
                    .cell(savedCell)
                    .member(leader)
                    .isLeader(true)
                    .build();

            // [Active Status Check]
            // 시스템에 활성화된 멤버가 없거나(초기 상태), 해당 연도가 활성화된 상태라면 자동 활성화
            boolean anyActive = memberCellHistoryRepository.existsByIsActiveTrue();
            boolean isYearActive = memberCellHistoryRepository.existsByCell_YearAndIsActiveTrue(savedCell.getYear());

            if (!anyActive || isYearActive) {
                history.updateStatus(true);
            }

            memberCellHistoryRepository.save(history);

            // [Role Update] 리더 권한 추가
            leader.addRole(MemberRole.CELL_LEADER);
            memberRepository.save(leader);
        }

        return savedCell.getCellId();
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

        cell.update(requestDto.getCellName());
    }

    // ★★★ 3. Cell 삭제 ★★★
    public void deleteCell(Long cellId) {
        Cell cell = cellRepository.findById(cellId)
                .orElseThrow(() -> new GeneralException(CommonErrorCode.CELL_NOT_FOUND));

        cellRepository.delete(cell);
    }

    @Transactional
    public void updateCellMembersBatch(CellMemberBatchUpdateRequestDto requestDto) {
        // 1. 요청에 포함된 모든 Cell ID와 Member ID 수집
        List<Long> targetCellIds = requestDto.getCellUpdates().stream()
                .map(CellMemberBatchUpdateRequestDto.CellUpdateInfo::getCellId)
                .toList();

        // 2. 요청된 순(Cell) 엔티티들 모두 조회
        List<Cell> targetCells = cellRepository.findAllById(targetCellIds);
        Map<Long, Cell> cellMap = targetCells.stream()
                .collect(Collectors.toMap(Cell::getCellId, c -> c));

        // 3. 해당 순들에 현재 소속된 모든 멤버 기록(History) 조회 (삭제 대상 판별용)
        // (주의: year가 일치하는지 확인 필요, 여기선 targetCells의 year를 기준)
        List<MemberCellHistory> existingHistories = memberCellHistoryRepository
                .findAllByCellInAndIsActiveTrue(targetCells);

        // 4. 업데이트 로직 수행
        // (Set을 이용해 '이번 요청에서 처리된 멤버 ID'를 추적)
        Set<Long> processedMemberIds = new HashSet<>();

        for (CellMemberBatchUpdateRequestDto.CellUpdateInfo info : requestDto.getCellUpdates()) {
            Cell targetCell = cellMap.get(info.getCellId());
            if (targetCell == null)
                continue;

            // 리더 + 순원 ID 합치기
            Set<Long> membersInCell = new HashSet<>();
            if (info.getMemberIds() != null)
                membersInCell.addAll(info.getMemberIds());
            if (info.getLeaderId() != null)
                membersInCell.add(info.getLeaderId());

            // 멤버들 처리
            List<Member> members = memberRepository.findAllById(membersInCell);
            for (Member member : members) {
                boolean isLeader = member.getId().equals(info.getLeaderId());
                processedMemberIds.add(member.getId()); // 처리됨 표시

                // [Role Update] 순장 권한 동기화 (1인 1순 원칙 가정)
                if (isLeader) {
                    member.addRole(MemberRole.CELL_LEADER);
                } else {
                    member.removeRole(MemberRole.CELL_LEADER);
                }

                // 이미 활동 중인 기록이 있는지 전수 조사 (다른 순에서 이동해온 경우 포함)
                Optional<MemberCellHistory> activeHistoryOpt = memberCellHistoryRepository
                        .findByMemberAndIsActiveTrue(member);

                if (activeHistoryOpt.isPresent()) {
                    // [이동/수정]: 기존 기록을 이 셀로 업데이트
                    activeHistoryOpt.get().changeAssignment(targetCell, isLeader);
                } else {
                    // [신규]: 기록 없으면 생성
                    MemberCellHistory newHistory = MemberCellHistory.builder()
                            .cell(targetCell)
                            .member(member)
                            .isLeader(isLeader)
                            .build();

                    // [Active Status Check]
                    boolean anyActive = memberCellHistoryRepository.existsByIsActiveTrue();
                    boolean isYearActive = memberCellHistoryRepository
                            .existsByCell_YearAndIsActiveTrue(targetCell.getYear());

                    if (!anyActive || isYearActive) {
                        newHistory.updateStatus(true);
                    }

                    memberCellHistoryRepository.save(newHistory);
                }
            }
        }

        // 5. [삭제 처리]
        // 기존에 이 순들에 있었는데, 이번 요청(processedMemberIds)에 포함되지 않은 사람 -> 삭제
        // (즉, 명단에서 아예 빠진 사람)
        for (MemberCellHistory history : existingHistories) {
            if (!processedMemberIds.contains(history.getMember().getId())) {
                memberCellHistoryRepository.delete(history);
            }
        }
    }

    public void activateCellsByYear(Integer year) {
        // Step A. 기존에 활성화되어 있던 모든 기록(작년 기록 등)을 비활성화(종료) 처리
        memberCellHistoryRepository.deactivateAllActiveHistories();

        // Step B. 요청된 연도(새해)의 모든 기록을 활성화
        memberCellHistoryRepository.activateHistoriesByYear(year);
    }

}
