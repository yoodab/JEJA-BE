package com.jeja.jejabe.newcomer;

import com.jeja.jejabe.global.exception.CommonErrorCode;
import com.jeja.jejabe.global.exception.GeneralException;
import com.jeja.jejabe.member.domain.Member;
import com.jeja.jejabe.member.MemberRepository;
import com.jeja.jejabe.member.domain.MemberRole;
import com.jeja.jejabe.member.domain.MemberStatus;
import com.jeja.jejabe.newcomer.domain.Newcomer;
import com.jeja.jejabe.newcomer.domain.NewcomerStatus;
import com.jeja.jejabe.newcomer.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NewcomerService {

    private final NewcomerRepository newcomerRepository;
    private final MemberRepository memberRepository;

    // == 쓰기 작업 (Write Operations) ==

    // == 1. 새신자 등록 ==
    @Transactional
    public Long registerNewcomer(NewcomerCreateRequestDto requestDto) {
        Member manager = null;
        if (requestDto.getManagerMemberId() != null) {
            manager = findMemberById(requestDto.getManagerMemberId());
        }

        Newcomer newcomer = Newcomer.builder()
                .name(requestDto.getName())
                .gender(requestDto.getGender())
                .birthDate(requestDto.getBirthDate())
                .phone(requestDto.getPhone())
                .address(requestDto.getAddress())
                .manager(manager)
                .build();

        return newcomerRepository.save(newcomer).getNewcomerId();
    }





    // == 2. 새신자 정보 수정 (이미지, 3단 상태, 담당자 포함) ==
    @Transactional
    public void updateNewcomer(Long newcomerId, NewcomerUpdateRequestDto requestDto) {
        Newcomer newcomer = findNewcomerById(newcomerId);

        Member newManager = null;
        if (requestDto.getManagerMemberId() != null) {
            newManager = findMemberById(requestDto.getManagerMemberId());
        }

        // 엔티티의 통합 수정 메서드 호출
        newcomer.updateInfo(
                requestDto.getAddress(),
                requestDto.getPhone(),
                requestDto.getAssignmentNote(),
                requestDto.getFirstStatus(),
                requestDto.getMiddleStatus(),
                requestDto.getRecentStatus(),
                requestDto.getProfileImageUrl(),
                newManager
        );
    }

    // == 3. 등반 처리 (새신자 -> 청년부 멤버) ==
    @Transactional
    public Long registerNewcomerAsMember(Long newcomerId) {
        Newcomer newcomer = findNewcomerById(newcomerId);

        // 정식 멤버 엔티티 생성
        Member newMember = Member.builder()
                .name(newcomer.getName())
                .gender(newcomer.getGender())
                .birthDate(newcomer.getBirthDate())
                .phone(newcomer.getPhone())
                .memberStatus(MemberStatus.ACTIVE)
                .memberImageUrl(newcomer.getProfileImageUrl())
                .role(MemberRole.MEMBER)
                .build();

        Member savedMember = memberRepository.save(newMember);

        // 새신자 엔티티에 멤버 연결 및 상태 변경(GRADUATED)
        newcomer.registerAsMember(savedMember);

        return savedMember.getId();
    }

    // == 4. 관리 상태 변경 (MANAGING, STOPPED 등) ==
    @Transactional
    public void changeNewcomerStatus(Long newcomerId, NewcomerStatus newStatus) {
        Newcomer newcomer = findNewcomerById(newcomerId);
        newcomer.changeStatus(newStatus);
    }

    // == 조회 로직 ==
    public List<NewcomerListResponseDto> getNewcomerList(NewcomerStatus status) {
        List<Newcomer> newcomers = (status != null)
                ? newcomerRepository.findAllByStatus(status)
                : newcomerRepository.findAll();

        return newcomers.stream()
                .map(NewcomerListResponseDto::new)
                .collect(Collectors.toList());
    }

    public NewcomerDetailResponseDto getNewcomerDetails(Long newcomerId) {
        Newcomer newcomer = findNewcomerById(newcomerId);
        return new NewcomerDetailResponseDto(newcomer); // DTO 생성자에서 first, middle, recent 매핑 필요
    }

    // == Helper Methods ==
    private Newcomer findNewcomerById(Long newcomerId) {
        return newcomerRepository.findById(newcomerId)
                .orElseThrow(() -> new GeneralException(CommonErrorCode.NEWCOMER_NOT_FOUND));
    }

    private Member findMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(CommonErrorCode.MEMBER_NOT_FOUND));
    }
}