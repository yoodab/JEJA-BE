package com.jeja.jejabe.form.dto;

import com.jeja.jejabe.form.domain.*;
import com.jeja.jejabe.schedule.domain.WorshipCategory;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class TemplateUpdateRequestDto {
    // 수정할 때만 필요한 필드들
    private String title;
    private String description;
    private FormCategory category;

    // 관리자 설정 정보 수정
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Boolean isActive;

    // 섹션 및 질문 목록 (수정 시에는 ID가 포함된 DTO 사용)
    private List<SectionUpdateDto> sections;
    private List<AccessDto> accessList;

    @Data
    public static class SectionUpdateDto {
        private Long id; // [중요] 기존 섹션 수정 시 ID 필수
        private String title;
        private String description;
        private NextActionType defaultNextAction;
        private Integer defaultTargetSectionIndex;
        private List<QuestionUpdateDto> questions;
    }

    @Data
    public static class QuestionUpdateDto {
        private Long id; // [중요] 기존 질문 수정 시 ID 필수 (없으면 신규 추가로 간주)
        private String label;
        private QuestionType inputType;
        private AttendanceSyncType syncType;
        private List<OptionDto> options;
        private boolean required;
        private boolean isMemberSpecific;
        private WorshipCategory linkedWorshipCategory;
        private Long linkedScheduleId;
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
