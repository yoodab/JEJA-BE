package com.jeja.jejabe.club.dto;

import com.jeja.jejabe.club.Club;
import com.jeja.jejabe.club.ClubMember;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

@Data
public class ClubDetailResponseDto {
    private Long id;
    private String name;
    private String description;
    private String meetingTime;
    private String meetingPlace;
    private String type;
    private Long leaderId;
    private String leaderName;
    private List<ClubMemberDto> members;

    public ClubDetailResponseDto(Club club) {
        this.id = club.getId();
        this.name = club.getName();
        this.description = club.getDescription();
        this.meetingTime = club.getMeetingTime();
        this.meetingPlace = club.getMeetingPlace();
        this.type = club.getType().name();
        if (club.getLeader() != null) {
            this.leaderId = club.getLeader().getId();
            this.leaderName = club.getLeader().getName();
        }
        this.members = club.getMembers().stream()
                .map(ClubMemberDto::new)
                .collect(Collectors.toList());
    }

    @Data
    public static class ClubMemberDto {
        private Long memberId;
        private String name;
        private String phone;

        public ClubMemberDto(ClubMember cm) {
            this.memberId = cm.getMember().getId();
            this.name = cm.getMember().getName();
            this.phone = cm.getMember().getPhone();
        }
    }
}
