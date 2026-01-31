package com.jeja.jejabe.form.dto;

import com.jeja.jejabe.form.domain.FormQuestion;
import com.jeja.jejabe.form.domain.QuestionType;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class QuestionSnapshot {
    private Long questionId;
    private String label;
    private QuestionType inputType;
    private int orderIndex;
    private boolean isMemberSpecific;
    private String options; // 기존 필드명 유지 (optionsJson 내용을 담음)

    public QuestionSnapshot(FormQuestion q) {
        this.questionId = q.getId();
        this.label = q.getLabel();
        this.inputType = q.getInputType();
        this.orderIndex = q.getOrderIndex();
        this.isMemberSpecific = q.isMemberSpecific();
        this.options = q.getOptionsJson(); // JSON 문자열 그대로 복사
    }
}