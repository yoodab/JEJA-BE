package com.jeja.jejabe.form.dto;

import com.jeja.jejabe.form.domain.AttendanceSyncType;
import com.jeja.jejabe.form.domain.FormQuestion;
import com.jeja.jejabe.form.domain.QuestionType;
import com.jeja.jejabe.schedule.domain.WorshipCategory;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
public class QuestionSnapshot {
    private Long questionId;
    private String label;
    private QuestionType inputType;
    private AttendanceSyncType syncType;
    private int orderIndex;
    private boolean isMemberSpecific;
    private String options; // 기존 필드명 유지 (optionsJson 내용을 담음)
    private WorshipCategory linkedWorshipCategory;
    private Long linkedScheduleId;
    private LocalDate linkedScheduleDate;

    public QuestionSnapshot(FormQuestion q) {
        this.questionId = q.getId();
        this.label = q.getLabel();
        this.inputType = q.getInputType();
        this.syncType = q.getSyncType();
        this.orderIndex = q.getOrderIndex();
        this.isMemberSpecific = q.isMemberSpecific();
        this.options = q.getOptionsJson(); // JSON 문자열 그대로 복사
        this.linkedWorshipCategory = q.getLinkedWorshipCategory();
        this.linkedScheduleId = q.getLinkedScheduleId();
        this.linkedScheduleDate = q.getLinkedScheduleDate();
    }
}