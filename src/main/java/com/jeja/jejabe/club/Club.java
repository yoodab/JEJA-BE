package com.jeja.jejabe.club;

import com.jeja.jejabe.member.domain.Member;
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
public class Club {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String meetingTime;
    private String meetingPlace;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ClubType type;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leader_id")
    private Member leader;

    @OneToMany(mappedBy = "club", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ClubMember> members = new ArrayList<>();

    // 가입 신청서 양식 ID
    private Long applicationTemplateId;

    @Builder
    public Club(String name, String description, String meetingTime, String meetingPlace, ClubType type, Member leader) {
        this.name = name;
        this.description = description;
        this.meetingTime = meetingTime;
        this.meetingPlace = meetingPlace;
        this.type = type;
        this.leader = leader;
    }

    public void updateInfo(String name, String description, String meetingTime, String meetingPlace) {
        this.name = name;
        this.description = description;
        this.meetingTime = meetingTime;
        this.meetingPlace = meetingPlace;
    }

    public void changeLeader(Member newLeader) {
        this.leader = newLeader;
    }

    public void setApplicationTemplateId(Long templateId) {
        this.applicationTemplateId = templateId;
    }
}