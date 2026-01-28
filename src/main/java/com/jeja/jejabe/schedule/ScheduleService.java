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
        List<ScheduleResponseDto> expandedSchedules = candidates.stream()
                .flatMap(s -> RecurrenceCalculator.generateSchedules(s, searchStart, searchEnd).stream())
                .collect(Collectors.toList());

        return filterBySharingScope(expandedSchedules, userDetails);
    }

    // [New] 일정 상세 조회 (앨범 정보 + 출석 명단)
    @Transactional(readOnly = true)
    public ScheduleDetailResponseDto getScheduleDetail(Long scheduleId, UserDetailsImpl userDetails) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new GeneralException(CommonErrorCode.SCHEDULE_NOT_FOUND));

        // 1. 연결된 앨범 조회
        Long linkedAlbumId = null;
        if (schedule.getAlbum() != null) {
            linkedAlbumId = schedule.getAlbum().getAlbumId();
        }

        // 2. 출석 명단 조회
        List<ScheduleAttendance> attendanceEntities = attendanceRepository.findAllBySchedule(schedule);
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
                            timeStr     // null 체크 적용됨
                    );
                })
                .collect(Collectors.toList());

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
        boolean isAdmin = user.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (isAdmin) return list;
        return list.stream()
                .filter(s -> s.getSharingScope() == SharingScope.PUBLIC || s.getSharingScope() == SharingScope.LOGGED_IN_USERS)
                .collect(Collectors.toList());
    }
}