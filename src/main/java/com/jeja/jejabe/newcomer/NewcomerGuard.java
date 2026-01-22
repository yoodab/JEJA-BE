package com.jeja.jejabe.newcomer;

import com.jeja.jejabe.auth.User;
import com.jeja.jejabe.auth.UserDetailsImpl;
import com.jeja.jejabe.auth.UserRole;
import com.jeja.jejabe.club.ClubMemberRepository;
import com.jeja.jejabe.club.ClubType;
import com.jeja.jejabe.member.MemberRepository;
import com.jeja.jejabe.member.domain.Member;
import com.jeja.jejabe.member.domain.MemberRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("newcomerGuard") // Spring Security SpEL에서 사용 가능하게 이름 지정
@RequiredArgsConstructor
public class NewcomerGuard {

    private final ClubMemberRepository clubMemberRepository;
    private final MemberRepository memberRepository;

    /**
     * 새신자 관리 기능 접근 권한 체크 (조회/등록/수정 등)
     * - 목사님/임원: 프리패스
     * - 새신자팀원: 가능
     * - 그 외: 불가
     */
    @Transactional(readOnly = true)
    public boolean canAccess(UserDetailsImpl userDetails) {
        if (userDetails == null) return false;

        User user = userDetails.getUser();
        Long memberId = user.getMember().getId(); // UserDetails 구현에 따라 수정
        Member member = memberRepository.findById(memberId)
                .orElse(null);

        if (member == null) return false;

        // 1. 전역 권한 체크 (목사님, 임원 프리패스)
        if (user.getUserRole() == UserRole.ROLE_PASTOR || member.getRoles().contains(MemberRole.EXECUTIVE)) {
            return true;
        }

        // 2. 새신자팀(NEW_BELIEVER) 소속 여부 체크
        // (팀원이기만 하면 되므로 Role 체크는 별도로 안 함, 필요하면 추가)
        return clubMemberRepository.findByMemberIdAndClubType(memberId, ClubType.NEW_BELIEVER).isPresent();
    }

    // 필요하다면 canDelete 등 더 세부적인 메소드 추가 가능
}
