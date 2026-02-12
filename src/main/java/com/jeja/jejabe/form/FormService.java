package com.jeja.jejabe.form;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jeja.jejabe.attendance.AttendanceService;
import com.jeja.jejabe.attendance.dto.AttendanceRegistrationDto;
import com.jeja.jejabe.auth.User;
import com.jeja.jejabe.auth.UserRepository;
import com.jeja.jejabe.auth.UserRole;
import com.jeja.jejabe.notification.service.FcmService;
import com.jeja.jejabe.cell.Cell;
import com.jeja.jejabe.cell.CellRepository;
import com.jeja.jejabe.cell.MemberCellHistory;
import com.jeja.jejabe.cell.MemberCellHistoryRepository;
import com.jeja.jejabe.club.Club;
import com.jeja.jejabe.club.ClubMember;
import com.jeja.jejabe.club.ClubMemberRepository;
import com.jeja.jejabe.club.ClubRepository;
import com.jeja.jejabe.form.domain.*;
import com.jeja.jejabe.form.dto.*;
import com.jeja.jejabe.form.repository.FormQuestionRepository;
import com.jeja.jejabe.form.repository.FormSubmissionRepository;
import com.jeja.jejabe.form.repository.FormTemplateRepository;
import com.jeja.jejabe.global.exception.CommonErrorCode;
import com.jeja.jejabe.global.exception.GeneralException;
import com.jeja.jejabe.member.MemberRepository;
import com.jeja.jejabe.member.domain.Member;
import com.jeja.jejabe.member.domain.MemberRole;
import com.jeja.jejabe.schedule.domain.Schedule;
import com.jeja.jejabe.schedule.ScheduleRepository;
import com.jeja.jejabe.schedule.domain.ScheduleType;
import com.jeja.jejabe.schedule.domain.WorshipCategory;
import com.jeja.jejabe.schedule.util.RecurrenceCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class FormService {

    private final FormTemplateRepository templateRepository;
    private final FormSubmissionRepository submissionRepository;
    private final FormQuestionRepository questionRepository;
    private final MemberRepository memberRepository;
    private final AttendanceService attendanceService;
    private final ScheduleRepository scheduleRepository;
    private final ClubRepository clubRepository;
    private final ClubMemberRepository clubMemberRepository;
    private final FcmService fcmService;
    private final UserRepository userRepository;
    private final CellRepository cellRepository;
    private final MemberCellHistoryRepository memberCellHistoryRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 1. 템플릿 생성
    public Long createTemplate(TemplateCreateRequestDto dto) {
        FormTemplate template = FormTemplate.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .category(dto.getCategory())
                .type(dto.getType())
                .targetClubId(dto.getTargetClubId())
                .build();

        for (int i = 0; i < dto.getSections().size(); i++) {
            var sDto = dto.getSections().get(i);
            FormSection section = FormSection.builder()
                    .title(sDto.getTitle())
                    .description(sDto.getDescription())
                    .orderIndex(i)
                    .defaultNextAction(sDto.getDefaultNextAction())
                    .defaultTargetSectionIndex(sDto.getDefaultTargetSectionIndex())
                    .build();
            template.addSection(section);

            for (int j = 0; j < sDto.getQuestions().size(); j++) {
                var qDto = sDto.getQuestions().get(j);
                String optionsJson = null;
                try {
                    optionsJson = objectMapper.writeValueAsString(qDto.getOptions());
                } catch (Exception e) {
                    throw new RuntimeException("옵션 변환 실패");
                }

                section.addQuestion(FormQuestion.builder()
                        .label(qDto.getLabel())
                        .inputType(qDto.getInputType())
                        .syncType(qDto.getSyncType()) // 연동 타입 (사전/사후)
                        .optionsJson(optionsJson)
                        .required(qDto.isRequired())
                        .linkedScheduleId(qDto.getLinkedScheduleId())
                        .linkedScheduleDate(qDto.getLinkedScheduleDate())
                        .orderIndex(j)
                        .linkedWorshipCategory(qDto.getLinkedWorshipCategory())
                        .isMemberSpecific(qDto.isMemberSpecific())
                        .build());
            }
        }
        return templateRepository.save(template).getId();
    }

    // 2. 폼 제출
    public void submitForm(SubmissionRequestDto dto, User user) {
        FormTemplate template = templateRepository.findById(dto.getTemplateId())
                .orElseThrow(() -> new IllegalArgumentException("템플릿을 찾을 수 없습니다."));

        if (user == null || !user.isPrivileged()) {
            template.validateSubmissionTime();
        }

        // [추가] 필수 답변 검증 (분기 로직으로 건너뛴 섹션 제외)
        validateRequiredAnswers(template, dto);

        Member submitter = (user != null) ? user.getMember() : null;

        LocalDate targetSunday = null;
        if (template.getCategory() == FormCategory.CELL_REPORT) {
            targetSunday = dto.getDate().with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
        }

        FormSubmission submission = FormSubmission.builder()
                .template(template)
                .submitter(submitter)
                .submitDate(LocalDate.now()) // 프론트엔드 전달값 대신 서버 현재 날짜 사용
                .targetSundayDate(targetSunday)
                .targetCellId(dto.getCellId())
                .targetClubId(dto.getClubId())
                .guestName(dto.getGuestName())
                .guestPhone(dto.getGuestPhone())
                .build();

        // 답변 저장
        for (SubmissionRequestDto.AnswerDto ans : dto.getAnswers()) {
            FormQuestion question = questionRepository.findById(ans.getQuestionId()).orElseThrow();

            Member target = null;

            if (ans.getTargetMemberId() != null) {
                // A. (대리 신청) 명시적으로 ID가 넘어온 경우
                target = memberRepository.findById(ans.getTargetMemberId()).orElse(null);
            } else {
                // B. (본인 신청) ID가 없으면 현재 로그인한 사람(submitter)을 타겟으로 함
                target = submitter;
            }

            submission.addAnswer(FormAnswer.builder()
                    .submission(submission)
                    .question(question)
                    .targetMember(target)
                    .value(ans.getValue())
                    .build());
        }
        submissionRepository.save(submission);

        // Club Application Notification
        if (template.getCategory() == FormCategory.CLUB_APPLICATION && template.getTargetClubId() != null) {
            sendClubApplicationNotification(template.getTargetClubId(), submitter, submission);
        }

        // [핵심] 질문별 syncType에 따른 출석/참석 연동 처리
        processAttendanceSync(submission, dto.getTargetScheduleId());
    }

    /**
     * 질문별 연동 설정(syncType)에 따라 출석 시스템에 데이터 반영
     */
    private void processAttendanceSync(FormSubmission submission, Long specificScheduleId) {
        final LocalDate targetDate = submission.getTargetSundayDate() != null
                ? submission.getTargetSundayDate()
                : LocalDate.now();

        // 사후 확정용 검색 범위 설정
        final LocalDateTime rangeStart;
        final LocalDateTime rangeEnd;

        if (submission.getTemplate().getCategory() == FormCategory.CELL_REPORT
                && submission.getTargetSundayDate() != null) {
            // 순 보고서의 경우: 해당 주간 (월요일 ~ 주일)
            // targetDate가 주일이므로, 6일 전인 월요일부터 주일 당일까지 검색
            rangeStart = targetDate.minusDays(6).atStartOfDay(); // 월요일 00:00:00
            rangeEnd = targetDate.atTime(23, 59, 59); // 주일 23:59:59
        } else {
            // 일반 폼의 경우: 기준일 전후 3일로 유연하게 매칭
            rangeStart = targetDate.minusDays(3).atStartOfDay();
            rangeEnd = targetDate.plusDays(3).atTime(23, 59, 59);
        }

        // 범위 내 후보 일정 로드
        List<Schedule> candidates = scheduleRepository.findCandidatesForMonth(rangeStart, rangeEnd);

        for (FormAnswer ans : submission.getAnswers()) {
            FormQuestion q = ans.getQuestion();
            WorshipCategory cat = q.getLinkedWorshipCategory();
            Member target = ans.getTargetMember();
            QuestionType type = q.getInputType();

            // 대상 멤버가 없는 경우 처리 불가
            if (target == null) {
                continue;
            }

            // 연동 타입 결정
            AttendanceSyncType syncType = q.getSyncType();
            if (syncType == AttendanceSyncType.NONE) {
                if (type == QuestionType.WORSHIP_ATTENDANCE || type == QuestionType.SCHEDULE_ATTENDANCE) {
                    syncType = AttendanceSyncType.POST_CONFIRMATION;
                } else {
                    continue;
                }
            }

            boolean isAttended = "true".equalsIgnoreCase(ans.getValue());

            switch (syncType) {
                case POST_CONFIRMATION: // 사후 확정 (즉시 출석 PRESENT)
                    if (isAttended) {
                        // 1. 특정 스케줄 ID가 있는 경우 우선 처리 (일정 참석)
                        if (q.getLinkedScheduleId() != null) {
                            scheduleRepository.findById(q.getLinkedScheduleId())
                                    .ifPresent(s -> attendanceService.checkInByLeader(s, target, targetDate));
                        }
                        // 2. 카테고리 기반 매칭 (예배 출석)
                        else if (cat != null) {
                            candidates.stream()
                                    .flatMap(s -> RecurrenceCalculator.generateSchedules(s, rangeStart, rangeEnd)
                                            .stream())
                                    .filter(sDto -> sDto.getWorshipCategory() == cat)
                                    // 해당 기간 내에 일치하는 모든 일정에 대해 출석 처리
                                    .forEach(sDto -> {
                                        scheduleRepository.findById(sDto.getScheduleId())
                                                .ifPresent(s -> attendanceService.checkInByLeader(s, target,
                                                        sDto.getStartDate().toLocalDate()));
                                    });
                        }
                    } else {
                        // 답변이 "false"이면 출석 취소 처리
                        if (q.getLinkedScheduleId() != null) {
                            scheduleRepository.findById(q.getLinkedScheduleId())
                                    .ifPresent(s -> attendanceService.revertCheckInByLeader(s, target, targetDate));
                        } else if (cat != null) {
                            candidates.stream()
                                    .flatMap(s -> RecurrenceCalculator.generateSchedules(s, rangeStart, rangeEnd)
                                            .stream())
                                    .filter(sDto -> sDto.getWorshipCategory() == cat)
                                    .forEach(sDto -> {
                                        scheduleRepository.findById(sDto.getScheduleId())
                                                .ifPresent(s -> attendanceService.revertCheckInByLeader(s, target,
                                                        sDto.getStartDate().toLocalDate()));
                                    });
                        }
                    }
                    break;

                case PRE_REGISTRATION: // 사전 신청 (명단 등록 REGISTERED)
                    if (isAttended) {
                        Long scheduleId = (specificScheduleId != null) ? specificScheduleId
                                : (q.getLinkedScheduleId() != null ? q.getLinkedScheduleId()
                                        : (cat != null ? findScheduleIdByCat(candidates, cat) : null));

                        if (scheduleId != null) {
                            AttendanceRegistrationDto regDto = new AttendanceRegistrationDto();
                            regDto.setTargetDate(targetDate != null ? targetDate : LocalDate.now());
                            regDto.setMemberIds(List.of(target.getId()));
                            attendanceService.registerAttendees(scheduleId, regDto);
                        }
                    }
                    // 사전 등록의 경우 "false"일 때 삭제할지 여부는 정책에 따라 다름
                    // 보통 신청 취소 로직이 따로 있으므로 여기서는 추가하지 않음
                    break;
            }
        }
    }

    private Long findScheduleIdByCat(List<Schedule> schedules, WorshipCategory cat) {
        return schedules.stream().filter(s -> s.getWorshipCategory() == cat)
                .map(Schedule::getScheduleId).findFirst().orElse(null);
    }

    /**
     * 필수 답변 검증: 답변이 하나라도 있는 섹션만 방문한 것으로 간주하여 해당 섹션의 필수 질문 체크
     */
    private void validateRequiredAnswers(FormTemplate template, SubmissionRequestDto dto) {
        Set<Long> answeredQuestionIds = dto.getAnswers().stream()
                .map(SubmissionRequestDto.AnswerDto::getQuestionId)
                .collect(Collectors.toSet());

        for (FormSection section : template.getSections()) {
            // 해당 섹션의 질문에 답변이 하나라도 있으면 방문한 페이지
            boolean sectionVisited = section.getQuestions().stream()
                    .anyMatch(q -> answeredQuestionIds.contains(q.getId()));

            // 첫 페이지거나 방문한 페이지인 경우만 필수 체크
            if (sectionVisited || section.getOrderIndex() == 0) {
                for (FormQuestion question : section.getQuestions()) {
                    if (question.isRequired()) {
                        boolean hasAnswer = dto.getAnswers().stream()
                                .anyMatch(ans -> ans.getQuestionId().equals(question.getId()) &&
                                        ans.getValue() != null && !ans.getValue().trim().isEmpty());
                        if (!hasAnswer) {
                            throw new IllegalArgumentException("필수 질문에 답변하지 않았습니다: " + question.getLabel());
                        }
                    }
                }
            }
        }
    }

    // 4. 권한 체크 로직
    private boolean hasPermission(FormTemplate template, User user, AccessType requiredType) {
        // 관리자는 무조건 통과
        if (user != null && user.isPrivileged())
            return true;

        // 비로그인 유저인 경우 GUEST 권한 확인
        if (user == null) {
            return template.getAccessList().stream()
                    .anyMatch(acc -> acc.getTargetType() == TargetType.GUEST);
        }

        Member member = user.getMember();

        for (FormAccess access : template.getAccessList()) {
            // 요구하는 권한 타입과 다르면서 관리 권한도 아니면 스킵
            if (access.getAccessType() != requiredType && access.getAccessType() != AccessType.MANAGER) {
                continue;
            }

            switch (access.getTargetType()) {
                case GUEST:
                case ALL:
                    return true;

                case ROLE:
                    String targetRole = access.getTargetValue();
                    // UserRole 체크
                    if (user.getUserRole().name().equals(targetRole))
                        return true;
                    // MemberRole 체크
                    if (member != null) {
                        if (member.getRoles().stream().anyMatch(role -> role.name().equals(targetRole)))
                            return true;
                    }
                    break;

                case USER:
                    if (member != null && String.valueOf(member.getId()).equals(access.getTargetValue())) {
                        return true;
                    }
                    break;

                case CLUB:
                    if (member != null && clubMemberRepository.existsByClubIdAndMemberId(
                            Long.parseLong(access.getTargetValue()), member.getId())) {
                        return true;
                    }
                    break;
            }
        }
        return false;
    }

    // 5. 마이페이지 (내 제출 내역)
    @Transactional(readOnly = true)
    public List<MySubmissionResponseDto> getMySubmissions(User user) {
        if (user.getMember() == null)
            return List.of();

        Member member = user.getMember();
        LocalDate oneMonthAgo = LocalDate.now().minusMonths(1);
        LocalDateTime now = LocalDateTime.now();

        // 1. 내가 제출한 내역 조회
        List<FormSubmission> mySubmissions = submissionRepository.findAllBySubmitterOrderBySubmitDateDesc(member);

        // 2. [수정] 내가 순장/부순장인 경우 우리 순의 보고서도 포함
        Set<FormSubmission> allSubmissions = new HashSet<>(mySubmissions);
        memberCellHistoryRepository.findByMemberAndIsActiveTrue(member)
                .ifPresent(h -> {
                    if (h.isLeader() || h.isSubLeader()) {
                        List<FormSubmission> cellSubmissions = submissionRepository
                                .findAllByTargetCellId(h.getCell().getCellId());
                        allSubmissions.addAll(cellSubmissions);
                    }
                });

        return allSubmissions.stream()
                .filter(s -> {
                    FormTemplate template = s.getTemplate();
                    if (template.isDeleted())
                        return false;
                    // 순 보고서는 마감 기한과 상관없이 내역에서 보여야 함
                    if (template.getCategory() == FormCategory.CELL_REPORT) {
                        return true;
                    }
                    if (!template.isActive())
                        return false;
                    if (template.getEndDate() != null && now.isAfter(template.getEndDate()))
                        return false;
                    return s.getSubmitDate() != null && !s.getSubmitDate().isBefore(oneMonthAgo);
                })
                .sorted(Comparator.comparing(FormSubmission::getCreatedAt).reversed())
                .map(this::createMySubmissionDto)
                .collect(Collectors.toList());
    }

    private MySubmissionResponseDto createMySubmissionDto(FormSubmission submission) {
        String cellName = null;
        if (submission.getTargetCellId() != null) {
            cellName = cellRepository.findById(submission.getTargetCellId())
                    .map(Cell::getCellName)
                    .orElse(null);
        }
        return new MySubmissionResponseDto(submission, cellName);
    }

    // 6. 마이페이지 (작성 가능 목록)
    @Transactional(readOnly = true)
    public List<AvailableFormResponseDto> getAvailableForms(User user) {
        List<FormTemplate> allTemplates = templateRepository.findAllByIsDeletedFalse();
        List<AvailableFormResponseDto> result = new ArrayList<>();

        // 기준 주일 계산 (오늘의 요일에 따라)
        // 월(1)~수(3): 지난 주일 / 목(4)~일(7): 이번 주 주일
        LocalDate today = LocalDate.now();
        DayOfWeek dayOfWeek = today.getDayOfWeek();
        LocalDate currentTargetSunday;

        if (dayOfWeek.getValue() >= 1 && dayOfWeek.getValue() <= 3) {
            currentTargetSunday = today.minusDays(dayOfWeek.getValue()); // 월->1일전, 화->2일전, 수->3일전
        } else {
            currentTargetSunday = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
        }

        for (FormTemplate template : allTemplates) {
            // 1. 비활성 폼 제외 (작년 보고서 등)
            if (!template.isActive())
                continue;

            // 2. 권한 체크
            if (!hasPermission(template, user, AccessType.RESPONDENT))
                continue;

            AvailableFormResponseDto dto = new AvailableFormResponseDto(template, false, null);

            // 3. [순 보고서] 날짜 계산 로직
            if (template.getCategory() == FormCategory.CELL_REPORT) {
                // 시작일 계산: 템플릿 시작일(필수) 또는 생성일
                LocalDate startDate = template.getStartDate() != null ? template.getStartDate().toLocalDate()
                        : template.getCreatedAt().toLocalDate();

                // 시작일이 속한 첫 주일 계산
                LocalDate loopDate = startDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

                List<LocalDate> missedDates = new ArrayList<>();

                // [최적화] 루프 밖에서 셀 정보 한 번만 조회
                Long cellId = null;
                if (user.getMember() != null) {
                    cellId = memberCellHistoryRepository.findByMemberAndIsActiveTrue(user.getMember())
                            .map(h -> h.getCell().getCellId())
                            .orElse(null);
                }

                // 시작일부터 기준 주차까지 순회
                while (!loopDate.isAfter(currentTargetSunday)) {
                    // 해당 주차 제출 확인
                    boolean submitted = false;
                    if (user.getMember() != null) {
                        if (cellId != null) {
                            // 해당 순에서 누군가 냈는지 확인 (REJECTED 제외)
                            submitted = submissionRepository
                                    .existsByTemplateIdAndTargetSundayDateAndTargetCellIdAndStatusNot(
                                            template.getId(), loopDate, cellId, SubmissionStatus.REJECTED);
                        } else {
                            // 소속된 순이 없는 경우 본인 제출 여부 확인
                            submitted = submissionRepository
                                    .existsBySubmitterAndTemplateAndTargetSundayDateAndStatusNot(
                                            user.getMember(), template, loopDate, SubmissionStatus.REJECTED);
                        }
                    }

                    if (!submitted) {
                        missedDates.add(loopDate);
                    }

                    // 1주씩 증가
                    loopDate = loopDate.plusWeeks(1);
                }

                // 최신 날짜가 위로 오도록 정렬
                missedDates.sort(Comparator.reverseOrder());

                dto.setSelectableDates(missedDates);

                if (missedDates.isEmpty()) {
                    dto.setSubmitted(true); // 모두 제출함
                    dto.setStatusMessage("모두 제출 완료");
                } else {
                    dto.setStatusMessage(missedDates.size() + "건 미제출");
                }
            }

            // 4. [일반/이벤트] 폼 로직
            else {
                if (user.getMember() != null) {
                    Optional<FormSubmission> last = submissionRepository
                            .findFirstByTemplateAndSubmitterOrderBySubmitDateDesc(
                                    template, user.getMember());
                    if (last.isPresent()) {
                        dto.setSubmitted(true);
                        dto.setLastSubmitDate(last.get().getSubmitDate());
                    }
                }

                // 마감 메시지 처리
                if (template.getEndDate() != null && LocalDateTime.now().isAfter(template.getEndDate())) {
                    dto.setStatusMessage("마감됨");
                }
            }

            result.add(dto);
        }
        return result;
    }

    // 7. 제출 승인 (동아리 가입 신청서 등)
    public void approveSubmission(Long submissionId, User currentUser) {
        FormSubmission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new IllegalArgumentException("제출 내역을 찾을 수 없습니다."));

        if (!hasPermission(submission.getTemplate(), currentUser, AccessType.MANAGER)) {
            throw new GeneralException(CommonErrorCode.FORBIDDEN);
        }

        submission.approve();

        if (submission.getTargetClubId() != null) {
            Club club = clubRepository.findById(submission.getTargetClubId())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 동아리입니다."));

            Member applicant = submission.getSubmitter();
            if (applicant != null) {
                if (clubMemberRepository.findByClubAndMember(club, applicant).isEmpty()) {
                    ClubMember newMember = new ClubMember(club, applicant);
                    clubMemberRepository.save(newMember);
                }
            }
        }
    }

    public void rejectSubmission(Long submissionId, User currentUser) {
        FormSubmission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new IllegalArgumentException("제출 내역을 찾을 수 없습니다."));

        if (!hasPermission(submission.getTemplate(), currentUser, AccessType.MANAGER)) {
            throw new GeneralException(CommonErrorCode.FORBIDDEN);
        }

        submission.reject();
    }

    @Transactional(readOnly = true)
    public List<ClubSubmissionResponseDto> getClubApplications(Long clubId, User user) {
        Club club = clubRepository.findById(clubId).orElseThrow();

        // 권한 체크: 관리자거나 해당 클럽의 리더여야 함
        if (!user.isPrivileged() &&
                (club.getLeader() == null || !club.getLeader().getId().equals(user.getMember().getId()))) {
            throw new GeneralException(CommonErrorCode.FORBIDDEN);
        }

        return submissionRepository.findAllByTargetClubId(clubId).stream()
                .map(ClubSubmissionResponseDto::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public FormDetailResponseDto getTemplateDetail(Long templateId, User user) {
        FormTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new IllegalArgumentException("템플릿을 찾을 수 없습니다."));
        return new FormDetailResponseDto(template);
    }

    @Transactional(readOnly = true)
    public FormDetailResponseDto getTemplateByClubId(Long clubId) {
        FormTemplate template = templateRepository.findByTargetClubId(clubId).orElse(null);
        if (template == null)
            return null;
        return new FormDetailResponseDto(template);
    }

    // 1. 전체 템플릿 목록 조회
    @Transactional(readOnly = true)
    public List<AvailableFormResponseDto> getAllTemplatesForAdmin(User user) {

        return templateRepository.findAllByTargetClubIdIsNullAndIsDeletedFalseOrderByCreatedAtDesc()
                .stream()
                .map(t -> {
                    // 관리자용 목록에는 상태 메시지에 활성 여부를 표시해주면 좋음
                    AvailableFormResponseDto dto = new AvailableFormResponseDto(t, false, null);
                    dto.setStatusMessage(t.isActive() ? "진행중" : "마감/숨김");
                    return dto;
                })
                .collect(Collectors.toList());
    }

    // 2. 템플릿 상세 조회 (수정용)
    @Transactional(readOnly = true)
    public AdminFormDetailResponseDto getTemplateDetailForAdmin(Long templateId, User user) {
        FormTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new IllegalArgumentException("템플릿을 찾을 수 없습니다."));

        // FormGuard를 통한 권한 체크 권장
        // if (!formGuard.canManage(templateId, new UserDetailsImpl(user))) throw ...

        return new AdminFormDetailResponseDto(template);
    }

    // 3. 템플릿 수정 (Soft Delete 적용 -> Orphan Removal로 변경)
    public void updateTemplate(Long templateId, TemplateUpdateRequestDto dto, User user) {
        FormTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new IllegalArgumentException("템플릿을 찾을 수 없습니다."));

        // 1. 기본 정보 업데이트
        template.updateBasicInfo(
                dto.getTitle(),
                dto.getDescription(),
                dto.getStartDate(),
                dto.getEndDate(),
                dto.getIsActive() != null ? dto.getIsActive() : template.isActive(),
                dto.getTargetClubId());

        // 2. 권한 목록 갱신
        template.getAccessList().clear();
        if (dto.getAccessList() != null) {
            for (TemplateUpdateRequestDto.AccessDto acc : dto.getAccessList()) {
                template.addAccess(FormAccess.builder()
                        .accessType(acc.getAccessType())
                        .targetType(acc.getTargetType())
                        .targetValue(acc.getTargetValue())
                        .build());
            }
        }

        // 3. 섹션/질문 업데이트
        // 요청된 섹션 ID 목록
        Set<Long> reqSectionIds = dto.getSections().stream()
                .map(TemplateUpdateRequestDto.SectionUpdateDto::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // 3-1. 삭제된 섹션 처리 (Soft Delete)
        // 기존: template.getSections().removeIf(section ->
        // !reqSectionIds.contains(section.getId()));
        template.getSections().stream()
                .filter(section -> !reqSectionIds.contains(section.getId()))
                .forEach(FormSection::disable);

        // 3-2. 섹션 갱신 및 추가
        for (int i = 0; i < dto.getSections().size(); i++) {
            var sDto = dto.getSections().get(i);

            FormSection section = null;
            if (sDto.getId() != null) {
                section = template.getSections().stream()
                        .filter(s -> s.getId().equals(sDto.getId()))
                        .findFirst()
                        .orElse(null);
            }

            if (section == null) {
                // 신규 섹션
                section = FormSection.builder()
                        .title(sDto.getTitle())
                        .description(sDto.getDescription())
                        .orderIndex(i)
                        .build();
                template.addSection(section);
            } else {
                // 기존 섹션 업데이트
                section.update(sDto.getTitle(), sDto.getDescription(), i);
                section.activate();
            }

            // 3-3. 질문 처리
            // 요청된 질문 ID 목록
            Set<Long> reqQuestionIds = sDto.getQuestions().stream()
                    .map(TemplateUpdateRequestDto.QuestionUpdateDto::getId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            // 삭제된 질문 처리 (Soft Delete)
            // 기존: section.getQuestions().removeIf(q ->
            // !reqQuestionIds.contains(q.getId()));
            section.getQuestions().stream()
                    .filter(q -> !reqQuestionIds.contains(q.getId()))
                    .forEach(FormQuestion::disable);

            for (int j = 0; j < sDto.getQuestions().size(); j++) {
                var qDto = sDto.getQuestions().get(j);
                String optionsJson = null;
                try {
                    optionsJson = objectMapper.writeValueAsString(qDto.getOptions());
                } catch (Exception e) {
                }

                FormQuestion question = null;
                if (qDto.getId() != null) {
                    question = section.getQuestions().stream()
                            .filter(q -> Objects.equals(q.getId(), qDto.getId()))
                            .findFirst()
                            .orElse(null);
                }

                if (question != null) {
                    // 기존 질문 수정
                    // 내용 변경 여부 확인
                    boolean isChanged = question.isContentChanged(
                            qDto.getLabel(), qDto.getInputType(), optionsJson, qDto.isRequired(),
                            qDto.isMemberSpecific(), qDto.getLinkedWorshipCategory(),
                            qDto.getLinkedScheduleId(), qDto.getLinkedScheduleDate(), qDto.getSyncType());

                    if (isChanged) {
                        // 내용 변경 시: 기존 질문 비활성화 + 신규 질문 생성
                        question.disable();

                        FormQuestion newQuestion = FormQuestion.builder()
                                .label(qDto.getLabel())
                                .inputType(qDto.getInputType())
                                .syncType(qDto.getSyncType())
                                .optionsJson(optionsJson)
                                .required(qDto.isRequired())
                                .orderIndex(j)
                                .isMemberSpecific(qDto.isMemberSpecific())
                                .linkedWorshipCategory(qDto.getLinkedWorshipCategory())
                                .linkedScheduleId(qDto.getLinkedScheduleId())
                                .linkedScheduleDate(qDto.getLinkedScheduleDate())
                                .build();
                        section.addQuestion(newQuestion);
                    } else {
                        // 내용 변경 없음: 순서만 업데이트 및 활성화
                        question.setOrderIndex(j);
                        question.setActive(true);
                    }
                } else {
                    // 신규 질문 생성
                    section.addQuestion(FormQuestion.builder()
                            .label(qDto.getLabel())
                            .inputType(qDto.getInputType())
                            .syncType(qDto.getSyncType())
                            .optionsJson(optionsJson)
                            .required(qDto.isRequired())
                            .orderIndex(j)
                            .isMemberSpecific(qDto.isMemberSpecific())
                            .linkedWorshipCategory(qDto.getLinkedWorshipCategory())
                            .linkedScheduleId(qDto.getLinkedScheduleId())
                            .linkedScheduleDate(qDto.getLinkedScheduleDate())
                            .build());
                }
            }
        }
    }

    // 4. 템플릿 삭제 (Soft Delete)
    public void deleteTemplate(Long templateId) {
        FormTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new IllegalArgumentException("템플릿을 찾을 수 없습니다."));
        template.delete();
        // template.setActive(false); // 엔티티에 setActive 메서드 추가 필요
    }

    // ================= [ADMIN] 응답(Submission) 관리 =================

    // 5. 특정 폼의 응답 목록 조회
    @Transactional(readOnly = true)
    public List<AdminSubmissionSummaryDto> getSubmissionsByTemplate(Long templateId) {
        FormTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new IllegalArgumentException("템플릿 없음"));

        return submissionRepository.findAllByTemplateOrderByCreatedAtDesc(template)
                .stream().map(AdminSubmissionSummaryDto::new).collect(Collectors.toList());
    }

    // 6. 응답 상세 조회 (기존 SubmissionDetailResponseDto 활용)
    @Transactional(readOnly = true)
    public SubmissionDetailResponseDto getSubmissionDetailForAdmin(Long submissionId) {
        FormSubmission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new IllegalArgumentException("제출 내역 없음"));
        return createSubmissionDetailDto(submission);
    }

    @Transactional(readOnly = true)
    public SubmissionDetailResponseDto getSubmissionDetail(Long submissionId, User user) {
        FormSubmission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new IllegalArgumentException("제출 내역 없음"));

        // 권한 체크: 작성자 본인이거나 관리자인 경우
        boolean isAuthor = submission.getSubmitter() != null
                && submission.getSubmitter().getId().equals(user.getMember().getId());
        boolean isCellLeader = false;

        // [추가] 순 보고서인 경우 해당 순의 리더/부리더 권한 체크
        if (submission.getTemplate().getCategory() == FormCategory.CELL_REPORT
                && submission.getTargetCellId() != null) {
            isCellLeader = memberCellHistoryRepository.findByMemberAndIsActiveTrue(user.getMember())
                    .map(h -> (h.isLeader() || h.isSubLeader())
                            && h.getCell().getCellId().equals(submission.getTargetCellId()))
                    .orElse(false);
        }

        if (!user.isPrivileged() && !isAuthor && !isCellLeader) {
            // 추가 권한 체크: 해당 템플릿의 매니저 권한이 있는지 확인
            if (!hasPermission(submission.getTemplate(), user, AccessType.MANAGER)) {
                throw new GeneralException(CommonErrorCode.FORBIDDEN);
            }
        }

        return createSubmissionDetailDto(submission);
    }

    private SubmissionDetailResponseDto createSubmissionDetailDto(FormSubmission submission) {
        String cellName = null;
        if (submission.getTargetCellId() != null) {
            cellName = cellRepository.findById(submission.getTargetCellId())
                    .map(Cell::getCellName)
                    .orElse(null);
        }
        return new SubmissionDetailResponseDto(submission, cellName);
    }

    public void updateTemplateStatus(Long templateId, Boolean isActive, User user) {
        // 1. 템플릿 조회
        FormTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new IllegalArgumentException("템플릿을 찾을 수 없습니다."));

        // 2. 권한 체크 (필요시 FormGuard 사용 또는 단순 관리자 체크)
        // 관리자가 아니면 권한 없음 예외 처리
        if (!user.isPrivileged()) {
            // 만약 동아리장도 자기 폼을 닫을 수 있게 하려면 여기에 로직 추가
            throw new GeneralException(CommonErrorCode.FORBIDDEN);
        }

        // 3. 상태 변경 (FormTemplate 엔티티에 이미 메서드가 있음)
        if (Boolean.TRUE.equals(isActive)) {
            template.activate();
        } else {
            template.deactivate();
        }
        templateRepository.save(template);
        // Transactional에 의해 자동 저장 (Dirty Checking)
    }

    @Transactional(readOnly = true)
    public SubmissionDetailResponseDto getLastSubmission(Long templateId, LocalDate date, Long cellId, User user) {
        if (user == null)
            return null;

        FormTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new IllegalArgumentException("템플릿을 찾을 수 없습니다."));
        Member member = user.getMember();

        FormSubmission submission = null;

        if (template.getCategory() == FormCategory.CELL_REPORT && date != null) {
            LocalDate targetSunday = date.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

            // [변경] 순 보고서는 제출자 기준이 아닌, 해당 순(cellId)의 보고서를 조회
            Long effectiveCellId = cellId;
            if (effectiveCellId == null && member != null) {
                effectiveCellId = memberCellHistoryRepository.findByMemberAndIsActiveTrue(member)
                        .map(h -> h.getCell().getCellId())
                        .orElse(null);
            }

            if (effectiveCellId != null) {
                submission = submissionRepository.findAllByTargetCellId(effectiveCellId).stream()
                        .filter(s -> s.getTemplate().getId().equals(templateId))
                        .filter(s -> s.getTargetSundayDate() != null && s.getTargetSundayDate().equals(targetSunday))
                        .sorted(Comparator.comparing(FormSubmission::getCreatedAt).reversed())
                        .findFirst().orElse(null);
            }
        } else {
            submission = submissionRepository.findFirstByTemplateAndSubmitterOrderBySubmitDateDesc(template, member)
                    .orElse(null);
        }

        if (submission == null)
            return null;
        return createSubmissionDetailDto(submission);
    }

    public void updateSubmission(Long submissionId, SubmissionRequestDto dto, User user) {
        FormSubmission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new IllegalArgumentException("제출 내역을 찾을 수 없습니다."));

        // 권한 체크: 작성자 본인이거나 해당 순의 리더/부리더인 경우
        boolean isAuthor = submission.getSubmitter() != null
                && submission.getSubmitter().getId().equals(user.getMember().getId());
        boolean isCellLeader = false;

        if (submission.getTemplate().getCategory() == FormCategory.CELL_REPORT
                && submission.getTargetCellId() != null) {
            isCellLeader = memberCellHistoryRepository.findByMemberAndIsActiveTrue(user.getMember())
                    .map(h -> (h.isLeader() || h.isSubLeader())
                            && h.getCell().getCellId().equals(submission.getTargetCellId()))
                    .orElse(false);
        }

        if (user == null || (!isAuthor && !isCellLeader)) {
            throw new GeneralException(CommonErrorCode.FORBIDDEN);
        }

        // 작성 후 1주일(7일) 이내만 수정 가능
        if (submission.getCreatedAt().isBefore(LocalDateTime.now().minusDays(7))) {
            throw new GeneralException(CommonErrorCode.BAD_REQUEST);
        }

        // 필수 답변 검증
        validateRequiredAnswers(submission.getTemplate(), dto);

        // [추가] 날짜 및 셀 정보 업데이트
        if (submission.getTemplate().getCategory() == FormCategory.CELL_REPORT && dto.getDate() != null) {
            LocalDate targetSunday = dto.getDate().with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
            submission.updateTargetInfo(targetSunday, dto.getCellId());
        } else if (dto.getCellId() != null) {
            submission.updateTargetInfo(submission.getTargetSundayDate(), dto.getCellId());
        }

        submission.getAnswers().clear();

        for (SubmissionRequestDto.AnswerDto ans : dto.getAnswers()) {
            FormQuestion question = questionRepository.findById(ans.getQuestionId()).orElseThrow();
            Member target = null;

            if (ans.getTargetMemberId() != null) {
                target = memberRepository.findById(ans.getTargetMemberId()).orElse(null);
            } else {
                target = user.getMember();
            }

            submission.addAnswer(FormAnswer.builder()
                    .submission(submission)
                    .question(question)
                    .targetMember(target)
                    .value(ans.getValue())
                    .build());
        }

        processAttendanceSync(submission, dto.getTargetScheduleId());
    }

    private void sendClubApplicationNotification(Long clubId, Member applicant, FormSubmission submission) {
        try {
            Club club = clubRepository.findById(clubId).orElse(null);
            if (club != null && club.getLeader() != null) {
                Member leader = club.getLeader();
                String applicantName = (applicant != null) ? applicant.getName() : submission.getGuestName();
                String title = "팀 가입 신청 알림";
                String body = applicantName + "님이 " + club.getName() + " 팀에 가입을 신청했습니다.";

                userRepository.findByMember(leader)
                        .ifPresent(user -> fcmService.sendNotificationToUser(user.getId(), title, body));
            }
        } catch (Exception e) {
            System.err.println("Failed to send notification: " + e.getMessage());
        }
    }
}