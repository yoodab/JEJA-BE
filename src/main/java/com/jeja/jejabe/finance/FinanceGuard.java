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
        return userDetails != null && userDetails.getUser().isPrivileged();
    }
}
