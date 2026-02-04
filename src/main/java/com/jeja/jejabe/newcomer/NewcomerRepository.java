package com.jeja.jejabe.newcomer;

import com.jeja.jejabe.newcomer.domain.Newcomer;
import com.jeja.jejabe.newcomer.domain.NewcomerStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface NewcomerRepository extends JpaRepository<Newcomer, Long> {

    long countByRegistrationDateBetween(LocalDate start, LocalDate end);

    Page<Newcomer> findAllByStatusAndRegistrationDateBetween(NewcomerStatus status, LocalDate start, LocalDate end, Pageable pageable);

    // 2. 전체 상태 + 기간 조회 (페이징)
    Page<Newcomer> findAllByRegistrationDateBetween(LocalDate start, LocalDate end, Pageable pageable);
    Page<Newcomer> findAllByStatus(NewcomerStatus status, Pageable pageable);
}
