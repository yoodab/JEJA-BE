package com.jeja.jejabe.form.dto;

import com.jeja.jejabe.form.domain.FormAnswer;
import com.jeja.jejabe.form.domain.FormSubmission;
import com.jeja.jejabe.form.domain.QuestionType;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Data
public class SubmissionDetailResponseDto {
    private Long submissionId;
    private String formTitle;
    private String submitDate;
    private List<QuestionAnswerDto> items;

    public SubmissionDetailResponseDto(FormSubmission submission) {
        this.submissionId = submission.getId();
        this.formTitle = submission.getTemplate().getTitle();
        this.submitDate = submission.getSubmitDate().toString();

        // 스냅샷 리스트를 순회하며 DTO 생성
        this.items = submission.getSnapshotList().stream()
                .map(snapshot -> new QuestionAnswerDto(snapshot, submission.getAnswers())) // 생성자 호출
                .collect(Collectors.toList());
    }

    @Data
    public static class QuestionAnswerDto {
        private Long questionId;
        private String label;
        private QuestionType inputType;

        // 답변 리스트 (멤버명 + 값)
        private List<AnswerDetail> answers;

        public QuestionAnswerDto(QuestionSnapshot snapshot, List<FormAnswer> allAnswers) {
            this.questionId = snapshot.getQuestionId();
            this.label = snapshot.getLabel();
            this.inputType = snapshot.getInputType();

            // 이 질문에 해당하는 모든 답변을 수집
            this.answers = allAnswers.stream()
                    .filter(a -> a.getQuestion().getId().equals(snapshot.getQuestionId()))
                    .map(a -> new AnswerDetail(
                            a.getTargetMember() != null ? a.getTargetMember().getName() : null,
                            a.getValue()
                    ))
                    .collect(Collectors.toList());
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