package com.jeja.jejabe.newcomer;

import com.jeja.jejabe.newcomer.domain.Newcomer;
import com.jeja.jejabe.newcomer.domain.NewcomerStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface NewcomerRepository extends JpaRepository<Newcomer, Long> {
    // 필요한 쿼리 메소드를 여기에 추가할 수 있습니다.
    // 예: List<Newcomer> findAllByStatus(NewcomerStatus status);
    List<Newcomer> findAllByStatus(NewcomerStatus status);

    long countByRegistrationDateBetween(LocalDate start, LocalDate end);
}
