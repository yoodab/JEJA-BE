package com.jeja.jejabe.board.dto;

import com.jeja.jejabe.board.domain.BoardAccessType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BoardUpdateRequestDto {
    private String name;
    private String description;
    private BoardAccessType accessType;
    private Long clubId;            // 변경할 클럽 ID (없으면 null)
    private Boolean isAlwaysSecret; // Boolean Wrapper로 null 체크 가능하게 함
}
