package com.jeja.jejabe.attendance;

import com.jeja.jejabe.auth.UserDetailsImpl;
import com.jeja.jejabe.auth.UserRole;
import com.jeja.jejabe.cell.MemberCellHistoryRepository;
import com.jeja.jejabe.member.MemberRepository;
import com.jeja.jejabe.member.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("attendanceGuard")
@RequiredArgsConstructor
public class AttendanceGuard {

    private final MemberRepository memberRepository;
    private final MemberCellHistoryRepository cellHistoryRepository;

    /**
     * 1. 본인 출석 체크 권한 확인
     * (요청하려는 이름/생년월일이 로그인한 본인이 맞는지)
     */
    @Transactional(readOnly = true)
    public boolean canCheckInSelf(String name, String birthDate, UserDetailsImpl userDetails) {
        if (userDetails == null) return false;

        Member currentMember = userDetails.getUser().getMember();
        if (currentMember == null) return false;

        // 로그인한 멤버의 정보와 요청 정보가 일치하는지 확인
        return currentMember.getName().equals(name) &&
                currentMember.getBirthDate().equals(birthDate);
    }

    /**
     * 2. 리더 대리 출석 체크 권한 확인
     * (요청자가 관리자이거나, 대상 멤버의 현재 순장인지)
     */
    @Transactional(readOnly = true)
    public boolean canCheckInOthers(Long targetMemberId, UserDetailsImpl userDetails) {
        if (userDetails == null) return false;

        // 1. 관리자 그룹 프리패스
        if (isAdminGroup(userDetails)) return true;

        // 2. 순장(Leader) 권한 체크
        Member leader = userDetails.getUser().getMember();
        Member target = memberRepository.findById(targetMemberId).orElse(null);
        if (target == null) return false;

        // 리더와 대상 멤버가 같은 셀에 속해있고, 요청자가 그 셀의 리더인지 확인
        return cellHistoryRepository.findByMemberAndIsActiveTrue(leader)
                .flatMap(leaderHistory ->
                        cellHistoryRepository.findByMemberAndIsActiveTrue(target)
                                .map(targetHistory ->
                                        leaderHistory.isLeader() &&
                                                leaderHistory.getCell().equals(targetHistory.getCell())
                                )
                ).orElse(false);
    }

    private boolean isAdminGroup(UserDetailsImpl userDetails) {
        return userDetails != null && userDetails.getUser().isPrivileged();
    }
}
