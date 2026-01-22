package com.jeja.jejabe.care;

import com.jeja.jejabe.attendance.AttendanceRepository;
import com.jeja.jejabe.attendance.ScheduleAttendance;
import com.jeja.jejabe.auth.User;
import com.jeja.jejabe.care.domain.AbsenceCare;
import com.jeja.jejabe.care.domain.CareConfig;
import com.jeja.jejabe.care.domain.CareLog;
import com.jeja.jejabe.care.domain.CareStatus;
import com.jeja.jejabe.care.dto.*;
import com.jeja.jejabe.care.repository.AbsenceCareRepository;
import com.jeja.jejabe.care.repository.CareConfigRepository;
import com.jeja.jejabe.care.repository.CareLogRepository;
import com.jeja.jejabe.member.MemberRepository;
import com.jeja.jejabe.member.domain.Member;
import com.jeja.jejabe.member.domain.MemberStatus;
import com.jeja.jejabe.schedule.domain.Schedule;
import com.jeja.jejabe.schedule.ScheduleRepository;
import com.jeja.jejabe.schedule.domain.ScheduleType;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CareService {

    private final AbsenceCareRepository absenceCareRepository;
    private final CareLogRepository careLogRepository;
    private final CareConfigRepository careConfigRepository;
    private final MemberRepository memberRepository;
    private final AttendanceRepository attendanceRepository;
    private final ScheduleRepository scheduleRepository;

    // --- 배치: 자동 상태 판별 (매주 주일 밤 10시) ---
    @Scheduled(cron = "0 0 22 * * SUN")
    public void updateAbsenceStatusBatch() {
        CareConfig config = careConfigRepository.findById(1L).orElse(new CareConfig());
        LocalDate today = LocalDate.now();

        List<Member> members = memberRepository.findAllByMemberStatus(MemberStatus.ACTIVE);

        for (Member member : members) {
            Optional<ScheduleAttendance> lastAttOpt = attendanceRepository.findTopByMemberOrderByAttendanceTimeDesc(member);

            if (lastAttOpt.isPresent()) {
                LocalDate lastDate = lastAttOpt.get().getAttendanceTime().toLocalDate();
                int weeksAbsent = (int) ChronoUnit.WEEKS.between(lastDate, today);

                Optional<AbsenceCare> activeCareOpt = absenceCareRepository.findActiveByMember(member);

                if (activeCareOpt.isPresent()) {
                    AbsenceCare care = activeCareOpt.get();
                    if (weeksAbsent == 0) {
                        handleResettling(care, config, member);
                    } else {
                        care.updateAbsenceWeeks(weeksAbsent);
                        care.updateLastAttendanceDate(lastDate);
                        if (weeksAbsent >= config.getLongTermWeeksThreshold()) {
                            care.updateStatus(CareStatus.LONG_TERM_ABSENCE);
                        }
                    }
                } else {
                    if (weeksAbsent >= config.getAttentionWeeksThreshold()) {
                        absenceCareRepository.save(AbsenceCare.builder()
                                .member(member)
                                .lastAttendanceDate(lastDate)
                                .consecutiveAbsenceWeeks(weeksAbsent)
                                .status(CareStatus.NEEDS_ATTENTION)
                                .startDate(today)
                                .build());
                    }
                }
            }
        }
    }

    private void handleResettling(AbsenceCare care, CareConfig config, Member member) {
        if (care.getStatus() == CareStatus.RESETTLING) {
            int consecutiveAttended = calculateConsecutiveAttendance(member);
            if (consecutiveAttended >= config.getResettlementWeeksThreshold()) {
                care.completeCare(); // 완료 처리 (삭제 X)
            }
        } else {
            care.updateStatus(CareStatus.RESETTLING);
            care.updateAbsenceWeeks(0);
        }
    }

    private int calculateConsecutiveAttendance(Member member) {
        List<Schedule> recentWorships = scheduleRepository.findTop10ByTypeOrderByStartDateDesc(ScheduleType.WORSHIP);
        int count = 0;
        for (Schedule s : recentWorships) {
            if (attendanceRepository.existsByScheduleAndMember(s, member)) count++;
            else if (s.getStartDate().isBefore(LocalDateTime.now())) break;
        }
        return count;
    }

    // --- API ---

    @Transactional(readOnly = true)
    public List<AbsenceCareResponseDto> getAllAbsentees() {
        return absenceCareRepository.findAllActive().stream()
                .map(care -> {
                    int attWeeks = 0;
                    if(care.getStatus() == CareStatus.RESETTLING) {
                        attWeeks = calculateConsecutiveAttendance(care.getMember());
                    }
                    return new AbsenceCareResponseDto(care, attWeeks);
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AbsenceCareDetailDto getAbsenteeDetail(Long memberId) {
        Member member = memberRepository.findById(memberId).orElseThrow();

        AbsenceCare currentCare = absenceCareRepository.findActiveByMember(member).orElse(null);
        AbsenceCareResponseDto currentInfoDto = null;
        if (currentCare != null) {
            int attWeeks = 0;
            if(currentCare.getStatus() == CareStatus.RESETTLING) {
                attWeeks = calculateConsecutiveAttendance(member);
            }
            currentInfoDto = new AbsenceCareResponseDto(currentCare, attWeeks);
        }

        List<AbsenceCareHistoryDto> history = absenceCareRepository.findAllByMemberOrderByStartDateDesc(member).stream()
                .map(AbsenceCareHistoryDto::new)
                .collect(Collectors.toList());

        List<CareLogDto> logs = careLogRepository.findAllByTargetMemberOrderByCareDateDesc(member)
                .stream().map(CareLogDto::new).collect(Collectors.toList());

        return new AbsenceCareDetailDto(currentInfoDto, history, logs);
    }

    public void addCareLog(Long memberId, CareLogCreateRequestDto dto, User user) {
        Member target = memberRepository.findById(memberId).orElseThrow();
        Member manager = user.getMember();

        careLogRepository.save(CareLog.builder()
                .targetMember(target)
                .manager(manager)
                .content(dto.getContent())
                .careMethod(dto.getCareMethod())
                .careDate(LocalDateTime.now())
                .build());
    }

    public Map<String, Long> getCareSummary() {
        Map<String, Long> summary = new HashMap<>();
        summary.put("needsAttention", absenceCareRepository.countByStatus(CareStatus.NEEDS_ATTENTION));
        summary.put("longTermAbsence", absenceCareRepository.countByStatus(CareStatus.LONG_TERM_ABSENCE));
        summary.put("resettling", absenceCareRepository.countByStatus(CareStatus.RESETTLING));
        return summary;
    }

    // [추가] 케어 완료 처리
    public void completeCare(Long memberId) {
        Member member = memberRepository.findById(memberId).orElseThrow();
        AbsenceCare care = absenceCareRepository.findActiveByMember(member)
                .orElseThrow(() -> new IllegalArgumentException("진행 중인 케어가 없습니다."));
        care.completeCare(); // 엔티티 메소드 호출
    }

    // [추가] 케어 로그 수정
    public void updateCareLog(Long memberId, Long logId, CareLogCreateRequestDto dto) {
        CareLog log = careLogRepository.findById(logId)
                .orElseThrow(() -> new IllegalArgumentException("로그를 찾을 수 없습니다."));
        // 본인 작성 여부 체크 로직 추가 가능
        log.update(dto.getContent(), dto.getCareMethod()); // 엔티티에 update 메소드 필요
    }

    // [추가] 케어 로그 삭제
    public void deleteCareLog(Long memberId, Long logId) {
        CareLog log = careLogRepository.findById(logId)
                .orElseThrow(() -> new IllegalArgumentException("로그를 찾을 수 없습니다."));
        careLogRepository.delete(log);
    }

    public void updateConfig(CareConfigDto dto) {
        CareConfig config = careConfigRepository.findById(1L).orElse(new CareConfig());
        config.setAttentionWeeksThreshold(dto.getAttentionWeeks());
        config.setLongTermWeeksThreshold(dto.getLongTermWeeks());
        config.setResettlementWeeksThreshold(dto.getResettlementWeeks());
        careConfigRepository.save(config);
    }

    @Transactional(readOnly = true)
    public CareConfigDto getConfig() {
        CareConfig config = careConfigRepository.findById(1L).orElse(new CareConfig());
        CareConfigDto dto = new CareConfigDto();
        dto.setAttentionWeeks(config.getAttentionWeeksThreshold());
        dto.setLongTermWeeks(config.getLongTermWeeksThreshold());
        dto.setResettlementWeeks(config.getResettlementWeeksThreshold());
        return dto;
    }

    public void updateManager(Long memberId, Long newManagerId) {
        Member member = memberRepository.findById(memberId).orElseThrow();
        Member manager = memberRepository.findById(newManagerId).orElseThrow();
        AbsenceCare care = absenceCareRepository.findActiveByMember(member).orElseThrow();
        care.changeManager(manager);
    }
}
