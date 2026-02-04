package com.jeja.jejabe.finance.repository;

import com.jeja.jejabe.finance.domain.DuesRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DuesRecordRepository extends JpaRepository<DuesRecord, Long> {
    List<DuesRecord> findAllByEventIdOrderByMemberNameAsc(Long eventId);

    void deleteAllByEventId(Long eventId);
}
