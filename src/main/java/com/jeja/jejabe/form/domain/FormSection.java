package com.jeja.jejabe.form.domain;

import com.jeja.jejabe.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FormSection extends BaseTimeEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id")
    private FormTemplate template;

    private String title;
    private String description;
    private int orderIndex; // 페이지 순서

    @Enumerated(EnumType.STRING)
    private NextActionType defaultNextAction = NextActionType.CONTINUE;

    private Integer defaultTargetSectionIndex; // 특정 섹션 이동 시 목적지 index

    @OneToMany(mappedBy = "section", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC")
    private List<FormQuestion> questions = new ArrayList<>();

    @Builder
    public FormSection(String title, String description, int orderIndex,
                       NextActionType defaultNextAction, Integer defaultTargetSectionIndex) {
        this.title = title;
        this.description = description;
        this.orderIndex = orderIndex;
        this.defaultNextAction = defaultNextAction != null ? defaultNextAction : NextActionType.CONTINUE;
        this.defaultTargetSectionIndex = defaultTargetSectionIndex;
    }

    // Template 세팅 (addSection에서 호출됨)
    public void setTemplate(FormTemplate template) {
        this.template = template;
    }

    // Section에 질문 추가할 때 사용
    public void addQuestion(FormQuestion question) {
        this.questions.add(question);
        question.setSection(this);
    }
}
