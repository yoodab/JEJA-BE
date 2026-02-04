package com.jeja.jejabe.form.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jeja.jejabe.form.dto.QuestionSnapshot;
import com.jeja.jejabe.global.entity.BaseTimeEntity;
import com.jeja.jejabe.member.domain.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FormSubmission extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private FormTemplate template;

    @ManyToOne(fetch = FetchType.LAZY)
    private Member submitter; // 로그인 사용자 (null 가능)

    // 비로그인 제출자 정보
    private String guestName;
    private String guestPhone;

    private LocalDate submitDate;
    private LocalDate targetSundayDate; // 순 보고서용 기준 주일

    private Long targetCellId;
    private Long targetClubId;

    @Enumerated(EnumType.STRING)
    private SubmissionStatus status;

    @OneToMany(mappedBy = "submission", cascade = CascadeType.ALL)
    private List<FormAnswer> answers = new ArrayList<>();

    @Builder
    public FormSubmission(FormTemplate template, Member submitter, String guestName, String guestPhone,
            LocalDate submitDate, LocalDate targetSundayDate, Long targetCellId, Long targetClubId) {
        this.template = template;
        this.submitter = submitter;
        this.guestName = guestName;
        this.guestPhone = guestPhone;
        this.submitDate = submitDate;
        this.targetSundayDate = targetSundayDate;
        this.targetCellId = targetCellId;
        this.targetClubId = targetClubId;
        this.status = SubmissionStatus.PENDING;
    }

    public void addAnswer(FormAnswer answer) {
        this.answers.add(answer);
    }

    public void approve() {
        this.status = SubmissionStatus.APPROVED;
    }

    public void reject() {
        this.status = SubmissionStatus.REJECTED;
    }
}