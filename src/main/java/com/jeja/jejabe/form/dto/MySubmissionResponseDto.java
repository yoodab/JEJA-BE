package com.jeja.jejabe.form.dto;

import com.jeja.jejabe.form.domain.FormSubmission;
import com.jeja.jejabe.form.domain.SubmissionStatus;
import lombok.Data;

import java.time.LocalDate;

@Data
public class MySubmissionResponseDto {
    private Long submissionId;
    private String templateTitle;
    private LocalDate submitDate;
    private SubmissionStatus status;

    public MySubmissionResponseDto(FormSubmission s) {
        this.submissionId = s.getId();
        this.templateTitle = s.getTemplate().getTitle();
        this.submitDate = s.getSubmitDate();
        this.status = s.getStatus();
    }
}
