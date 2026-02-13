package com.jeja.jejabe.club;

import com.jeja.jejabe.auth.User;
import com.jeja.jejabe.auth.UserRole;
import com.jeja.jejabe.club.dto.ClubCreateRequestDto;
import com.jeja.jejabe.club.dto.ClubDetailResponseDto;
import com.jeja.jejabe.club.dto.ClubResponseDto;
import com.jeja.jejabe.club.dto.ClubUpdateRequestDto;
import com.jeja.jejabe.global.exception.CommonErrorCode;
import com.jeja.jejabe.global.exception.GeneralException;
import com.jeja.jejabe.member.domain.Member;
import com.jeja.jejabe.member.domain.MemberRole;
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
        if (member == null)
            return List.of();

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
            ClubMember newClubMember = new ClubMember(club, newLeader);
            clubMemberRepository.save(newClubMember);
            club.getMembers().add(newClubMember); // 리스트에도 추가하여 즉시 반영되도록 함
        }

        club.changeLeader(newLeader);

        // 새 팀장에게 TEAM_LEADER 역할 부여 (없을 경우)
        newLeader.addRole(MemberRole.TEAM_LEADER);
        memberRepository.save(newLeader);
    }

    // 1. 팀 정보 수정 (관리자 or 팀장)
    public void updateClub(Long clubId, ClubUpdateRequestDto dto, User user) {
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new IllegalArgumentException("팀을 찾을 수 없습니다."));

        // 권한 체크 (관리자이거나 해당 팀의 팀장만 가능)
        validateLeaderOrAdmin(club, user);

        club.updateInfo(
                dto.getName(),
                dto.getDescription());
    }

    // 2. 팀 삭제 (관리자 전용)
    public void deleteClub(Long clubId) {
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new IllegalArgumentException("팀을 찾을 수 없습니다."));

        // Cascade 설정이 되어 있다면 ClubMember도 같이 삭제됨
        clubRepository.delete(club);
    }

    // 3. 멤버 직접 추가 (관리자 or 팀장)
    public void addMember(Long clubId, Long memberId, User user) {
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new IllegalArgumentException("팀을 찾을 수 없습니다."));

        validateLeaderOrAdmin(club, user);

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        // 이미 가입된 멤버인지 확인
        if (clubMemberRepository.existsByClubAndMember(club, member)) {
            throw new IllegalArgumentException("이미 해당 팀에 소속된 멤버입니다.");
        }

        // 멤버 저장
        ClubMember clubMember = new ClubMember(club, member);
        clubMemberRepository.save(clubMember);
    }

    @Transactional(readOnly = true)
    public ClubDetailResponseDto getClubByType(ClubType type) {
        Club club = clubRepository.findByType(type)
                .orElseThrow(() -> new IllegalArgumentException("해당 타입의 팀이 존재하지 않습니다. type: " + type));

        return new ClubDetailResponseDto(club);
    }

    // 권한 체크 헬퍼
    private void validateLeaderOrAdmin(Club club, User user) {
        if (user.isPrivileged())
            return;

        Member member = user.getMember();
        if (member == null || club.getLeader() == null || !club.getLeader().getId().equals(member.getId())) {
            throw new GeneralException(CommonErrorCode.FORBIDDEN);
        }
    }
}
