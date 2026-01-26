package com.jeja.jejabe.schedule;

import com.jeja.jejabe.album.Album;
import com.jeja.jejabe.album.AlbumRepository;
import com.jeja.jejabe.album.PermissionType;
import com.jeja.jejabe.attendance.AttendanceRepository;
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
    private final WorshipCategoryRepository worshipCategoryRepository;
    // [New] 추가된 의존성
    private final AlbumRepository albumRepository;
    private final AttendanceRepository attendanceRepository;

    // --- 1. 조회 (Read) ---
    @Transactional(readOnly = true)
    public List<ScheduleResponseDto> getSchedulesByMonth(int year, int month, UserDetailsImpl userDetails) {
        YearMonth ym = YearMonth.of(year, month);
        LocalDateTime start = ym.atDay(1).atStartOfDay();
        LocalDateTime end = ym.atEndOfMonth().atTime(23, 59, 59);

        List<Schedule> candidates = scheduleRepository.findCandidatesForMonth(start, end);

        List<ScheduleResponseDto> expandedSchedules = candidates.stream()
                .flatMap(s -> RecurrenceCalculator.generateSchedules(s, year, month).stream())
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
                .map(att -> new AttendanceRecordDto(
                        att.getMember().getId(),
                        att.getMember().getName(),
                        true,
                        att.getAttendanceTime().toLocalTime().toString()
                ))
                .collect(Collectors.toList());

        return new ScheduleDetailResponseDto(schedule, linkedAlbumId, attendees);
    }

    // --- 2. 등록 (Create) ---
    public Long createSchedule(ScheduleCreateRequestDto requestDto) {
        WorshipCategory category = resolveCategory(requestDto.getWorshipCategoryId());

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
                .worshipCategory(category)
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
            WorshipCategory category = resolveCategory(dto.getWorshipCategoryId());
            schedule.update(dto, category);
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
    private WorshipCategory resolveCategory(Long id) {
        if (id == null) return null;
        return worshipCategoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid WorshipCategory ID"));
    }

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