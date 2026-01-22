package com.jeja.jejabe.form.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class SubmissionRequestDto {
    private Long templateId;
    private LocalDate date;
    private Long cellId;
    private Long clubId;
    private String guestName;
    private String guestPhone;
    private List<AnswerDto> answers;

    @Data
    public static class AnswerDto {
        private Long questionId;
        private Long targetMemberId;
        private String value;
    }
}