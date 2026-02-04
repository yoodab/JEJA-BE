package com.jeja.jejabe.cell;

import com.jeja.jejabe.member.domain.Member;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberCellHistoryRepository extends JpaRepository<MemberCellHistory, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from MemberCellHistory m where m.member = :member and m.isActive = true")
    Optional<MemberCellHistory> findByMemberAndIsActiveTrue(@Param("member") Member member);

    List<MemberCellHistory> findAllByCellAndIsActiveTrue(Cell cell);

    List<MemberCellHistory> findAllByMemberAndIsActiveTrue(Member member);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<MemberCellHistory> findAllByCell(Cell cell);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<MemberCellHistory> findByMemberAndCell_Year(Member member, Integer year);

    List<MemberCellHistory> findAllByCellInAndIsActiveTrue(List<Cell> cells);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE MemberCellHistory h SET h.isActive = false WHERE h.isActive = true")
    void deactivateAllActiveHistories();

    @Modifying(clearAutomatically = true)
    @Query("UPDATE MemberCellHistory h SET h.isActive = true WHERE h.cell.year = :year")
    void activateHistoriesByYear(@Param("year") Integer year);

    boolean existsByCell_YearAndIsActiveTrue(Integer year);

    boolean existsByIsActiveTrue();
}
