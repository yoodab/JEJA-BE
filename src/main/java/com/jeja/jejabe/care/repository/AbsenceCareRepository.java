package com.jeja.jejabe.care.repository;

import com.jeja.jejabe.care.domain.AbsenceCare;
import com.jeja.jejabe.care.domain.CareStatus;
import com.jeja.jejabe.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AbsenceCareRepository extends JpaRepository<AbsenceCare, Long> {

    Optional<AbsenceCare> findByMember(Member member);

    void deleteByMember(Member member);

    @Query("SELECT ac FROM AbsenceCare ac WHERE ac.member = :member AND ac.status <> 'COMPLETED'")
    Optional<AbsenceCare> findActiveByMember(Member member);

    List<AbsenceCare> findAllByMemberOrderByStartDateDesc(Member member);

    @Query("SELECT ac FROM AbsenceCare ac WHERE ac.status <> 'COMPLETED'")
    List<AbsenceCare> findAllActive();

    long countByStatus(CareStatus status);
}
