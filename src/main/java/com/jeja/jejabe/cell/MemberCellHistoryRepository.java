package com.jeja.jejabe.cell;

import com.jeja.jejabe.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberCellHistoryRepository extends JpaRepository<MemberCellHistory, Long> {

    // 특정 멤버의 현재 활동 중인 셀 기록을 찾는 기능
    Optional<MemberCellHistory> findByMemberAndIsActiveTrue(Member member);

    // 특정 Cell에 속한 모든 '활동 중인' 멤버 기록들을 찾는 기능
    List<MemberCellHistory> findAllByCellAndIsActiveTrue(Cell cell);

    List<MemberCellHistory> findAllByMemberAndIsActiveTrue(Member member);
}
