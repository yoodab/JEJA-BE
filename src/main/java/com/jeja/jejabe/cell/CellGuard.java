package com.jeja.jejabe.cell;

import com.jeja.jejabe.auth.User;
import com.jeja.jejabe.auth.UserDetailsImpl;
import com.jeja.jejabe.auth.UserRole;
import com.jeja.jejabe.global.exception.CommonErrorCode;
import com.jeja.jejabe.global.exception.GeneralException;
import com.jeja.jejabe.member.domain.Member;
import com.jeja.jejabe.member.domain.MemberRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("cellGuard")
@RequiredArgsConstructor
public class CellGuard {

    private final CellRepository cellRepository;
    private final MemberCellHistoryRepository historyRepository;

    // 셀 정보 수정 권한 (관리자 OR 해당 셀의 현직 순장)
    @Transactional(readOnly = true)
    public boolean canManageCell(Long cellId, UserDetailsImpl userDetails) {
        if (userDetails == null) return false;

        // 1. 관리자 프리패스
        if (isAdmin(userDetails)) return true;

        // 2. 해당 셀의 '현재 활동 중인 리더'인지 확인
        Member member = userDetails.getUser().getMember();
        if (member == null) return false;

        Cell cell = cellRepository.findById(cellId)
                .orElseThrow(() -> new GeneralException(CommonErrorCode.CELL_NOT_FOUND));

        return historyRepository.findByMemberAndIsActiveTrue(member)
                .map(history -> history.isLeader() && history.getCell().getCellId().equals(cellId))
                .orElse(false);
    }

    private boolean isAdmin(UserDetailsImpl userDetails) {
        if (userDetails == null) return false;

        // 1. 시스템 관리자(UserRole) 체크
        UserRole userRole = userDetails.getUser().getUserRole();
        if (userRole == UserRole.ROLE_ADMIN || userRole == UserRole.ROLE_PASTOR) {
            return true;
        }

        // 2. 임원(MemberRole) 체크
        // UserDetailsImpl 안에 이미 authorities가 세팅되어 있으므로 그걸 활용하는 게 가장 깔끔하고 빠릅니다.
        // (DB 조회를 또 하지 않아도 됨)
        return userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_EXECUTIVE"));
    }
}
