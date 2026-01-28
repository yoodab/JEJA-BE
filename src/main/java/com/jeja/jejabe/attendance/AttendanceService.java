package com.jeja.jejabe.attendance;

import com.jeja.jejabe.attendance.dto.*;
import com.jeja.jejabe.auth.UserDetailsImpl;
import com.jeja.jejabe.cell.MemberCellHistoryRepository;
import com.jeja.jejabe.global.exception.CommonErrorCode;
import com.jeja.jejabe.global.exception.GeneralException;
import com.jeja.jejabe.global.util.LocationUtil;
import com.jeja.jejabe.member.MemberRepository;
import com.jeja.jejabe.member.domain.Member;
import com.jeja.jejabe.member.domain.MemberStatus;
import com.jeja.jejabe.schedule.ScheduleRepository;
import com.jeja.jejabe.schedule.domain.RecurrenceRule;
import com.jeja.jejabe.schedule.domain.Schedule;
import com.jeja.jejabe.schedule.domain.ScheduleType;
import com.jeja.jejabe.schedule.domain.WorshipCategory;
import com.jeja.jejabe.schedule.dto.ScheduleResponseDto;
import com.jeja.jejabe.attendance.dto.AttendanceStatisticsResponseDto;
import com.jeja.jejabe.attendance.dto.AttendanceStatisticsResponseDto.ScheduleStatDto;
import com.jeja.jejabe.user.dto.MyAttendanceStatResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AttendanceService {

    private final ScheduleRepository scheduleRepository;
    private final AttendanceRepository attendanceRepository;
    private final MemberRepository memberRepository;
    private final LocationUtil locationUtil;

    private final MemberCellHistoryRepository cellHistoryRepository;

    @Value("${church.location.latitude}")
    private double churchLatitude;
    @Value("${church.location.longitude}")
    private double churchLongitude;
    @Value("${church.location.allowed-radius-meters}")
    private double allowedRadius;
    @Value("${church.attendance.ip-limit-per-schedule}")
    private int ipLimitPerSchedule;


    // [New] 1. 출석부 명단 생성 (미리 인원 등록)
    public void registerAttendees(Long scheduleId, AttendanceRegistrationDto requestDto) {
        Schedule schedule = findScheduleById(scheduleId);
        List<Member> members = memberRepository.findAllById(requestDto.getMemberIds());

        for (Member member : members) {
            // 이미 명단에 있거나 출석한 경우 건너뜀
            if (attendanceRepository.existsByScheduleAndMember(schedule, member)) {
                continue;
            }

            ScheduleAttendance registration = ScheduleAttendance.builder()
                    .schedule(schedule)
                    .member(member)
                    .status(AttendanceStatus.REGISTERED) // 상태: 등록됨(미출석)
                    .attendanceTime(null) // 시간 없음
                    .build();
            attendanceRepository.save(registration);
        }
    }

    public void removeRegisteredMembers(Long scheduleId, AttendanceRemoveRequestDto requestDto) {
        Schedule schedule = findScheduleById(scheduleId);
        List<Member> members = memberRepository.findAllById(requestDto.getMemberIds());

        for (Member member : members) {
            Optional<ScheduleAttendance> attendanceOpt = attendanceRepository.findByScheduleAndMember(schedule, member);

            if (attendanceOpt.isPresent()) {
                ScheduleAttendance attendance = attendanceOpt.get();

                // [중요] 이미 '출석 완료(PRESENT)'된 사람은 실수로 삭제되지 않도록 방지
                if (attendance.getStatus() == AttendanceStatus.PRESENT) {
                    throw new GeneralException(CommonErrorCode.CANNOT_REMOVE_PRESENT_MEMBER);
                    // 또는 continue; 로 조용히 넘어가게 처리 가능
                }

                // '등록(REGISTERED)' 상태인 경우에만 삭제
                attendanceRepository.delete(attendance);
            }
        }
    }

    // --- [2] 사용자용: 참석 신청 (Self Apply) ---
    public void applyForSchedule(Long scheduleId, UserDetailsImpl userDetails) {
        Schedule schedule = findScheduleById(scheduleId);
        Member member = userDetails.getUser().getMember();

        // 이미 명단에 있거나 출석했는지 확인
        if (attendanceRepository.existsByScheduleAndMember(schedule, member)) {
            throw new GeneralException(CommonErrorCode.ALREADY_REGISTERED);
        }

        ScheduleAttendance application = ScheduleAttendance.builder()
                .schedule(schedule)
                .member(member)
                .status(AttendanceStatus.REGISTERED) // 상태: 신청됨(명단 등록)
                // 시간, 위치 정보는 없음 (아직 출석 전이므로)
                .build();

        attendanceRepository.save(application);
    }

    // --- [3] 사용자용: 참석 신청 취소 (Self Cancel) ---
    public void cancelApplication(Long scheduleId, UserDetailsImpl userDetails) {
        Schedule schedule = findScheduleById(scheduleId);
        Member member = userDetails.getUser().getMember();

        ScheduleAttendance attendance = attendanceRepository.findByScheduleAndMember(schedule, member)
                .orElseThrow(() -> new GeneralException(CommonErrorCode.NOT_REGISTERED));

        // 이미 출석 체크를 완료(PRESENT)한 경우 취소 불가 (관리자 문의 필요 등 정책에 따라 결정)
        if (attendance.getStatus() == AttendanceStatus.PRESENT) {
            throw new GeneralException(CommonErrorCode.ALREADY_ATTENDED_CANNOT_CANCEL);
        }

        attendanceRepository.delete(attendance);
    }

    // 관리자 일괄 출석
    public void checkInByAdmin(Long scheduleId, AdminAttendanceRequestDto requestDto) {
        Schedule schedule = findScheduleById(scheduleId);


        LocalDate targetDate = requestDto.getTargetDate();
        if (targetDate == null) targetDate = LocalDate.now();

        // 1. 해당 일정+날짜의 기존 기록 조회
        List<ScheduleAttendance> existingRecords = attendanceRepository.findAllByScheduleAndScheduleDate(schedule, targetDate);


        // 1. 요청된 멤버 ID 중복 제거 (혹시 모를 에러 방지)
        Set<Long> requestMemberIds = new HashSet<>(requestDto.getAttendedMemberIds());

        // 2. 해당 스케줄의 '모든' 기존 기록 조회 (Map으로 변환하여 검색 속도 향상)
        Map<Long, ScheduleAttendance> attendanceMap = existingRecords.stream()
                .collect(Collectors.toMap(att -> att.getMember().getId(), att -> att));

        // 3. 요청된 멤버들에 대해 처리 (Update or Insert)
        List<Member> members = memberRepository.findAllById(requestMemberIds);
        List<ScheduleAttendance> toSave = new ArrayList<>();

        for (Member member : members) {
            if (attendanceMap.containsKey(member.getId())) {
                // A. 이미 기록이 있는 경우 -> 상태 업데이트
                ScheduleAttendance existing = attendanceMap.get(member.getId());

                // 이미 출석(PRESENT) 상태라면 굳이 건드리지 않음 (GPS 정보 보존 등)
                // 단, REGISTERED(명단등록) 상태였다면 PRESENT로 변경
                if (existing.getStatus() != AttendanceStatus.PRESENT) {
                    existing.updateStatus(AttendanceStatus.PRESENT, AttendanceType.ADMIN_CHECK, AttendanceSource.ADMIN);
                    // JPA의 Dirty Checking에 의해 트랜잭션 종료 시 자동 저장됨
                }

                // 처리된 멤버는 Map에서 제거 (나중에 명단에 없는 사람 찾기 위해)
                attendanceMap.remove(member.getId());
            } else {
                // B. 기록이 없는 경우 -> 신규 생성 (Insert)
                ScheduleAttendance newRecord = ScheduleAttendance.builder()
                        .schedule(schedule)
                        .member(member)
                        .scheduleDate(targetDate)
                        .status(AttendanceStatus.PRESENT)
                        .attendanceTime(LocalDateTime.now())
                        .type(AttendanceType.ADMIN_CHECK)
                        .source(AttendanceSource.ADMIN)
                        .build();
                toSave.add(newRecord);
            }
        }

        // 신규 생성된 건들 일괄 저장
        attendanceRepository.saveAll(toSave);

        // 4. 요청 명단에 포함되지 않은 나머지 인원 처리 (체크 해제된 사람들)
        // map에 남아있는 사람 = 기존엔 있었으나 이번 요청엔 빠진 사람
        for (ScheduleAttendance leftOver : attendanceMap.values()) {
            // 방법 1: 아예 삭제 (Walk-in 이었다면 삭제가 맞음)
            // attendanceRepository.delete(leftOver);

            // 방법 2: 'REGISTERED'(미출석) 상태로 변경 (명단은 유지하고 싶을 때 권장)
            leftOver.updateStatus(AttendanceStatus.REGISTERED, null, null);
        }
    }

    // 출석 가능 일정 목록 조회
    @Transactional(readOnly = true)
    public List<ScheduleResponseDto> getCheckableSchedules() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);
        List<Schedule> todaySchedules = scheduleRepository.findAllByStartDateBetween(startOfDay, endOfDay);
        return todaySchedules.stream().map(ScheduleResponseDto::new).collect(Collectors.toList());


    }

    // [Modified] 2. 사용자 직접 출석 (기존 로직 수정)
    public void checkIn(Long scheduleId, CheckInRequestDto dto, HttpServletRequest request, UserDetailsImpl userDetails) {
        Schedule schedule = findScheduleById(scheduleId);

        LocalDate targetDate = LocalDate.now();

        // (옵션) 만약 반복 일정이 아닌데 오늘이 스케줄 날짜와 다르면 에러 처리 필요할 수 있음
        if (schedule.getRecurrenceRule() == RecurrenceRule.NONE && !schedule.getStartDate().toLocalDate().isEqual(targetDate)) {
            // 단순히 날짜가 다르면 에러 or 그냥 오늘 날짜로 기록 (정책 결정 필요)
            targetDate = schedule.getStartDate().toLocalDate();
        }


        String clientIp = getClientIp(request);

        if (attendanceRepository.existsByScheduleAndScheduleDateAndIpAddress(schedule, targetDate, clientIp)) {
            throw new GeneralException(CommonErrorCode.IP_ALREADY_USED);
        }

        // IP 중복 체크
        if (attendanceRepository.existsByScheduleAndIpAddress(schedule, clientIp)) {
            throw new GeneralException(CommonErrorCode.IP_ALREADY_USED);
        }

        // 위치 검증
        validateLocation(dto.getLatitude(), dto.getLongitude());

        // 멤버 식별
        Member member;
        if (userDetails != null && userDetails.getUser().getMember() != null) {
            member = userDetails.getUser().getMember();
        } else {
            if (dto.getName() == null || dto.getBirthDate() == null) {
                throw new GeneralException(CommonErrorCode.BAD_REQUEST);
            }
            member = memberRepository.findByNameAndBirthDate(dto.getName(), dto.getBirthDate())
                    .orElseThrow(() -> new GeneralException(CommonErrorCode.MEMBER_NOT_FOUND_FOR_CHECK_IN));
        }

        Optional<ScheduleAttendance> existingRecord = attendanceRepository.findByScheduleAndMemberAndScheduleDate(schedule, member, targetDate);
        if (existingRecord.isPresent()) {
            ScheduleAttendance attendance = existingRecord.get();
            // 이미 출석완료(PRESENT) 상태라면 에러
            if (attendance.getStatus() == AttendanceStatus.PRESENT) {
                throw new GeneralException(CommonErrorCode.ALREADY_ATTENDED);
            }
            // 명단에 있음(REGISTERED) -> 출석 정보 업데이트
            attendance.markAsPresent(
                    LocalDateTime.now(),
                    AttendanceType.USER_SELF_CHECK,
                    dto.getLatitude(),
                    dto.getLongitude(),
                    clientIp,
                    AttendanceSource.GPS
            );
        } else {
            // 명단에 없음(Walk-in) -> 새로 생성 (현장 즉석 참여)
            ScheduleAttendance newAttendance = ScheduleAttendance.builder()
                    .schedule(schedule)
                    .member(member)
                    .scheduleDate(targetDate)
                    .status(AttendanceStatus.PRESENT)
                    .attendanceTime(LocalDateTime.now())
                    .type(AttendanceType.USER_SELF_CHECK)
                    .latitude(dto.getLatitude())
                    .longitude(dto.getLongitude())
                    .ipAddress(clientIp)
                    .source(AttendanceSource.GPS)
                    .build();
            attendanceRepository.save(newAttendance);
        }
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

    // [Modified] 3. 출석부 조회 (명단 기준)
    // 기존에는 전체 멤버를 다 불러왔으나, 이제는 '등록된 인원 + 현장 출석 인원'만 조회합니다.
    @Transactional(readOnly = true)
    public AttendanceSheetResponseDto getAttendanceSheet(Long scheduleId, LocalDate targetDate) {
        Schedule schedule = findScheduleById(scheduleId);
        List<ScheduleAttendance> records = attendanceRepository.findAllByScheduleAndScheduleDate(schedule, targetDate);

        // ScheduleType에 따른 모드 결정
        String mode = (schedule.getType() == ScheduleType.WORSHIP) ? "GENERAL" : "PARTICIPANT";

        // 만약 EVENT인데 사전 등록자가 0명이라면?
        // 유연함을 위해 '명단이 아예 없으면' GENERAL로 자동 전환되게 안전장치를 둘 수도 있습니다.
        if (records.isEmpty()) {
            mode = "GENERAL";
        }

        List<AttendanceRecordDto> dtoList = records.stream()
                .map(record -> new AttendanceRecordDto(
                        record.getMember().getId(),
                        record.getMember().getName(),
                        record.getMember().getPhone(),
                        record.getStatus() == AttendanceStatus.PRESENT,
                        record.getAttendanceTime() != null ? record.getAttendanceTime().toLocalTime().toString() : "-"
                ))
                .collect(Collectors.toList());

        return new AttendanceSheetResponseDto(mode, dtoList);
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

    @Transactional(readOnly = true)
    public AttendanceStatisticsResponseDto getPeriodStatistics(
            LocalDate startDate,
            LocalDate endDate,
            Long cellId,
            List<ScheduleType> scheduleTypes,
            List<WorshipCategory> worshipCategories
    ) {
        // [삭제] LocalDateTime 변환 불필요 (Repository가 LocalDate를 받도록 수정했으므로)
        // LocalDateTime startDateTime = startDate.atStartOfDay();
        // LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        // NULL 처리 로직 유지
        if (scheduleTypes != null && scheduleTypes.isEmpty()) scheduleTypes = null;
        if (worshipCategories != null && worshipCategories.isEmpty()) worshipCategories = null;

        // 1. 조회 (파라미터로 LocalDate 그대로 전달)
        List<ScheduleAttendance> attendances = attendanceRepository.findAllPresentWithFilter(
                startDate, endDate, scheduleTypes, worshipCategories
        );

        // 2. Cell ID 필터링 (유지)
        if (cellId != null) {
            attendances = attendances.stream()
                    .filter(att -> isMemberInCell(att.getMember(), cellId))
                    .collect(Collectors.toList());
        }

        // 3. [핵심 수정] 그룹핑 기준 변경: (스케줄 + 날짜)
        // 기존: Map<Schedule, Long> -> 스케줄 ID가 같으면 날짜 달라도 합쳐짐 (문제 발생)
        // 변경: 날짜별, 스케줄별로 쪼개야 함.

        // 키를 복합적으로 만들기 위해 임시 클래스나 Pair를 써도 되지만,
        // 여기서는 Stream을 두 번 타거나, Map Key를 String으로 만드는 등 여러 방법이 있습니다.
        // 가장 깔끔한 방법은 결과 DTO를 바로 생성하는 것입니다.

        Map<String, List<ScheduleAttendance>> groupedMap = attendances.stream()
                .collect(Collectors.groupingBy(att ->
                        att.getSchedule().getScheduleId() + "_" + att.getScheduleDate()
                ));

        List<ScheduleStatDto> scheduleStats = new ArrayList<>();

        for (List<ScheduleAttendance> group : groupedMap.values()) {
            if (group.isEmpty()) continue;

            ScheduleAttendance first = group.get(0); // 대표 객체 하나 꺼냄
            Schedule schedule = first.getSchedule();
            LocalDate date = first.getScheduleDate(); // ★ 실제 출석 날짜 사용
            long count = group.size();

            scheduleStats.add(ScheduleStatDto.builder()
                    .scheduleId(schedule.getScheduleId())
                    .date(date) // ★ 여기에 sa.scheduleDate가 들어감
                    .time(schedule.getStartDate().toLocalTime()) // 시간은 스케줄의 원래 시간 사용
                    .scheduleName(schedule.getTitle())
                    .category(schedule.getWorshipCategory())
                    .count(count)
                    .offering(0)
                    .build());
        }

        // 4. 정렬 (날짜 -> 시간 순서)
        scheduleStats.sort(Comparator.comparing(ScheduleStatDto::getDate)
                .thenComparing(ScheduleStatDto::getTime));

        // 5. Summary 계산 (유지)
        long totalAttendance = scheduleStats.stream().mapToLong(ScheduleStatDto::getCount).sum();
        double average = scheduleStats.isEmpty() ? 0 : (double) totalAttendance / scheduleStats.size();

        ScheduleStatDto maxStat = scheduleStats.stream()
                .max(Comparator.comparingLong(ScheduleStatDto::getCount))
                .orElse(null);

        return AttendanceStatisticsResponseDto.builder()
                .summary(AttendanceStatisticsResponseDto.SummaryDto.builder()
                        .averageAttendance(Math.round(average * 10) / 10.0)
                        .maxAttendanceDate(maxStat != null ? maxStat.getDate() : null)
                        .maxAttendanceScheduleName(maxStat != null ? maxStat.getScheduleName() : null)
                        .totalOffering(0)
                        .build())
                .scheduleStats(scheduleStats)
                .build();
    }

    // ========================================================================
    // [Admin] 3-7. 멤버별 출석 현황 요약
    // ========================================================================
    @Transactional(readOnly = true)
    public List<MemberAttendanceStatResponseDto> getMemberStats(int year, Long cellId) {
        LocalDateTime startOfYear = LocalDateTime.of(year, 1, 1, 0, 0);
        LocalDateTime endOfYear = LocalDateTime.of(year, 12, 31, 23, 59, 59);
        LocalDateTime now = LocalDateTime.now();

        // 1. 전체 멤버 조회 (Active 상태만) & Cell 필터링
        List<Member> members = memberRepository.findAllByMemberStatus(MemberStatus.ACTIVE);
        if (cellId != null) {
            members = members.stream()
                    .filter(m -> isMemberInCell(m, cellId))
                    .collect(Collectors.toList());
        }

        // 2. 스케줄 조회 (청년부 예배만!!)
        // 2-1. 올해 통계용 (출석률)
        List<Schedule> youthServicesThisYear = scheduleRepository.findAllByWorshipCategoryAndStartDateBetween(
                WorshipCategory.YOUTH_SERVICE, startOfYear, endOfYear);

        // 2-2. 연속 결석 계산용 (과거 전체, 최신순) - 최근 4주 히스토리도 여기서 추출 가능
        List<Schedule> pastYouthServices = scheduleRepository.findAllByWorshipCategoryAndStartDateBeforeOrderByStartDateDesc(
                WorshipCategory.YOUTH_SERVICE, now);

        // 2-3. 최근 4주 스케줄 (최신순 4개 -> 시간순 정렬 필요 시 변환)
        List<Schedule> recent4Services = pastYouthServices.stream().limit(4).toList();

        // 3. 성능 최적화를 위한 출석 데이터 일괄 로딩 (N+1 방지)
        // 올해 출석 + 과거 출석 모두 필요하므로, pastYouthServices 전체에 대한 출석을 가져오는 것이 안전하나
        // 데이터 양이 많을 경우 올해 것과 최근 것만 가져오고 나머지는 쿼리할 수도 있음.
        // 여기서는 메모리 효율을 위해 '필요한 스케줄들'에 대한 출석만 가져옵니다.

        Set<Schedule> schedulesToCheck = new HashSet<>();
        schedulesToCheck.addAll(youthServicesThisYear);
        schedulesToCheck.addAll(recent4Services);
        // 연속 결석 계산을 위해 최근 10~20개 정도 더 가져오거나, 로직 내에서 필요시 쿼리하도록 처리
        // 여기서는 간단하게 pastYouthServices 전체를 대상으로 로딩 (스케줄 개수가 수천 개가 아니라고 가정)
        schedulesToCheck.addAll(pastYouthServices);

        List<ScheduleAttendance> allAttendances = attendanceRepository.findAllByScheduleInAndStatusPresent(new ArrayList<>(schedulesToCheck));

        // Map<MemberId, Set<ScheduleId>> 형태로 변환하여 빠른 조회
        Map<Long, Set<Long>> memberAttendanceMap = new HashMap<>();
        for (ScheduleAttendance att : allAttendances) {
            memberAttendanceMap
                    .computeIfAbsent(att.getMember().getId(), k -> new HashSet<>())
                    .add(att.getSchedule().getScheduleId());
        }

        // 4. 멤버별 통계 계산
        List<MemberAttendanceStatResponseDto> result = new ArrayList<>();
        int totalSchedulesThisYear = youthServicesThisYear.size();

        for (Member member : members) {
            Set<Long> attendedScheduleIds = memberAttendanceMap.getOrDefault(member.getId(), Collections.emptySet());

            // A. 출석률 (올해 청년부 예배 기준)
            long myCount = youthServicesThisYear.stream()
                    .filter(s -> attendedScheduleIds.contains(s.getScheduleId()))
                    .count();
            int rate = totalSchedulesThisYear == 0 ? 0 : (int) ((myCount * 100) / totalSchedulesThisYear);

            // B. 연속 결석 (청년부 예배 기준, 최신 -> 과거)
            int consecutiveAbsence = 0;
            for (Schedule schedule : pastYouthServices) {
                if (attendedScheduleIds.contains(schedule.getScheduleId())) {
                    break; // 출석 기록 만나면 종료
                }
                consecutiveAbsence++;
            }

            // C. 최근 4주 현황 (날짜 오름차순: 과거 -> 오늘)
            List<Boolean> history = recent4Services.stream()
                    .sorted(Comparator.comparing(Schedule::getStartDate))
                    .map(s -> attendedScheduleIds.contains(s.getScheduleId()))
                    .collect(Collectors.toList());

            // D. 셀 이름
            String cellName = getCellName(member);

            result.add(MemberAttendanceStatResponseDto.builder()
                    .memberId(member.getId())
                    .name(member.getName())
                    .cellName(cellName)
                    .attendanceRate(rate)
                    .attendanceCount((int) myCount)
                    .consecutiveAbsenceCount(consecutiveAbsence)
                    .attendanceHistory(history)
                    .build());
        }

        return result;
    }

    // --- Helper Methods ---
    private boolean isMemberInCell(Member member, Long cellId) {
        return cellHistoryRepository.findByMemberAndIsActiveTrue(member)
                .map(history -> history.getCell().getCellId().equals(cellId))
                .orElse(false);
    }

    private String getCellName(Member member) {
        return cellHistoryRepository.findByMemberAndIsActiveTrue(member)
                .map(history -> history.getCell().getCellName())
                .orElse("미배정");
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