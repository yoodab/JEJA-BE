package com.jeja.jejabe.form.dto;

import com.jeja.jejabe.form.domain.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class FormDetailResponseDto {
    private Long templateId;
    private String title;
    private String description;
    private List<SectionResponseDto> sections;

    public FormDetailResponseDto(FormTemplate t) {
        this.templateId = t.getId();
        this.title = t.getTitle();
        this.description = t.getDescription();
        this.sections = t.getSections().stream()
                .map(SectionResponseDto::new)
                .collect(Collectors.toList());
    }

    @Data
    public static class SectionResponseDto {
        private String title;
        private String description;
        private int orderIndex;
        private NextActionType defaultNextAction;
        private Integer defaultTargetSectionIndex;
        private List<QuestionResponseDto> questions;

        public SectionResponseDto(FormSection s) {
            this.title = s.getTitle();
            this.description = s.getDescription();
            this.orderIndex = s.getOrderIndex();
            this.defaultNextAction = s.getDefaultNextAction();
            this.defaultTargetSectionIndex = s.getDefaultTargetSectionIndex();
            this.questions = s.getQuestions().stream()
                    .filter(FormQuestion::isActive) // [중요] 삭제된 질문 제외하고 보내기
                    .map(QuestionResponseDto::new)
                    .collect(Collectors.toList());
        }
    }

    @Data
    public static class QuestionResponseDto {
        private Long id;
        private String label;
        private QuestionType inputType;
        private String optionsJson; // 프론트에서 파싱해서 분기 로직 처리
        private boolean required;
        private boolean isMemberSpecific;
        private Long linkedScheduleId; // 특정 일정 ID
        private LocalDate linkedScheduleDate;

        public QuestionResponseDto(FormQuestion q) {
            this.id = q.getId();
            this.label = q.getLabel();
            this.inputType = q.getInputType();
            this.optionsJson = q.getOptionsJson();
            this.required = q.isRequired();
            this.isMemberSpecific = q.isMemberSpecific();
            this.linkedScheduleId = q.getLinkedScheduleId();
            this.linkedScheduleDate = q.getLinkedScheduleDate();

        }
    }
}
