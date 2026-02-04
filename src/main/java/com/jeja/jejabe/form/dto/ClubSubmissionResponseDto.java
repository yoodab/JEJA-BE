package com.jeja.jejabe.form.dto;

import com.jeja.jejabe.form.domain.FormSubmission;
import com.jeja.jejabe.form.domain.SubmissionStatus;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ClubSubmissionResponseDto {
    private Long submissionId;
    private String templateTitle;
    private String submitterName; // 작성자 이름 (회원 or 게스트)
    private LocalDate submitDate;
    private SubmissionStatus status;

    public ClubSubmissionResponseDto(FormSubmission s) {
        this.submissionId = s.getId();
        this.templateTitle = s.getTemplate().getTitle();
        if (s.getSubmitter() != null) {
            this.submitterName = s.getSubmitter().getName();
        } else {
            this.submitterName = s.getGuestName();
        }
        this.submitDate = s.getSubmitDate();
        this.status = s.getStatus();
    }
}
