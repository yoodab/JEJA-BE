package com.jeja.jejabe.schedule.domain;

import com.jeja.jejabe.album.Album;
import com.jeja.jejabe.global.entity.BaseTimeEntity;
import com.jeja.jejabe.schedule.dto.ScheduleRequestDto;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Schedule extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long scheduleId;

    @Column(nullable = false, length = 200)
    private String title;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private LocalDateTime startDate;

    @Column(nullable = false)
    private LocalDateTime endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ScheduleType type;

    @Column(length = 100)
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SharingScope sharingScope;

    @Enumerated(EnumType.STRING)
    private RecurrenceRule recurrenceRule;

    private LocalDate recurrenceEndDate;

    @OneToOne(mappedBy = "schedule", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Album album;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "worship_category_id")
    private WorshipCategory worshipCategory;

    @Builder
    public Schedule(String title, String content, LocalDateTime startDate, LocalDateTime endDate,
                    ScheduleType type, String location, SharingScope sharingScope,
                    RecurrenceRule recurrenceRule, LocalDate recurrenceEndDate,
                    WorshipCategory worshipCategory) {
        this.title = title;
        this.content = content;
        this.startDate = startDate;
        this.endDate = endDate;
        this.type = type;
        this.location = location;
        this.sharingScope = sharingScope;
        this.recurrenceRule = recurrenceRule == null ? RecurrenceRule.NONE : recurrenceRule;
        this.recurrenceEndDate = recurrenceEndDate;
        this.worshipCategory = worshipCategory;
    }

    public void update(ScheduleRequestDto dto, WorshipCategory newCategory) {
        this.title = dto.getTitle();
        this.content = dto.getContent();
        this.startDate = dto.getStartDate();
        this.endDate = dto.getEndDate();
        this.type = dto.getType();
        this.location = dto.getLocation();
        this.sharingScope = dto.getSharingScope();
        this.recurrenceRule = dto.getRecurrenceRule() == null ? RecurrenceRule.NONE : dto.getRecurrenceRule();
        this.recurrenceEndDate = dto.getRecurrenceEndDate();

        // 카테고리 업데이트
        this.worshipCategory = newCategory;
    }
}
