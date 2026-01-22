package com.jeja.jejabe.cell;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cell") // 테이블 이름도 'cell'로 변경
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Cell {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cell_id")
    private Long cellId;

    @Column(nullable = false, length = 100)
    private String cellName; // 순 이름 -> 셀 이름

    @Column(nullable = false)
    private Integer year; // 해당 셀이 운영되는 연도

    @OneToMany(mappedBy = "cell", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MemberCellHistory> memberHistories = new ArrayList<>();

    // 리더(순장) 정보는 직접 관리하지 않고, MemberCellHistory를 통해 유추할 수 있습니다.
    // 또는 편의를 위해 leaderId를 직접 가질 수도 있습니다. (선택적)
    // private Long leaderId;

    @Builder
    public Cell(String cellName, Integer year) {
        this.cellName = cellName;
        this.year = year;
    }

    // 셀 정보 업데이트 메소드
    public void update(String cellName, Integer year) {
        this.cellName = cellName;
        this.year = year;
    }
}
