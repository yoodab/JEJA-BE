package com.jeja.jejabe.club.dto;

import com.jeja.jejabe.club.Club;
import lombok.Data;

@Data
public class ClubResponseDto {
    private Long id;
    private String name;
    private String description;
    private String type;
    private String leaderName;
    private int memberCount;

    public ClubResponseDto(Club club) {
        this.id = club.getId();
        this.name = club.getName();
        this.description = club.getDescription();
        this.type = club.getType().name();
        this.leaderName = club.getLeader() != null ? club.getLeader().getName() : "미정";
        this.memberCount = club.getMembers().size();
    }
}
