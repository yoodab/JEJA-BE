package com.jeja.jejabe.schedule;

import com.jeja.jejabe.album.Album;
import com.jeja.jejabe.album.AlbumRepository;
import com.jeja.jejabe.album.PermissionType;
import com.jeja.jejabe.attendance.AttendanceRepository;
import com.jeja.jejabe.attendance.AttendanceStatus;
import com.jeja.jejabe.attendance.ScheduleAttendance;
import com.jeja.jejabe.attendance.dto.AttendanceRecordDto;
import com.jeja.jejabe.auth.UserDetailsImpl;
import com.jeja.jejabe.global.exception.CommonErrorCode;
import com.jeja.jejabe.global.exception.GeneralException;
import com.jeja.jejabe.schedule.domain.RecurrenceRule;
import com.jeja.jejabe.schedule.domain.Schedule;
import com.jeja.jejabe.schedule.domain.SharingScope;
import com.jeja.jejabe.schedule.domain.WorshipCategory;
import com.jeja.jejabe.schedule.dto.*;
import com.jeja.jejabe.schedule.util.RecurrenceCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final AlbumRepository albumRepository;
    private final AttendanceRepository attendanceRepository;

    // --- 1. 조회 (Read) ---
    @Transactional(readOnly = true)
    public List<ScheduleResponseDto> getSchedules(int year, int month, Integer day, UserDetailsImpl userDetails) {
        List<ScheduleResponseDto> expandedSchedules = getRawSchedules(year, month, day);
        return filterBySharingScope(expandedSchedules, userDetails);
    }

    // [New] 관리자용 전체 조회 (권한 필터링 없음)
    @Transactional(readOnly = true)
    public List<ScheduleResponseDto> getAdminSchedules(int year, int month, Integer day) {
        return getRawSchedules(year, month, day);
    }

    private List<ScheduleResponseDto> getRawSchedules(int year, int month, Integer day) {
        LocalDateTime searchStart;
        LocalDateTime searchEnd;

        // 날짜(day)가 있으면 해당 일 하루만 조회, 없으면 월 전체 조회
        if (day != null) {
            LocalDate date = LocalDate.of(year, month, day);
            searchStart = date.atStartOfDay();
            searchEnd = date.atTime(23, 59, 59);
        } else {
            YearMonth ym = YearMonth.of(year, month);
            searchStart = ym.atDay(1).atStartOfDay();
            searchEnd = ym.atEndOfMonth().atTime(23, 59, 59);
        }

        // Repository는 범위 내에 걸치는 원본 일정들을 가져옴
        List<Schedule> candidates = scheduleRepository.findCandidatesForMonth(searchStart, searchEnd);

        // Calculator에 '조회 범위'를 직접 전달하도록 수정
        return candidates.stream()
                .flatMap(s -> RecurrenceCalculator.generateSchedules(s, searchStart, searchEnd).stream())
                .collect(Collectors.toList());
    }

    // [New] 다가오는 일정 조회 (로그인 여부에 따라 Public 또는 Public+Member 일정 합쳐서 최신순 3개)
    @Transactional(readOnly = true)
    public List<ScheduleResponseDto> getUpcomingSchedules(UserDetailsImpl userDetails) {
        LocalDateTime now = LocalDateTime.now();
        // 검색 범위: 오늘부터 3개월 뒤까지 (충분한 미래 범위)
        LocalDateTime searchEnd = now.plusMonths(3);

        // 1. 후보 일정 조회
        List<Schedule> candidates = scheduleRepository.findCandidatesForMonth(now, searchEnd);

        // 2. 인스턴스 생성 및 필터링 (미래 일정만)
        List<ScheduleResponseDto> allFutureSchedules = candidates.stream()
                .flatMap(s -> RecurrenceCalculator.generateSchedules(s, now, searchEnd).stream())
                .filter(s -> s.getStartDate().isAfter(now)) // 현재 시점 이후인 것만
                .sorted((s1, s2) -> s1.getStartDate().compareTo(s2.getStartDate())) // 시간순 정렬
                .collect(Collectors.toList());

        // 3. 권한에 따른 필터링 및 상위 3개 추출
        return allFutureSchedules.stream()
                .filter(s -> {
                    // Public은 무조건 포함
                    if (s.getSharingScope() == SharingScope.PUBLIC) {
                        return true;
                    }
                    // 로그인한 경우 Member 포함
                    if (userDetails != null && s.getSharingScope() == SharingScope.LOGGED_IN_USERS) {
                        return true;
                    }
                    return false;
                })
                .limit(3)
                .collect(Collectors.toList());
    }

    // [New] 일정 상세 조회 (앨범 정보 + 출석 명단)
    @Transactional(readOnly = true)
    public ScheduleDetailResponseDto getScheduleDetail(Long scheduleId, LocalDate targetDate,
            UserDetailsImpl userDetails) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new GeneralException(CommonErrorCode.SCHEDULE_NOT_FOUND));

        // 1. 연결된 앨범 조회
        Long linkedAlbumId = null;
        if (schedule.getAlbum() != null) {
            linkedAlbumId = schedule.getAlbum().getAlbumId();
        }

        // 2. 출석 명단 조회
        List<ScheduleAttendance> attendanceEntities;
        if (targetDate != null) {
            attendanceEntities = attendanceRepository.findAllByScheduleAndScheduleDate(schedule, targetDate);
        } else {
            attendanceEntities = attendanceRepository.findAllBySchedule(schedule);
        }

        List<AttendanceRecordDto> attendees = attendanceEntities.stream()
                .map(att -> {
                    // (1) 출석 시간 처리: null이면 "-" 또는 null로 반환
                    String timeStr = (att.getAttendanceTime() != null)
                            ? att.getAttendanceTime().toLocalTime().toString()
                            : "-";

                    // (2) 출석 여부 처리
                    boolean isAttended = att.getStatus() == AttendanceStatus.PRESENT;

                    return new AttendanceRecordDto(
                            att.getMember().getId(),
                            att.getMember().getName(),
                            att.getMember().getPhone(),
                            isAttended, // 기존 true -> 실제 상태로 변경 권장
                            timeStr // null 체크 적용됨
                    );
                })
                .collect(Collectors.toList());

        // 반복 일정인 경우 특정 날짜의 발생 정보를 찾아서 반환
        if (targetDate != null && schedule.getRecurrenceRule() != RecurrenceRule.NONE) {
            LocalDateTime rangeStart = targetDate.atStartOfDay();
            LocalDateTime rangeEnd = targetDate.atTime(LocalTime.MAX);
            List<ScheduleResponseDto> occurrences = RecurrenceCalculator.generateSchedules(schedule, rangeStart,
                    rangeEnd);

            if (!occurrences.isEmpty()) {
                ScheduleResponseDto occurrence = occurrences.get(0);
                return new ScheduleDetailResponseDto(schedule, occurrence.getStartDate(), occurrence.getEndDate(),
                        linkedAlbumId, attendees);
            }
        }

        return new ScheduleDetailResponseDto(schedule, linkedAlbumId, attendees);
    }

    // --- 2. 등록 (Create) ---
    public Long createSchedule(ScheduleCreateRequestDto requestDto) {

        Schedule schedule = Schedule.builder()
                .title(requestDto.getTitle())
                .content(requestDto.getContent())
                .startDate(requestDto.getStartDate())
                .endDate(requestDto.getEndDate())
                .type(requestDto.getType())
                .location(requestDto.getLocation())
                .sharingScope(requestDto.getSharingScope())
                .recurrenceRule(requestDto.getRecurrenceRule())
                .recurrenceEndDate(requestDto.getRecurrenceEndDate())
                .recurrenceDays(requestDto.getRecurrenceDays())
                .worshipCategory(requestDto.getWorshipCategory())
                .build();

        Schedule savedSchedule = scheduleRepository.save(schedule);

        // [New] 앨범 동시 생성 로직
        if (requestDto.isCreateAlbum()) {
            createLinkedAlbum(savedSchedule);
        }

        return savedSchedule.getScheduleId();
    }

    // [New] 앨범 생성 헬퍼 메서드
    private void createLinkedAlbum(Schedule schedule) {
        Album album = Album.builder()
                .title(schedule.getTitle()) // 일정 제목을 앨범 제목으로 사용
                .description(schedule.getContent())
                .schedule(schedule)
                .readPermission(PermissionType.PUBLIC_READ) // 기본 권한 설정
                .writePermission(PermissionType.MEMBERS_WRITE)
                .build();
        albumRepository.save(album);
    }

    // --- 3. 수정 (Update) ---
    public void updateSchedule(Long scheduleId, ScheduleUpdateRequestDto dto) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new GeneralException(CommonErrorCode.SCHEDULE_NOT_FOUND));

        if (schedule.getRecurrenceRule() == RecurrenceRule.NONE || dto.getUpdateType() == UpdateType.ALL) {
            schedule.update(dto);
            return;
        }

        if (dto.getTargetDate() == null) {
            throw new IllegalArgumentException("반복 일정 수정 시 targetDate는 필수입니다.");
        }

        switch (dto.getUpdateType()) {
            case FUTURE -> {
                schedule.changeRecurrenceEndDate(dto.getTargetDate().minusDays(1));
                // 향후 일정 생성 (앨범 생성 옵션은 기본값 false로 처리됨)
                createSchedule(ScheduleCreateRequestDto.from(dto).build());
            }
            case THIS_ONLY -> {
                schedule.addExceptionDate(dto.getTargetDate());
                createSchedule(ScheduleCreateRequestDto.from(dto)
                        .recurrenceRule(RecurrenceRule.NONE)
                        .recurrenceEndDate(null)
                        .build());
            }
        }
    }

    // --- 4. 삭제 (Delete) ---
    public void deleteSchedule(Long scheduleId, UpdateType updateType, LocalDate targetDate) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new GeneralException(CommonErrorCode.SCHEDULE_NOT_FOUND));

        if (schedule.getRecurrenceRule() == RecurrenceRule.NONE || updateType == UpdateType.ALL) {
            scheduleRepository.delete(schedule);
            return;
        }

        if (targetDate == null) {
            throw new IllegalArgumentException("반복 일정 삭제 시 targetDate는 필수입니다.");
        }

        switch (updateType) {
            case FUTURE -> {
                schedule.changeRecurrenceEndDate(targetDate.minusDays(1));
            }
            case THIS_ONLY -> {
                schedule.addExceptionDate(targetDate);
            }
        }
    }

    // --- Helper Methods ---

    private List<ScheduleResponseDto> filterBySharingScope(List<ScheduleResponseDto> list, UserDetailsImpl user) {
        if (user == null) {
            return list.stream().filter(s -> s.getSharingScope() == SharingScope.PUBLIC).collect(Collectors.toList());
        }
        // 로그인한 사용자 (관리자 포함) -> 일반 조회에서는 PUBLIC + LOGGED_IN_USERS만 노출
        // 관리자용(PRIVATE 포함) 조회는 별도 API(getAdminSchedules) 사용
        return list.stream()
                .filter(s -> s.getSharingScope() == SharingScope.PUBLIC
                        || s.getSharingScope() == SharingScope.LOGGED_IN_USERS)
                .collect(Collectors.toList());
    }
}