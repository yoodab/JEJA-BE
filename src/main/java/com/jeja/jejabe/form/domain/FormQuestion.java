package com.jeja.jejabe.form.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FormQuestion {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id")
    private FormTemplate template;

    @Column(nullable = false)
    private String label;
    private int orderIndex;
    private boolean required;

    @Enumerated(EnumType.STRING)
    private QuestionType inputType;

    @Column(columnDefinition = "TEXT")
    private String options;

    private boolean isMemberSpecific;


    private Long linkedWorshipCategoryId; // <-- 추가: 어떤 예배 스케줄과 연동할지 ID 저장


    @Builder
    public FormQuestion(String label, int orderIndex, boolean required, QuestionType inputType,
                        String options, boolean isMemberSpecific, Long linkedWorshipCategoryId) {
        this.label = label;
        this.orderIndex = orderIndex;
        this.required = required;
        this.inputType = inputType;
        this.options = options;
        this.isMemberSpecific = isMemberSpecific;
        this.linkedWorshipCategoryId = linkedWorshipCategoryId;
    }
}