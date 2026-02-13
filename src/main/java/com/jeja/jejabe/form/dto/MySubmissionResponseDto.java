package com.jeja.jejabe.form.dto;

import com.jeja.jejabe.form.domain.FormSubmission;
import com.jeja.jejabe.form.domain.SubmissionStatus;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class MySubmissionResponseDto {
    private Long submissionId;
    private Long templateId;
    private String templateTitle;
    private String submitterName;
    private LocalDateTime submitTime;
    private LocalDate targetSundayDate;
    private SubmissionStatus status;
    private String targetCellName;
    private String category;

    public MySubmissionResponseDto(FormSubmission s) {
        this(s, null);
    }

    public MySubmissionResponseDto(FormSubmission s, String cellName) {
        this.submissionId = s.getId();
        this.templateId = s.getTemplate().getId();
        this.templateTitle = s.getTemplate().getTitle();
        this.submitterName = s.getSubmitter() != null ? s.getSubmitter().getName() : "익명";
        this.submitTime = s.getCreatedAt();
        this.targetSundayDate = s.getTargetSundayDate();
        this.status = s.getStatus();
        this.targetCellName = cellName;
        this.category = s.getTemplate().getCategory() != null ? s.getTemplate().getCategory().name() : null;
    }
}
