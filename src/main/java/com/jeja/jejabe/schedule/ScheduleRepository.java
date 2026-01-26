package com.jeja.jejabe.schedule;

import com.jeja.jejabe.schedule.domain.Schedule;
import com.jeja.jejabe.schedule.domain.ScheduleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    // 월별 조회를 위해 모든 일정을 가져오는 기본 findAll 사용 (개선 가능)

// 특정 연월에 해당하는 일정을 조회 (시작일 기준)
    List<Schedule> findAllByStartDateBetween(LocalDateTime startOfMonth, LocalDateTime endOfMonth);
    // 사용자 출석 체크용 쿼리 메소드
    Optional<Schedule> findTopByEndDateBeforeOrderByEndDateDesc(LocalDateTime now);
    List<Schedule> findAllByStartDateBeforeAndEndDateAfter(LocalDateTime start, LocalDateTime end);
    Optional<Schedule> findTopByStartDateAfterOrderByStartDateAsc(LocalDateTime now);

    List<Schedule> findByTypeAndStartDateBetween(ScheduleType scheduleType, LocalDateTime start, LocalDateTime end);

    List<Schedule> findTop10ByTypeOrderByStartDateDesc(ScheduleType scheduleType);

    // [NEW] 최적화된 월별 조회 쿼리
    // 1. 반복 없음(NONE): 조회 기간 내에 포함되는지 확인
    // 2. 반복 있음(!= NONE): 시작일이 조회 종료일 이전이고, (종료일이 없거나 조회 시작일 이후인 경우)
    @Query("SELECT s FROM Schedule s " +
            "LEFT JOIN FETCH s.worshipCategory " +
            "WHERE " +
            "(s.recurrenceRule = 'NONE' AND s.startDate < :endOfMonth AND s.endDate > :startOfMonth) " +
            "OR " +
            "(s.recurrenceRule != 'NONE' AND s.startDate <= :endOfMonth " +
            "AND (s.recurrenceEndDate IS NULL OR s.recurrenceEndDate >= CAST(:startOfMonth AS LocalDate)))")
    List<Schedule> findCandidatesForMonth(@Param("startOfMonth") LocalDateTime startOfMonth,
                                          @Param("endOfMonth") LocalDateTime endOfMonth);
}
