package com.jeja.jejabe.form.domain;

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
public class FormTemplate {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FormCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FormType type;

    private Long targetClubId; // 특정 클럽 전용

    @OneToMany(mappedBy = "template", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FormQuestion> questions = new ArrayList<>();

    @OneToMany(mappedBy = "template", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FormAccess> accessList = new ArrayList<>();

    @Builder
    public FormTemplate(String title, String description, FormCategory category, FormType type, Long targetClubId) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.type = type;
        this.targetClubId = targetClubId;
    }

    public void addQuestion(FormQuestion question) {
        this.questions.add(question);
        question.setTemplate(this);
    }

    public void addAccess(FormAccess access) {
        this.accessList.add(access);
        access.setTemplate(this);
    }
}
