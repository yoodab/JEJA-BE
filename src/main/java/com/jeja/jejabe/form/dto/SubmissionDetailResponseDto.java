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
        this(submission, cellName, null);
    }

    public SubmissionDetailResponseDto(FormSubmission submission, String cellName, List<QuestionSnapshot> snapshots) {
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

        if (snapshots != null && !snapshots.isEmpty()) {
            // 스냅샷이 있으면 스냅샷 기준으로 응답 구성 (제출 당시의 질문 상태 유지)
            this.items = snapshots.stream()
                    .map(snapshot -> {
                        // 해당 질문에 대한 답변 찾기 (모든 답변)
                        List<FormAnswer> answers = submission.getAnswers().stream()
                                .filter(a -> a.getQuestion().getId().equals(snapshot.getQuestionId()))
                                .collect(Collectors.toList());

                        return new QuestionAnswerDto(snapshot, answers);
                    })
                    .collect(Collectors.toList());
        } else {
            // 스냅샷이 없으면 기존 방식대로 답변 리스트 기준 구성
            this.items = submission.getAnswers().stream()
                    .map(answer -> new QuestionAnswerDto(answer))
                    .collect(Collectors.toList());
        }
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

        // 스냅샷 기반 생성자
        public QuestionAnswerDto(QuestionSnapshot snapshot, List<FormAnswer> answers) {
            this.questionId = snapshot.getQuestionId();
            this.label = snapshot.getLabel();
            this.inputType = snapshot.getInputType();

            if (answers != null && !answers.isEmpty()) {
                this.answers = answers.stream()
                        .map(answer -> new AnswerDetail(
                                answer.getTargetMember() != null ? answer.getTargetMember().getName() : null,
                                answer.getValue()))
                        .collect(Collectors.toList());
            } else {
                this.answers = List.of();
            }
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