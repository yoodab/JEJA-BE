package com.jeja.jejabe.form.dto;

import com.jeja.jejabe.form.domain.FormAnswer;
import com.jeja.jejabe.form.domain.FormSubmission;
import com.jeja.jejabe.form.domain.QuestionType;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
public class SubmissionDetailResponseDto {
    private Long submissionId;
    private Long templateId;
    private String formTitle;
    private String submitDate;
    private LocalDateTime submitTime;
    private String targetSundayDate;
    private String status;
    private String submitterName;
    private String targetCellName;
    private Long targetCellId;
    private List<QuestionAnswerDto> items;

    public SubmissionDetailResponseDto(FormSubmission submission) {
        this(submission, null);
    }

    public SubmissionDetailResponseDto(FormSubmission submission, String cellName) {
        this.submissionId = submission.getId();
        this.templateId = submission.getTemplate().getId();
        this.formTitle = submission.getTemplate().getTitle();
        this.submitDate = submission.getSubmitDate().toString();
        this.submitTime = submission.getCreatedAt();
        this.targetSundayDate = submission.getTargetSundayDate() != null ? submission.getTargetSundayDate().toString()
                : null;
        this.status = submission.getStatus().toString();
        this.submitterName = submission.getSubmitter() != null ? submission.getSubmitter().getName() : "익명";
        this.targetCellName = cellName;
        this.targetCellId = submission.getTargetCellId();

        // 답변 리스트를 순회하며 DTO 생성
        this.items = submission.getAnswers().stream()
                .map(answer -> new QuestionAnswerDto(answer))
                .collect(Collectors.toList());
    }

    @Data
    public static class QuestionAnswerDto {
        private Long questionId;
        private String label;
        private QuestionType inputType;

        // 답변 리스트 (멤버명 + 값) - 단일 답변이지만 구조 유지
        private List<AnswerDetail> answers;

        public QuestionAnswerDto(FormAnswer answer) {
            this.questionId = answer.getQuestion().getId();
            this.label = answer.getQuestion().getLabel();
            this.inputType = answer.getQuestion().getInputType();

            this.answers = List.of(new AnswerDetail(
                    answer.getTargetMember() != null ? answer.getTargetMember().getName() : null,
                    answer.getValue()));
        }
    }

    @Data
    public static class AnswerDetail {
        private String memberName;
        private String value;

        public AnswerDetail(String memberName, String value) {
            this.memberName = memberName;
            this.value = value;
        }
    }
}