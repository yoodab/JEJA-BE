package com.jeja.jejabe.form;

import com.jeja.jejabe.auth.UserDetailsImpl;
import com.jeja.jejabe.auth.UserRole;
import com.jeja.jejabe.club.ClubMemberRepository;
import com.jeja.jejabe.form.domain.AccessType;
import com.jeja.jejabe.form.domain.FormAccess;
import com.jeja.jejabe.form.domain.FormTemplate;
import com.jeja.jejabe.form.domain.TargetType;
import com.jeja.jejabe.form.repository.FormTemplateRepository;
import com.jeja.jejabe.member.domain.Member;
import com.jeja.jejabe.member.domain.MemberRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Component("formGuard")
@RequiredArgsConstructor
public class FormGuard {

    private final FormTemplateRepository templateRepository;
    private final ClubMemberRepository clubMemberRepository;

    // 폼 제출 권한 체크 (RESPONDENT)
    @Transactional(readOnly = true)
    public boolean canSubmit(Long templateId, UserDetailsImpl userDetails) {
        FormTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new IllegalArgumentException("템플릿을 찾을 수 없습니다."));

        return checkPermission(template, userDetails, AccessType.RESPONDENT);
    }

    // 폼 결과 조회/관리 권한 체크 (MANAGER)
    @Transactional(readOnly = true)
    public boolean canManage(Long templateId, UserDetailsImpl userDetails) {
        if (userDetails == null) return false;

        FormTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new IllegalArgumentException("템플릿을 찾을 수 없습니다."));

        return checkPermission(template, userDetails, AccessType.MANAGER);
    }

    // 내부 로직 (Service에서 이관)
    private boolean checkPermission(FormTemplate template, UserDetailsImpl userDetails, AccessType requiredType) {
        // 1. 관리자 프리패스
        if (userDetails != null && userDetails.getUser().getUserRole() == UserRole.ROLE_ADMIN) return true;

        // 2. 비로그인 유저(GUEST) 체크
        if (userDetails == null) {
            return template.getAccessList().stream()
                    .anyMatch(acc -> acc.getTargetType() == TargetType.GUEST);
        }

        Member member = userDetails.getUser().getMember();

        for (FormAccess access : template.getAccessList()) {
            // 요구하는 권한 타입과 다르면서, 관리자 권한도 아니면 스킵
            if (access.getAccessType() != requiredType && access.getAccessType() != AccessType.MANAGER) {
                continue;
            }

            switch (access.getTargetType()) {
                case GUEST:
                case ALL:
                    return true;

                case ROLE:
                    String targetRole = access.getTargetValue();
                    if (userDetails.getUser().getUserRole().name().equals(targetRole)) return true;
                    if (member != null) {
                        Set<MemberRole> myRoles = member.getRoles();
                        if (myRoles.stream().anyMatch(r -> r.name().equals(targetRole))) return true;
                    }
                    break;

                case USER:
                    if (member != null && String.valueOf(member.getId()).equals(access.getTargetValue())) {
                        return true;
                    }
                    break;

                case CLUB:
                    if (member != null && clubMemberRepository.existsByClubIdAndMemberId(
                            Long.parseLong(access.getTargetValue()), member.getId())) {
                        return true;
                    }
                    break;
            }
        }
        return false;
    }
}
