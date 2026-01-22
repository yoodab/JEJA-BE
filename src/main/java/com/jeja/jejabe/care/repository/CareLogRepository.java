package com.jeja.jejabe.care.repository;

import com.jeja.jejabe.care.domain.CareLog;
import com.jeja.jejabe.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CareLogRepository extends JpaRepository<CareLog, Long> {
    List<CareLog> findAllByTargetMemberOrderByCareDateDesc(Member member);
}
