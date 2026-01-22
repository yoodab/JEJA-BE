package com.jeja.jejabe.form.domain;

import com.jeja.jejabe.member.domain.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FormAnswer {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submission_id")
    private FormSubmission submission;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id")
    private FormQuestion question;

    // 순 보고서일 때: 누구에 대한 데이터인지 (개인보고서면 보통 null 또는 submitter와 동일)
    @ManyToOne(fetch = FetchType.LAZY)
    private Member targetMember;

    @Column(columnDefinition = "TEXT")
    private String value; // 답변 내용 ("true", "텍스트", "옵션1")

    @Builder
    public FormAnswer(FormSubmission submission, FormQuestion question, Member targetMember, String value) {
        this.submission = submission;
        this.question = question;
        this.targetMember = targetMember;
        this.value = value;
    }
}
