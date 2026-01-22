package com.jeja.jejabe.care.domain;

import com.jeja.jejabe.global.entity.BaseTimeEntity;
import com.jeja.jejabe.member.domain.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CareLog extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_member_id")
    private Member targetMember;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_member_id")
    private Member manager;

    @Column(columnDefinition = "TEXT")
    private String content;

    private String careMethod; // CALL, VISIT
    private LocalDateTime careDate;

    @Builder
    public CareLog(Member targetMember, Member manager, String content, String careMethod, LocalDateTime careDate) {
        this.targetMember = targetMember;
        this.manager = manager;
        this.content = content;
        this.careMethod = careMethod;
        this.careDate = careDate;
    }

    public void update(String content, String careMethod) {
        this.content = content;
        this.careMethod = careMethod;
    }
}
