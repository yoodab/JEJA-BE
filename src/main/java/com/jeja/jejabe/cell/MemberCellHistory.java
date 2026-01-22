package com.jeja.jejabe.cell;

import com.jeja.jejabe.member.domain.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "member_cell_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberCellHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    private Long historyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cell_id")
    private Cell cell;

    @Column(nullable = false)
    private LocalDate startDate; // 이 셀에 배정된 시작일

    private LocalDate endDate;   // 이 셀에서 나간 종료일

    @Column(nullable = false)
    private boolean isActive;    // 현재 활동 중인 셀인지를 나타내는 플래그

    @Column(nullable = false)
    private boolean isLeader;    // 이 셀에서 리더(순장)인지 여부

    @Builder
    public MemberCellHistory(Member member, Cell cell, LocalDate startDate, boolean isLeader) {
        this.member = member;
        this.cell = cell;
        this.startDate = startDate;
        this.endDate = null; // 처음 생성 시 종료일은 없음
        this.isActive = true; // 처음 생성 시 활동 상태
        this.isLeader = isLeader;
    }

    // 현재 활동을 종료시키는 비즈니스 메소드
    public void endActivity(LocalDate endDate) {
        this.isActive = false;
        this.endDate = endDate;
    }
}
