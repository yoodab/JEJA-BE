package com.jeja.jejabe.form.domain;

import com.jeja.jejabe.global.entity.BaseTimeEntity;
import com.jeja.jejabe.schedule.domain.WorshipCategory;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FormQuestion extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id")
    private FormSection section;

    @Column(nullable = false)
    private String label;
    private int orderIndex;
    private boolean required;

    @Enumerated(EnumType.STRING)
    private QuestionType inputType;

    @Enumerated(EnumType.STRING)
    private AttendanceSyncType syncType = AttendanceSyncType.NONE;

    @Column(columnDefinition = "TEXT")
    private String optionsJson; // 선택지 + 분기 로직 JSON 저장

    @Column(nullable = false)
    private boolean isActive = true;

    private boolean isMemberSpecific;
    private WorshipCategory linkedWorshipCategory;
    private Long linkedScheduleId;
    private LocalDate linkedScheduleDate;

    public void disable() {
        this.isActive = false;
    }

    public boolean isContentChanged(String label, QuestionType inputType, String optionsJson, boolean required,
            boolean isMemberSpecific, WorshipCategory linkedWorshipCategory,
            Long linkedScheduleId, LocalDate linkedScheduleDate, AttendanceSyncType syncType) {
        if (!this.label.equals(label))
            return true;
        if (this.inputType != inputType)
            return true;
        if (this.required != required)
            return true;
        if (this.isMemberSpecific != isMemberSpecific)
            return true;
        if (this.syncType != syncType)
            return true;

        // Null checks
        if (this.optionsJson == null && optionsJson != null)
            return true;
        if (this.optionsJson != null && !this.optionsJson.equals(optionsJson))
            return true;

        if (this.linkedWorshipCategory != linkedWorshipCategory)
            return true;

        if (this.linkedScheduleId == null && linkedScheduleId != null)
            return true;
        if (this.linkedScheduleId != null && !this.linkedScheduleId.equals(linkedScheduleId))
            return true;

        if (this.linkedScheduleDate == null && linkedScheduleDate != null)
            return true;
        if (this.linkedScheduleDate != null && !this.linkedScheduleDate.equals(linkedScheduleDate))
            return true;

        return false;
    }

    @Builder
    public FormQuestion(String label, int orderIndex, boolean required, QuestionType inputType,
            AttendanceSyncType syncType, String optionsJson, Long linkedScheduleId,
            boolean isMemberSpecific, WorshipCategory linkedWorshipCategory, LocalDate linkedScheduleDate) {
        this.label = label;
        this.orderIndex = orderIndex;
        this.required = required;
        this.inputType = inputType;
        this.syncType = syncType != null ? syncType : AttendanceSyncType.NONE;
        this.optionsJson = optionsJson;
        this.isMemberSpecific = isMemberSpecific;
        this.linkedWorshipCategory = linkedWorshipCategory;
        this.linkedScheduleId = linkedScheduleId;
        this.linkedScheduleDate = linkedScheduleDate;
    }

}