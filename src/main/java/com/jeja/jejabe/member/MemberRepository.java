package com.jeja.jejabe.member;

import com.jeja.jejabe.club.ClubType;
import com.jeja.jejabe.member.domain.Member;
import com.jeja.jejabe.member.domain.MemberStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByNameAndBirthDate(String name, String birthDate);

    Optional<Member> findByNameAndPhone(String name, String phone);
    // 특정 상태의 모든 멤버를 조회
    List<Member> findAllByMemberStatus(MemberStatus status);

    Optional<Member> findByPhone(String phone);

    List<Member> findAllByMemberStatusNotIn(List<MemberStatus> inactive);

    // 미배정 인원 조회: 현재 활성화된 CellHistory가 없는 멤버
    @Query("SELECT m FROM Member m WHERE m.memberStatus = 'ACTIVE' AND m.id NOT IN " +
            "(SELECT h.member.id FROM MemberCellHistory h WHERE h.isActive = true)")
    List<Member> findUnassignedMembers();

    // 새신자팀(특정 클럽)에 속한 멤버 조회 (담당 MD 후보용)
    @Query("SELECT cm.member FROM ClubMember cm WHERE cm.club.type = :clubType")
    List<Member> findByClubType(@Param("clubType") ClubType clubType);

    @Query("SELECT m FROM Member m WHERE " +
            "(:keyword IS NULL OR m.name LIKE %:keyword% OR m.phone LIKE %:keyword%) " +
            "AND m.memberStatus NOT IN :excluded")
    Page<Member> findAllByKeyword(
            @Param("keyword") String keyword,
            @Param("excluded") List<MemberStatus> excluded, // 여기로 Enum 리스트를 받습니다.
            Pageable pageable
    );
}
