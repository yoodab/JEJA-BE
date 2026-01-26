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

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    private Long historyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cell_id")
    private Cell cell;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private boolean isActive;

    @Column(nullable = false)
    private boolean isLeader;

    @Builder
    public MemberCellHistory(Member member, Cell cell, boolean isLeader) {
        this.member = member;
        this.cell = cell;
        this.startDate = LocalDate.now();
        this.isActive = false;
        this.isLeader = isLeader;
    }

    // 순 이동/리더 변경 (수정)
    public void changeAssignment(Cell newCell, boolean isLeader) {
        this.cell = newCell;
        this.isLeader = isLeader;
    }

    // 상태 변경 (활성화/비활성화)
    public void updateStatus(boolean isActive) {
        this.isActive = isActive;
    }
}
