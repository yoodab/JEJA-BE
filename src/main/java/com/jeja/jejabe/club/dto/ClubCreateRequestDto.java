package com.jeja.jejabe.club.dto;

import com.jeja.jejabe.club.ClubType;
import lombok.Data;

@Data
public class ClubCreateRequestDto {
    private String name;
    private String description;
    private ClubType type;
    private Long leaderMemberId;
}
