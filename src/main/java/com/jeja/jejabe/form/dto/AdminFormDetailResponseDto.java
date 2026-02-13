package com.jeja.jejabe.form.dto;

import com.jeja.jejabe.form.domain.*;
import com.jeja.jejabe.schedule.domain.WorshipCategory;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class AdminFormDetailResponseDto {
    private Long id;
    private String title;
    private String description;
    private FormCategory category;
    private FormType type;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private boolean isActive;

    private List<AccessDto> accessList;
    private List<SectionDto> sections;

    public AdminFormDetailResponseDto(FormTemplate t) {
        this.id = t.getId();
        this.title = t.getTitle();
        this.description = t.getDescription();
        this.category = t.getCategory();
        this.type = t.getType();
        this.startDate = t.getStartDate();
        this.endDate = t.getEndDate();
        this.isActive = t.isActive();

        this.accessList = t.getAccessList().stream()
                .map(AccessDto::new)
                .collect(Collectors.toList());

        this.sections = t.getSections().stream()
                .map(SectionDto::new)
                .collect(Collectors.toList());
    }

    @Data
    public static class AccessDto {
        private AccessType accessType;
        private TargetType targetType;
        private String targetValue;

        public AccessDto(FormAccess a) {
            this.accessType = a.getAccessType();
            this.targetType = a.getTargetType();
            this.targetValue = a.getTargetValue();
        }
    }

    // 기존 FormDetailResponseDto의 SectionDto, QuestionDto 재활용하거나
    // 관리자용으로 더 많은 필드(id 등)가 필요하면 아래처럼 별도 정의
    @Data
    public static class SectionDto {
        private Long id;
        private String title;
        private String description;
        private NextActionType defaultNextAction;
        private Integer defaultTargetSectionIndex;
        private List<QuestionDto> questions;

        public SectionDto(FormSection s) {
            this.id = s.getId();
            this.title = s.getTitle();
            this.description = s.getDescription();
            this.defaultNextAction = s.getDefaultNextAction();
            this.defaultTargetSectionIndex = s.getDefaultTargetSectionIndex();
            // 관리자는 숨겨진(isActive=false) 질문도 볼 필요가 있다면 필터링 제거, 아니면 유지
            this.questions = s.getQuestions().stream()
                    .filter(FormQuestion::isActive)
                    .map(QuestionDto::new).collect(Collectors.toList());
        }
    }

    @Data
    public static class QuestionDto {
        private Long id;
        private String label;
        private QuestionType inputType;
        private AttendanceSyncType syncType;
        private String optionsJson;
        private boolean required;
        private boolean isMemberSpecific;
        private WorshipCategory linkedWorshipCategory;
        private Long linkedScheduleId;
        private LocalDate linkedScheduleDate;

        public QuestionDto(FormQuestion q) {
            this.id = q.getId();
            this.label = q.getLabel();
            this.inputType = q.getInputType();
            this.syncType = q.getSyncType();
            this.optionsJson = q.getOptionsJson();
            this.required = q.isRequired();
            this.isMemberSpecific = q.isMemberSpecific();
            this.linkedWorshipCategory = q.getLinkedWorshipCategory();
            this.linkedScheduleId = q.getLinkedScheduleId();
            this.linkedScheduleDate = q.getLinkedScheduleDate();
        }
    }
}
