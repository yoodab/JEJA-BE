package com.jeja.jejabe.club;

import com.jeja.jejabe.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ClubMemberRepository extends JpaRepository<ClubMember, Long> {
    Optional<ClubMember> findByClubAndMember(Club club, Member member);
    List<ClubMember> findAllByMember(Member member); // 내가 속한 팀 조회
    boolean existsByClubIdAndMemberId(Long clubId, Long memberId);

    Optional<Object> findByMemberIdAndClubType(Long memberId, ClubType clubType);

    @Query("SELECT cm.club.id FROM ClubMember cm WHERE cm.member.id = :memberId")
    List<Long> findClubIdsByMemberId(@Param("memberId") Long memberId);

    boolean existsByClubAndMember(Club club, Member member);
}
