package com.jeja.jejabe.form.dto;

import com.jeja.jejabe.form.domain.*;
import com.jeja.jejabe.schedule.domain.WorshipCategory;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class TemplateCreateRequestDto {
    private String title;
    private String description;
    private FormCategory category;
    private List<SectionDto> sections;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Boolean isActive;
    private List<AccessDto> accessList;
    private FormType type;
    private Long targetClubId;

    @Data
    public static class SectionDto {
        private Long id;
        private String title;
        private String description;
        private NextActionType defaultNextAction;
        private Integer defaultTargetSectionIndex;
        private List<QuestionDto> questions;
    }

    @Data
    public static class QuestionDto {
        private String label;
        private QuestionType inputType;
        private AttendanceSyncType syncType;
        private List<OptionDto> options;
        private boolean required;
        private boolean MemberSpecific; // 멤버별 입력 여부 추가
        private WorshipCategory linkedWorshipCategory;
        private Long linkedScheduleId; // 특정 일정 ID
        private LocalDate linkedScheduleDate;
    }

    @Data
    public static class OptionDto {
        private String label;
        private NextActionType nextAction;
        private Integer targetSectionIndex;
    }

    @Data
    public static class AccessDto {
        private AccessType accessType;
        private TargetType targetType;
        private String targetValue;
    }
}