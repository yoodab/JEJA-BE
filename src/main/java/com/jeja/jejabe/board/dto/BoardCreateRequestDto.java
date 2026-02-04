package com.jeja.jejabe.board.dto;

import com.jeja.jejabe.board.domain.BoardAccessType;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class BoardCreateRequestDto {
    private String name;
    private String boardKey;
    private String description;
    private BoardAccessType accessType;
    private BoardAccessType writeAccessType;
    private Long clubId;
    private boolean isAlwaysSecret;
}
