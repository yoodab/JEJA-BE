package com.jeja.jejabe.form;

import com.jeja.jejabe.attendance.AttendanceService;
import com.jeja.jejabe.auth.User;
import com.jeja.jejabe.auth.UserRole;
import com.jeja.jejabe.club.Club;
import com.jeja.jejabe.club.ClubMember;
import com.jeja.jejabe.club.ClubMemberRepository;
import com.jeja.jejabe.club.ClubRepository;
import com.jeja.jejabe.form.domain.*;
import com.jeja.jejabe.form.dto.AvailableFormResponseDto;
import com.jeja.jejabe.form.dto.MySubmissionResponseDto;
import com.jeja.jejabe.form.dto.SubmissionRequestDto;
import com.jeja.jejabe.form.dto.TemplateCreateRequestDto;
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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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

    // 1. 템플릿 생성
    public Long createTemplate(TemplateCreateRequestDto dto) {
        FormTemplate template = FormTemplate.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .category(dto.getCategory())
                .type(dto.getType())
                .targetClubId(dto.getTargetClubId())
                .build();

        for (int i = 0; i < dto.getQuestions().size(); i++) {
            var qDto = dto.getQuestions().get(i);
            String options = qDto.getOptions() != null ? String.join(",", qDto.getOptions()) : null;

            template.addQuestion(FormQuestion.builder()
                    .label(qDto.getLabel())
                    .inputType(qDto.getInputType())
                    .options(options)
                    .required(qDto.isRequired())
                    .isMemberSpecific(qDto.isMemberSpecific())
                    .linkedWorshipCategory(qDto.getLinkedWorshipCategory())
                    .orderIndex(i)
                    .build());
        }

        if (dto.getAccessList() != null) {
            for (TemplateCreateRequestDto.AccessDto acc : dto.getAccessList()) {
                template.addAccess(FormAccess.builder()
                        .accessType(acc.getAccessType())
                        .targetType(acc.getTargetType())
                        .targetValue(acc.getTargetValue())
                        .build());
            }
        }

        Long id = templateRepository.save(template).getId();
        if (dto.getTargetClubId() != null) {
            Club club = clubRepository.findById(dto.getTargetClubId()).orElseThrow();
            club.setApplicationTemplateId(id);
        }
        return id;
    }

    // 2. 폼 제출
    public void submitForm(SubmissionRequestDto dto, User user) {
        FormTemplate template = templateRepository.findById(dto.getTemplateId())
                .orElseThrow(() -> new IllegalArgumentException("템플릿을 찾을 수 없습니다."));

        // 권한 체크
        if (!hasPermission(template, user, AccessType.RESPONDENT)) {
            throw new GeneralException(CommonErrorCode.FORBIDDEN);
        }

        Member submitter = null;
        if (user != null) {
            submitter = user.getMember();
        } else if (dto.getGuestName() != null && dto.getGuestPhone() != null) {
            String phone = dto.getGuestPhone().replaceAll("[^0-9]", "");
            submitter = memberRepository.findByPhone(phone).orElse(null);
        }

        // 중복 제출 방지 (셀 보고서의 경우 이번 주 주일 기준)
        LocalDate targetSunday = null;
        if (template.getCategory() == FormCategory.CELL_REPORT) {
            targetSunday = dto.getDate().with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
            if (submitter != null && submissionRepository.existsBySubmitterAndTargetSundayDateAndTargetCellId(
                    submitter, targetSunday, dto.getCellId())) {
                throw new IllegalArgumentException("이미 제출하셨습니다.");
            }
        }

        FormSubmission submission = FormSubmission.builder()
                .template(template)
                .submitter(submitter)
                .guestName(dto.getGuestName())
                .guestPhone(dto.getGuestPhone())
                .submitDate(dto.getDate())
                .targetSundayDate(targetSunday)
                .targetCellId(dto.getCellId())
                .targetClubId(dto.getClubId())
                .build();
        submissionRepository.save(submission);

        for (SubmissionRequestDto.AnswerDto ans : dto.getAnswers()) {
            FormQuestion question = questionRepository.findById(ans.getQuestionId()).orElseThrow();
            Member target = ans.getTargetMemberId() != null ? memberRepository.findById(ans.getTargetMemberId()).orElse(null) : null;

            submission.addAnswer(FormAnswer.builder()
                    .submission(submission)
                    .question(question)
                    .targetMember(target)
                    .value(ans.getValue())
                    .build());
        }

        // 순 보고서일 경우 자동 출석 처리
        if (template.getCategory() == FormCategory.CELL_REPORT) {
            syncAttendance(submission);
        }
    }

    // 3. [핵심] 출석 연동 로직 (DB 기반 동적 카테고리 매칭)
    private void syncAttendance(FormSubmission submission) {
        // 보고서 제출 기준일의 해당 주간(월~일) 계산
        LocalDate targetDate = submission.getTargetSundayDate();
        LocalDateTime startOfWeek = targetDate.minusDays(6).atStartOfDay(); // 월요일 00:00
        LocalDateTime endOfWeek = targetDate.atTime(23, 59, 59);        // 일요일 23:59

        // 해당 주간의 모든 예배 스케줄 조회
        List<Schedule> weeklySchedules = scheduleRepository.findByTypeAndStartDateBetween(ScheduleType.WORSHIP, startOfWeek, endOfWeek);

        for (FormAnswer ans : submission.getAnswers()) {
            // 질문에 연결된 WorshipCategory ID 확인
            WorshipCategory category = ans.getQuestion().getLinkedWorshipCategory();

            // 연결된 카테고리가 없으면 일반 질문이므로 스킵
            if (category == null) continue;

            // 답변이 "true"이고 대상 멤버가 명확한 경우
            if ("true".equalsIgnoreCase(ans.getValue()) && ans.getTargetMember() != null) {

                // 해당 주간 스케줄 중, 질문에 연결된 카테고리와 일치하는 스케줄 찾기
                Schedule targetSchedule = weeklySchedules.stream()
                        .filter(s -> s.getWorshipCategory() == category)
                        .findFirst()
                        .orElse(null);

                // 일치하는 스케줄이 있으면 출석 처리
                if (targetSchedule != null) {
                    attendanceService.checkInByLeader(targetSchedule, ans.getTargetMember());
                }
            }
        }
    }

    // 4. 권한 체크 로직
    private boolean hasPermission(FormTemplate template, User user, AccessType requiredType) {
        // 관리자는 무조건 통과
        if (user != null && user.getUserRole() == UserRole.ROLE_ADMIN) return true;

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
                    if (user.getUserRole().name().equals(targetRole)) return true;
                    // MemberRole 체크
                    if (member != null) {
                        if (member.getRoles().stream().anyMatch(role -> role.name().equals(targetRole))) return true;
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
        if (user.getMember() == null) return List.of();
        return submissionRepository.findAllBySubmitterOrderBySubmitDateDesc(user.getMember())
                .stream().map(MySubmissionResponseDto::new).collect(Collectors.toList());
    }

    // 6. 마이페이지 (작성 가능 목록)
    @Transactional(readOnly = true)
    public List<AvailableFormResponseDto> getAvailableForms(User user) {
        List<FormTemplate> allTemplates = templateRepository.findAll();
        List<AvailableFormResponseDto> result = new ArrayList<>();

        for (FormTemplate template : allTemplates) {
            if (hasPermission(template, user, AccessType.RESPONDENT)) {
                boolean isSubmitted = false;
                LocalDate lastDate = null;

                if (template.getCategory() == FormCategory.CELL_REPORT) {
                    LocalDate targetSunday = LocalDate.now().with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
                    if (user.getMember() != null) {
                        isSubmitted = submissionRepository.existsBySubmitterAndTargetSundayDateAndTargetCellId(
                                user.getMember(), targetSunday, null);
                    }
                } else {
                    if (user.getMember() != null) {
                        Optional<FormSubmission> last = submissionRepository.findFirstByTemplateAndSubmitterOrderBySubmitDateDesc(
                                template, user.getMember());
                        if (last.isPresent()) {
                            isSubmitted = true;
                            lastDate = last.get().getSubmitDate();
                        }
                    }
                }
                result.add(new AvailableFormResponseDto(template, isSubmitted, lastDate));
            }
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

    @Transactional(readOnly = true)
    public List<MySubmissionResponseDto> getClubApplications(Long clubId, User user) {
        Club club = clubRepository.findById(clubId).orElseThrow();

        // 권한 체크: 관리자거나 해당 클럽의 리더여야 함
        if (user.getUserRole() != UserRole.ROLE_ADMIN &&
                (club.getLeader() == null || !club.getLeader().getId().equals(user.getMember().getId()))) {
            throw new GeneralException(CommonErrorCode.FORBIDDEN);
        }

        return submissionRepository.findAllByTargetClubId(clubId).stream()
                .map(MySubmissionResponseDto::new)
                .collect(Collectors.toList());
    }
}