package com.jeja.jejabe.finance;

import com.jeja.jejabe.auth.UserDetailsImpl;
import com.jeja.jejabe.auth.UserRole;
import com.jeja.jejabe.member.domain.MemberRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("financeGuard")
@RequiredArgsConstructor
public class FinanceGuard {

    // 재정 데이터 조회/수정/삭제 권한
    public boolean canManageFinance(UserDetailsImpl userDetails) {
        if (userDetails == null) return false;

        // 1. 시스템 관리자 및 교역자
        UserRole userRole = userDetails.getUser().getUserRole();
        if (userRole == UserRole.ROLE_ADMIN || userRole == UserRole.ROLE_PASTOR) {
            return true;
        }

        // 2. 임원(EXECUTIVE) 권한 체크
        // (필요하다면 재정팀 멤버인지 체크하는 로직을 여기에 추가 가능)
        return userDetails.getUser().getMember().getRoles().contains(MemberRole.EXECUTIVE);
    }
}
