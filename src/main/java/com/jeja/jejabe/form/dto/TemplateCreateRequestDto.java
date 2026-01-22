package com.jeja.jejabe.form.dto;

import com.jeja.jejabe.form.domain.*;
import lombok.Data;

import java.util.List;

@Data
public class TemplateCreateRequestDto {
    private String title;
    private String description;
    private FormCategory category;
    private FormType type;
    private Long targetClubId;
    private List<QuestionDto> questions;
    private List<AccessDto> accessList;

    @Data
    public static class QuestionDto {
        private String label;
        private QuestionType inputType;
        private List<String> options;
        private boolean required;
        private boolean isMemberSpecific;
        private Long linkedWorshipCategoryId;
    }

    @Data
    public static class AccessDto {
        private AccessType accessType;
        private TargetType targetType;
        private String targetValue;
    }
}