package com.jeja.jejabe.form.dto;

import com.jeja.jejabe.form.domain.FormSubmission;
import com.jeja.jejabe.form.domain.SubmissionStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminSubmissionSummaryDto {
    private Long id;
    private String submitterName; // 로그인 유저면 이름, 아니면 guestName
    private String description;   // "000 순 보고서" or "동아리 신청"
    private LocalDateTime submitTime; // BaseTimeEntity의 createdAt
    private SubmissionStatus status;

    public AdminSubmissionSummaryDto(FormSubmission s) {
        this.id = s.getId();
        this.status = s.getStatus();
        this.submitTime = s.getCreatedAt();

        if (s.getSubmitter() != null) {
            this.submitterName = s.getSubmitter().getName();
        } else {
            this.submitterName = s.getGuestName() + "(비회원)";
        }

        this.description = s.getTemplate().getTitle();
    }
}
