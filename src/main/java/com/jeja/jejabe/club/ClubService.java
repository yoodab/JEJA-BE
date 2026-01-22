package com.jeja.jejabe.club;

import com.jeja.jejabe.auth.User;
import com.jeja.jejabe.auth.UserRole;
import com.jeja.jejabe.club.dto.ClubCreateRequestDto;
import com.jeja.jejabe.club.dto.ClubDetailResponseDto;
import com.jeja.jejabe.club.dto.ClubResponseDto;
import com.jeja.jejabe.global.exception.CommonErrorCode;
import com.jeja.jejabe.global.exception.GeneralException;
import com.jeja.jejabe.member.domain.Member;
import com.jeja.jejabe.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ClubService {

    private final ClubRepository clubRepository;
    private final ClubMemberRepository clubMemberRepository;
    private final MemberRepository memberRepository;

    // 팀 생성 (관리자)
    public Long createClub(ClubCreateRequestDto dto) {
        Member leader = memberRepository.findById(dto.getLeaderMemberId())
                .orElseThrow(() -> new GeneralException(CommonErrorCode.MEMBER_NOT_FOUND));

        Club club = Club.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .meetingTime(dto.getMeetingTime())
                .meetingPlace(dto.getMeetingPlace())
                .type(dto.getType())
                .leader(leader)
                .build();

        // 리더도 멤버로 추가
        club.getMembers().add(new ClubMember(club, leader));

        return clubRepository.save(club).getId();
    }

    // 전체 목록 조회
    @Transactional(readOnly = true)
    public List<ClubResponseDto> getAllClubs() {
        return clubRepository.findAll().stream()
                .map(ClubResponseDto::new)
                .collect(Collectors.toList());
    }

    // 내 팀 목록 조회
    @Transactional(readOnly = true)
    public List<ClubResponseDto> getMyClubs(User user) {
        Member member = user.getMember();
        if (member == null) return List.of();

        return clubMemberRepository.findAllByMember(member).stream()
                .map(cm -> new ClubResponseDto(cm.getClub()))
                .collect(Collectors.toList());
    }

    // 상세 조회
    @Transactional(readOnly = true)
    public ClubDetailResponseDto getClubDetail(Long clubId) {
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new IllegalArgumentException("팀을 찾을 수 없습니다."));
        return new ClubDetailResponseDto(club);
    }

    // 멤버 퇴출 (팀장 권한)
    public void removeMember(Long clubId, Long memberId, User currentUser) {
        Club club = clubRepository.findById(clubId).orElseThrow();
        validateLeaderOrAdmin(club, currentUser);

        if (club.getLeader().getId().equals(memberId)) {
            throw new IllegalArgumentException("팀장은 퇴출할 수 없습니다.");
        }

        Member member = memberRepository.findById(memberId).orElseThrow();
        ClubMember clubMember = clubMemberRepository.findByClubAndMember(club, member)
                .orElseThrow(() -> new IllegalArgumentException("팀원이 아닙니다."));

        clubMemberRepository.delete(clubMember);
    }

    // 팀장 양도
    public void changeLeader(Long clubId, Long newLeaderId, User currentUser) {
        Club club = clubRepository.findById(clubId).orElseThrow();
        validateLeaderOrAdmin(club, currentUser);

        Member newLeader = memberRepository.findById(newLeaderId).orElseThrow();

        // 새 팀장이 멤버가 아니면 추가
        if (clubMemberRepository.findByClubAndMember(club, newLeader).isEmpty()) {
            clubMemberRepository.save(new ClubMember(club, newLeader));
        }

        club.changeLeader(newLeader);
    }

    // 권한 체크 헬퍼
    private void validateLeaderOrAdmin(Club club, User user) {
        if (user.getUserRole() == UserRole.ROLE_ADMIN) return;

        Member member = user.getMember();
        if (member == null || !club.getLeader().getId().equals(member.getId())) {
            throw new GeneralException(CommonErrorCode.FORBIDDEN);
        }
    }
}
