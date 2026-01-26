package com.jeja.jejabe.member;

import com.jeja.jejabe.global.exception.CommonErrorCode;
import com.jeja.jejabe.global.exception.GeneralException;
import com.jeja.jejabe.member.domain.Member;
import com.jeja.jejabe.member.domain.MemberRole;
import com.jeja.jejabe.member.domain.MemberStatus;
import com.jeja.jejabe.member.dto.MemberCreateRequestDto;
import com.jeja.jejabe.member.dto.MemberDto;
import com.jeja.jejabe.member.dto.MemberStatisticsResponse;
import com.jeja.jejabe.member.dto.MemberUpdateRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberService {

    private final MemberRepository memberRepository;



    @Transactional(readOnly = true)
    public Page<MemberDto> getMembers(String keyword, MemberStatus status, Pageable pageable) {

        // 제외할 상태 목록 정의 (INACTIVE, SYSTEM)
        List<MemberStatus> excludedStatuses = List.of(MemberStatus.INACTIVE, MemberStatus.SYSTEM);

        // Repository 호출 시 리스트 전달
        return memberRepository.findAllByKeywordAndStatus(keyword, status, excludedStatuses, pageable)
                .map(MemberDto::new);
    }

    // 2. 특정 멤버 상세 조회
    @Transactional(readOnly = true)
    public MemberDto getMemberById(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(CommonErrorCode.MEMBER_NOT_FOUND)); // ★★★ 에러코드 추가 필요
        return new MemberDto(member);
    }

    // 3. 새 멤버 등록
    public Long createMember(MemberCreateRequestDto requestDto) {

        Member newMember = Member.builder()
                .name(requestDto.getName())
                .phone(requestDto.getPhone())
                .birthDate(requestDto.getBirthDate())
                .memberStatus(requestDto.getMemberStatus())
                .role(MemberRole.MEMBER)
                .gender(requestDto.getGender())
                .memberImageUrl(requestDto.getMemberImageUrl())
                .build();

        Member savedMember = memberRepository.save(newMember);
        return savedMember.getId();
    }

    // 4. 멤버 정보 수정
    public void updateMember(Long memberId, MemberUpdateRequestDto requestDto) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(CommonErrorCode.MEMBER_NOT_FOUND));

        member.update(requestDto); // 엔티티의 비즈니스 메소드 호출
    }



    // 5. 멤버 삭제 (논리적 삭제 또는 물리적 삭제)
    // 여기서는 물리적 삭제로 구현. User와 연관관계가 있으므로 주의 필요
    public void deleteMember(Long memberId) {
        // User와 연결된 경우, User를 먼저 처리해야 함 (정책 결정 필요)
        // 예: User가 있으면 삭제 불가, 또는 User도 함께 삭제
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(CommonErrorCode.MEMBER_NOT_FOUND));

        if (member.getUser() != null) {
            throw new IllegalStateException("웹 계정이 있는 멤버는 삭제할 수 없습니다. 계정을 먼저 비활성화하세요.");
        }

        memberRepository.deleteById(memberId);
    }

    public MemberStatisticsResponse getStatistics() {
        // 1. 제외할 상태 목록 정의 (여기서 관리하니 더 명확함)
        List<MemberStatus> excludedStatuses = List.of(MemberStatus.SYSTEM, MemberStatus.INACTIVE);

        // 2. 파라미터로 전달하여 조회
        List<Object[]> results = memberRepository.countMembersGroupedByMemberStatus(excludedStatuses);

        long activeCount = 0;
        long newcomerCount = 0;
        long inactiveCount = 0;

        for (Object[] result : results) {
            MemberStatus status = (MemberStatus) result[0];
            long count = (Long) result[1];

            switch (status) {
                case ACTIVE -> activeCount += count;
                case NEWCOMER -> newcomerCount += count;
                case LONG_TERM_ABSENT, MOVED -> inactiveCount += count;
                default -> {}
            }
        }

        long totalCount = activeCount + newcomerCount + inactiveCount;

        return MemberStatisticsResponse.builder()
                .totalCount(totalCount)
                .activeCount(activeCount)
                .inactiveCount(inactiveCount)
                .newcomerCount(newcomerCount)
                .build();
    }

    @Transactional(readOnly = true)
    public List<MemberDto> getUnassignedMembers(Integer year) {
        int targetYear = (year != null) ? year : LocalDate.now().getYear();

        return memberRepository.findUnassignedMembersByYear(targetYear).stream()
                .map(MemberDto::new)
                .collect(Collectors.toList());
    }
}
