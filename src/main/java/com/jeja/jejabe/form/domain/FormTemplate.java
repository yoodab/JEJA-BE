package com.jeja.jejabe.form.domain;

import com.jeja.jejabe.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FormTemplate extends BaseTimeEntity {

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

    private Long targetClubId;
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    @Column(nullable = false)
    private boolean isActive = true;

    @Column(nullable = false)
    private boolean isDeleted = false;

    @OneToMany(mappedBy = "template", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FormAccess> accessList = new ArrayList<>();

    @OneToMany(mappedBy = "template", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC")
    private List<FormSection> sections = new ArrayList<>();

    @Builder
    public FormTemplate(String title, String description, FormCategory category, FormType type,
                        Long targetClubId, LocalDateTime startDate, LocalDateTime endDate, Boolean isActive) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.type = type;
        this.targetClubId = targetClubId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.isActive = false;
        this.isDeleted = false;
    }

    public void addAccess(FormAccess access) {
        this.accessList.add(access);
        access.setTemplate(this);
    }

    public void addSection(FormSection section) {
        this.sections.add(section);
        section.setTemplate(this);
    }

    // 전체 질문 목록을 가져오는 편의 메서드 (스냅샷용)
    public List<FormQuestion> getAllQuestions() {
        List<FormQuestion> all = new ArrayList<>();
        for (FormSection section : sections) {
            all.addAll(section.getQuestions());
        }
        return all;
    }
    public void updateBasicInfo(String title, String description,
                                LocalDateTime startDate, LocalDateTime endDate,
                                Boolean isActive, Long targetClubId) {
        this.title = title;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.targetClubId = targetClubId;
        // null이 아닐 때만 상태 변경 (혹시나 프론트에서 안 보냈을 경우 대비)
        if (isActive != null) {
            this.isActive = isActive;
        }
    }

    public void delete() {
        this.isDeleted = true;
        this.isActive = false; // 삭제된 건 당연히 비활성화
    }

    public void deactivate() {
        this.isActive = false;
    }

    public void activate() {
        this.isActive = true;
    }

    public void validateSubmissionTime() {
        if (this.isDeleted) throw new IllegalArgumentException("삭제된 신청서입니다."); // 삭제 체크 추가
        if (!this.isActive) throw new IllegalArgumentException("현재 비활성화된 신청서입니다.");

        if (this.category == FormCategory.CELL_REPORT || this.category == FormCategory.CLUB_APPLICATION) return;

        LocalDateTime now = LocalDateTime.now();
        if (this.startDate != null && now.isBefore(this.startDate)) throw new IllegalArgumentException("제출 기간이 시작되지 않았습니다.");
        if (this.endDate != null && now.isAfter(this.endDate)) throw new IllegalArgumentException("제출 기한이 마감되었습니다.");
    }
}