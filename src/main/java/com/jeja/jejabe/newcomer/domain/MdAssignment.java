package com.jeja.jejabe.newcomer.domain;

import com.jeja.jejabe.global.entity.BaseTimeEntity;
import com.jeja.jejabe.member.domain.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MdAssignment extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    private String charge;
    private String ageGroup;

    @Builder
    public MdAssignment(Member member, String charge, String ageGroup) {
        this.member = member;
        this.charge = charge;
        this.ageGroup = ageGroup;
    }

    public void update(String charge, String ageGroup) {
        this.charge = charge;
        this.ageGroup = ageGroup;
    }
}