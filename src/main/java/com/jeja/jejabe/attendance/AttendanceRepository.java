package com.jeja.jejabe.attendance;

import com.jeja.jejabe.member.domain.Member;
import com.jeja.jejabe.schedule.domain.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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

    // 특정 기간 동안의 출석 횟수 조회
    int countByMemberAndAttendanceTimeBetween(Member member, LocalDateTime start, LocalDateTime end);

    // 최근 출석 기록 N개 조회
    List<ScheduleAttendance> findTop5ByMemberOrderByAttendanceTimeDesc(Member member);

    List<ScheduleAttendance> findAllBySchedule(Schedule schedule);
}

