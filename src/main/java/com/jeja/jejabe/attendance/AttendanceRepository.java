package com.jeja.jejabe.attendance;

import com.jeja.jejabe.member.domain.Member;
import com.jeja.jejabe.schedule.domain.Schedule;
import com.jeja.jejabe.schedule.domain.ScheduleType;
import com.jeja.jejabe.schedule.domain.WorshipCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Repository
public interface AttendanceRepository extends JpaRepository<ScheduleAttendance, Long> {

    boolean existsByScheduleAndMember(Schedule schedule, Member member);

    void deleteAllBySchedule(Schedule schedule);

    // IP 주소 기반 중복 확인 (1회만 허용하므로 exists로 충분)
    boolean existsByScheduleAndIpAddress(Schedule schedule, String ipAddress);

    // 특정 멤버의 가장 최근 출석 기록 한 건을 조회 (ScheduleAttendance 기준)
    Optional<ScheduleAttendance> findTopByMemberOrderByAttendanceTimeDesc(Member member);

    Optional<ScheduleAttendance> findByScheduleAndMember(Schedule schedule, Member member);

    // 특정 멤버의 특정 기간 내 'PRESENT' 상태인 출석 횟수 (scheduleDate 기준)
    int countByMemberAndStatusAndScheduleDateBetween(Member member, AttendanceStatus status, LocalDate start, LocalDate end);

    // 특정 멤버의 최근 'PRESENT' 상태인 출석 기록 5개 (scheduleDate 기준)
    List<ScheduleAttendance> findTop5ByMemberAndStatusOrderByScheduleDateDesc(Member member, AttendanceStatus status);

    List<ScheduleAttendance> findAllBySchedule(Schedule schedule);

    // [수정] 날짜까지 포함해서 확인
    boolean existsByScheduleAndMemberAndScheduleDate(Schedule schedule, Member member, LocalDate scheduleDate);

    // [수정] IP 중복 체크도 해당 날짜(회차) 기준으로 해야 함 (지난주에 왔던 IP가 이번주에 못오면 안되니까)
    boolean existsByScheduleAndScheduleDateAndIpAddress(Schedule schedule, LocalDate scheduleDate, String ipAddress);

    // [수정] 특정 날짜의 출석 기록 조회
    Optional<ScheduleAttendance> findByScheduleAndMemberAndScheduleDate(Schedule schedule, Member member, LocalDate scheduleDate);

    // [수정] 특정 날짜의 해당 스케줄 출석 명단 전체 조회
    List<ScheduleAttendance> findAllByScheduleAndScheduleDate(Schedule schedule, LocalDate scheduleDate);


    // [New] 기간 내 출석 완료(PRESENT)된 기록 조회 (Fetch Join으로 최적화)
    @Query("SELECT sa FROM ScheduleAttendance sa " +
            "JOIN FETCH sa.schedule s " +
            "JOIN FETCH sa.member m " +
            "WHERE s.startDate BETWEEN :start AND :end " +
            "AND sa.status = 'PRESENT'")
    List<ScheduleAttendance> findAllPresentBetween(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    // [New] 특정 스케줄 목록에 대한 모든 출석 기록 조회 (일괄 조회용)
    @Query("SELECT sa FROM ScheduleAttendance sa WHERE sa.schedule IN :schedules AND sa.status = 'PRESENT'")
    List<ScheduleAttendance> findAllByScheduleInAndStatusPresent(@Param("schedules") List<Schedule> schedules);

    @Query("SELECT sa FROM ScheduleAttendance sa " +
            "JOIN FETCH sa.schedule s " +
            "JOIN FETCH sa.member m " +
            "WHERE sa.scheduleDate BETWEEN :startDate AND :endDate " + // ★ 핵심 변경
            "AND sa.status = 'PRESENT' " +
            "AND (:types IS NULL OR s.type IN :types) " +
            "AND (:categories IS NULL OR s.worshipCategory IN :categories)")
    List<ScheduleAttendance> findAllPresentWithFilter(
            @Param("startDate") LocalDate startDate,  // 타입 변경
            @Param("endDate") LocalDate endDate,      // 타입 변경
            @Param("types") List<ScheduleType> types,
            @Param("categories") List<WorshipCategory> categories);
}

