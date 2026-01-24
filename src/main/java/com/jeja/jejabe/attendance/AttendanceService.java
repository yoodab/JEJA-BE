package com.jeja.jejabe.attendance;

import com.jeja.jejabe.attendance.dto.AdminAttendanceRequestDto;
import com.jeja.jejabe.attendance.dto.AttendanceRecordDto;
import com.jeja.jejabe.attendance.dto.AttendanceStatusDto;
import com.jeja.jejabe.attendance.dto.CheckInRequestDto;
import com.jeja.jejabe.auth.UserDetailsImpl;
import com.jeja.jejabe.global.exception.CommonErrorCode;
import com.jeja.jejabe.global.exception.GeneralException;
import com.jeja.jejabe.global.util.LocationUtil;
import com.jeja.jejabe.member.MemberRepository;
import com.jeja.jejabe.member.domain.Member;
import com.jeja.jejabe.member.domain.MemberStatus;
import com.jeja.jejabe.schedule.ScheduleRepository;
import com.jeja.jejabe.schedule.domain.Schedule;
import com.jeja.jejabe.schedule.domain.ScheduleType;
import com.jeja.jejabe.schedule.dto.ScheduleResponseDto;
import com.jeja.jejabe.user.dto.MyAttendanceStatResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AttendanceService {

    private final ScheduleRepository scheduleRepository;
    private final AttendanceRepository attendanceRepository;
    private final MemberRepository memberRepository;
    private final LocationUtil locationUtil;

    @Value("${church.location.latitude}")
    private double churchLatitude;
    @Value("${church.location.longitude}")
    private double churchLongitude;
    @Value("${church.location.allowed-radius-meters}")
    private double allowedRadius;
    @Value("${church.attendance.ip-limit-per-schedule}")
    private int ipLimitPerSchedule;

    // 관리자 일괄 출석
    public void checkInByAdmin(Long scheduleId, AdminAttendanceRequestDto requestDto) {
        Schedule schedule = findScheduleById(scheduleId);
        attendanceRepository.deleteAllBySchedule(schedule);

        List<Member> members = memberRepository.findAllById(requestDto.getAttendedMemberIds());
        List<ScheduleAttendance> attendances = members.stream()
                .map(member -> ScheduleAttendance.builder()
                        .schedule(schedule)
                        .member(member)
                        .attendanceTime(LocalDateTime.now())
                        .type(AttendanceType.ADMIN_CHECK)
                        .build())
                .collect(Collectors.toList());

        attendanceRepository.saveAll(attendances);
    }

    // 출석 가능 일정 목록 조회
    @Transactional(readOnly = true)
    public List<ScheduleResponseDto> getCheckableSchedules() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);
        List<Schedule> todaySchedules = scheduleRepository.findAllByStartDateBetween(startOfDay, endOfDay);
        return todaySchedules.stream().map(ScheduleResponseDto::new).collect(Collectors.toList());


    }

    // 사용자 직접 출석
    public void checkIn(Long scheduleId, CheckInRequestDto dto, HttpServletRequest request, UserDetailsImpl userDetails) {
        Schedule schedule = findScheduleById(scheduleId);

        // 1. IP 주소 기반 중복 확인
        String clientIp = getClientIp(request);
        if (attendanceRepository.existsByScheduleAndIpAddress(schedule, clientIp)) {
            throw new GeneralException(CommonErrorCode.IP_ALREADY_USED);
        }

        // 2. 위치 검증
        validateLocation(dto.getLatitude(), dto.getLongitude());
        Member member;
        if (userDetails != null && userDetails.getUser().getMember() != null) {
            // 로그인한 경우: 토큰에 있는 멤버 사용
            member = userDetails.getUser().getMember();
        } else {
            // 비로그인(게스트)인 경우: 이름과 생년월일로 검색
            if (dto.getName() == null || dto.getBirthDate() == null) {
                throw new GeneralException(CommonErrorCode.BAD_REQUEST); // 입력값 부족 에러
            }
            String name = dto.getName();
            LocalDate identifier = dto.getBirthDate();
            member = memberRepository.findByNameAndBirthDate(name, identifier)
                    .orElseThrow(() -> new GeneralException(CommonErrorCode.MEMBER_NOT_FOUND_FOR_CHECK_IN));
        }
        // 4. 멤버 기준 중복 출석 확인
        if (attendanceRepository.existsByScheduleAndMember(schedule, member)) {
            throw new GeneralException(CommonErrorCode.ALREADY_ATTENDED);
        }

        // 5. 출석 기록 생성
        ScheduleAttendance attendance = ScheduleAttendance.builder()
                .schedule(schedule)
                .member(member)
                .attendanceTime(LocalDateTime.now())
                .type(AttendanceType.USER_SELF_CHECK)
                .latitude(dto.getLatitude())
                .longitude(dto.getLongitude())
                .ipAddress(clientIp)
                .build();

        attendanceRepository.save(attendance);
    }

    @Transactional
    public void checkInByLeader(Schedule schedule, Member member) {
        Optional<ScheduleAttendance> existing = attendanceRepository.findByScheduleAndMember(schedule, member);

        if (existing.isPresent()) {
            ScheduleAttendance att = existing.get();
            // 이미 GPS로 출석했으면 -> 교차 검증됨(GPS_AND_LEADER)으로 변경
            if (att.getSource() == AttendanceSource.GPS) {
                att.updateSource(AttendanceSource.GPS_AND_LEADER);
            }
        } else {
            // 기록 없으면 새로 생성 (리더가 보증)
            ScheduleAttendance newAtt = ScheduleAttendance.builder()
                    .schedule(schedule)
                    .member(member)
                    .attendanceTime(LocalDateTime.now())
                    .type(AttendanceType.ADMIN_CHECK)
                    .source(AttendanceSource.LEADER) // ★ 소스: 리더
                    .build();
            attendanceRepository.save(newAtt);
        }
    }

    public List<AttendanceRecordDto> getAttendanceSheet(Long scheduleId) {
        Schedule schedule = findScheduleById(scheduleId);
        List<Member> allMembers = memberRepository.findAllByMemberStatus(MemberStatus.ACTIVE);
        List<ScheduleAttendance> attendances = attendanceRepository.findAllBySchedule(schedule);

        // Map으로 변환하여 조회 속도 향상
        Map<Long, ScheduleAttendance> attendanceMap = attendances.stream()
                .collect(Collectors.toMap(a -> a.getMember().getId(), a -> a));

        return allMembers.stream().map(member -> {
            boolean attended = attendanceMap.containsKey(member.getId());
            String time = attended ? attendanceMap.get(member.getId()).getAttendanceTime().toLocalTime().toString() : null;
            return new AttendanceRecordDto(member.getId(), member.getName(), attended, time);
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public MyAttendanceStatResponseDto getMyAttendanceStats(Member member) {
        LocalDateTime now = LocalDateTime.now();

        // 이번 달
        LocalDateTime startOfMonth = now.withDayOfMonth(1).toLocalDate().atStartOfDay();
        int thisMonth = attendanceRepository.countByMemberAndAttendanceTimeBetween(member, startOfMonth, now);

        // 올해
        LocalDateTime startOfYear = now.withDayOfYear(1).toLocalDate().atStartOfDay();
        int thisYear = attendanceRepository.countByMemberAndAttendanceTimeBetween(member, startOfYear, now);

        // 최근 5개 날짜
        List<String> recentDates = attendanceRepository.findTop5ByMemberOrderByAttendanceTimeDesc(member)
                .stream()
                .map(att -> att.getAttendanceTime().toLocalDate().toString())
                .collect(Collectors.toList());

        return new MyAttendanceStatResponseDto(thisMonth, thisYear, recentDates);
    }

    // [추가] 오늘 출석 여부 로직
    @Transactional(readOnly = true)
    public AttendanceStatusDto getMyTodayAttendance(Member member) {
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = LocalDate.now().atTime(LocalTime.MAX);

        // 오늘 날짜의 '예배' 스케줄에 출석했는지 확인
        // (구체적으로 어떤 예배인지 체크하려면 로직 추가 필요, 여기선 하나라도 있으면 OK)
        List<Schedule> todayWorships = scheduleRepository.findByTypeAndStartDateBetween(ScheduleType.WORSHIP, start, end);

        boolean attended = false;
        LocalDateTime time = null;

        for (Schedule s : todayWorships) {
            Optional<ScheduleAttendance> att = attendanceRepository.findByScheduleAndMember(s, member);
            if (att.isPresent()) {
                attended = true;
                time = att.get().getAttendanceTime();
                break;
            }
        }

        return new AttendanceStatusDto(attended, time, LocalDate.now());
    }

    private Schedule findScheduleById(Long scheduleId) {
        return scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new GeneralException(CommonErrorCode.SCHEDULE_NOT_FOUND));
    }

    private void validateLocation(Double latitude, Double longitude) {
        if (latitude == null || longitude == null) {
            throw new GeneralException(CommonErrorCode.LOCATION_REQUIRED);
        }
        double distance = locationUtil.calculateDistance(churchLatitude, churchLongitude, latitude, longitude);
        if (distance > allowedRadius) {
            throw new GeneralException(CommonErrorCode.TOO_FAR_FROM_CHURCH);
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null) ip = request.getHeader("Proxy-Client-IP");
        if (ip == null) ip = request.getHeader("WL-Proxy-Client-IP");
        if (ip == null) ip = request.getRemoteAddr();
        return ip;
    }
}